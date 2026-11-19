package com.harshvardhan.cartwise.data.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.harshvardhan.cartwise.data.database.CartwiseDatabase
import com.harshvardhan.cartwise.data.database.ProductEntity
import com.harshvardhan.cartwise.data.database.RemoteKeyEntity
import com.harshvardhan.cartwise.data.model.toEntity
import com.harshvardhan.cartwise.data.network.ProductApi
import kotlinx.coroutines.CancellationException
import java.util.concurrent.TimeUnit

/**
 * Offline-first paging: the UI only ever reads Room. This mediator fetches pages from the
 * network and writes them to Room; Room's PagingSource then invalidates and the list updates.
 */
@OptIn(ExperimentalPagingApi::class)
class ProductRemoteMediator(
    private val api: ProductApi,
    private val db: CartwiseDatabase,
    private val now: () -> Long = System::currentTimeMillis,
) : RemoteMediator<Int, ProductEntity>() {

    private val productDao = db.productDao()
    private val keyDao = db.remoteKeyDao()
    private val cartDao = db.cartDao()

    override suspend fun initialize(): InitializeAction {
        val refreshedAt = keyDao.get(LIST_ID)?.refreshedAt ?: return InitializeAction.LAUNCH_INITIAL_REFRESH
        return if (now() - refreshedAt < CACHE_TTL) {
            InitializeAction.SKIP_INITIAL_REFRESH
        } else {
            InitializeAction.LAUNCH_INITIAL_REFRESH
        }
    }

    override suspend fun load(loadType: LoadType, state: PagingState<Int, ProductEntity>): MediatorResult {
        val skip = when (loadType) {
            LoadType.REFRESH -> 0
            LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
            LoadType.APPEND -> {
                val key = keyDao.get(LIST_ID)
                key?.nextSkip ?: return MediatorResult.Success(endOfPaginationReached = key != null)
            }
        }

        return try {
            val response = api.products(limit = state.config.pageSize, skip = skip)
            val nextSkip = (skip + response.products.size).takeIf { response.products.isNotEmpty() && it < response.total }

            db.withTransaction {
                if (loadType == LoadType.REFRESH) productDao.clear()
                productDao.upsertAll(response.products.mapIndexed { i, p -> p.toEntity(position = skip + i) })
                keyDao.upsert(RemoteKeyEntity(LIST_ID, nextSkip, refreshedAt = now()))
                cartDao.syncPricesFromCatalog()
            }
            MediatorResult.Success(endOfPaginationReached = nextSkip == null)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // IOException (offline) and HttpException both land here; Paging surfaces them as
            // LoadState.Error while the cached rows stay on screen.
            MediatorResult.Error(e)
        }
    }

    companion object {
        const val LIST_ID = "catalog"
        val CACHE_TTL = TimeUnit.HOURS.toMillis(1)
    }
}
