package com.rivalspawnchain.network;

/**
 * Client-side cache updated by {@link ClientNetworkHandler}.
 * The PokéNav HUD reads from here — no server round-trip needed per frame.
 */
public class ClientChainCache {

    private static String species = "";
    private static int    koCount = 0;

    public static void update(String species, int koCount) {
        ClientChainCache.species = species == null ? "" : species;
        ClientChainCache.koCount = koCount;
    }

    public static String getSpecies() { return species; }
    public static int    getKoCount() { return koCount; }

    /** True when the player has an active chain. */
    public static boolean hasActiveChain() {
        return koCount > 0 && !species.isEmpty();
    }

    /**
     * Returns the boosted shiny denominator for display.
     * Mirrors the logic in PlayerChainData so the HUD matches the server.
     */
    public static int getShinyDenominator() {
        if (koCount >= 100) return 512;
        if (koCount >= 65)  return 1024;
        if (koCount >= 30)  return 2048;
        return 4096;
    }
}
