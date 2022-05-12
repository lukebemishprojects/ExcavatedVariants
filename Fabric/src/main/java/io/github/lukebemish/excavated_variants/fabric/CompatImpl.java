package io.github.lukebemish.excavated_variants.fabric;

import io.github.lukebemish.excavated_variants.ICompat;
import io.github.lukebemish.excavated_variants.api.IOreListModifier;
import com.google.auto.service.AutoService;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;

import java.util.List;

@AutoService(ICompat.class)
public class CompatImpl implements ICompat {
    private static final String ENTRYPOINT_NAME = "excavated_variants";

    public List<IOreListModifier> getOreListModifiers() {
        var containers = FabricLoader.getInstance().getEntrypointContainers(ENTRYPOINT_NAME, IOreListModifier.class);
        return containers.stream().map(EntrypointContainer::getEntrypoint).toList();
    }
}
