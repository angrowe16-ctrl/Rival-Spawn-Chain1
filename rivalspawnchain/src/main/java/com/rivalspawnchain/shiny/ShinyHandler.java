package com.rivalspawnchain.shiny;

import com.rivalspawnchain.chain.ChainManager;
import com.rivalspawnchain.chain.PlayerChainData;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;

import java.util.Random;

public class ShinyHandler {

    private static final Random RNG = new Random();

    public static void register() {
        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (!(entity instanceof PokemonEntity pokemonEntity)) return;
            var pokemon = pokemonEntity.getPokemon();
            if (pokemon.getShiny()) return;

            String species = pokemon.getSpecies().getName().toLowerCase();

            for (ServerPlayerEntity player : world.getPlayers()) {
                PlayerChainData data = ChainManager.INSTANCE.get(player.getUuid());
                if (data.getKoCount() < 30) continue;
                if (!data.getChainSpecies().equalsIgnoreCase(species)) continue;
                if (RNG.nextInt(data.getShinyDenominator()) == 0) {
                    pokemon.setShiny(true);
                }
                break;
            }
        });
    }
}
