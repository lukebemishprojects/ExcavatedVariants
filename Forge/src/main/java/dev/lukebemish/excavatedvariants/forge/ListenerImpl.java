package dev.lukebemish.excavatedvariants.forge;

import java.util.List;

import com.google.auto.service.AutoService;
import dev.lukebemish.excavatedvariants.api.client.ClientCompatPlugin;
import dev.lukebemish.excavatedvariants.platform.services.Listener;
import dev.lukebemish.excavatedvariants.api.ExcavatedVariantsListener;
import dev.lukebemish.excavatedvariants.api.CompatPlugin;

@AutoService(Listener.class)
public class ListenerImpl implements Listener {
    @Override
    public List<CompatPlugin> getAllListeners() {
        return PluginFinder.getInstances(ExcavatedVariantsListener.class, CompatPlugin.class).stream().sorted().toList();
    }

    @Override
    public List<ClientCompatPlugin> getAllClientListeners() {
        return PluginFinder.getInstances(ExcavatedVariantsListener.class, ClientCompatPlugin.class).stream().sorted().toList();
    }
}
