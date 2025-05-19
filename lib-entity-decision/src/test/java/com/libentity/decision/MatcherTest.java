package com.libentity.decision;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class MatcherTest {

    @Test
    void testIsRuleEval() {
        assertTrue(Rule.is("samba").eval("samba"));
        assertFalse(Rule.is("samba").eval("biome"));
        assertTrue(Rule.is("samba").not().eval("biome"));
    }

    @Test
    void testIsSetRuleEval() {
        assertTrue(Rule.isPresent().eval(9982));
        assertFalse(Rule.isPresent().eval(null));
        assertTrue(Rule.isPresent().not().eval(null));
    }

    @Test
    void testAnyRuleEval() {
        assertTrue(Rule.any().eval("samba"));
        assertTrue(Rule.any().eval(null));
        assertFalse(Rule.any().not().eval("keep max"));
    }

    @Test
    void testGtAndGteRuleEval() {
        assertTrue(Rule.gt(25).eval(30));
        assertFalse(Rule.gt(25).eval(20));
        assertTrue(Rule.gt(45).not().eval(30));

        assertTrue(Rule.gte(25).eval(25));
        assertTrue(Rule.gte(25).eval(30));
        assertFalse(Rule.gte(25).eval(20));
        assertTrue(Rule.gte(45).not().eval(30));
    }

    @Test
    void testLtAndLteRuleEval() {
        assertTrue(Rule.lt(25).eval(20));
        assertFalse(Rule.lt(25).eval(30));
        assertTrue(Rule.lt(15).not().eval(30));

        assertTrue(Rule.lte(25).eval(25));
        assertTrue(Rule.lte(25).eval(20));
        assertFalse(Rule.lte(25).eval(30));
        assertTrue(Rule.lte(15).not().eval(30));
    }

    @Test
    void testArbitraryPredicates() {
        assertTrue(Rule.test((Integer f) -> f > 1).eval(2));
        assertFalse(Rule.test((Integer f) -> 0 > f).eval(1));
        assertTrue(Rule.test((Integer f) -> 0 == f).not().eval(1));
    }
}
