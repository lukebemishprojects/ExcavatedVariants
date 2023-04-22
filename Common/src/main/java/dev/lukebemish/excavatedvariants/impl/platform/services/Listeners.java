package dev.lukebemish.excavatedvariants.impl.platform.services;

import java.util.List;

import dev.lukebemish.excavatedvariants.api.CommonListener;
import dev.lukebemish.excavatedvariants.api.client.ClientListener;

public interface Listeners {

    @SuppressWarnings("unchecked")
    default <T extends CommonListener> List<T> getListeners(Class<T> clazz) {
        return getAllListeners().stream()
                .filter(clazz::isInstance)
                .map(it -> (T)it).toList();
    }

    List<CommonListener> getAllListeners();

    @SuppressWarnings("unchecked")
    default <T extends ClientListener> List<T> getClientListeners(Class<T> clazz) {
        return getAllClientListeners().stream()
                .filter(clazz::isInstance)
                .map(it -> (T)it).toList();
    }

    List<ClientListener> getAllClientListeners();
}
