package com.github.lukebemish.excavated_variants;

import com.github.lukebemish.excavated_variants.data.BaseOre;
import com.github.lukebemish.excavated_variants.data.BaseStone;
import com.github.lukebemish.excavated_variants.mixin.IBlockPropertiesMixin;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class ModifiedOreBlock extends OreBlock {
    public final BaseOre ore;
    public final BaseStone stone;

    private Block target;
    private Block stoneTarget;

    public ModifiedOreBlock(BaseOre ore, BaseStone stone) {
        super(copyProperties(ore, stone));
        this.ore = ore;
        this.stone = stone;
        if (staticProps != null) {
            this.props = staticProps.clone();
            staticProps = null;
            copyBlockstateDefs();
        }
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

    private static Properties copyProperties(BaseOre ore, BaseStone stone) {
        Block target = RegistryUtil.getBlockById(ore.block_id.get(0));
        Block stoneTarget = RegistryUtil.getBlockById(stone.block_id);
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
            return properties;
        }
        return BlockBehaviour.Properties.of(Material.STONE).requiresCorrectToolForDrops().strength(3.0f, 3.0f);
    }

    static Property<?>[] staticProps;
    public static void setupStaticWrapper(BaseOre ore, BaseStone stone) {
        Block target = RegistryUtil.getBlockById(ore.block_id.get(0));
        Block stoneTarget = RegistryUtil.getBlockById(stone.block_id);
        if (target!=null && stoneTarget!=null) {
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
            staticProps = propBuilder.toArray(new Property<?>[]{});
        } else {
            ExcavatedVariants.LOGGER.warn("Could not find block properties for: {}, {}",ore.block_id.get(0),stone.block_id);
        }

    }

    public void copyBlockstateDefs() {
        if (target==null || stoneTarget==null) {
            target = RegistryUtil.getBlockById(ore.block_id.get(0));
            stoneTarget = RegistryUtil.getBlockById(stone.block_id);
        }
        if (target != null && stoneTarget != null) {
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
                    bs = bs.setValue(BlockStateProperties.AXIS, Direction.Axis.Y);
                }
            }
            this.registerDefaultState(bs);
        }
    }

    public boolean isLit() {
        return isLit;
    }

    private boolean isLit = false;
    private boolean isHorizontalFacing = false;
    private boolean isFacing = false;
    private boolean isHopperFacing = false;
    private boolean isAxis = false;
    private boolean isHorizontalAxis = false;

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
        if (isLit) {
            ItemStack itemStack = player.getItemInHand(hand);
            return itemStack.getItem() instanceof BlockItem && (new BlockPlaceContext(player, hand, itemStack, hit)).canPlace() ? InteractionResult.PASS : InteractionResult.SUCCESS;
        }
        return super.use(state,level,pos,player,hand,hit);

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

    public void animateTick(BlockState state, Level level, BlockPos pos, Random random) {
        if (target!=null) target.animateTick(state,level,pos,random);
        if (stoneTarget!=null) stoneTarget.animateTick(state,level,pos,random);
    }

    private Property<?>[] props;

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        if (props != null) {
            if (props.length != 0) {
                builder.add(props);
            }
        } else if (staticProps != null && staticProps.length != 0) {
            builder.add(staticProps);
        }
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
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
        Block target = RegistryUtil.getBlockById(ore.block_id.get(0));
        if (target != null) {
            target.spawnAfterBreak(state, level, pos, stack);
        } else {
            super.spawnAfterBreak(state, level, pos, stack);
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
            def = def.setValue(BlockStateProperties.FACING_HOPPER, d.getAxis()== Direction.Axis.Y?Direction.DOWN:d);
        } else if (isHorizontalFacing) {
            def = def.setValue(BlockStateProperties.HORIZONTAL_FACING, context.getHorizontalDirection());
        }
        return def;
    }

    public boolean isAxisType() {
        return isAxis||isHorizontalAxis;
    }

    public boolean isFacingType() {
        return isFacing||isHorizontalFacing||isHopperFacing;
    }

    public BlockState getStateForReplacement(BlockState thisState) {
        if (isFacing && thisState.hasProperty(BlockStateProperties.FACING)) {
            return defaultBlockState().setValue(BlockStateProperties.FACING,thisState.getValue(BlockStateProperties.FACING));
        } else if (isHorizontalFacing && thisState.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            return defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING,thisState.getValue(BlockStateProperties.HORIZONTAL_FACING));
        } else if (isHopperFacing && thisState.hasProperty(BlockStateProperties.FACING_HOPPER)) {
            return defaultBlockState().setValue(BlockStateProperties.FACING_HOPPER,thisState.getValue(BlockStateProperties.FACING_HOPPER));
        } else if (isAxis && thisState.hasProperty(BlockStateProperties.AXIS)) {
            return defaultBlockState().setValue(BlockStateProperties.AXIS,thisState.getValue(BlockStateProperties.AXIS));
        } else if (isHorizontalAxis && thisState.hasProperty(BlockStateProperties.HORIZONTAL_AXIS)) {
            return defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_AXIS,thisState.getValue(BlockStateProperties.HORIZONTAL_AXIS));
        }
        return defaultBlockState();
    }
}
