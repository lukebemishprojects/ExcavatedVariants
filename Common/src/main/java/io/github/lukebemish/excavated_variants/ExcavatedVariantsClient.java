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
                    if (!ExcavatedVariants.getConfig().configResource.blacklist_stones.contains(stone.id)) {
                        stoneMap.put(stone.id, stone);
                    }
                }
            }
        }

        Map<String, Pair<BaseOre,BaseStone>> extractorMap = ExcavatedVariants.oreStoneList.stream().flatMap(p->p.last().stream().map(
                stone -> new Pair<>(stone.id+"_"+p.first().id, new Pair<>(p.first(), stoneMap.get(p.first().stone.get(0)))))).collect(Collectors.toMap(
                        Pair::first, Pair::last
        ));
        List<Pair<BaseOre,BaseStone>> to_make = new ArrayList<>();

        for (Pair<BaseOre, HashSet<BaseStone>> p : ExcavatedVariants.oreStoneList) {
            var ore = p.first();
            for (BaseStone stone : p.last()) {
                String full_id = stone.id+"_"+ore.id;
                to_make.add(new Pair<>(ore,stone));
                DynAssetGeneratorClientAPI.planLoadingStream(new ResourceLocation(ExcavatedVariants.MOD_ID, "models/item/" + full_id + ".json"),
                        JsonHelper.getItemModel(full_id));
                langBuilder.add(full_id, stone, ore);
            }
        }

        DynAssetGeneratorClientAPI.planLoadingStream(new ResourceLocation(ExcavatedVariants.MOD_ID, "lang/en_us.json"),langBuilder.build());
        BlockStateAssembler.setupClientAssets(extractorMap.values(),to_make);
    }
}
