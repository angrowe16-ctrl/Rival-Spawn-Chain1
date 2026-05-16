package com.rivalspawnchain;

import com.rivalspawnchain.network.ClientNetworkHandler;
import net.fabricmc.api.ClientModInitializer;

public class RivalSpawnChainClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientNetworkHandler.register();
        RivalSpawnChain.LOGGER.info("[RivalSpawnChain] Client ready.");
    }
}
