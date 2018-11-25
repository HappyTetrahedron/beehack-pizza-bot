package io.beekeeper.bots.pizza.providers.dieci

import io.beekeeper.bots.pizza.test.TestMenuItem
import org.junit.Assert.assertEquals
import org.junit.Test

class ParserTest {

    @Test
    fun testSomething() {
        val menu = mapOf(
                "Gorgonzola" to TestMenuItem("Gorgonzola Whee"),
                "Margherita" to TestMenuItem("Margherita")
        )
        val p = DieciMenuItemParser<TestMenuItem>(menu)

        val result = p.parse("gorgonzola")

        assertEquals("Gorgonzola Whee", result?.articleName)
    }

    @Test
    fun testMatchesWordsNotAtStart() {
        val menu = mapOf(
                "Gorgonzola Thing Pizza" to TestMenuItem("Gorgonzola Whee"),
                "Margherita" to TestMenuItem("Margherita")
        )
        val p = DieciMenuItemParser<TestMenuItem>(menu)

        val result = p.parse("thing pizza")

        assertEquals("Gorgonzola Whee", result?.articleName)
    }

    @Test
    fun testMatchesMinimalPrefixes() {
        val menu = mapOf(
                "Gorgonzola Thing Pizza" to TestMenuItem("Gorgonzola Whee"),
                "Margherita" to TestMenuItem("Margherita")
        )
        val p = DieciMenuItemParser<TestMenuItem>(menu)

        val result = p.parse("gor th pi")

        assertEquals("Gorgonzola Whee", result?.articleName)
    }

    @Test
    fun testMatchesNaturalString() {
        val menu = mapOf(
                "Gorgonzola Pizza" to TestMenuItem("Gorgonzola Whee"),
                "Margherita" to TestMenuItem("Margherita")
        )
        val p = DieciMenuItemParser<TestMenuItem>(menu)

        val result = p.parse("One gorgonzola pizza plx!")

        assertEquals("Gorgonzola Whee", result?.articleName)
    }

    @Test
    fun testFindsBestWhenMultipleMatch() {
        val menu = mapOf(
                "Gorgonzola Pizza" to TestMenuItem("Gorgonzola Whee"),
                "Gorgonzola" to TestMenuItem("Margherita"),
                "Gorgonzola Pizza Margherita" to TestMenuItem("correct")
        )
        val p = DieciMenuItemParser<TestMenuItem>(menu)

        val result = p.parse("gorgonzola pizza margherita")

        assertEquals("correct", result?.articleName)
    }

    @Test
    fun testFindsWithUmlaut() {
        val menu = mapOf(
                "öGorgonzöla Pizza" to TestMenuItem("Gorgonzola Whee"),
                "Margherita" to TestMenuItem("Margherita")
        )
        val p = DieciMenuItemParser<TestMenuItem>(menu)

        val result = p.parse("ogorgonzola pizza")

        assertEquals("Gorgonzola Whee", result?.articleName)
    }

    @Test
    fun testFindsWithUmlaut2() {
        val menu = mapOf(
                "oGorgonzöla Pizza" to TestMenuItem("Gorgonzola Whee"),
                "Margherita" to TestMenuItem("Margherita")
        )
        val p = DieciMenuItemParser<TestMenuItem>(menu)

        val result = p.parse("ögörgönzölä pizzä")

        assertEquals("Gorgonzola Whee", result?.articleName)
    }

}
