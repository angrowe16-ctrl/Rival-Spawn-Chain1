package com.rivalspawnchain.shiny;

import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.rivalspawnchain.chain.ChainManager;
import com.rivalspawnchain.chain.PlayerChainData;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.Random;

/**
 * After a Pokémon spawns, if it matches a nearby player's chain species,
 * re-roll shiny at the chain-boosted denominator.
 *
 * Thresholds:
 *   0–29  KOs  → 1/4096  (vanilla — no re-roll needed)
 *  30–64  KOs  → 1/2048
 *  65–99  KOs  → 1/1024
 * 100+    KOs  → 1/512
 */
public class ShinyHandler {

    private static final Random RNG = new Random();

    public static void register() {
        CobblemonEvents.POKEMON_SPAWNED.subscribe(event -> {
            var pokemon = event.getPokemon();
            // Skip if already shiny (vanilla roll may have already hit)
            if (pokemon.getShiny()) return;

            String species = pokemon.getSpecies().name().toLowerCase();
            ServerWorld world = (ServerWorld) pokemon.getWorld();
            if (world == null) return;

            for (ServerPlayerEntity player : world.getPlayers()) {
                PlayerChainData data = ChainManager.INSTANCE.get(player.getUuid());
                if (data.getKoCount() < 30) continue;                     // below first threshold
                if (!data.getChainSpecies().equalsIgnoreCase(species)) continue;

                int denom = data.getShinyDenominator();
                if (RNG.nextInt(denom) == 0) {
                    pokemon.setShiny(true);
                }
                break; // one player's chain is enough
            }
        });
    }
}
