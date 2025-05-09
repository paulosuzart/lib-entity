package com.libentity.core.action;

public record ActionResult<S, R, C>(S state, R request, C command) {}
