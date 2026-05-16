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
                if (data.getKoCount() <= 0) continue;
                if (!data.getChainSpecies().equalsIgnoreCase(species)) continue;

                // Massive weight multiplier — chain species dominates spawns
                float boost = getBoost(data.getKoCount());
                event.getDetails().weight = event.getDetails().weight * boost;
                break;
            }
        });
    }

    private static float getBoost(int ko) {
        if (ko >= 50) return 200.0f;  // completely floods the area
        if (ko >= 25) return 80.0f;   // vast majority of spawns
        if (ko >= 15) return 30.0f;   // clearly dominant
        if (ko >= 5)  return 10.0f;   // noticeably more common
        return 3.0f;                   // slight increase
    }
}
