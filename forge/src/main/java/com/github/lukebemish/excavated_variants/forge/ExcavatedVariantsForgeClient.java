package com.github.lukebemish.excavated_variants.forge;

import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.util.ArrayList;

public class ExcavatedVariantsForgeClient {
    public static final ArrayList<Runnable> queue = new ArrayList<>();
    public static void clientSetup(FMLClientSetupEvent e) {
        e.enqueueWork(() -> {
            for (Runnable r : queue) {
                r.run();
            }
            queue.clear();
        });
    }
}
