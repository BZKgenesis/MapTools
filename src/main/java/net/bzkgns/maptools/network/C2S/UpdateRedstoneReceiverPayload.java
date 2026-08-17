package net.bzkgns.maptools.network.C2S;

import io.netty.buffer.ByteBuf;
import net.bzkgns.maptools.Maptools;
import net.bzkgns.maptools.entities.redstone_receiver.RedstoneReceiverConfig;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record UpdateRedstoneReceiverPayload(int receiverId,
                                            RedstoneReceiverConfig config) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<UpdateRedstoneReceiverPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Maptools.MOD_ID, "update_redstone_receiver"));

    public static final StreamCodec<ByteBuf, UpdateRedstoneReceiverPayload>
            STREAM_CODEC = StreamCodec.composite(

            ByteBufCodecs.VAR_INT,
            UpdateRedstoneReceiverPayload::receiverId,

            RedstoneReceiverConfig.CONFIG_CODEC,
            UpdateRedstoneReceiverPayload::config,

            UpdateRedstoneReceiverPayload::new
    );

    @Override
    public CustomPacketPayload.@NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
