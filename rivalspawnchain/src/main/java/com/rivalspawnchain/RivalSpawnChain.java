package com.rivalspawnchain;

import com.rivalspawnchain.chain.ChainManager;
import com.rivalspawnchain.command.ChainCommand;
import com.rivalspawnchain.compat.PlayerEventListener;
import com.rivalspawnchain.network.NetworkHandler;
import com.rivalspawnchain.shiny.ShinyHandler;
import com.rivalspawnchain.spawn.SpawnWeightHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RivalSpawnChain implements ModInitializer {

    public static final String MOD_ID = "rivalspawnchain";
    public static final Logger LOGGER  = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("[RivalSpawnChain] Initializing server systems...");

        // Network (register S2C packet type before any client connects)
        NetworkHandler.registerServer();

        // Cobblemon event hooks
        SpawnWeightHandler.register();
        ShinyHandler.register();
        PlayerEventListener.register();

        // Commands
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, env) ->
                ChainCommand.register(dispatcher));

        // Chain persistence
        ServerLifecycleEvents.SERVER_STARTED.register(ChainManager.INSTANCE::onServerStart);
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> ChainManager.INSTANCE.onServerStop());

        LOGGER.info("[RivalSpawnChain] Ready.");
    }
}
