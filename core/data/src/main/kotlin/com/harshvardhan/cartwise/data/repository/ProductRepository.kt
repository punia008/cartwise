package com.harshvardhan.cartwise.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import androidx.room.withTransaction
import com.harshvardhan.cartwise.data.database.CartwiseDatabase
import com.harshvardhan.cartwise.data.database.ProductEntity
import com.harshvardhan.cartwise.data.model.Product
import com.harshvardhan.cartwise.data.model.toEntity
import com.harshvardhan.cartwise.data.model.toModel
import com.harshvardhan.cartwise.data.network.ProductApi
import com.harshvardhan.cartwise.data.paging.ProductRemoteMediator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

interface ProductRepository {
    fun catalog(): Flow<PagingData<Product>>
    fun observeProduct(id: Long): Flow<Product?>

    /** Fetches [id] if it isn't cached yet (e.g. opened from a deep link). */
    suspend fun ensureProduct(id: Long)

    /** Refreshes the first catalog page in place. Used by the background sync worker. */
    suspend fun refreshFirstPage()
}

@Singleton
class OfflineFirstProductRepository @Inject constructor(
    private val api: ProductApi,
    private val db: CartwiseDatabase,
) : ProductRepository {

    @OptIn(ExperimentalPagingApi::class)
    override fun catalog(): Flow<PagingData<Product>> = Pager(
        config = PagingConfig(pageSize = PAGE_SIZE, prefetchDistance = PAGE_SIZE / 2, enablePlaceholders = false),
        remoteMediator = ProductRemoteMediator(api, db),
        pagingSourceFactory = { db.productDao().catalogPagingSource() },
    ).flow.map { page -> page.map { it.toModel() } }

    override fun observeProduct(id: Long): Flow<Product?> =
        db.productDao().observe(id).map { it?.toModel() }

    override suspend fun ensureProduct(id: Long) {
        if (db.productDao().exists(id)) return
        db.productDao().insertIfAbsent(api.product(id).toEntity(ProductEntity.NOT_IN_CATALOG))
    }

    override suspend fun refreshFirstPage() {
        val response = api.products(limit = PAGE_SIZE, skip = 0)
        db.withTransaction {
            db.productDao().upsertAll(response.products.mapIndexed { i, p -> p.toEntity(position = i) })
            db.cartDao().syncPricesFromCatalog()
        }
    }

    companion object {
        const val PAGE_SIZE = 20
    }
}
