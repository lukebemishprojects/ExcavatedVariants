package io.github.lukebemish.excavated_variants;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public record S2CConfigAgreementPacket(Set<ResourceLocation> blocks) {

    public void encoder(FriendlyByteBuf buffer) {
        buffer.writeInt(blocks.size());
        for (var block : blocks) buffer.writeResourceLocation(block);
    }

    public static S2CConfigAgreementPacket decoder(FriendlyByteBuf buffer) {
        ArrayList<ResourceLocation> blocks = new ArrayList<>();
        int i = buffer.readInt();
        for (int j = 0; j < i; j++) blocks.add(buffer.readResourceLocation());
        return new S2CConfigAgreementPacket(new HashSet<>(blocks));
    }

    public void consumeMessage(Consumer<String> disconnecter) {
        ExcavatedVariants.setupMap();
        Set<ResourceLocation> knownBlocks = ExcavatedVariants.oreStoneList.stream().flatMap(p -> p.last().stream().map(
                stone -> new ResourceLocation(ExcavatedVariants.MOD_ID,
                        stone.id + "_" + p.first().id))).collect(Collectors.toSet());
        var serverOnly = this.blocks.stream().filter(b -> !knownBlocks.contains(b)).collect(Collectors.toSet());
        var clientOnly = knownBlocks.stream().filter(b -> !this.blocks.contains(b)).collect(Collectors.toSet());

        if (clientOnly.size() == 0 && serverOnly.size() == 0) {
            return;
        }
        String disconnect = "Connection closed - mismatched ore variant list";
        if (clientOnly.size() > 0) {
            String clientOnlyStr = String.join("\n    ",clientOnly.stream().map(ResourceLocation::getPath).toList());
            ExcavatedVariants.LOGGER.error("Client contains ore variants not present on server:\n    {}", clientOnlyStr);
            disconnect+="\nMissing on server: "+ellipsis(clientOnly.toString(), 50);
        }
        if (serverOnly.size() > 0) {
            String serverOnlyStr = String.join("\n    ",serverOnly.stream().map(ResourceLocation::getPath).toList());
            ExcavatedVariants.LOGGER.error("Server contains ore variants not present on client:\n    {}", serverOnlyStr);
            disconnect+="\nMissing on client: "+ellipsis(serverOnly.toString(), 50);
        }
        disconnecter.accept(disconnect);
    }

    private static String ellipsis(String str, int length) {
        if (str.length()<=length) return str;
        else return str.substring(0,length-3)+"...";
    }
}
