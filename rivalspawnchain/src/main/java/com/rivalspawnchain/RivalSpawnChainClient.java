package com.rivalspawnchain;

import com.rivalspawnchain.network.ClientNetworkHandler;
import com.rivalspawnchain.pokedex.PokeNavOverlay;
import net.fabricmc.api.ClientModInitializer;

public class RivalSpawnChainClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientNetworkHandler.register();
        PokeNavOverlay.register();
        RivalSpawnChain.LOGGER.info("[RivalSpawnChain] Client ready.");
    }
}
