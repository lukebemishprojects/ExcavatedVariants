package dev.lukebemish.excavatedvariants.client;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

public class VariantAssembler {
    public String model;
    public int x;
    public int y;

    public static VariantAssembler fromFacing(ResourceLocation model, Direction dir) {
        var va = new VariantAssembler();
        va.model = model.toString();
        switch (dir) {
            case UP -> va.x = 270;
            case DOWN -> va.x = 90;
            case EAST -> va.y = 90;
            case NORTH -> {
            }
            case WEST -> va.y = 270;
            case SOUTH -> va.y = 180;
        }
        return va;
    }

    public static VariantAssembler fromAxis(ResourceLocation model, Direction.Axis axis) {
        var va = new VariantAssembler();
        va.model = model.toString();
        switch (axis) {
            case X -> {
                va.x = 90;
                va.y = 90;
            }
            case Y -> {

            }
            case Z -> va.x = 90;
        }
        return va;
    }

    public static VariantAssembler fromModel(ResourceLocation model) {
        var va = new VariantAssembler();
        va.model = model.toString();
        return va;
    }
}
