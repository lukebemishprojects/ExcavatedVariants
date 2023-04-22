package dev.lukebemish.excavatedvariants.platform.services;

import java.util.List;

import dev.lukebemish.excavatedvariants.api.CompatPlugin;
import dev.lukebemish.excavatedvariants.api.client.ClientCompatPlugin;

public interface Listener {

    @SuppressWarnings("unchecked")
    default <T extends CompatPlugin> List<T> getListeners(Class<T> clazz) {
        return getAllListeners().stream()
                .filter(clazz::isInstance)
                .map(it -> (T)it).toList();
    }

    List<CompatPlugin> getAllListeners();

    @SuppressWarnings("unchecked")
    default <T extends ClientCompatPlugin> List<T> getClientListeners(Class<T> clazz) {
        return getAllClientListeners().stream()
                .filter(clazz::isInstance)
                .map(it -> (T)it).toList();
    }

    List<ClientCompatPlugin> getAllClientListeners();
}
