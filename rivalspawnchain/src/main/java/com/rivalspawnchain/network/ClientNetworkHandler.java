package com.rivalspawnchain.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

/**
 * Registers the client-side receiver for {@link NetworkHandler.SyncChainPayload}.
 * Must be called from the client entrypoint only.
 */
public class ClientNetworkHandler {

    public static void register() {
        // Register type on client side too (required by Fabric 1.21.1)
        PayloadTypeRegistry.playS2C().register(
                NetworkHandler.SyncChainPayload.ID,
                NetworkHandler.SyncChainPayload.CODEC
        );

        ClientPlayNetworking.registerGlobalReceiver(
                NetworkHandler.SyncChainPayload.ID,
                (payload, context) -> {
                    // Already on network thread; schedule client-thread update
                    context.client().execute(() ->
                            ClientChainCache.update(payload.species(), payload.koCount())
                    );
                }
        );
    }
}
