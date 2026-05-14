package com.rivalspawnchain.chain;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Singleton that owns every player's {@link PlayerChainData}.
 * Chains persist in  <world>/rivalspawnchain/chains.nbt
 */
public class ChainManager {

    public static final ChainManager INSTANCE = new ChainManager();
    private ChainManager() {}

    private final Map<UUID, PlayerChainData> chains = new HashMap<>();
    private Path savePath;

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    public void onServerStart(MinecraftServer server) {
        savePath = server.getSavePath(WorldSavePath.ROOT).resolve("rivalspawnchain/chains.nbt");
        load();
    }

    public void onServerStop() {
        save();
    }

    // ── Access ────────────────────────────────────────────────────────────────

    public PlayerChainData get(UUID playerId) {
        return chains.computeIfAbsent(playerId, id -> new PlayerChainData());
    }

    // ── Persistence ───────────────────────────────────────────────────────────

    public void save() {
        if (savePath == null) return;
        try {
            Files.createDirectories(savePath.getParent());
            NbtCompound root = new NbtCompound();
            chains.forEach((uuid, data) -> root.put(uuid.toString(), data.toNbt()));
            NbtIo.write(root, savePath);
        } catch (IOException e) {
            System.err.println("[RivalSpawnChain] Failed to save chains: " + e.getMessage());
        }
    }

    private void load() {
        if (savePath == null || !Files.exists(savePath)) return;
        try {
            NbtCompound root = NbtIo.read(savePath);
            if (root == null) return;
            for (String key : root.getKeys()) {
                try {
                    UUID uuid = UUID.fromString(key);
                    chains.put(uuid, PlayerChainData.fromNbt(root.getCompound(key)));
                } catch (IllegalArgumentException ignored) {}
            }
        } catch (IOException e) {
            System.err.println("[RivalSpawnChain] Failed to load chains: " + e.getMessage());
        }
    }
}
