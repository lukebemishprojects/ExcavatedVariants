package io.github.lukebemish.excavated_variants;

import io.github.lukebemish.dynamic_asset_generator.client.api.DynAssetGeneratorClientAPI;
import io.github.lukebemish.excavated_variants.client.BlockStateAssembler;
import io.github.lukebemish.excavated_variants.data.BaseOre;
import io.github.lukebemish.excavated_variants.data.BaseStone;
import io.github.lukebemish.excavated_variants.data.ModData;
import io.github.lukebemish.excavated_variants.platform.Services;
import io.github.lukebemish.excavated_variants.util.Pair;
import net.minecraft.resources.ResourceLocation;

import java.util.*;
import java.util.stream.Collectors;

public class ExcavatedVariantsClient {
    public static void init() {
        LangBuilder langBuilder = new LangBuilder();
        Collection<String> modids = Services.PLATFORM.getModIds();

        ExcavatedVariants.setupMap();

        Map<String, BaseStone> stoneMap = new HashMap<>();
        for (ModData mod : ExcavatedVariants.getConfig().mods) {
            if (modids.containsAll(mod.mod_id)) {
                for (BaseStone stone : mod.provided_stones) {
                    stoneMap.put(stone.id, stone);
                }
            }
        }

        Map<String, Pair<BaseOre,BaseStone>> extractorMap = ExcavatedVariants.oreStoneList.stream().flatMap(p->p.last().stream().map(
                stone -> new Pair<>(stone.id+"_"+p.first().id, new Pair<>(p.first(), stoneMap.get(p.first().stone.get(0)))))).collect(Collectors.toMap(
                        Pair::first, Pair::last
        ));
        List<Pair<BaseOre,BaseStone>> toMake = new ArrayList<>();

        for (Pair<BaseOre, HashSet<BaseStone>> p : ExcavatedVariants.oreStoneList) {
            var ore = p.first();
            for (BaseStone stone : p.last()) {
                String fullId = stone.id+"_"+ore.id;
                toMake.add(new Pair<>(ore,stone));
                DynAssetGeneratorClientAPI.planLoadingStream(new ResourceLocation(ExcavatedVariants.MOD_ID, "models/item/" + fullId + ".json"),
                        JsonHelper.getItemModel(fullId));
                langBuilder.add(fullId, stone, ore);
            }
        }

        DynAssetGeneratorClientAPI.planLoadingStream(new ResourceLocation(ExcavatedVariants.MOD_ID, "lang/en_us.json"),langBuilder.build());
        BlockStateAssembler.setupClientAssets(extractorMap.values(),toMake);
    }
}
