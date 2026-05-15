package com.rivalspawnchain.compat;

import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.rivalspawnchain.chain.ChainManager;
import com.rivalspawnchain.chain.PlayerChainData;
import com.rivalspawnchain.network.NetworkHandler;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.UUID;

public class PlayerEventListener {

    public static void register() {

        CobblemonEvents.BATTLE_VICTORY.subscribe(event -> {
            event.getWinners().forEach(actor ->
                actor.getPlayerUUIDs().forEach(uid -> {
                    event.getLosers().forEach(loser ->
                        loser.getPokemonList().forEach(bp -> {
                            String species = bp.getOriginalPokemon()
                                    .getSpecies().getName().toLowerCase();
                            PlayerChainData data = ChainManager.INSTANCE.get(uid);
                            data.recordKo(species);
                        })
                    );
                })
            );
            ChainManager.INSTANCE.save();
        });

        CobblemonEvents.BATTLE_FLED.subscribe(event ->
            event.getBattle().getActors().forEach(actor ->
                actor.getPlayerUUIDs().forEach(uid -> {
                    ChainManager.INSTANCE.get(uid).breakChain();
                })
            )
        );

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            NetworkHandler.sendToPlayer(player, ChainManager.INSTANCE.get(player.getUuid()));
        });
    }
}
