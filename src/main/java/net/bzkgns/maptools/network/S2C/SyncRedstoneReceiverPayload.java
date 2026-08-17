package net.bzkgns.maptools.network.S2C;

import io.netty.buffer.ByteBuf;
import net.bzkgns.maptools.Maptools;
import net.bzkgns.maptools.entities.redstone_receiver.RedstoneReceiverConfig;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;


public record SyncRedstoneReceiverPayload(int receiverId,
                                          RedstoneReceiverConfig config) implements CustomPacketPayload {
    public static final Type<SyncRedstoneReceiverPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Maptools.MOD_ID, "sync_redstone_receiver"));


    public static final StreamCodec<ByteBuf, SyncRedstoneReceiverPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT,
                    SyncRedstoneReceiverPayload::receiverId,

                    RedstoneReceiverConfig.CONFIG_STREAM_CODEC,
                    SyncRedstoneReceiverPayload::config,

                    SyncRedstoneReceiverPayload::new
            );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
