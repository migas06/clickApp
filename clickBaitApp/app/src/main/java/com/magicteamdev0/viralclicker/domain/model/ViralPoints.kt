package com.magicteamdev0.viralclicker.domain.model

import java.math.BigDecimal
import java.math.MathContext

/**
 * Value class wrapping BigDecimal for Viral Points.
 * Provides human-readable formatting with suffix notation.
 */
@JvmInline
value class ViralPoints(val value: BigDecimal = BigDecimal.ZERO) {

    operator fun plus(other: ViralPoints) = ViralPoints(value + other.value)
    operator fun minus(other: ViralPoints) = ViralPoints((value - other.value).coerceAtLeast())
    operator fun times(multiplier: Double) = ViralPoints(value.multiply(BigDecimal(multiplier)))
    operator fun compareTo(other: ViralPoints) = value.compareTo(other.value)

    fun isGreaterThanOrEqual(other: ViralPoints) = value >= other.value

    fun toDisplayString(): String {
        val v = value
        return when {
            v < BigDecimal("1000") -> v.toLong().toString()
            v < BigDecimal("1000000") -> formatWithSuffix(v, 1_000.0, "K")
            v < BigDecimal("1000000000") -> formatWithSuffix(v, 1_000_000.0, "M")
            v < BigDecimal("1000000000000") -> formatWithSuffix(v, 1_000_000_000.0, "B")
            v < BigDecimal("1000000000000000") -> formatWithSuffix(v, 1_000_000_000_000.0, "T")
            v < BigDecimal("1000000000000000000") -> formatWithSuffix(v, 1_000_000_000_000_000.0, "Qa")
            else -> formatWithSuffix(v, 1_000_000_000_000_000_000.0, "Qi")
        }
    }

    private fun formatWithSuffix(v: BigDecimal, divisor: Double, suffix: String): String {
        val divided = v.divide(BigDecimal(divisor), MathContext.DECIMAL64).toDouble()
        return if (divided >= 100) "%.0f%s".format(divided, suffix)
        else if (divided >= 10) "%.1f%s".format(divided, suffix)
        else "%.2f%s".format(divided, suffix)
    }

    companion object {
        val ZERO = ViralPoints(BigDecimal.ZERO)
        fun fromString(s: String) = ViralPoints(BigDecimal(s))
        fun fromDouble(d: Double) = ViralPoints(BigDecimal(d))
        fun fromLong(l: Long) = ViralPoints(BigDecimal(l))
    }
}

private fun BigDecimal.coerceAtLeast(min: BigDecimal = BigDecimal.ZERO): BigDecimal =
    if (this < min) min else this
