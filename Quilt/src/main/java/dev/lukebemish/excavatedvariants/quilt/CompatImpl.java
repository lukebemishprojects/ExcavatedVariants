package dev.lukebemish.excavatedvariants.quilt;

import com.google.auto.service.AutoService;
import dev.lukebemish.excavatedvariants.ICompat;
import dev.lukebemish.excavatedvariants.api.IOreListModifier;
import org.quiltmc.loader.api.QuiltLoader;
import org.quiltmc.loader.api.entrypoint.EntrypointContainer;

import java.util.List;

@AutoService(ICompat.class)
public class CompatImpl implements ICompat {
    private static final String ENTRYPOINT_NAME = "excavated_variants";

    public List<IOreListModifier> getOreListModifiers() {
        var containers = QuiltLoader.getEntrypointContainers(ENTRYPOINT_NAME, IOreListModifier.class);
        return containers.stream().map(EntrypointContainer::getEntrypoint).toList();
    }
}
