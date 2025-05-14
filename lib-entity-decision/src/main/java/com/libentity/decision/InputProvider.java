package com.libentity.decision;

import java.util.List;

public interface InputProvider<I> {

    List<Rule<?>> getRules(I input);
}
