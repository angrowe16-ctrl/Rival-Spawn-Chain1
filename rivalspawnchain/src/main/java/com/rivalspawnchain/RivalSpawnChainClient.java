package com.rivalspawnchain;

import com.rivalspawnchain.network.ClientNetworkHandler;
import com.rivalspawnchain.pokedex.PokéNavOverlay;
import net.fabricmc.api.ClientModInitializer;

public class RivalSpawnChainClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // Register S2C receiver
        ClientNetworkHandler.register();

        // Register silent PokéNav HUD
        PokéNavOverlay.register();

        RivalSpawnChain.LOGGER.info("[RivalSpawnChain] Client systems ready.");
    }
}
