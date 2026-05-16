package com.rivalspawnchain.pokedex;

import com.rivalspawnchain.network.ClientChainCache;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public class PokeNavOverlay {

    private static final int MARGIN = 4;

    public static void register() {
        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> render(drawContext));
    }

    private static void render(DrawContext ctx) {
        if (!ClientChainCache.hasActiveChain()) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.options.hudHidden) return;

        String species = cap(ClientChainCache.getSpecies());
        int    ko      = ClientChainCache.getKoCount();
        int    denom   = ClientChainCache.getShinyDenominator();
        String line1   = "Chain: " + species + " x" + ko;
        String line2   = "Spawn: " + spawnLabel(ko);
        String line3   = "Shiny: 1/" + denom;
        int    color   = shinyColor(ko);
        int    sw      = ctx.getScaledWindowWidth();
        int    sh      = ctx.getScaledWindowHeight();
        int    fh      = mc.textRenderer.fontHeight;
        int    x       = sw - mc.textRenderer.getWidth(line1) - MARGIN - 2;
        int    y       = sh - (fh * 3) - MARGIN - 4;

        ctx.fill(x - 2, y - 2, sw - MARGIN, y + fh * 3 + 2, 0x88000000);
        ctx.drawText(mc.textRenderer, Text.literal(line1), x, y,               0xFFFFFF, true);
        ctx.drawText(mc.textRenderer, Text.literal(line2), x, y + fh + 1,      0x55FFFF, true);
        ctx.drawText(mc.textRenderer, Text.literal(line3), x, y + fh * 2 + 2,  color,   true);
    }

    private static String spawnLabel(int ko) {
        if (ko >= 50) return "10x boosted";
        if (ko >= 25) return "5x boosted";
        if (ko >= 15) return "3x boosted";
        if (ko >= 5)  return "2x boosted";
        return "normal";
    }

    private static int shinyColor(int ko) {
        if (ko >= 100) return 0xFF5555;
        if (ko >= 65)  return 0xFFAA00;
        if (ko >= 30)  return 0xFFFF55;
        return 0xFFFFFF;
    }

    private static String cap(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
