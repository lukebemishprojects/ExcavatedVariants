/*
 * Copyright (C) 2023 Luke Bemish and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package dev.lukebemish.excavatedvariants.impl;

import dev.lukebemish.excavatedvariants.api.data.Ore;
import dev.lukebemish.excavatedvariants.api.data.Stone;
import dev.lukebemish.excavatedvariants.api.data.modifier.Flag;
import dev.lukebemish.excavatedvariants.impl.mixin.BlockPropertiesMixin;
import net.minecraft.advancements.critereon.EnchantmentPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ModifiedOreBlock extends DropExperienceBlock {
    static Property<?>[] STATIC_PROPS;
    public final Ore ore;
    public final Stone stone;
    final Set<Flag> flags;
    protected final Block target;
    protected final Block stoneTarget;
    protected final boolean delegateSpecialDrops;
    private boolean isLit = false;
    private boolean isHorizontalFacing = false;
    private boolean isFacing = false;
    private boolean isHopperFacing = false;
    private boolean isAxis = false;
    private boolean isHorizontalAxis = false;
    private Property<?>[] props;

    public ModifiedOreBlock(ExcavatedVariants.VariantFuture future) {
        super(copyProperties(future), getXpProvider(future));
        this.ore = future.ore;
        this.stone = future.stone;
        this.flags = Set.copyOf(future.flags);
        this.target = future.foundOre;
        this.stoneTarget = future.foundStone;
        if (STATIC_PROPS != null) {
            this.props = STATIC_PROPS.clone();
            STATIC_PROPS = null;
            copyBlockstateDefs();
        }

        MutableBoolean shouldDelegateSpecialDrops = new MutableBoolean(true);
        for (var propsModifier : future.propsModifiers) {
            propsModifier.setXpDropped(xp -> shouldDelegateSpecialDrops.setValue(true));
        }
        this.delegateSpecialDrops = shouldDelegateSpecialDrops.booleanValue();
    }

    private static IntProvider getXpProvider(ExcavatedVariants.VariantFuture future) {
        Mutable<IntProvider> out = new MutableObject<>(ConstantInt.of(0));
        for (var propsModifier : future.propsModifiers) {
            propsModifier.setXpDropped(out::setValue);
        }
        return out.getValue();
    }

    private static float avgF(float a, float b, float weight) {
        return (a * (weight) + b * (1 - weight));
    }

    private static MapColor avgColor(MapColor a, MapColor b, @SuppressWarnings("SameParameterValue") float weight) {
        int avgColor = (int) (a.calculateRGBColor(MapColor.Brightness.HIGH) * weight + b.calculateRGBColor(MapColor.Brightness.HIGH) * (1 - weight));
        int lowest = 0;
        int lowDiff = 0xFFFFFF;
        for (int i = 0; i < 64; i++) {
            MapColor c = MapColor.byId(i);
            int diff = Math.abs(c.calculateRGBColor(MapColor.Brightness.HIGH) - avgColor);
            if (diff < lowDiff) {
                lowDiff = diff;
                lowest = i;
            }
        }
        return MapColor.byId(lowest);
    }

    private static Properties copyProperties(ExcavatedVariants.VariantFuture future) {
        Block target = future.foundOre;
        Block stoneTarget = future.foundStone;
        BlockBehaviour.Properties outProperties;
        if (target != null && stoneTarget != null) {
            Properties properties = Properties.copy(stoneTarget);
            Properties oreProperties = Properties.copy(target);
            properties.requiresCorrectToolForDrops();
            BlockPropertiesMixin newProperties = (BlockPropertiesMixin) properties;
            BlockPropertiesMixin oreProps = (BlockPropertiesMixin) oreProperties;
            properties.strength(avgStrength(target.defaultDestroyTime(), stoneTarget.defaultDestroyTime(), 0.5f),
                            avgF(target.getExplosionResistance(), stoneTarget.getExplosionResistance(), 0.5f))
                    .mapColor(avgColor(stoneTarget.defaultMapColor(), target.defaultMapColor(), 0.8F));
            newProperties.setDynamicShape(false);
            newProperties.setHasCollision(true);
            newProperties.setIsRandomlyTicking(oreProps.getIsRandomlyTicking());
            newProperties.setLightEmission(blockstate -> oreProps.getLightEmission().applyAsInt(withProperties(blockstate, target)));
            outProperties = properties;
        } else {
            outProperties = BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .requiresCorrectToolForDrops()
                    .strength(3.0f, 3.0f);
        }
        for (var propsModifier : future.propsModifiers) {
            propsModifier.setExplosionResistance(outProperties::explosionResistance);
            propsModifier.setDestroyTime(outProperties::destroyTime);
        }
        return outProperties;
    }

    public static float avgStrength(float a, float b, float weight) {
        if (a < 0 || b < 0) return -1f;
        return avgF(a, b, weight);
    }

    private static BlockState withProperties(BlockState state, Block target) {
        BlockState blockState = target.defaultBlockState();
        Block block = state.getBlock();
        if (block instanceof ModifiedOreBlock self) {

            var arr = self.props == null ? STATIC_PROPS : self.props;
            for (Property<?> property : arr) {
                if (blockState.hasProperty(property)) {
                    blockState = copyProperty(state, blockState, property);
                }
            }
        }

        return blockState;
    }

    private static <T extends Comparable<T>> BlockState copyProperty(BlockState sourceState, BlockState targetState, Property<T> property) {
        return targetState.setValue(property, sourceState.getValue(property));
    }

    public static void setupStaticWrapper(ExcavatedVariants.VariantFuture future) {
        Block target = future.foundOre;
        Block stoneTarget = future.foundStone;
        ArrayList<Property<?>> propBuilder = new ArrayList<>();
        for (Property<?> p : target.defaultBlockState().getProperties()) {
            if (p == BlockStateProperties.LIT) {
                propBuilder.add(p);
            }
        }
        for (Property<?> p : stoneTarget.defaultBlockState().getProperties()) {
            if (p == BlockStateProperties.AXIS
                    || p == BlockStateProperties.HORIZONTAL_AXIS
                    || p == BlockStateProperties.FACING
                    || p == BlockStateProperties.FACING_HOPPER
                    || p == BlockStateProperties.HORIZONTAL_FACING) {
                propBuilder.add(p);
            }
        }
        STATIC_PROPS = propBuilder.toArray(new Property<?>[]{});
    }

    private void copyBlockstateDefs() {
        BlockState bs = this.defaultBlockState();
        for (Property<?> p : props) {
            if (p == BlockStateProperties.LIT) {
                this.isLit = true;
                bs = bs.setValue(BlockStateProperties.LIT, false);
            }
            if (p == BlockStateProperties.FACING) {
                this.isFacing = true;
                bs = bs.setValue(BlockStateProperties.FACING, Direction.NORTH);
            }
            if (p == BlockStateProperties.FACING_HOPPER) {
                this.isHopperFacing = true;
                bs = bs.setValue(BlockStateProperties.FACING_HOPPER, Direction.NORTH);
            }
            if (p == BlockStateProperties.HORIZONTAL_FACING) {
                this.isHorizontalFacing = true;
                bs = bs.setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH);
            }
            if (p == BlockStateProperties.AXIS) {
                this.isAxis = true;
                bs = bs.setValue(BlockStateProperties.AXIS, Direction.Axis.Y);
            }
            if (p == BlockStateProperties.HORIZONTAL_AXIS) {
                this.isHorizontalAxis = true;
                bs = bs.setValue(BlockStateProperties.HORIZONTAL_AXIS, Direction.Axis.Z);
            }
        }
        this.registerDefaultState(bs);
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        if (isLit) {
            return state.getValue(BlockStateProperties.LIT);
        }
        return super.isRandomlyTicking(state);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void attack(BlockState state, Level level, BlockPos pos, Player player) {
        interact(state, level, pos);
        super.attack(state, level, pos, player);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        interact(state, level, pos);
        super.stepOn(level, pos, state, entity);
    }

    private void interact(BlockState state, Level level, BlockPos pos) {
        if (isLit && !state.getValue(BlockStateProperties.LIT)) {
            level.setBlock(pos, state.setValue(BlockStateProperties.LIT, true), 3);
        }

    }

    @SuppressWarnings("deprecation")
    @Override
    public @NonNull InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide) {
            interact(state, level, pos);
        }
        if (isLit) {
            ItemStack itemStack = player.getItemInHand(hand);
            return itemStack.getItem() instanceof BlockItem && (new BlockPlaceContext(player, hand, itemStack, hit)).canPlace() ? InteractionResult.PASS : InteractionResult.SUCCESS;
        }
        return super.use(state, level, pos, player, hand, hit);

    }

    @SuppressWarnings("deprecation")
    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (isLit) {
            if (state.getValue(BlockStateProperties.LIT)) {
                level.setBlock(pos, state.setValue(BlockStateProperties.LIT, false), 3);
            }
        }
        super.randomTick(state, level, pos, random);
    }

    @Override
    public float defaultDestroyTime() {
        if (target != null) {
            return this.target.defaultDestroyTime();
        }
        return super.defaultDestroyTime();
    }

    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (target != null) target.animateTick(state, level, pos, random);
        if (stoneTarget != null) stoneTarget.animateTick(state, level, pos, random);
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        if (props != null) {
            if (props.length != 0) {
                builder.add(props);
            }
        } else if (STATIC_PROPS != null && STATIC_PROPS.length != 0) {
            builder.add(STATIC_PROPS);
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public @NonNull List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        if (target != null) {
            BlockState targetState = target.defaultBlockState();
            List<ItemStack> items = target.getDrops(targetState, builder);
            ItemStack tool = builder.withParameter(LootContextParams.BLOCK_STATE, targetState).create(LootContextParamSets.BLOCK).getParamOrNull(LootContextParams.TOOL);
            boolean isSilk = ItemPredicate.Builder.item().hasEnchantment(new EnchantmentPredicate(Enchantments.SILK_TOUCH, MinMaxBounds.Ints.atLeast(1))).build().matches(tool);
            return items.stream().map(x -> {
                if (x.is(target.asItem()) && this.asItem() != Items.AIR && !(flags.contains(Flag.ORIGINAL_ALWAYS))
                        && !(flags.contains(Flag.ORIGINAL_WITHOUT_SILK) && !isSilk)) {
                    int count = x.getCount();
                    return new ItemStack(this.asItem(), count);
                }
                return x;
            }).toList();
        }
        return new ArrayList<>();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void spawnAfterBreak(BlockState state, ServerLevel level, BlockPos pos, ItemStack stack, boolean bl) {
        if (target != null && this.delegateSpecialDrops) {
            target.spawnAfterBreak(state, level, pos, stack, bl);
        } else {
            super.spawnAfterBreak(state, level, pos, stack, bl);
        }
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState def = this.defaultBlockState();
        if (isAxis) {
            def = def.setValue(BlockStateProperties.AXIS, context.getClickedFace().getAxis());
        } else if (isHorizontalAxis) {
            def = def.setValue(BlockStateProperties.HORIZONTAL_AXIS, context.getHorizontalDirection().getAxis());
        } else if (isFacing) {
            def = def.setValue(BlockStateProperties.FACING, context.getClickedFace().getOpposite());
        } else if (isHopperFacing) {
            var d = context.getClickedFace().getOpposite();
            def = def.setValue(BlockStateProperties.FACING_HOPPER, d.getAxis() == Direction.Axis.Y ? Direction.DOWN : d);
        } else if (isHorizontalFacing) {
            def = def.setValue(BlockStateProperties.HORIZONTAL_FACING, context.getHorizontalDirection());
        }
        return def;
    }

    public boolean isAxisType() {
        return isAxis || isHorizontalAxis;
    }

    public boolean isFacingType() {
        return isFacing || isHorizontalFacing || isHopperFacing;
    }
}
