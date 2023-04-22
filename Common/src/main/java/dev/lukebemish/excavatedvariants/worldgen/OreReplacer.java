package dev.lukebemish.excavatedvariants.worldgen;

import java.util.HashSet;

import com.mojang.datafixers.util.Pair;
import dev.lukebemish.excavatedvariants.ExcavatedVariants;
import dev.lukebemish.excavatedvariants.ModifiedOreBlock;
import dev.lukebemish.excavatedvariants.data.BaseOre;
import dev.lukebemish.excavatedvariants.data.BaseStone;
import dev.lukebemish.excavatedvariants.platform.Services;
import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class OreReplacer extends Feature<NoneFeatureConfiguration> {
    private static final int[] xs = new int[]{-1, 0, 1, 1, -1, -1, 0, 1};
    private static final int[] zs = new int[]{-1, -1, -1, 0, 0, 1, 1, 1};
    private static final int[] as = {1, 0, 0, -1, 0, 0};
    private static final int[] bs = {0, -1, 1, 0, 0, 0};
    private static final int[] ys = {0, 0, 0, 0, -1, 1};

    public OreReplacer() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
        if (!ExcavatedVariants.getConfig().attemptWorldgenReplacement)
            return true;
        return modifyUnmodifiedNeighboringChunks(ctx.level(), ctx.origin());
    }

    public boolean modifyUnmodifiedNeighboringChunks(WorldGenLevel level, BlockPos pos) {
        OreGenMapSavedData data = OreGenMapSavedData.getOrCreate(level);
        int minY = level.getMinBuildHeight();
        int maxY = level.getMaxBuildHeight();
        if (data.edgeCount.containsKey(new Pair<>(pos.getX(), pos.getZ())) && data.edgeCount.get(new Pair<>(pos.getX(), pos.getZ())) == 8) {
            ChunkAccess chunkAccess = level.getChunk(pos);
            modifyChunk(chunkAccess, minY, maxY);
            data.edgeCount.put(new Pair<>(pos.getX(), pos.getZ()), 9);
        }
        BlockPos.MutableBlockPos newPos = new BlockPos.MutableBlockPos(pos.getX(), pos.getY(), pos.getZ());
        for (int i = 0; i < xs.length; i++) {
            newPos.setX(pos.getX() + xs[i] * 16);
            newPos.setZ(pos.getZ() + zs[i] * 16);
            Pair<Integer, Integer> chunkPos = new Pair<>(newPos.getX(), newPos.getZ());
            if (!data.edgeCount.containsKey(chunkPos)) data.edgeCount.put(chunkPos, 0);
            data.edgeCount.put(chunkPos, data.edgeCount.get(chunkPos) + 1);
            if (data.edgeCount.get(chunkPos) == 8 && data.ranMap.containsKey(chunkPos) && data.ranMap.get(chunkPos)) {
                ChunkAccess chunkAccess = level.getChunk(newPos);
                modifyChunk(chunkAccess, minY, maxY);
                data.edgeCount.put(chunkPos, 9);
            }
        }
        data.ranMap.put(new Pair<>(pos.getX(), pos.getZ()), true);
        return true;
    }

    public void modifyChunk(ChunkAccess chunkAccess, int minY, int maxY) {
        LevelChunkSection chunkSection = chunkAccess.getSection(chunkAccess.getSectionIndex(minY));
        for (int y = minY; y < maxY; y++) {
            BlockState[][][] cache = new BlockState[16][16][16];
            int sectionIndex = chunkAccess.getSectionIndex(y);
            if (chunkAccess.getSectionIndex(chunkSection.bottomBlockY()) != sectionIndex) {
                chunkSection = chunkAccess.getSection(sectionIndex);
            }
            if (chunkSection.hasOnlyAir()) {
                continue;
            }
            for (int i = 0; i < 16; i++) {
                inner_loop:
                for (int j = 0; j < 16; j++) {
                    BlockState newState = cache[i][y & 15][j] == null ? chunkSection.getBlockState(i, y & 15, j) : cache[i][y & 15][j];
                    @Nullable Pair<BaseOre, HashSet<BaseStone>> pair = ((OreFound) newState.getBlock()).excavated_variants$getPair();
                    if (cache[i][y & 15][j] == null) {
                        cache[i][y & 15][j] = newState;
                    }
                    if (pair != null) {
                        for (int c = 0; c < as.length; c++) {
                            if (i + as[c] < 16 && i + as[c] >= 0 && j + bs[c] < 16 && j + bs[c] >= 0 && y + ys[c] >= chunkSection.bottomBlockY() && y + ys[c] < chunkSection.bottomBlockY() + 16) {
                                BlockState thisState = cache[i + as[c]][y + ys[c] & 15][j + bs[c]];
                                if (thisState == null) {
                                    thisState = chunkSection.getBlockState(i + as[c], y + ys[c] & 15, j + bs[c]);
                                    cache[i + as[c]][y + ys[c] & 15][j + bs[c]] = thisState;
                                }
                                BaseStone stone = ((OreFound) thisState.getBlock()).excavated_variants$getStone();
                                if (stone != null) {
                                    Block oreBlock = Services.REGISTRY_UTIL.getBlockById(new ResourceLocation(ExcavatedVariants.MOD_ID, stone.id + "_" + pair.getFirst().id));
                                    if (pair.getSecond().contains(stone) && oreBlock instanceof ModifiedOreBlock modifiedOreBlock) {
                                        BlockState def = modifiedOreBlock.withPropertiesOf(thisState);
                                        chunkSection.setBlockState(i, y & 15, j, def, false);
                                        continue inner_loop;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
