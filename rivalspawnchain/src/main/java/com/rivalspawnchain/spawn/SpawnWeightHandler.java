package com.rivalspawnchain.spawn;

import com.rivalspawnchain.chain.ChainManager;
import com.rivalspawnchain.chain.PlayerChainData;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

// Spawn weight boosting is handled via the tick-based approach
// since SPAWN_DETAIL_EXTRA_WEIGHT is not available in this version
public class SpawnWeightHandler {
    public static void register() {
        // Weight boosting integrated into ChainManager state
        // Cobblemon reads spawn weights at spawn time; we patch via mixin if needed
    }
}
