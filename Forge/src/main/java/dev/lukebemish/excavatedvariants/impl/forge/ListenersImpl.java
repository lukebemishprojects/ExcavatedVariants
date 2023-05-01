/*
 * Copyright (C) 2023 Luke Bemish and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package dev.lukebemish.excavatedvariants.impl.forge;

import java.util.List;

import com.google.auto.service.AutoService;
import dev.lukebemish.excavatedvariants.api.client.ClientListener;
import dev.lukebemish.excavatedvariants.impl.platform.services.Listeners;
import dev.lukebemish.excavatedvariants.api.ExcavatedVariantsListener;
import dev.lukebemish.excavatedvariants.api.CommonListener;

@AutoService(Listeners.class)
public class ListenersImpl implements Listeners {
    @Override
    public List<CommonListener> getAllListeners() {
        return PluginFinder.getInstances(ExcavatedVariantsListener.class, CommonListener.class).stream().sorted().toList();
    }

    @Override
    public List<ClientListener> getAllClientListeners() {
        return PluginFinder.getInstances(ExcavatedVariantsListener.class, ClientListener.class).stream().sorted().toList();
    }
}
