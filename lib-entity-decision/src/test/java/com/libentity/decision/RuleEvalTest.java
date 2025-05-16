package com.libentity.decision;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class RuleEvalTest {

    @Test
    void testIsRuleEval() {
        assertTrue(Rule.is("samba").eval("samba"));
        assertFalse(Rule.is("samba").eval("biome"));
        assertTrue(Rule.is("samba").not().eval("biome"));
    }

    @Test
    void testIsSetRuleEval() {
        assertTrue(Rule.isSet().eval(9982));
        assertFalse(Rule.isSet().eval(null));
        assertTrue(Rule.isSet().not().eval(null));
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
}
