package io.github.lukebemish.excavated_variants;

import io.github.lukebemish.excavated_variants.data.BaseOre;
import io.github.lukebemish.excavated_variants.data.BaseStone;
import io.github.lukebemish.excavated_variants.mixin.IBlockPropertiesMixin;
import io.github.lukebemish.excavated_variants.platform.Services;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ModifiedOreBlock extends DropExperienceBlock {
    public final BaseOre ore;
    public final BaseStone stone;

    protected Block target;
    protected Block stoneTarget;

    protected boolean delegateSpecialDrops = true;

    public ModifiedOreBlock(BaseOre ore, BaseStone stone) {
        super(copyProperties(ore, stone), getXpProvider(ore, stone));
        this.ore = ore;
        this.stone = stone;
        if (staticProps != null) {
            this.props = staticProps.clone();
            staticProps = null;
            copyBlockstateDefs();
        }

        ExcavatedVariants.getConfig().modifiers.stream().filter(m->m.filter().matches(ore,stone)).forEach(modifier -> {
            if (modifier.xpDropped().isPresent()) this.delegateSpecialDrops = false;
        });
    }

    private static IntProvider getXpProvider(BaseOre ore, BaseStone stone) {
        return ExcavatedVariants.getConfig().modifiers.stream().filter(m->m.filter().matches(ore,stone)).map(modifier -> {
            if (modifier.xpDropped().isPresent()) return modifier.xpDropped().get();
            return null;
        }).filter(Objects::nonNull).collect(Collectors.collectingAndThen(Collectors.toList(), list -> {
            Collections.reverse(list);
            return list.stream();
        })).findFirst().orElse(ConstantInt.of(0));
    }

    private static float avgF(float a, float b, float weight) {
        return (a*(weight)+b*(1-weight));
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
        Block target = Services.REGISTRY_UTIL.getBlockById(ore.block_id.get(0));
        Block stoneTarget = Services.REGISTRY_UTIL.getBlockById(stone.block_id);
        BlockBehaviour.Properties outProperties;
        if (target != null && stoneTarget != null) {
            Properties properties = Properties.copy(stoneTarget);
            Properties oreProperties = Properties.copy(target);
            properties.requiresCorrectToolForDrops();
            IBlockPropertiesMixin newProperties = (IBlockPropertiesMixin) properties;
            IBlockPropertiesMixin oreProps = (IBlockPropertiesMixin) oreProperties;
            properties.strength(avgStrength(target.defaultDestroyTime(),stoneTarget.defaultDestroyTime(),0.5f),
                            avgF(target.getExplosionResistance(),stoneTarget.getExplosionResistance(),0.5f))
                    .color(avgColor(stoneTarget.defaultMaterialColor(),target.defaultMaterialColor(),0.8F));
            newProperties.setDynamicShape(false);
            newProperties.setHasCollision(true);
            newProperties.setIsRandomlyTicking(oreProps.getIsRandomlyTicking());
            newProperties.setLightEmission(blockstate -> oreProps.getLightEmission().applyAsInt(withProperties(blockstate, target)));
            outProperties = properties;
        } else {
            outProperties = BlockBehaviour.Properties.of(Material.STONE).requiresCorrectToolForDrops().strength(3.0f, 3.0f);
        }
        ExcavatedVariants.getConfig().modifiers.stream().filter(m->m.filter().matches(ore,stone)).forEach(modifier -> {
                modifier.explosionResistance().ifPresent(outProperties::explosionResistance);
                modifier.destroyTime().ifPresent(outProperties::destroyTime);
        });
        return outProperties;
    }

    public static float avgStrength(float a, float b, float weight) {
        if (a<0 || b<0) return -1f;
        return avgF(a,b,weight);
    }

    private static BlockState withProperties(BlockState state, Block target) {
        BlockState blockState = target.defaultBlockState();
        Block block = state.getBlock();
        if (block instanceof ModifiedOreBlock self) {

            var arr = self.props == null ? staticProps : self.props;
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

    static Property<?>[] staticProps;
    public static void setupStaticWrapper(BaseOre ore, BaseStone stone) {
        Block target = Services.REGISTRY_UTIL.getBlockById(ore.block_id.get(0));
        Block stoneTarget = Services.REGISTRY_UTIL.getBlockById(stone.block_id);
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
            target = Services.REGISTRY_UTIL.getBlockById(ore.block_id.get(0));
            stoneTarget = Services.REGISTRY_UTIL.getBlockById(stone.block_id);
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
                    bs = bs.setValue(BlockStateProperties.HORIZONTAL_AXIS, Direction.Axis.Z);
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
            List<ItemStack> items = target.getDrops(state, builder);
            return items.stream().map(x -> {
                if (x.is(target.asItem()) && this.asItem() != Items.AIR && !ExcavatedVariants.getConfig().unobtainable_variants) {
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
}
