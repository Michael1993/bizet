package com.javax1.bizet.main;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BizetTests {

    @Test
    @DisplayName("determines if field type contains multiples")
    void testMultiple() throws NoSuchFieldException {
        assertTrue(Bizet.isMultiple(TestCases.class.getDeclaredField("arrayField").getType()));
        assertTrue(Bizet.isMultiple(TestCases.class.getDeclaredField("setField").getType()));
        assertTrue(Bizet.isMultiple(TestCases.class.getDeclaredField("listField").getType()));
        assertTrue(Bizet.isMultiple(TestCases.class.getDeclaredField("collectionField").getType()));
        assertFalse(Bizet.isMultiple(TestCases.class.getDeclaredField("notMultiple").getType()));
        assertFalse(Bizet.isMultiple(TestCases.class.getDeclaredField("notMultiple2").getType()));

    }

    final static class TestCases {
        Integer[] arrayField;
        Set<?> setField;
        List<?> listField;
        Collection<?> collectionField;

        Double notMultiple;
        TestCases notMultiple2;
    }
}
