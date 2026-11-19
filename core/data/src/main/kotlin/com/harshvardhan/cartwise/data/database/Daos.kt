package com.harshvardhan.cartwise.data.database

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {

    @Query("SELECT * FROM products WHERE position >= 0 ORDER BY position ASC")
    fun catalogPagingSource(): PagingSource<Int, ProductEntity>

    @Query("SELECT * FROM products WHERE id = :id")
    fun observe(id: Long): Flow<ProductEntity?>

    @Query("SELECT EXISTS(SELECT 1 FROM products WHERE id = :id)")
    suspend fun exists(id: Long): Boolean

    @Upsert
    suspend fun upsertAll(products: List<ProductEntity>)

    /** For single-product fetches: never overwrite a catalog row (that would reset its position). */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIfAbsent(product: ProductEntity)

    @Query("DELETE FROM products")
    suspend fun clear()
}

@Dao
interface RemoteKeyDao {

    @Query("SELECT * FROM remote_keys WHERE listId = :listId")
    suspend fun get(listId: String): RemoteKeyEntity?

    @Upsert
    suspend fun upsert(key: RemoteKeyEntity)

    @Query("DELETE FROM remote_keys WHERE listId = :listId")
    suspend fun delete(listId: String)
}

@Dao
interface CartDao {

    @Query("SELECT * FROM cart_items ORDER BY addedAt ASC")
    fun observeAll(): Flow<List<CartItemEntity>>

    @Query("SELECT quantity FROM cart_items WHERE productId = :productId")
    fun observeQuantity(productId: Long): Flow<Int?>

    @Query("SELECT * FROM cart_items WHERE productId = :productId")
    suspend fun get(productId: Long): CartItemEntity?

    @Upsert
    suspend fun upsert(item: CartItemEntity)

    @Query("DELETE FROM cart_items WHERE productId = :productId")
    suspend fun delete(productId: Long)

    @Query("DELETE FROM cart_items")
    suspend fun clear()

    /** Carry fresh catalog prices into the cart so the total matches what the list shows. */
    @Query(
        """
        UPDATE cart_items SET unitPriceCents =
            (SELECT priceCents FROM products WHERE products.id = cart_items.productId)
        WHERE productId IN (SELECT id FROM products)
        """
    )
    suspend fun syncPricesFromCatalog()

    @Transaction
    suspend fun changeQuantity(productId: Long, delta: Int) {
        val item = get(productId) ?: return
        val quantity = (item.quantity + delta).coerceAtMost(item.maxQuantity)
        if (quantity <= 0) delete(productId) else upsert(item.copy(quantity = quantity))
    }
}
