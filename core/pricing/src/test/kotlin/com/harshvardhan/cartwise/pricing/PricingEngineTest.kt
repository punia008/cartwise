package com.harshvardhan.cartwise.pricing

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PricingEngineTest {

    private val engine = PricingEngine()

    private val buy2Get1Beauty = Offer.MultiBuy("b2g1", "Beauty: buy 2 get 1 free", "beauty", 2, 1, 100)
    private val bogoHalfGroceries = Offer.MultiBuy("bogo50", "Groceries: 2nd at 50% off", "groceries", 1, 1, 50)
    private val tenOff150 = Offer.OrderPercentOff("o10", "10% off over $150", Money.of(150), 10)
    private val fifteenOff500 = Offer.OrderPercentOff("o15", "15% off over $500", Money.of(500), 15)

    private fun line(id: Long, price: Money, qty: Int = 1, category: String = "beauty") =
        CartLine(id, "Item $id", category, price, qty)

    @Test
    fun `empty cart prices to zero`() {
        val cart = engine.price(emptyList(), listOf(tenOff150))
        assertEquals(Money.ZERO, cart.total)
        assertNull(cart.nudge)
    }

    @Test
    fun `no offers means total equals subtotal`() {
        val cart = engine.price(listOf(line(1, Money.of(10, 50), qty = 3)), emptyList())
        assertEquals(Money.of(31, 50), cart.subtotal)
        assertEquals(Money.of(31, 50), cart.total)
    }

    @Test
    fun `buy 2 get 1 frees the cheapest unit of the group`() {
        val cart = engine.price(
            listOf(line(1, Money.of(30)), line(2, Money.of(20)), line(3, Money.of(10))),
            listOf(buy2Get1Beauty),
        )
        assertEquals(Money.of(10), cart.itemDiscount)
        assertEquals(Money.of(10), cart.lines.single { it.line.productId == 3L }.discount)
    }

    @Test
    fun `incomplete group gets no discount`() {
        val cart = engine.price(listOf(line(1, Money.of(30), qty = 2)), listOf(buy2Get1Beauty))
        assertEquals(Money.ZERO, cart.itemDiscount)
    }

    @Test
    fun `groups are formed most expensive first, not cheapest units overall`() {
        // 60, 50, 40 | 30, 20, 10 -> frees 40 and 10 (not the two cheapest, 20 and 10).
        val lines = listOf(60, 50, 40, 30, 20, 10).mapIndexed { i, p -> line(i + 1L, Money.of(p.toLong())) }
        val cart = engine.price(lines, listOf(buy2Get1Beauty))
        assertEquals(Money.of(50), cart.itemDiscount)
    }

    @Test
    fun `quantity on one line counts as separate units`() {
        val cart = engine.price(listOf(line(1, Money.of(12), qty = 3)), listOf(buy2Get1Beauty))
        assertEquals(Money.of(12), cart.itemDiscount)
        assertEquals(Money.of(24), cart.total)
    }

    @Test
    fun `offer only applies to its category`() {
        val cart = engine.price(
            listOf(line(1, Money.of(10), qty = 3, category = "furniture")),
            listOf(buy2Get1Beauty),
        )
        assertEquals(Money.ZERO, cart.itemDiscount)
    }

    @Test
    fun `half price discount rounds half up to the cent`() {
        // 50% of $3.99 = 199.5 cents -> 200 cents.
        val cart = engine.price(
            listOf(line(1, Money.of(3, 99), qty = 2, category = "groceries")),
            listOf(bogoHalfGroceries),
        )
        assertEquals(Money.of(2), cart.itemDiscount)
        assertEquals(Money.of(5, 98), cart.total)
    }

    @Test
    fun `a unit used by one multi-buy is not reused by a later one`() {
        val anyBuy1Get1 = Offer.MultiBuy("any", "Any: buy 1 get 1 free", null, 1, 1, 100)
        val cart = engine.price(listOf(line(1, Money.of(10), qty = 3)), listOf(buy2Get1Beauty, anyBuy1Get1))
        // All three units go to buy-2-get-1; nothing is left for the second offer.
        assertEquals(Money.of(10), cart.itemDiscount)
        assertEquals(listOf("b2g1"), cart.appliedOffers.map { it.offer.id })
    }

    @Test
    fun `order offer is checked against the total after item discounts`() {
        // $160 subtotal, $40 item discount -> $120, below the $150 threshold.
        val cart = engine.price(
            listOf(line(1, Money.of(60)), line(2, Money.of(60)), line(3, Money.of(40))),
            listOf(buy2Get1Beauty, tenOff150),
        )
        assertEquals(Money.ZERO, cart.orderDiscount)
        assertEquals(Money.of(30), cart.nudge?.amountNeeded)
    }

    @Test
    fun `only the best order offer applies`() {
        val cart = engine.price(listOf(line(1, Money.of(600), category = "furniture")), listOf(tenOff150, fifteenOff500))
        assertEquals(Money.of(90), cart.orderDiscount)
        assertEquals(Money.of(510), cart.total)
        assertNull(cart.nudge)
    }

    @Test
    fun `nudge points at the next better order offer`() {
        val cart = engine.price(listOf(line(1, Money.of(200), category = "furniture")), listOf(tenOff150, fifteenOff500))
        assertEquals(Money.of(20), cart.orderDiscount)
        assertEquals("o15", cart.nudge?.offer?.id)
        assertEquals(Money.of(300), cart.nudge?.amountNeeded)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `zero quantity is rejected`() {
        line(1, Money.of(1), qty = 0)
    }
}
