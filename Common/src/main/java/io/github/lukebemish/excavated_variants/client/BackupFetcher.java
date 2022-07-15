package io.github.lukebemish.excavated_variants.client;

import io.github.lukebemish.dynamic_asset_generator.api.client.ClientPrePackRepository;
import io.github.lukebemish.excavated_variants.ExcavatedVariants;
import net.minecraft.resources.ResourceLocation;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class BackupFetcher {
    public static InputStream provideBlockstateFile(ResourceLocation oreRl) {
        ExcavatedVariants.LOGGER.info("Attempting to provide backup blockstate for {}",oreRl);
        InputStream out;
        try {
            ResourceLocation testBS = new ResourceLocation(oreRl.getNamespace(), "excavated_variants_backups/blockstates" + oreRl.getPath() + ".json");
            out = ClientPrePackRepository.getResource(testBS);
        } catch (IOException e) {
            ExcavatedVariants.LOGGER.info("Could not get backup blockstate for {}; wildly guessing at model location.",oreRl);
            String blockstate = "{\"variants\":{\"\":{\"model\":\""+oreRl.getNamespace()+":block/"+oreRl.getPath()+"\"}}}";
            out = new ByteArrayInputStream(blockstate.getBytes());
        }
        return out;
    }

    public static InputStream provideBlockModelFile(ResourceLocation oreRl) {
        ExcavatedVariants.LOGGER.info("Attempting to provide backup block model for {}",oreRl);
        InputStream out;
        try {
            ResourceLocation testBS = new ResourceLocation(oreRl.getNamespace(), "excavated_variants_backups/models/block" + oreRl.getPath() + ".json");
            out = ClientPrePackRepository.getResource(testBS);
        } catch (IOException e) {
            ExcavatedVariants.LOGGER.info("Could not get backup block model for {}; wildly guessing at texture location.",oreRl);
            String model = "{\"parent\":\"block/cube_all\",\"textures\":{\"all\":\""+oreRl.getNamespace()+":"+oreRl.getPath()+"\"}}";
            out = new ByteArrayInputStream(model.getBytes());
        }
        return out;
    }
}
