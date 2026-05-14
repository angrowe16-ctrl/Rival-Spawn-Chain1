package com.rivalspawnchain.compat;

import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.events.battles.BattleFledEvent;
import com.cobblemon.mod.common.api.events.battles.BattleVictoryEvent;
import com.rivalspawnchain.chain.ChainManager;
import com.rivalspawnchain.chain.PlayerChainData;
import com.rivalspawnchain.network.NetworkHandler;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.UUID;

/**
 * Hooks:
 *  BATTLE_VICTORY → KO increments chain; losing (player fled) breaks it.
 *  BATTLE_FLED    → breaks chain.
 *  SERVER JOIN    → sync chain to joining player.
 *
 * L8Games style: flee = break; capture = continues (counts as KO).
 */
public class PlayerEventListener {

    public static void register() {

        // ── KO / Victory ─────────────────────────────────────────────────────
        CobblemonEvents.BATTLE_VICTORY.subscribe(event -> {
            // event.winners / event.losers contain BattleActor lists
            // Find the player actor on the winning side
            event.getWinners().forEach(actor -> {
                actor.getPlayerUUIDs().forEach(uid -> {
                    // Determine the defeated (wild) species
                    event.getLosers().forEach(loserActor -> {
                        loserActor.getPokemonList().forEach(battlePokemon -> {
                            String species = battlePokemon.getOriginalPokemon()
                                    .getSpecies().name().toLowerCase();

                            PlayerChainData data = ChainManager.INSTANCE.get(uid);
                            // Capture check: if pokemon was caught this battle, recordCapture
                            // For simplicity treat all victories as KOs (L8Games style)
                            data.recordKo(species);

                            // Sync to player
                            syncToPlayer(uid, data, event.getBattle().getServer() != null
                                    ? event.getBattle().getServer()
                                    : null);
                        });
                    });
                });
            });
            // Save periodically on each battle end
            ChainManager.INSTANCE.save();
        });

        // ── Fled → break chain ────────────────────────────────────────────────
        CobblemonEvents.BATTLE_FLED.subscribe((BattleFledEvent event) -> {
            // The player who fled breaks their chain
            event.getBattle().getActors().forEach(actor -> {
                actor.getPlayerUUIDs().forEach(uid -> {
                    PlayerChainData data = ChainManager.INSTANCE.get(uid);
                    data.breakChain();
                    // Notify client: empty chain
                    var server = event.getBattle().getServer();
                    if (server != null) {
                        ServerPlayerEntity player = server.getPlayerManager().getPlayer(uid);
                        if (player != null) NetworkHandler.sendToPlayer(player, data);
                    }
                });
            });
            ChainManager.INSTANCE.save();
        });

        // ── Player join → sync chain ───────────────────────────────────────────
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            PlayerChainData data = ChainManager.INSTANCE.get(player.getUuid());
            NetworkHandler.sendToPlayer(player, data);
        });
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static void syncToPlayer(UUID uid, PlayerChainData data, net.minecraft.server.MinecraftServer server) {
        if (server == null) return;
        ServerPlayerEntity player = server.getPlayerManager().getPlayer(uid);
        if (player != null) NetworkHandler.sendToPlayer(player, data);
    }
}
