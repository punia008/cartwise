package com.harshvardhan.cartwise.pricing

import org.junit.Assert.assertEquals
import org.junit.Test

class MoneyTest {

    @Test
    fun `formats cents with two digits`() {
        assertEquals("$4.05", Money(405).format())
        assertEquals("$0.00", Money.ZERO.format())
        assertEquals("-$1.50", Money(-150).format())
    }

    @Test
    fun `percent rounds half up`() {
        assertEquals(Money(200), Money(399).percent(50))
        assertEquals(Money(33), Money(333).percent(10))
    }
}
