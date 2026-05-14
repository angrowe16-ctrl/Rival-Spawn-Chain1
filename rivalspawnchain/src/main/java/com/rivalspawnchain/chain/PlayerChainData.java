package com.rivalspawnchain.chain;

import net.minecraft.nbt.NbtCompound;

/**
 * Holds the KO-chain state for one player.
 * Shiny thresholds (denominator):
 *   0–29  KOs → 1/4096 (vanilla)
 *  30–64  KOs → 1/2048
 *  65–99  KOs → 1/1024
 * 100+    KOs → 1/512
 */
public class PlayerChainData {

    public static final int[] SHINY_THRESHOLDS  = {30, 65, 100};
    public static final int[] SHINY_DENOMINATORS = {2048, 1024, 512};
    public static final int   VANILLA_SHINY_DENOM = 4096;

    // Max additive weight multiplier cap (×10 of base weight)
    public static final double MAX_WEIGHT_BONUS = 10.0;

    private String chainSpecies = "";   // Cobblemon species name (lower-case)
    private int    koCount      = 0;

    // ── Getters ─────────────────────────────────────────────────────────────

    public String  getChainSpecies()  { return chainSpecies; }
    public int     getKoCount()       { return koCount; }
    public boolean hasActiveChain()   { return koCount > 0 && !chainSpecies.isEmpty(); }

    /** Returns the current shiny denominator based on KO count. */
    public int getShinyDenominator() {
        for (int i = SHINY_THRESHOLDS.length - 1; i >= 0; i--) {
            if (koCount >= SHINY_THRESHOLDS[i]) return SHINY_DENOMINATORS[i];
        }
        return VANILLA_SHINY_DENOM;
    }

    /**
     * Additive spawn-weight bonus factor.
     * +1 % per KO, capped at MAX_WEIGHT_BONUS (i.e. 1000 KOs → ×10).
     */
    public double getSpawnWeightMultiplier() {
        if (chainSpecies.isEmpty()) return 1.0;
        double bonus = 1.0 + (koCount / 100.0);
        return Math.min(bonus, MAX_WEIGHT_BONUS);
    }

    // ── Mutation ─────────────────────────────────────────────────────────────

    /** Called when the player KOs a Pokémon. */
    public void recordKo(String species) {
        String s = species.toLowerCase();
        if (!s.equals(chainSpecies)) {
            // New species — start fresh chain
            chainSpecies = s;
            koCount = 0;
        }
        koCount++;
    }

    /** Called when the player flees a battle — chain breaks. */
    public void breakChain() {
        chainSpecies = "";
        koCount = 0;
    }

    /** Called on capture — chain continues (L8Games style). */
    public void recordCapture(String species) {
        // Capture does NOT break chain; treat same as a KO for weighting
        recordKo(species);
    }

    // ── NBT persistence ───────────────────────────────────────────────────────

    public NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.putString("chainSpecies", chainSpecies);
        nbt.putInt("koCount", koCount);
        return nbt;
    }

    public static PlayerChainData fromNbt(NbtCompound nbt) {
        PlayerChainData d = new PlayerChainData();
        d.chainSpecies = nbt.getString("chainSpecies");
        d.koCount      = nbt.getInt("koCount");
        return d;
    }
}
