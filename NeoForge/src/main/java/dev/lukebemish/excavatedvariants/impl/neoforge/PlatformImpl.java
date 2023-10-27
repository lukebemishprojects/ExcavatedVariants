/*
 * Copyright (C) 2023 Luke Bemish and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package dev.lukebemish.excavatedvariants.impl.neoforge;

import com.google.auto.service.AutoService;
import com.google.common.base.Suppliers;
import com.google.common.graph.ElementOrder;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.lukebemish.dynamicassetgenerator.api.ResourceGenerationContext;
import dev.lukebemish.excavatedvariants.impl.ExcavatedVariants;
import dev.lukebemish.excavatedvariants.impl.neoforge.mixin.ForgeTierSortingRegistryAccessor;
import dev.lukebemish.excavatedvariants.impl.platform.services.Platform;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Tier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.loading.toposort.TopologicalSort;
import net.minecraftforge.forgespi.language.IModInfo;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

@AutoService(Platform.class)
public class PlatformImpl implements Platform {
    private static final String MOD_VERSION = ModList.get().getModFileById(ExcavatedVariants.MOD_ID).versionString();

    @Override
    public Path getConfigFolder() {
        return FMLPaths.CONFIGDIR.get();
    }
    @Override
    public Path getModDataFolder() {
        return FMLPaths.GAMEDIR.get().resolve(".cache").resolve(ExcavatedVariants.MOD_ID);
    }

    @Override
    public String getModVersion() {
        return MOD_VERSION;
    }

    @Override
    public boolean isClient() {
        return FMLLoader.getDist() == Dist.CLIENT;
    }

    private static final Supplier<Set<String>> MOD_IDS = Suppliers.memoize(() -> Set.copyOf(ModList.get().getMods().stream().map(IModInfo::getModId).toList()));
    @Override
    public Set<String> getModIds() {
        return MOD_IDS.get();
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public List<ResourceLocation> getMiningLevels(ResourceGenerationContext context) {
        ResourceLocation ORDERING = new ResourceLocation("forge", "item_tier_ordering.json");
        var tiers = ForgeTierSortingRegistryAccessor.getTiers();
        List<Tier> tierList = new ArrayList<>();
        try {
            var resource = context.getResourceSource().getResource(ORDERING);
            if (resource == null) throw new IOException("Tier ordering resource not found");
            try (var stream = resource.get()) {
                byte[] bytes = stream.readAllBytes();
                var ordering = ItemTierOrdering.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseReader(new InputStreamReader(new ByteArrayInputStream(bytes)))).getOrThrow(false, e->{});
                boolean missingTiers = tiers.keySet().stream().anyMatch(tier -> !ordering.order.contains(tier));
                boolean extraTiers = ordering.order.stream().anyMatch(tier -> !tiers.containsKey(tier));
                if (!missingTiers && !extraTiers) {
                    for (ResourceLocation rl : ordering.order) {
                        tierList.add(tiers.get(rl));
                    }
                    return tierList.stream().filter(it -> it.getTag() != null).map(it->{
                        var l = it.getTag().location();
                        return l.withPrefix("blocks/");
                    }).toList();
                }
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
        return tierList.stream().filter(it -> it.getTag() != null).map(it->{
            var l = it.getTag().location();
            return new ResourceLocation(l.getNamespace(), "blocks/"+l.getPath());
        }).toList();
    }

    private record ItemTierOrdering(List<ResourceLocation> order) {
        static final Codec<ItemTierOrdering> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceLocation.CODEC.listOf().optionalFieldOf("order", List.of()).forGetter(ItemTierOrdering::order)
        ).apply(instance, ItemTierOrdering::new));
    }

    private static final Supplier<ModContainer> EV_CONTAINER = Suppliers.memoize(() -> ModList.get().getModContainerById(ExcavatedVariants.MOD_ID).orElseThrow());

    public void register(ExcavatedVariants.VariantFuture future) {
        ExcavatedVariants.registerBlockAndItem((rlr, bl) -> {
            final ModContainer activeContainer = ModLoadingContext.get().getActiveContainer();
            ModLoadingContext.get().setActiveContainer(EV_CONTAINER.get());
            ForgeRegistries.BLOCKS.register(rlr, bl);
            ModLoadingContext.get().setActiveContainer(activeContainer);
        }, (rlr, it) -> ExcavatedVariantsForge.TO_REGISTER.register(rlr.getPath(), it), future);
    }
}
