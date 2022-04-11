package com.github.lukebemish.excavated_variants;

import com.github.lukebemish.excavated_variants.data.BaseOre;
import com.github.lukebemish.excavated_variants.data.BaseStone;
import com.github.lukebemish.excavated_variants.mixin.IBlockBehaviorMixin;
import com.github.lukebemish.excavated_variants.mixin.IBlockMixin;
import com.github.lukebemish.excavated_variants.mixin.IBlockPropertiesMixin;
import com.github.lukebemish.excavated_variants.mixin.IBlockStateBaseMixin;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.OreBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class ModifiedOreBlock extends OreBlock {
    public final BaseOre ore;
    public final BaseStone stone;

    private Block target;
    private Block stoneTarget;

    public ModifiedOreBlock(Properties properties, BaseOre ore, BaseStone stone) {
        super(properties);
        this.ore = ore;
        this.stone = stone;
    }

    private static float avgF(float a, float b) {
        return (a+b)/2f;
    }

    private static MaterialColor avgColor(MaterialColor a, MaterialColor b, float weight) {
        int avgColor = (int)(a.calculateRGBColor(MaterialColor.Brightness.HIGH)*weight+b.calculateRGBColor(MaterialColor.Brightness.HIGH)*(1-weight));
        int lowest = 0;
        int lowDiff = 0xFFFFFF;
        for (int i = 0; i<64; i++) {
            MaterialColor c = MaterialColor.byId(i);
            int diff = Math.abs(c.calculateRGBColor(MaterialColor.Brightness.HIGH)-avgColor);
            if (diff < lowDiff) {
                lowDiff = diff;
                lowest = i;
            }
        }
        return MaterialColor.byId(lowest);
    }

    private void copyProperties() {
        if (target != null && stoneTarget != null) {
            Properties properties = Properties.copy(stoneTarget);
            Properties oreProperties = Properties.copy(target);
            properties.requiresCorrectToolForDrops();
            IBlockPropertiesMixin newProperties = (IBlockPropertiesMixin) properties;
            IBlockPropertiesMixin oreProps = (IBlockPropertiesMixin) oreProperties;
            properties.strength(avgF(target.defaultDestroyTime(),stoneTarget.defaultDestroyTime()),avgF(target.getExplosionResistance(),stoneTarget.getExplosionResistance()))
                    .color(avgColor(stoneTarget.defaultMaterialColor(),target.defaultMaterialColor(),0.8F));
            newProperties.setDynamicShape(false);
            newProperties.setHasCollision(true);
            newProperties.setIsRandomlyTicking(oreProps.getIsRandomlyTicking());
            newProperties.setLightEmission(oreProps.getLightEmission());
            IBlockBehaviorMixin setter = (IBlockBehaviorMixin) this;
            setter.setMaterial(newProperties.getMaterial());
            setter.setExplosionResistance(newProperties.getExplosionResistance());
            setter.setIsRandomlyTicking(newProperties.getIsRandomlyTicking());
            setter.setSoundType(newProperties.getSoundType());
            setter.setFriction(newProperties.getFriction());
            setter.setSpeedFactor(newProperties.getSpeedFactor());
            setter.setJumpFactor(newProperties.getJumpFactor());
            setter.setProperties(properties);
        }
    }

    public void copyBlockstateDefs() {
        if (target==null || stoneTarget==null) {
            this.target = RegistryUtil.getBlockById(ore.rl_block_id.get(0));
            this.stoneTarget = RegistryUtil.getBlockById(stone.rl_block_id);
            this.copyProperties();

            ArrayList<Property<?>> propBuilder = new ArrayList<>();
            for (Property<?> p : target.defaultBlockState().getProperties()) {
                System.out.println(stone.id+"_"+ore.id+", "+p);
                if (p == BlockStateProperties.LIT) {
                    propBuilder.add(BlockStateProperties.LIT);
                }
            }
            props = propBuilder.toArray(new Property<?>[]{});

            StateDefinition.Builder<Block, BlockState> builder = new StateDefinition.Builder<>(this);
            this.createBlockStateDefinition(builder);
            ((IBlockMixin) this).setStateDefinition(builder.create(Block::defaultBlockState, BlockState::new));
            this.registerDefaultState(this.stateDefinition.any());

            BlockState bs = this.defaultBlockState();
            for (Property<?> p : props) {
                if (p == BlockStateProperties.LIT) {
                    bs.setValue(BlockStateProperties.LIT,target.defaultBlockState().getValue(BlockStateProperties.LIT));
                }
            }
            this.registerDefaultState(bs);
        }
    }

    private boolean isLit = false;

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        if (isLit) {
            return state.getValue(BlockStateProperties.LIT);
        }
        return super.isRandomlyTicking(state);
    }

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
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide) {
            interact(state, level, pos);
        }
        return super.use(state, level, pos, player, hand, hit);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, Random random) {
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

    private Property<?>[] props;

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        if (props != null && props.length != 0) {
            builder.add(props);
        }
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        Block target = RegistryUtil.getBlockById(ore.rl_block_id.get(0));
        if (target != null) {
            state = target.defaultBlockState();
            ResourceLocation resourceLocation = target.getLootTable();
            if (resourceLocation == BuiltInLootTables.EMPTY) {
                return Collections.emptyList();
            }
            LootContext lootContext = builder.withParameter(LootContextParams.BLOCK_STATE, state).create(LootContextParamSets.BLOCK);
            ServerLevel serverLevel = lootContext.getLevel();
            LootTable lootTable = serverLevel.getServer().getLootTables().get(resourceLocation);
            List<ItemStack> items = lootTable.getRandomItems(lootContext);
            return items.stream().map((x) -> {
                if (x.is(target.asItem()) && this.asItem() != Items.AIR) {
                    int count = x.getCount();
                    ItemStack out = new ItemStack(this.asItem(), count);
                    return out;
                }
                return x;
            }).toList();
        }
        return new ArrayList<>();
    }

    @Override
    public void spawnAfterBreak(BlockState state, ServerLevel level, BlockPos pos, ItemStack stack) {
        Block target = RegistryUtil.getBlockById(ore.rl_block_id.get(0));
        if (target != null) {
            target.spawnAfterBreak(state, level, pos, stack);
        } else {
            super.spawnAfterBreak(state, level, pos, stack);
        }
    }
}
