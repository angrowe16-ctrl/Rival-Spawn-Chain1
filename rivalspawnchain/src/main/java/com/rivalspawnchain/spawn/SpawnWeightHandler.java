package com.rivalspawnchain.spawn;

import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.rivalspawnchain.chain.ChainManager;
import com.rivalspawnchain.chain.PlayerChainData;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.UUID;

/**
 * Multiplies the spawn weight for the chained species near the chaining player.
 *
 * Cobblemon fires  SpawnEvent.EXTRA_WEIGHT  (or equivalent) allowing additive
 * weight injection.  We hook it here and boost weight for matching species.
 *
 * Weight formula:  baseWeight * multiplier
 *   multiplier = min(1 + KOcount/100, 10)   → +1 % per KO, cap ×10
 */
public class SpawnWeightHandler {

    public static void register() {
        // Cobblemon 1.7.x exposes CobblemonEvents.SPAWN_DETAIL_EXTRA_WEIGHT
        // The lambda receives a SpawnDetailExtraWeightEvent with:
        //   - detail.species  (Species)
        //   - detail.cause    (SpawnCause, which may carry a player)
        //   - a mutable weight field
        CobblemonEvents.SPAWN_DETAIL_EXTRA_WEIGHT.subscribe(event -> {
            String species = event.getDetails().species.name().toLowerCase();
            // Try to find a nearby chaining player who matches this species
            ServerWorld world = event.getDetails().cause.getWorld();
            if (world == null) return;

            for (ServerPlayerEntity player : world.getPlayers()) {
                UUID uid = player.getUuid();
                PlayerChainData data = ChainManager.INSTANCE.get(uid);
                if (data.getKoCount() > 0
                        && data.getChainSpecies().equalsIgnoreCase(species)) {
                    // Multiply the spawner's weight by our bonus
                    double multiplier = data.getSpawnWeightMultiplier();
                    // event.setWeight takes a float; Cobblemon weight is typically 0–100+
                    float boosted = (float)(event.getDetails().weight * multiplier);
                    event.getDetails().weight = boosted;
                    break; // first matching player wins
                }
            }
        });
    }
}
