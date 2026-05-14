package com.rivalspawnchain.network;

import com.rivalspawnchain.chain.PlayerChainData;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

/**
 * Sends chain state from server → client so the PokéNav HUD stays current.
 * Packet layout:  [String species] [int koCount]
 */
public class NetworkHandler {

    public static final Identifier PACKET_ID =
            Identifier.of("rivalspawnchain", "sync_chain");

    // Fabric 1.21.1 typed payload ─────────────────────────────────────────────

    public record SyncChainPayload(String species, int koCount)
            implements CustomPayload {

        public static final CustomPayload.Id<SyncChainPayload> ID =
                new CustomPayload.Id<>(PACKET_ID);

        public static final PacketCodec<PacketByteBuf, SyncChainPayload> CODEC =
                PacketCodec.tuple(
                        PacketCodec.STRING, SyncChainPayload::species,
                        PacketCodec.INT,    SyncChainPayload::koCount,
                        SyncChainPayload::new
                );

        @Override public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    // ── Registration ──────────────────────────────────────────────────────────

    public static void registerServer() {
        PayloadTypeRegistry.playS2C().register(SyncChainPayload.ID, SyncChainPayload.CODEC);
    }

    // ── Send helper ───────────────────────────────────────────────────────────

    public static void sendToPlayer(ServerPlayerEntity player, PlayerChainData data) {
        ServerPlayNetworking.send(
                player,
                new SyncChainPayload(data.getChainSpecies(), data.getKoCount())
        );
    }
}
