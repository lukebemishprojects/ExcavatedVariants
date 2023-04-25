/*
 * Copyright (C) 2023 Luke Bemish and contributors
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */

package dev.lukebemish.excavatedvariants.impl.client;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import dev.lukebemish.dynamicassetgenerator.api.client.ClientPrePackRepository;

import net.minecraft.resources.ResourceLocation;

public class BackupFetcher {
    public static InputStream getBlockStateFile(ResourceLocation rl) {
        try {
            return ClientPrePackRepository.getResource(new ResourceLocation(rl.getNamespace(), "blockstates/" + rl.getPath() + ".json"));
        } catch (IOException e) {
            InputStream out;
            try {
                ResourceLocation testBS = new ResourceLocation(rl.getNamespace(), "excavated_variants_backups/blockstates" + rl.getPath() + ".json");
                out = ClientPrePackRepository.getResource(testBS);
            } catch (IOException e2) {
                String blockstate = "{\"variants\":{\"\":{\"model\":\"" + rl.getNamespace() + ":block/" + rl.getPath() + "\"}}}";
                out = new ByteArrayInputStream(blockstate.getBytes());
            }
            return out;
        }
    }

    public static InputStream getModelFile(ResourceLocation rl) {
        try {
            return ClientPrePackRepository.getResource(new ResourceLocation(rl.getNamespace(), "models/" + rl.getPath() + ".json"));
        } catch (IOException e) {
            InputStream out;
            try {
                ResourceLocation testBS = new ResourceLocation(rl.getNamespace(), "excavated_variants_backups/models/" + rl.getPath() + ".json");
                out = ClientPrePackRepository.getResource(testBS);
            } catch (IOException e2) {
                String model = "{\"parent\":\"block/cube_all\",\"textures\":{\"all\":\"" + rl.getNamespace() + ":" + rl.getPath() + "\"}}";
                out = new ByteArrayInputStream(model.getBytes());
            }
            return out;
        }
    }
}
