package io.github.lukebemish.excavated_variants.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class ConfigResource {
    public static final Codec<ConfigResource> CODEC = RecordCodecBuilder.create(instance->instance.group(
            VariantFilter.CODEC.optionalFieldOf("blacklist",new VariantFilter()).forGetter(r->r.blacklist),
            ResourceLocation.CODEC.listOf().optionalFieldOf("priority",List.of()).forGetter(r->r.priority)
    ).apply(instance,ConfigResource::new));
    public final List<ResourceLocation> priority;
    public final VariantFilter blacklist;

    private ConfigResource(VariantFilter blacklist, List<ResourceLocation> priority) {
        this.blacklist = blacklist;
        this.priority = new ArrayList<>(priority);
    }

    public static ConfigResource empty() {
        return new ConfigResource(new VariantFilter(), List.of());
    }

    public void addFrom(ConfigResource resource) {
        this.blacklist.mergeFrom(resource.blacklist);
        addAllNew(this.priority, resource.priority);
    }

    private static <T> void addAllNew(List<? super T> to, List<? extends T> from) {
        from.stream().filter(to::contains).forEach(to::add);
    }
}
