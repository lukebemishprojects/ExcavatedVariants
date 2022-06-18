package io.github.lukebemish.excavated_variants.worldgen;

import io.github.lukebemish.excavated_variants.ExcavatedVariants;
import io.github.lukebemish.excavated_variants.ModifiedOreBlock;
import io.github.lukebemish.excavated_variants.data.BaseOre;
import io.github.lukebemish.excavated_variants.data.BaseStone;
import io.github.lukebemish.excavated_variants.platform.Services;
import io.github.lukebemish.excavated_variants.util.Pair;
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
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;

public class OreReplacer extends Feature<NoneFeatureConfiguration>  {
    public OreReplacer() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
        return modifyUnmodifiedNeighboringChunks(ctx.level(), ctx.origin());
    }

    public boolean modifyUnmodifiedNeighboringChunks(WorldGenLevel level, BlockPos pos) {
        OreGenMapSavedData data = OreGenMapSavedData.getOrCreate(level);
        int minY = level.getMinBuildHeight();
        int maxY = level.getMaxBuildHeight();
        if (data.edgeCount.containsKey(new Pair<>(pos.getX(),pos.getZ())) && data.edgeCount.get(new Pair<>(pos.getX(),pos.getZ())) == 8) {
            ChunkAccess chunkAccess = level.getChunk(pos);
            modifyChunk(chunkAccess,minY,maxY);
            data.edgeCount.put(new Pair<>(pos.getX(),pos.getZ()),9);
        }
        int[] xs = new int[] {-1, 0, 1, 1,-1,-1, 0, 1};
        int[] zs = new int[] {-1,-1,-1, 0, 0, 1, 1, 1};
        for (int i = 0; i < xs.length; i++) {

            BlockPos newPos = new BlockPos(pos.getX()+xs[i]*16, pos.getY(), pos.getZ()+zs[i]*16);
            Pair<Integer,Integer> chunkPos = new Pair<>(pos.getX() + xs[i] * 16, pos.getZ() + zs[i] * 16);
            if (!data.edgeCount.containsKey(chunkPos)) data.edgeCount.put(chunkPos,0);
            data.edgeCount.put(chunkPos, data.edgeCount.get(chunkPos)+1);
            if (data.edgeCount.get(chunkPos) == 8 && data.ranMap.containsKey(chunkPos)&&data.ranMap.get(chunkPos)) {
                ChunkAccess chunkAccess = level.getChunk(newPos);
                modifyChunk(chunkAccess, minY, maxY);
                data.edgeCount.put(chunkPos,9);
            }
        }
        data.ranMap.put(new Pair<>(pos.getX(),pos.getZ()), true);
        return true;
    }

    public boolean modifyChunk(ChunkAccess chunkAccess, int minY, int maxY) {
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
            for (int i=0;i<16;i++) {
                inner_loop:
                for (int j=0;j<16;j++) {
                    BlockState newState = cache[i][y&15][j]==null ? chunkSection.getBlockState(i,y & 15,j) : cache[i][y&15][j];
                    @Nullable Pair<BaseOre, HashSet<BaseStone>> pair = ((IOreFound)newState.getBlock()).excavated_variants$get_pair();
                    if (cache[i][y&15][j]==null) {
                        cache[i][y&15][j] = newState;
                    }
                    if (pair != null) {
                        int[] as = {1, 0, 0,-1, 0, 0};
                        int[] bs = {0,-1, 1, 0, 0, 0};
                        int[] ys = {0, 0, 0, 0,-1, 1};
                        for (int c = 0; c < as.length; c++) {
                            if (i + as[c] < 16 && i + as[c] >= 0 && j + bs[c] < 16 && j + bs[c] >= 0 && y+ys[c] >=chunkSection.bottomBlockY() && y+ys[c] < chunkSection.bottomBlockY()+16) {
                                BlockState thisState = cache[i+as[c]][y+ys[c]&15][j+bs[c]];
                                if (thisState == null){
                                    thisState = chunkSection.getBlockState(i + as[c], y + ys[c] & 15, j + bs[c]);
                                    cache[i + as[c]][y + ys[c] & 15][j + bs[c]] = thisState;
                                }
                                BaseStone stone = ((IOreFound)thisState.getBlock()).excavated_variants$get_stone();
                                if (stone != null) {
                                    Block oreBlock = Services.REGISTRY_UTIL.getBlockById(new ResourceLocation(ExcavatedVariants.MOD_ID, stone.id + "_" + pair.first().id));
                                    if (pair.last().contains(stone) && oreBlock instanceof ModifiedOreBlock modifiedOreBlock) {
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
        return true;
    }
}
