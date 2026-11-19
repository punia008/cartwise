package com.harshvardhan.cartwise.data

import com.harshvardhan.cartwise.data.model.toCents
import com.harshvardhan.cartwise.data.network.NetworkProduct
import com.harshvardhan.cartwise.data.model.toEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class MappersTest {

    @Test
    fun `prices convert to exact cents`() {
        assertEquals(1999L, 19.99.toCents())
        assertEquals(29L, 0.29.toCents()) // (0.29 * 100).toLong() would give 28
        assertEquals(500L, 5.0.toCents())
        assertEquals(1000L, 9.995.toCents())
    }

    @Test
    fun `network product maps to entity with its catalog position`() {
        val entity = NetworkProduct(id = 7, title = "Lipstick", price = 12.49, category = "beauty").toEntity(position = 26)
        assertEquals(1249L, entity.priceCents)
        assertEquals(26, entity.position)
        assertEquals("beauty", entity.category)
    }
}
