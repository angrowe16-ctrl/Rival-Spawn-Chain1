# Rival Spawn Chain — Fabric 1.21.1 + Cobblemon 1.7.3

## Easiest way to get your .jar (3 steps, no coding)

### Option A — GitHub Actions (recommended, no local Java needed)

1. Create a free account at https://github.com if you don't have one
2. Create a new repo ("rivalspawnchain"), and upload the contents of this zip
3. Go to **Actions** tab → click "Build Mod" → click **Run workflow**
4. When it finishes (~3 min), click the run, scroll down to **Artifacts**, download **rivalspawnchain-jar.zip**
5. Unzip it — inside is `rivalspawnchain-1.0.0.jar`
6. Drop that into your `.minecraft/mods/` folder alongside Cobblemon 1.7.3 + Fabric API

The `.github/workflows/build.yml` file is already included; GitHub Actions will trigger automatically on every push too.

---

### Option B — Build locally (needs Java 21)

```bash
# Windows (in the extracted folder)
gradlew.bat build

# Mac / Linux
chmod +x gradlew && ./gradlew build
```

Your jar appears at: `build/libs/rivalspawnchain-1.0.0.jar`

---

## Features

| Feature | Detail |
|---|---|
| KO chain | Same species = chain builds; different species = reset |
| Spawn weight | +1 % per KO, up to ×10 for chained species |
| Shiny odds | 30 KO = 1/2048 · 65 = 1/1024 · 100 = 1/512 |
| PokéNav HUD | Bottom-right, silent — no popups ever |
| Chain breaks | Fleeing a battle breaks the chain |
| Chain persists | Saved per-player, survives server restart |
| Multiplayer | Per-UUID isolation, S2C sync on join |

## Commands

| Command | Who | Effect |
|---|---|---|
| `/chain` | Player | Show your current chain + shiny odds |
| `/chain reset` | Player | Reset your own chain |
| `/chain reset <name>` | OP (level 2) | Reset another player's chain |

## HUD Colour Guide

- **White** — chain active, vanilla shiny odds (0–29 KOs)
- **Yellow** — 1/2048 (30–64 KOs)
- **Orange** — 1/1024 (65–99 KOs)  
- **Red** — 1/512 (100+ KOs)

## Dependencies (auto-downloaded by Gradle)
- Fabric Loader ≥ 0.16.0
- Fabric API 0.102.0+1.21.1
- Cobblemon 1.7.3+1.21.1
