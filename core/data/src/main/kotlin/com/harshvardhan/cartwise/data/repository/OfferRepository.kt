package com.harshvardhan.cartwise.data.repository

import com.harshvardhan.cartwise.pricing.Money
import com.harshvardhan.cartwise.pricing.Offer
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Active promotions. Hard-coded for the demo; in production this would come from a remote
 * config endpoint, and only this class would change.
 */
interface OfferRepository {
    fun activeOffers(): List<Offer>
    fun badgeFor(category: String): String?
}

@Singleton
class StaticOfferRepository @Inject constructor() : OfferRepository {

    private val offers = listOf(
        Offer.MultiBuy("beauty-3for2", "Beauty: buy 2, get 1 free", "beauty", buy = 2, get = 1, percentOff = 100),
        Offer.MultiBuy("groceries-bogo50", "Groceries: 2nd item half price", "groceries", buy = 1, get = 1, percentOff = 50),
        Offer.OrderPercentOff("order-10", "10% off orders over $150", Money.of(150), percentOff = 10),
        Offer.OrderPercentOff("order-15", "15% off orders over $500", Money.of(500), percentOff = 15),
    )

    override fun activeOffers() = offers

    override fun badgeFor(category: String): String? =
        when (offers.filterIsInstance<Offer.MultiBuy>().firstOrNull { it.category == category }?.id) {
            "beauty-3for2" -> "3 for 2"
            "groceries-bogo50" -> "2nd half price"
            else -> null
        }
}
