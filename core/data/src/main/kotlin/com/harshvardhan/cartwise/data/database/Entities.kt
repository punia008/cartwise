package com.harshvardhan.cartwise.data.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A cached product. [position] is its index in the remote catalog, so the list keeps the server's
 * order offline. Products fetched on their own (a deep link to a detail page) get [NOT_IN_CATALOG]
 * and are kept out of the catalog list.
 */
@Entity(tableName = "products", indices = [Index("position")])
data class ProductEntity(
    @PrimaryKey val id: Long,
    val title: String,
    val description: String,
    val category: String,
    val brand: String?,
    val priceCents: Long,
    val rating: Double,
    val stock: Int,
    val thumbnail: String,
    val images: List<String>,
    val position: Int,
) {
    companion object {
        const val NOT_IN_CATALOG = -1
    }
}

/** Where the next catalog page starts. One row per paged list; Cartwise has one. */
@Entity(tableName = "remote_keys")
data class RemoteKeyEntity(
    @PrimaryKey val listId: String,
    val nextSkip: Int?,
    val refreshedAt: Long,
)

/**
 * A cart line. Title, category and price are copied in when the item is added so the cart
 * still renders if the product drops out of the catalog cache.
 */
@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey val productId: Long,
    val title: String,
    val category: String,
    val thumbnail: String,
    val unitPriceCents: Long,
    val quantity: Int,
    val maxQuantity: Int,
    val addedAt: Long,
)
