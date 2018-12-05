package io.beekeeper.bots.pizza.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class MoneyUtilTest {

    @Test
    fun testWithDecimals() {
        val expected = "13.37"
        val actual = MoneyUtil.formatPrice(13.37f)
        assertEquals(expected, actual)
    }

    @Test
    fun testWithoutDecimals() {
        val expected = "5.00"
        val actual = MoneyUtil.formatPrice(5f)
        assertEquals(expected, actual)
    }

}
