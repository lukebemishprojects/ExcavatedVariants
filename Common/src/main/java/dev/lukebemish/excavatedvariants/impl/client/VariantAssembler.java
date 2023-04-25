package dev.lukebemish.excavatedvariants.impl.client;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

public class VariantAssembler {
    public static final Codec<VariantAssembler> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        ResourceLocation.CODEC.fieldOf("model").forGetter(VariantAssembler::getModel),
        Codec.INT.optionalFieldOf("x", 0).forGetter(VariantAssembler::getX),
        Codec.INT.optionalFieldOf("y", 0).forGetter(VariantAssembler::getY)
    ).apply(instance, VariantAssembler::new));

    private final ResourceLocation model;
    private int x;
    private int y;

    public ResourceLocation getModel() {
        return model;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public VariantAssembler(ResourceLocation model) {
        this.model = model;
        this.x = 0;
        this.y = 0;
    }

    public VariantAssembler(ResourceLocation model, int x, int y) {
        this.model = model;
        this.x = x;
        this.y = y;
    }

    public static VariantAssembler fromFacing(ResourceLocation model, Direction dir) {
        var va = new VariantAssembler(model);
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
        var va = new VariantAssembler(model);
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
        return new VariantAssembler(model);
    }
}
