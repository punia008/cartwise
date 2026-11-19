package com.harshvardhan.cartwise.pricing

import kotlin.math.abs

/**
 * An amount in the smallest currency unit (cents). Prices are never stored as Double:
 * 0.1 + 0.2 != 0.3 in floating point, and a cart total must add up to the cent.
 */
@JvmInline
value class Money(val cents: Long) : Comparable<Money> {

    operator fun plus(other: Money) = Money(cents + other.cents)
    operator fun minus(other: Money) = Money(cents - other.cents)
    operator fun times(quantity: Int) = Money(cents * quantity)
    override fun compareTo(other: Money) = cents.compareTo(other.cents)

    /** [percent] of this amount, rounded half-up to the nearest cent. */
    fun percent(percent: Int): Money {
        require(percent in 0..100) { "percent must be 0..100, was $percent" }
        return Money((cents * percent + 50) / 100)
    }

    fun format(symbol: String = "$"): String {
        val sign = if (cents < 0) "-" else ""
        val value = abs(cents)
        return "$sign$symbol${value / 100}.${(value % 100).toString().padStart(2, '0')}"
    }

    override fun toString() = format()

    companion object {
        val ZERO = Money(0)
        fun of(units: Long, cents: Long = 0) = Money(units * 100 + cents)
    }
}

fun Iterable<Money>.sum(): Money = fold(Money.ZERO, Money::plus)
