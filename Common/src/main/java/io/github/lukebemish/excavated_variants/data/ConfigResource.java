package io.github.lukebemish.excavated_variants.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class ConfigResource {
    public static final Codec<ConfigResource> CODEC = RecordCodecBuilder.create(instance->instance.group(
            Codec.STRING.listOf().optionalFieldOf("blacklist_ids",List.of()).forGetter(r->r.blacklist_ids),
            Codec.STRING.listOf().optionalFieldOf("blacklist_stones",List.of()).forGetter(r->r.blacklist_stones),
            Codec.STRING.listOf().optionalFieldOf("blacklist_ores",List.of()).forGetter(r->r.blacklist_ores),
            ResourceLocation.CODEC.listOf().optionalFieldOf("priority",List.of()).forGetter(r->r.priority)
    ).apply(instance,ConfigResource::new));

    public final List<String> blacklist_ids;
    public final List<String> blacklist_stones;
    public final List<String> blacklist_ores;
    public final List<ResourceLocation> priority;

    private ConfigResource(List<String> blacklist_ids, List<String> blacklist_stones, List<String> blacklist_ores, List<ResourceLocation> priority) {
        this.blacklist_ids = new ArrayList<>(blacklist_ids);
        this.blacklist_stones = new ArrayList<>(blacklist_stones);
        this.blacklist_ores = new ArrayList<>(blacklist_ores);
        this.priority = new ArrayList<>(priority);
    }

    public static ConfigResource empty() {
        return new ConfigResource(List.of(), List.of(), List.of(), List.of());
    }

    public void addFrom(ConfigResource resource) {
        addAllNew(this.blacklist_ids, resource.blacklist_ids);
        addAllNew(this.blacklist_stones, resource.blacklist_stones);
        addAllNew(this.blacklist_ores, resource.blacklist_ores);
        addAllNew(this.priority, resource.priority);
    }

    private static <T> void addAllNew(List<? super T> to, List<? extends T> from) {
        from.stream().filter(to::contains).forEach(to::add);
    }
}
