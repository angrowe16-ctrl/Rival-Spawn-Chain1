package com.rivalspawnchain.network;

import com.rivalspawnchain.chain.PlayerChainData;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class NetworkHandler {

    public static final Identifier PACKET_ID =
            Identifier.of("rivalspawnchain", "sync_chain");

    public record SyncChainPayload(String species, int koCount)
            implements CustomPayload {

        public static final CustomPayload.Id<SyncChainPayload> ID =
                new CustomPayload.Id<>(PACKET_ID);

        public static final PacketCodec<RegistryByteBuf, SyncChainPayload> CODEC =
                PacketCodec.tuple(
                        PacketCodecs.STRING,  SyncChainPayload::species,
                        PacketCodecs.INTEGER, SyncChainPayload::koCount,
                        SyncChainPayload::new
                );

        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    public static void registerServer() {
        PayloadTypeRegistry.playS2C().register(SyncChainPayload.ID, SyncChainPayload.CODEC);
    }

    public static void sendToPlayer(ServerPlayerEntity player, PlayerChainData data) {
        ServerPlayNetworking.send(player,
                new SyncChainPayload(data.getChainSpecies(), data.getKoCount()));
    }
}
