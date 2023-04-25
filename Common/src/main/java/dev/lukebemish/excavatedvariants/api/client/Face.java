package dev.lukebemish.excavatedvariants.api.client;

public enum Face {
    NORTH("north"),
    SOUTH("south"),
    EAST("east"),
    WEST("west"),
    UP("up"),
    DOWN("down");

    public final String faceName;

    Face(String faceName) {
        this.faceName = faceName;
    }
}
