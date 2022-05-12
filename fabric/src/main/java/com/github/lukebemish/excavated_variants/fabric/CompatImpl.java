package com.github.lukebemish.excavated_variants.fabric;

import com.github.lukebemish.excavated_variants.api.IOreListModifier;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;

import java.util.List;

public class CompatImpl {
    private static final String ENTRYPOINT_NAME = "excavated_variants";

    public static List<IOreListModifier> getOreListModifiers() {
        var containers = FabricLoader.getInstance().getEntrypointContainers(ENTRYPOINT_NAME, IOreListModifier.class);
        return containers.stream().map(EntrypointContainer::getEntrypoint).toList();
    }
}
