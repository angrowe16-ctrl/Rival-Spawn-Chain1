package com.rivalspawnchain.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class ClientNetworkHandler {

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(
                NetworkHandler.SyncChainPayload.ID,
                (payload, context) ->
                        context.client().execute(() ->
                                ClientChainCache.update(payload.species(), payload.koCount())
                        )
        );
    }
}
