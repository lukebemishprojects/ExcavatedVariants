package dev.lukebemish.excavatedvariants.api.client;

import org.jetbrains.annotations.Nullable;

public enum Face {
    NORTH,
    SOUTH,
    EAST,
    WEST,
    UP,
    DOWN;

    @Nullable
    public static Face ofString(String face) {
        return switch (face) {
            case "north" -> NORTH;
            case "south" -> SOUTH;
            case "east" -> EAST;
            case "west" -> WEST;
            case "up" -> UP;
            case "down" -> DOWN;
            default -> null;
        };
    }
}
