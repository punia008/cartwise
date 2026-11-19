package com.harshvardhan.cartwise.data.repository

import com.harshvardhan.cartwise.data.database.CartDao
import com.harshvardhan.cartwise.data.database.CartItemEntity
import com.harshvardhan.cartwise.data.model.Product
import com.harshvardhan.cartwise.data.model.toCartLine
import com.harshvardhan.cartwise.pricing.PricedCart
import com.harshvardhan.cartwise.pricing.PricingEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

interface CartRepository {
    /** The cart, already priced against the active offers. */
    val cart: Flow<PricedCart>
    fun quantityOf(productId: Long): Flow<Int>
    suspend fun add(product: Product)
    suspend fun changeQuantity(productId: Long, delta: Int)
    suspend fun remove(productId: Long)
    suspend fun clear()
}

@Singleton
class LocalCartRepository @Inject constructor(
    private val cartDao: CartDao,
    private val offers: OfferRepository,
    private val engine: PricingEngine,
) : CartRepository {

    override val cart: Flow<PricedCart> = cartDao.observeAll()
        .map { items -> engine.price(items.map { it.toCartLine() }, offers.activeOffers()) }
        .flowOn(Dispatchers.Default)

    override fun quantityOf(productId: Long): Flow<Int> =
        cartDao.observeQuantity(productId).map { it ?: 0 }

    override suspend fun add(product: Product) {
        if (cartDao.get(product.id) != null) {
            cartDao.changeQuantity(product.id, +1)
            return
        }
        if (product.stock <= 0) return
        cartDao.upsert(
            CartItemEntity(
                productId = product.id,
                title = product.title,
                category = product.category,
                thumbnail = product.thumbnail,
                unitPriceCents = product.price.cents,
                quantity = 1,
                maxQuantity = product.stock.coerceAtMost(MAX_PER_ITEM),
                addedAt = System.currentTimeMillis(),
            )
        )
    }

    override suspend fun changeQuantity(productId: Long, delta: Int) = cartDao.changeQuantity(productId, delta)

    override suspend fun remove(productId: Long) = cartDao.delete(productId)

    override suspend fun clear() = cartDao.clear()

    companion object {
        const val MAX_PER_ITEM = 10
    }
}
