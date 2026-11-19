package com.harshvardhan.cartwise.pricing

data class CartLine(
    val productId: Long,
    val title: String,
    val category: String,
    val unitPrice: Money,
    val quantity: Int,
) {
    init {
        require(quantity > 0) { "quantity must be positive, was $quantity" }
        require(unitPrice >= Money.ZERO) { "unitPrice must not be negative" }
    }

    val subtotal: Money get() = unitPrice * quantity
}

sealed interface Offer {
    val id: String
    val label: String

    /**
     * "Buy [buy], get [get] at [percentOff]% off", for units in [category] (or any category when null).
     * Buy 2 get 1 free = (2, 1, 100). Buy 1 get 1 at half price = (1, 1, 50).
     */
    data class MultiBuy(
        override val id: String,
        override val label: String,
        val category: String?,
        val buy: Int,
        val get: Int,
        val percentOff: Int,
    ) : Offer {
        init {
            require(buy > 0 && get > 0) { "buy and get must be positive" }
            require(percentOff in 1..100) { "percentOff must be 1..100" }
        }
    }

    /** [percentOff]% off the whole order once it reaches [minSubtotal] after item discounts. */
    data class OrderPercentOff(
        override val id: String,
        override val label: String,
        val minSubtotal: Money,
        val percentOff: Int,
    ) : Offer {
        init {
            require(percentOff in 1..100) { "percentOff must be 1..100" }
        }
    }
}

data class PricedLine(
    val line: CartLine,
    val discount: Money,
) {
    val total: Money get() = line.subtotal - discount
}

data class AppliedOffer(val offer: Offer, val discount: Money)

/** How much more the customer must add to unlock [offer]. */
data class OfferNudge(val offer: Offer.OrderPercentOff, val amountNeeded: Money)

data class PricedCart(
    val lines: List<PricedLine>,
    val appliedOffers: List<AppliedOffer>,
    val orderDiscount: Money,
    val nudge: OfferNudge?,
) {
    val subtotal: Money get() = lines.map { it.line.subtotal }.sum()
    val itemDiscount: Money get() = lines.map { it.discount }.sum()
    val totalDiscount: Money get() = itemDiscount + orderDiscount
    val total: Money get() = subtotal - totalDiscount
    val itemCount: Int get() = lines.sumOf { it.line.quantity }

    companion object {
        val EMPTY = PricedCart(emptyList(), emptyList(), Money.ZERO, null)
    }
}
