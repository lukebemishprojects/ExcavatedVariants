/*
 * Copyright (C) 2023 Luke Bemish and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package dev.lukebemish.excavatedvariants.impl.quilt;

import java.util.List;

import com.google.auto.service.AutoService;
import dev.lukebemish.excavatedvariants.api.client.ClientListener;
import dev.lukebemish.excavatedvariants.impl.platform.services.Listeners;
import dev.lukebemish.excavatedvariants.api.CommonListener;

import org.quiltmc.loader.api.QuiltLoader;
import org.quiltmc.loader.api.entrypoint.EntrypointContainer;

@AutoService(Listeners.class)
public class ListenersImpl implements Listeners {
    private static final String ENTRYPOINT_NAME = "excavated_variants";
    private static final String CLIENT_ENTRYPOINT_NAME = "excavated_variants_client";

    @Override
    public List<CommonListener> getAllListeners() {
        var containers = QuiltLoader.getEntrypointContainers(ENTRYPOINT_NAME, CommonListener.class);
        return containers.stream()
                .map(EntrypointContainer::getEntrypoint)
                .sorted().toList();
    }

    @Override
    public List<ClientListener> getAllClientListeners() {
        var containers = QuiltLoader.getEntrypointContainers(CLIENT_ENTRYPOINT_NAME, ClientListener.class);
        return containers.stream()
                .map(EntrypointContainer::getEntrypoint)
                .sorted().toList();
    }
}
