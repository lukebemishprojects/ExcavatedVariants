package dev.lukebemish.excavatedvariants.impl;

public final class ModLifecycle {

    private static State LOAD_STATE = State.PRE;
    private ModLifecycle() {}

    public enum State {
        PRE,
        REGISTRATION,
        POST
    }

    static synchronized void inRegistrationState() {
        LOAD_STATE = State.REGISTRATION;
    }
    static synchronized void inPostState() {
        LOAD_STATE = State.POST;
    }

    public static State getState() {
        return LOAD_STATE;
    }
}
