package com.rivalspawnchain;

import com.rivalspawnchain.chain.ChainManager;
import com.rivalspawnchain.chain.PlayerChainData;
import com.rivalspawnchain.command.ChainCommand;
import com.rivalspawnchain.compat.PlayerEventListener;
import com.rivalspawnchain.network.NetworkHandler;
import com.rivalspawnchain.shiny.ShinyHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RivalSpawnChain implements ModInitializer {

    public static final String MOD_ID = "rivalspawnchain";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("[RivalSpawnChain] Initializing...");

        NetworkHandler.registerServer();
        ShinyHandler.register();
        PlayerEventListener.register();

        // Flood spawns: despawn non-chain species near chaining players
        // AND boost chain species by leaving spawn slots open for them
        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (!(entity instanceof PokemonEntity pe)) return;
            String species = pe.getPokemon().getSpecies().getName().toLowerCase();

            for (ServerPlayerEntity player : world.getPlayers()) {
                PlayerChainData data = ChainManager.INSTANCE.get(player.getUuid());
                if (data.getKoCount() < 5) continue;

                double dist = entity.squaredDistanceTo(player);
                if (dist > 1024) continue; // 32 block radius

                // If it's the chain species, keep it — maybe boost shiny roll
                if (data.getChainSpecies().equalsIgnoreCase(species)) break;

                // Not the chain species — despawn it based on chain length
                float roll = (float) Math.random();
                if (roll < getDespawnChance(data.getKoCount())) {
                    entity.discard();
                }
                break;
            }
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, env) ->
                ChainCommand.register(dispatcher));

        ServerLifecycleEvents.SERVER_STARTED.register(ChainManager.INSTANCE::onServerStart);
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> ChainManager.INSTANCE.onServerStop());

        LOGGER.info("[RivalSpawnChain] Ready.");
    }

    private static float getDespawnChance(int ko) {
        if (ko >= 50) return 0.97f; // 97% of other species gone = L8Games flood
        if (ko >= 25) return 0.85f;
        if (ko >= 15) return 0.65f;
        if (ko >= 5)  return 0.40f;
        return 0.0f;
    }
}
