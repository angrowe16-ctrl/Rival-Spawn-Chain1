package com.rivalspawnchain.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.rivalspawnchain.chain.ChainManager;
import com.rivalspawnchain.chain.PlayerChainData;
import com.rivalspawnchain.network.NetworkHandler;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.UUID;

/**
 * /chain               → show your current chain
 * /chain reset         → reset your own chain
 * /chain reset <name>  → OP only: reset another player's chain
 */
public class ChainCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {

        dispatcher.register(
            CommandManager.literal("chain")

                // /chain  — show status
                .executes(ctx -> {
                    ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
                    PlayerChainData data = ChainManager.INSTANCE.get(player.getUuid());
                    sendStatus(ctx.getSource(), data);
                    return 1;
                })

                // /chain reset  — self-reset
                .then(CommandManager.literal("reset")
                    .executes(ctx -> {
                        ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
                        ChainManager.INSTANCE.get(player.getUuid()).breakChain();
                        NetworkHandler.sendToPlayer(player, ChainManager.INSTANCE.get(player.getUuid()));
                        ChainManager.INSTANCE.save();
                        ctx.getSource().sendFeedback(
                            () -> Text.literal("[RSC] Chain reset."), false);
                        return 1;
                    })

                    // /chain reset <playerName>  — OP-only
                    .then(CommandManager.argument("target", StringArgumentType.word())
                        .requires(src -> src.hasPermissionLevel(2))
                        .executes(ctx -> {
                            String targetName = StringArgumentType.getString(ctx, "target");
                            ServerPlayerEntity target = ctx.getSource().getServer()
                                    .getPlayerManager().getPlayer(targetName);
                            if (target == null) {
                                ctx.getSource().sendError(
                                    Text.literal("Player not found: " + targetName));
                                return 0;
                            }
                            UUID uid = target.getUuid();
                            ChainManager.INSTANCE.get(uid).breakChain();
                            NetworkHandler.sendToPlayer(target,
                                    ChainManager.INSTANCE.get(uid));
                            ChainManager.INSTANCE.save();
                            ctx.getSource().sendFeedback(
                                () -> Text.literal("[RSC] Reset chain for " + targetName + "."),
                                true);
                            return 1;
                        })
                    )
                )
        );
    }

    private static void sendStatus(ServerCommandSource src, PlayerChainData data) {
        if (!data.hasActiveChain()) {
            src.sendFeedback(() -> Text.literal("[RSC] No active chain."), false);
        } else {
            src.sendFeedback(() -> Text.literal(
                "[RSC] Chain: " + data.getChainSpecies()
                + " | KOs: " + data.getKoCount()
                + " | Shiny: 1/" + data.getShinyDenominator()
                + " | Weight ×" + String.format("%.2f", data.getSpawnWeightMultiplier())
            ), false);
        }
    }
}

// Helper so PlayerChainData doesn't need a separate hasActiveChain method
// (we add it in the class itself, but expose a quick shim here)
