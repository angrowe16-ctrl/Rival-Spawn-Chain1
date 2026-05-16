package com.rivalspawnchain;

import com.cobblemon.mod.common.api.spawning.CobblemonSpawnPools;
import com.rivalspawnchain.chain.ChainManager;
import com.rivalspawnchain.command.ChainCommand;
import com.rivalspawnchain.compat.PlayerEventListener;
import com.rivalspawnchain.network.NetworkHandler;
import com.rivalspawnchain.shiny.ShinyHandler;
import com.rivalspawnchain.spawn.SpawnWeightHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.rivalspawnchain.chain.PlayerChainData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RivalSpawnChain implements ModInitializer {

    public static final String MOD_ID = "rivalspawnchain";
    public static final Logger LOGGER  = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("[RivalSpawnChain] Initializing...");

        NetworkHandler.registerServer();
        ShinyHandler.register();
        PlayerEventListener.register();

        // Aggressive spawn flooding via entity load hook
        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (!(entity instanceof PokemonEntity pe)) return;
            String species = pe.getPokemon().getSpecies().getName().toLowerCase();

            for (ServerPlayerEntity player : world.getPlayers()) {
                PlayerChainData data = ChainManager.INSTANCE.get(player.getUuid());
                if (data.getKoCount() <= 0) continue;
                if (!data.getChainSpecies().equalsIgnoreCase(species)) continue;

                // Already the right species — nothing to do, it spawned naturally
                // The flooding happens by despawning non-chain spawns
                break;
            }

            // Despawn non-chained species near a chaining player
            for (ServerPlayerEntity player : world.getPlayers()) {
                PlayerChainData data = ChainManager.INSTANCE.get(player.getUuid());
                if (data.getKoCount() < 5) continue;
                if (data.getChainSpecies().equalsIgnoreCase(species)) continue;

                double dist = player.squaredDistanceTo(entity);
                if (dist > 1024) continue; // only within 32 blocks

                float roll = (float) Math.random();
                float despawnChance = getDespawnChance(data.getKoCount());
                if (roll < despawnChance) {
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
        if (ko >= 50) return 0.95f; // 95% of other species despawn = chain floods area
        if (ko >= 25) return 0.80f;
        if (ko >= 15) return 0.60f;
        if (ko >= 5)  return 0.35f;
        return 0.0f;
    }
}
