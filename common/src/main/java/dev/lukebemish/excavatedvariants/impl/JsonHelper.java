/*
 * Copyright (C) 2023 Luke Bemish and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package dev.lukebemish.excavatedvariants.impl;

import net.minecraft.server.packs.resources.IoSupplier;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class JsonHelper {
    public static IoSupplier<InputStream> getItemModel(String id) {
        String json = "{\"parent\": \"" + ExcavatedVariants.MOD_ID + ":" + "block/" + id + "__0\"}";
        return () -> new ByteArrayInputStream(json.getBytes());
    }
}
