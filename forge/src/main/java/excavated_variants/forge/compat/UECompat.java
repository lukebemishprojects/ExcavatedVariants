package excavated_variants.forge.compat;

import excavated_variants.ExcavatedVariants;
import excavated_variants.Pair;
import excavated_variants.RegistryUtil;
import excavated_variants.data.BaseOre;
import excavated_variants.data.BaseStone;
import excavated_variants.data.ModData;
import excavated_variants.worldgen.OreFinderUtil;
import lilypuree.unearthed.compat.IStoneType;
import lilypuree.unearthed.compat.StoneTypeEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class UECompat {
    private Map<Block, Pair<BaseOre, List<BaseStone>>> excavatedVariantsOres;

    @SubscribeEvent
    public void onStoneTypeEvent(StoneTypeEvent event) {
        if (excavatedVariantsOres == null) {
            excavatedVariantsOres = new IdentityHashMap<>();
            for (ModData modData : ExcavatedVariants.getConfig().mods) {
                for (BaseOre baseOre : modData.provided_ores) {
                    Block baseOreBlock = RegistryUtil.getBlockById(new ResourceLocation(modData.mod_id, baseOre.id));
                    if (baseOreBlock != null) {
                        var pair = OreFinderUtil.getBaseOre(baseOreBlock.defaultBlockState());
                        if (pair != null)
                            excavatedVariantsOres.put(baseOreBlock, pair);
                    }
                }
            }
        }

        IStoneType stoneType = event.getStoneType();
        Block baseBlock = stoneType.getBaseBlock().getBlock();
        excavatedVariantsOres.forEach((baseOreBlock, pair) -> {
            List<BaseStone> Stones = pair.last();
            Stones.stream().filter(stone -> RegistryUtil.getBlockById(stone.rl_block_id) == baseBlock).findAny().ifPresent(stone -> {
                Block oreBlock = RegistryUtil.getBlockById(new ResourceLocation(ExcavatedVariants.MOD_ID, stone.id + "_" + pair.first().id));
                stoneType.getOreMap().put(baseOreBlock, oreBlock.defaultBlockState());
            });
        });
    }
}
