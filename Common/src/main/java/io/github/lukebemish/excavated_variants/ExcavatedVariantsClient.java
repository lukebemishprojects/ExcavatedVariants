package io.github.lukebemish.excavated_variants;

import com.mojang.datafixers.util.Pair;
import io.github.lukebemish.dynamic_asset_generator.api.client.AssetResourceCache;
import io.github.lukebemish.excavated_variants.client.TextureRegistrar;
import io.github.lukebemish.excavated_variants.data.BaseOre;
import io.github.lukebemish.excavated_variants.data.BaseStone;
import io.github.lukebemish.excavated_variants.data.ModData;
import io.github.lukebemish.excavated_variants.platform.Services;
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

        Map<String, Pair<BaseOre,BaseStone>> extractorMap = ExcavatedVariants.oreStoneList.stream().flatMap(p->p.getSecond().stream().map(
                stone -> new Pair<>(stone.id+"_"+p.getFirst().id, new Pair<>(p.getFirst(), stoneMap.get(p.getFirst().stone.get(0)))))).collect(Collectors.toMap(
                        Pair::getFirst, Pair::getSecond
        ));
        List<Pair<BaseOre,BaseStone>> toMake = new ArrayList<>();

        for (Pair<BaseOre, HashSet<BaseStone>> p : ExcavatedVariants.oreStoneList) {
            var ore = p.getFirst();
            for (BaseStone stone : p.getSecond()) {
                String fullId = stone.id+"_"+ore.id;
                toMake.add(new Pair<>(ore,stone));
                AssetResourceCache.INSTANCE.planSource(new ResourceLocation(ExcavatedVariants.MOD_ID, "models/item/" + fullId + ".json"),
                        rl->JsonHelper.getItemModel(fullId));
                langBuilder.add(fullId, stone, ore);
            }
        }

        AssetResourceCache.INSTANCE.planSource(new ResourceLocation(ExcavatedVariants.MOD_ID, "lang/en_us.json"),rl->langBuilder.build());

        AssetResourceCache.INSTANCE.planSource(new TextureRegistrar(extractorMap.values(), toMake));
    }
}
