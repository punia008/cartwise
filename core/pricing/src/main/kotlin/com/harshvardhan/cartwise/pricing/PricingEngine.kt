package com.harshvardhan.cartwise.pricing

/**
 * Prices a cart against a list of offers. Pure Kotlin and deterministic, so every
 * rule below is pinned by a unit test rather than discovered at checkout.
 *
 * Rules:
 * 1. Multi-buy offers run in the order given. A unit discounted by one multi-buy is not
 *    eligible for a later one: a customer never gets two item offers on the same unit.
 * 2. Within a multi-buy, eligible units are sorted most expensive first and taken in groups
 *    of (buy + get). In each complete group the last `get` units (the cheapest of that group)
 *    are discounted. Incomplete groups get nothing.
 * 3. After item discounts, the single best order-level offer the cart qualifies for is applied.
 *    Order offers never stack.
 * 4. If a better order offer is still out of reach, [PricedCart.nudge] says how much more to add.
 */
class PricingEngine {

    fun price(lines: List<CartLine>, offers: List<Offer>): PricedCart {
        if (lines.isEmpty()) return PricedCart.EMPTY

        val units = lines.flatMap { line -> List(line.quantity) { Unit(line) } }
        val discountByProduct = mutableMapOf<Long, Money>()
        val applied = mutableListOf<AppliedOffer>()

        offers.filterIsInstance<Offer.MultiBuy>().forEach { offer ->
            val discount = applyMultiBuy(offer, units, discountByProduct)
            if (discount > Money.ZERO) applied += AppliedOffer(offer, discount)
        }

        val pricedLines = lines.map { PricedLine(it, discountByProduct[it.productId] ?: Money.ZERO) }
        val afterItems = pricedLines.map { it.total }.sum()

        val orderOffers = offers.filterIsInstance<Offer.OrderPercentOff>()
        val best = orderOffers
            .filter { afterItems >= it.minSubtotal }
            .maxByOrNull { afterItems.percent(it.percentOff) }
        val orderDiscount = best?.let { afterItems.percent(it.percentOff) } ?: Money.ZERO
        if (best != null) applied += AppliedOffer(best, orderDiscount)

        val nudge = orderOffers
            .filter { afterItems < it.minSubtotal && it.percentOff > (best?.percentOff ?: 0) }
            .minByOrNull { it.minSubtotal }
            ?.let { OfferNudge(it, it.minSubtotal - afterItems) }

        return PricedCart(pricedLines, applied, orderDiscount, nudge)
    }

    private fun applyMultiBuy(
        offer: Offer.MultiBuy,
        units: List<Unit>,
        discountByProduct: MutableMap<Long, Money>,
    ): Money {
        val eligible = units
            .filter { !it.consumed && (offer.category == null || it.line.category == offer.category) }
            .sortedByDescending { it.line.unitPrice }
        val groupSize = offer.buy + offer.get
        var total = Money.ZERO

        eligible.chunked(groupSize)
            .filter { it.size == groupSize }
            .forEach { group ->
                group.forEach { it.consumed = true }
                group.takeLast(offer.get).forEach { unit ->
                    val discount = unit.line.unitPrice.percent(offer.percentOff)
                    discountByProduct.merge(unit.line.productId, discount, Money::plus)
                    total += discount
                }
            }
        return total
    }

    private class Unit(val line: CartLine) {
        var consumed = false
    }
}
