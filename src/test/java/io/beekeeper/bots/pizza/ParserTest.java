package io.beekeeper.bots.pizza;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class ParserTest {

    @Test
    public void testSomething() {
        Map<String, String> menu = new HashMap<>();
        menu.put("Gorgonzola", "Gorgonzola Whee");
        menu.put("Margherita", "Margherita");
        Parser<String> p = new Parser<String>(menu);

        String result = p.parse("gorgonzola");

        Assert.assertEquals("Gorgonzola Whee", result);
    }

    @Test
    public void testDoesNotMatchIfOnlyOneWord() {
        Map<String, String> menu = new HashMap<>();
        menu.put("Gorgonzola Thing", "Gorgonzola Whee");
        menu.put("Margherita", "Margherita");
        Parser<String> p = new Parser<String>(menu);

        String result = p.parse("gorgonzola");

        Assert.assertEquals(null, result);
    }

    @Test
    public void testMatchesWordsNotAtStart() {
        Map<String, String> menu = new HashMap<>();
        menu.put("Gorgonzola Thing Pizza", "Gorgonzola Whee");
        menu.put("Margherita", "Margherita");
        Parser<String> p = new Parser<>(menu);

        String result = p.parse("thing pizza");

        Assert.assertEquals("Gorgonzola Whee", result);
    }

    @Test
    public void testMatchesMinimalPrefixes() {
        Map<String, String> menu = new HashMap<>();
        menu.put("Gorgonzola Thing Pizza", "Gorgonzola Whee");
        menu.put("Margherita", "Margherita");
        Parser<String> p = new Parser<>(menu);

        String result = p.parse("gor th pi");

        Assert.assertEquals("Gorgonzola Whee", result);
    }

    @Test
    public void testMatchesNaturalString() {
        Map<String, String> menu = new HashMap<>();
        menu.put("Gorgonzola Pizza", "Gorgonzola Whee");
        menu.put("Margherita", "Margherita");
        Parser<String> p = new Parser<>(menu);

        String result = p.parse("One gorgonzola pizza plx!");

        Assert.assertEquals("Gorgonzola Whee", result);
    }

    @Test
    public void testFindsBestWhenMultipleMatch() {
        Map<String, String> menu = new HashMap<>();
        menu.put("Gorgonzola Pizza", "Gorgonzola Whee");
        menu.put("Gorgonzola", "Margherita");
        menu.put("Gorgonzola Pizza Margherita", "correct");
        Parser<String> p = new Parser<>(menu);

        String result = p.parse("gorgonzola pizza margherita");

        Assert.assertEquals("correct", result);
    }
}
