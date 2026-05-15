package com.rivalspawnchain.compat;

import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.rivalspawnchain.chain.ChainManager;
import com.rivalspawnchain.chain.PlayerChainData;
import com.rivalspawnchain.network.NetworkHandler;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.UUID;

public class PlayerEventListener {

    public static void register() {

        CobblemonEvents.BATTLE_VICTORY.subscribe(event -> {
            var battle = event.getBattle();
            MinecraftServer server = battle.getServer() instanceof MinecraftServer s ? s : null;

            event.getWinners().forEach(actor ->
                actor.getPlayerUUIDs().forEach(uid -> {
                    event.getLosers().forEach(loser ->
                        loser.getPokemonList().forEach(bp -> {
                            String species = bp.getOriginalPokemon()
                                    .getSpecies().name().toLowerCase();
                            PlayerChainData data = ChainManager.INSTANCE.get(uid);
                            data.recordKo(species);
                            syncPlayer(server, uid, data);
                        })
                    );
                })
            );
            ChainManager.INSTANCE.save();
        });

        CobblemonEvents.BATTLE_FLED.subscribe(event -> {
            var battle = event.getBattle();
            MinecraftServer server = battle.getServer() instanceof MinecraftServer s ? s : null;

            event.getBattle().getActors().forEach(actor ->
                actor.getPlayerUUIDs().forEach(uid -> {
                    PlayerChainData data = ChainManager.INSTANCE.get(uid);
                    data.breakChain();
                    syncPlayer(server, uid, data);
                })
            );
            ChainManager.INSTANCE.save();
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            NetworkHandler.sendToPlayer(player, ChainManager.INSTANCE.get(player.getUuid()));
        });
    }

    private static void syncPlayer(MinecraftServer server, UUID uid, PlayerChainData data) {
        if (server == null) return;
        ServerPlayerEntity player = server.getPlayerManager().getPlayer(uid);
        if (player != null) NetworkHandler.sendToPlayer(player, data);
    }
}
