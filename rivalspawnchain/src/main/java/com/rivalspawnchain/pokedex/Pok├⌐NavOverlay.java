package com.rivalspawnchain.pokedex;

import com.rivalspawnchain.network.ClientChainCache;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

/**
 * Silent PokéNav-style HUD overlay.
 * Renders bottom-right; no popups, no chat spam, no title messages.
 *
 * Colour coding:
 *   White  – chain active, shiny odds vanilla (0–29 KOs)
 *   Yellow – 1/2048  (30–64)
 *   Orange – 1/1024  (65–99)
 *   Red    – 1/512   (100+)
 */
public class PokéNavOverlay {

    private static final int MARGIN = 4;

    public static void register() {
        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> render(drawContext));
    }

    private static void render(DrawContext ctx) {
        if (!ClientChainCache.hasActiveChain()) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.options.hudHidden) return;

        String species = capitalize(ClientChainCache.getSpecies());
        int    ko      = ClientChainCache.getKoCount();
        int    denom   = ClientChainCache.getShinyDenominator();

        // Build display lines
        String line1 = "Chain: " + species + " ×" + ko;
        String line2 = "Shiny: 1/" + denom;

        int color = shinyColor(ko);
        int screenW = ctx.getScaledWindowWidth();
        int screenH = ctx.getScaledWindowHeight();
        int fontH   = mc.textRenderer.fontHeight;
        int x       = screenW  - mc.textRenderer.getWidth(line1) - MARGIN - 2;
        int y       = screenH  - (fontH * 2) - MARGIN - 4;

        // Semi-transparent background
        ctx.fill(x - 2, y - 2,
                 screenW - MARGIN, y + fontH * 2 + 2,
                 0x88000000);

        ctx.drawText(mc.textRenderer, Text.literal(line1), x, y,             0xFFFFFF, true);
        ctx.drawText(mc.textRenderer, Text.literal(line2), x, y + fontH + 1, color,   true);
    }

    private static int shinyColor(int ko) {
        if (ko >= 100) return 0xFF5555; // red
        if (ko >= 65)  return 0xFFAA00; // orange
        if (ko >= 30)  return 0xFFFF55; // yellow
        return 0xFFFFFF;                 // white
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
