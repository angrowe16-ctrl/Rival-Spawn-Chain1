package com.rivalspawnchain.spawn;

import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.rivalspawnchain.chain.ChainManager;
import com.rivalspawnchain.chain.PlayerChainData;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public class SpawnWeightHandler {

    public static void register() {
        CobblemonEvents.SPAWN_DETAIL_EXTRA_WEIGHT.subscribe(event -> {
            String species = event.getDetails().species.name().toLowerCase();
            ServerWorld world = (ServerWorld) event.getDetails().getCause().getWorld();
            if (world == null) return;

            for (ServerPlayerEntity player : world.getPlayers()) {
                PlayerChainData data = ChainManager.INSTANCE.get(player.getUuid());
                if (data.getKoCount() > 0 && data.getChainSpecies().equalsIgnoreCase(species)) {
                    event.getDetails().weight *= (float) data.getSpawnWeightMultiplier();
                    break;
                }
            }
        });
    }
}
