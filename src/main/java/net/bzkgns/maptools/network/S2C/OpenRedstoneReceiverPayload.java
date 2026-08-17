package net.bzkgns.maptools.network.S2C;

import io.netty.buffer.ByteBuf;
import net.bzkgns.maptools.Maptools;
import net.bzkgns.maptools.entities.redstone_receiver.RedstoneReceiverConfig;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;


public record OpenRedstoneReceiverPayload(int receiverId,
                                          RedstoneReceiverConfig config) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<OpenRedstoneReceiverPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Maptools.MOD_ID, "open_redstone_receiver"));


    public static final StreamCodec<ByteBuf, OpenRedstoneReceiverPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT,
                    OpenRedstoneReceiverPayload::receiverId,

                    RedstoneReceiverConfig.CONFIG_CODEC,
                    OpenRedstoneReceiverPayload::config,

                    OpenRedstoneReceiverPayload::new
            );

    @Override
    public CustomPacketPayload.@NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
