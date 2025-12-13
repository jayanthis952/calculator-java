package com.example;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CalculatorTest {

    Calculator calc = new Calculator();

    @Test
    void testAdd() {
        assertEquals(5, calc.add(2, 3));
        assertEquals(0, calc.add(-2, 2));
    }

    @Test
    void testSubtract() {
        assertEquals(1, calc.subtract(3, 2));
        assertEquals(-4, calc.subtract(-2, 2));
    }

    @Test
    void testIsPositive() {
        assertTrue(calc.isPositive(5));
        assertFalse(calc.isPositive(-1));
        assertFalse(calc.isPositive(0));
    }
}
