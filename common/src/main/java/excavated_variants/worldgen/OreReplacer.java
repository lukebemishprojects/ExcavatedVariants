package excavated_variants.worldgen;

import excavated_variants.ExcavatedVariants;
import excavated_variants.Pair;
import excavated_variants.RegistryUtil;
import excavated_variants.data.BaseOre;
import excavated_variants.data.BaseStone;
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

import java.util.List;

public class OreReplacer extends Feature<NoneFeatureConfiguration>  {
    public OreReplacer() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
        WorldGenLevel level = ctx.level();
        BlockPos pos = ctx.origin();
        ChunkAccess chunkAccess = level.getChunk(pos);
        int minY = level.getMinBuildHeight();
        LevelChunkSection chunkSection = chunkAccess.getSection(chunkAccess.getSectionIndex(minY));
        for (int y = minY; y < level.getMaxBuildHeight(); y++) {
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
                    BlockState newState = chunkSection.getBlockState(i,y & 15,j);
                    @Nullable Pair<BaseOre, List<BaseStone>> pair = OreFinderUtil.getBaseOre(newState);
                    if (cache[i][y&15][j]==null) {
                        cache[i][y&15][j] = newState;
                    }
                    if (pair != null) {
                        int[] as = {1, 0, 0,-1, 0, 0, 1, 1, 1, 1,-1,-1,-1,-1};
                        int[] bs = {0,-1, 1, 0, 0, 0, 1, 1,-1,-1, 1, 1,-1,-1};
                        int[] ys = {0, 0, 0, 0,-1, 1, 1,-1, 1,-1, 1,-1, 1,-1};
                        for (int c = 0; c < as.length; c++) {
                            if (i + as[c] < 16 && i + as[c] >= 0 && j + bs[c] < 16 && j + bs[c] >= 0 && y+ys[c] >=chunkSection.bottomBlockY() && y+ys[c] < chunkSection.bottomBlockY()+16) {
                                BlockState thisState = cache[i+as[c]][y+ys[c]&15][j+bs[c]];
                                if (thisState == null){
                                    thisState = chunkSection.getBlockState(i + as[c], y + ys[c] & 15, j + bs[c]);
                                    cache[i + as[c]][y + ys[c] & 15][j + bs[c]] = thisState;
                                }
                                for (BaseStone stone : pair.last()) {
                                    Block stoneBlock = RegistryUtil.getBlockById(stone.rl_block_id);
                                    Block oreBlock = RegistryUtil.getBlockById(new ResourceLocation(ExcavatedVariants.MOD_ID, stone.id + "_" + pair.first().id));
                                    if (oreBlock != null && stoneBlock != null && thisState.is(stoneBlock)) {
                                        BlockState def = oreBlock.defaultBlockState();
                                        chunkSection.setBlockState(i,y & 15,j,def,false);
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
