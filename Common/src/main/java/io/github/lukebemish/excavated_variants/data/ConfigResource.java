package io.github.lukebemish.excavated_variants.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lukebemish.excavated_variants.data.filter.Filter;
import io.github.lukebemish.excavated_variants.data.filter.ObjectFilter;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class ConfigResource {
    public static final Codec<ConfigResource> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Filter.CODEC.optionalFieldOf("blacklist", ObjectFilter.EmptyFilter.INSTANCE).forGetter(r -> r.blacklist),
            ResourceLocation.CODEC.listOf().optionalFieldOf("priority", List.of()).forGetter(r -> r.priority)
    ).apply(instance, ConfigResource::new));
    public final List<ResourceLocation> priority;
    private Filter blacklist;

    private ConfigResource(Filter blacklist, List<ResourceLocation> priority) {
        this.blacklist = blacklist;
        this.priority = new ArrayList<>(priority);
    }

    public static ConfigResource empty() {
        return new ConfigResource(ObjectFilter.EmptyFilter.INSTANCE, List.of());
    }

    private static <T> void addAllNew(List<? super T> to, List<? extends T> from) {
        from.stream().filter(it->!to.contains(it)).forEach(to::add);
    }

    public Filter getBlacklist() {
        return blacklist;
    }

    public void addFrom(ConfigResource resource) {
        this.blacklist = Filter.union(this.blacklist, resource.blacklist);
        addAllNew(this.priority, resource.priority);
    }
}
