package dev.lukebemish.excavatedvariants.impl.forge;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.auto.service.AutoService;
import com.google.common.graph.ElementOrder;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.lukebemish.dynamicassetgenerator.api.ServerPrePackRepository;
import dev.lukebemish.excavatedvariants.impl.forge.mixin.ForgeTierSortingRegistryAccessor;
import dev.lukebemish.excavatedvariants.impl.platform.services.Platform;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.loading.toposort.TopologicalSort;
import net.minecraftforge.forgespi.language.IModInfo;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Tier;

@AutoService(Platform.class)
public class PlatformImpl implements Platform {
    public boolean isQuilt() {
        return false;
    }

    public boolean isForge() {
        return true;
    }

    public Collection<String> getModIds() {
        return ModList.get().getMods().stream().map(IModInfo::getModId).toList();
    }

    public Path getConfigFolder() {
        return FMLPaths.CONFIGDIR.get();
    }

    @Override
    public Path getModDataFolder() {
        return FMLPaths.GAMEDIR.get().resolve("mod_data/excavated_variants");
    }

    @Override
    public boolean isClient() {
        return FMLLoader.getDist() == Dist.CLIENT;
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public List<ResourceLocation> getMiningLevels() {
        ResourceLocation ORDERING = new ResourceLocation("forge", "item_tier_ordering.json");
        var tiers = ForgeTierSortingRegistryAccessor.getTiers();
        List<Tier> tierList = new ArrayList<>();
        try (var stream = ServerPrePackRepository.getResource(ORDERING);
             var reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            var ordering = ItemTierOrdering.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseReader(reader)).getOrThrow(false, e->{});
            boolean missingTiers = tiers.keySet().stream().anyMatch(tier -> !ordering.order.contains(tier));
            boolean extraTiers = ordering.order.stream().anyMatch(tier -> !tiers.containsKey(tier));
            if (!missingTiers && !extraTiers) {
                for (ResourceLocation rl : ordering.order) {
                    tierList.add(tiers.get(rl));
                }
                return tierList.stream().map(it->{
                    var l = it.getTag().location();
                    return new ResourceLocation(l.getNamespace(), "blocks/"+l.getPath());
                }).toList();
            }
        } catch (IOException | RuntimeException ignored) {
            // Huh, who knows, let's ignore it
            // (Could be it just doesn't exist)
        }
        final MutableGraph<Tier> graph = GraphBuilder.directed().nodeOrder(ElementOrder.<Tier>insertion()).build();

        for(Tier tier : tiers.values()) {
            graph.addNode(tier);
        }
        ForgeTierSortingRegistryAccessor.getEdges().forEach((key, value) -> {
            if (tiers.containsKey(key) && tiers.containsKey(value))
                graph.putEdge(tiers.get(key), tiers.get(value));
        });
        tierList = TopologicalSort.topologicalSort(graph, null);
        return tierList.stream().map(it->{
            var l = it.getTag().location();
            return new ResourceLocation(l.getNamespace(), "blocks/"+l.getPath());
        }).toList();
    }

    private record ItemTierOrdering(List<ResourceLocation> order) {
        static final Codec<ItemTierOrdering> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceLocation.CODEC.listOf().optionalFieldOf("order", List.of()).forGetter(ItemTierOrdering::order)
        ).apply(instance, ItemTierOrdering::new));
    }
}
