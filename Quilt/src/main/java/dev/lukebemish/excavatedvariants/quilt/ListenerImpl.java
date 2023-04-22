package dev.lukebemish.excavatedvariants.quilt;

import java.util.List;

import com.google.auto.service.AutoService;
import dev.lukebemish.excavatedvariants.api.client.ClientCompatPlugin;
import dev.lukebemish.excavatedvariants.platform.services.Listener;
import dev.lukebemish.excavatedvariants.api.CompatPlugin;

import org.quiltmc.loader.api.QuiltLoader;
import org.quiltmc.loader.api.entrypoint.EntrypointContainer;

@AutoService(Listener.class)
public class ListenerImpl implements Listener {
    private static final String ENTRYPOINT_NAME = "excavated_variants";
    private static final String CLIENT_ENTRYPOINT_NAME = "excavated_variants_client";

    @Override
    public List<CompatPlugin> getAllListeners() {
        var containers = QuiltLoader.getEntrypointContainers(ENTRYPOINT_NAME, CompatPlugin.class);
        return containers.stream()
                .map(EntrypointContainer::getEntrypoint)
                .sorted().toList();
    }

    @Override
    public List<ClientCompatPlugin> getAllClientListeners() {
        var containers = QuiltLoader.getEntrypointContainers(CLIENT_ENTRYPOINT_NAME, ClientCompatPlugin.class);
        return containers.stream()
                .map(EntrypointContainer::getEntrypoint)
                .sorted().toList();
    }
}
