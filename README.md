# Void Island Control Remastered

A remaster of **Void Island Control** for Minecraft **1.12.2**.

Original CurseForge project: https://www.curseforge.com/minecraft/mc-mods/void-island-control

Void Island Control adds a world type for a customizable, multiplayer void world. Players create and manage isolated islands, invite teammates, and run skyblock-style play in a void overworld (with optional void nether and end). This remaster keeps that feature set, rebuilds it on Log4j-patched Forge **1.12.2-14.23.5.2864**, and adds visit / spectate / list tools that work while the island owner is offline.

## License and attribution (GPLv3)

This project is licensed under the **GNU General Public License v3.0**. The full license text is in [`LICENSE.md`](LICENSE.md).

Original work:

- Author: **Bartz24**
- CurseForge: https://www.curseforge.com/minecraft/mc-mods/void-island-control
- Original source: https://github.com/Bartz24/VoidIslandControl

Remaster:

- Maintainer: **kreezxil**
- Source: https://github.com/kreezxil/VoidIslandControl

Bartz24's original code and this remaster (including the new visit / spectate work) are both under GPLv3. You may copy, modify, and redistribute this mod under that license. Modified versions must keep the license, keep copyright notices, and mark the changes.

## What this mod does

(From the original CurseForge description, plus remaster notes.)

Void Island Control adds a world type for a customizable, multiplayer void world. It is meant for isolated islands, teams, and skyblock-style play.

**Original features**

- Multiplayer island create, invite, join, leave, kick, home, spawn, reset, and one-chunk mode.
- Five default island types: Grass, Sand, Snow, Wood, and Garden of Glass (if Botania and Garden of Glass are installed). Each type is configurable.
- Custom islands from structure-block `.nbt` files in the config folder.
- Config for fill / bottom blocks, spawn protection, overworld generation mode, cloud / horizon Y, island size and distance, void nether / end with optional structures, one-chunk mode, starter chest, starting inventory, command blocks on new islands, and commands run when the world first loads.
- Compatible as a starting-island companion for packs that also use Sky Resources (Sky Resources itself was incompatible on 1.10; this remaster is 1.12.2 only).
- 1.10 / 1.11 originals needed CompatLayer. This remaster does **not** — it is 1.12.2 Forge only.

**Basic use (same as upstream)**

Install the mod on client and server. Pick **Void? World** in singleplayer or set `level-type=voidworld` on a server. Use `/island create [optional type]` unless auto-create is on.

It is a **normal dual-side Forge mod**. The same jar goes on the dedicated server **and** on every client. The Forge handshake will reject a client that does not have it.

### World type

- Create a world with type **Void? World** (singleplayer) or `voidworld` (server `level-type`).
- Overworld generation can be true void, vanilla overworld, superflat, another world type, or customized.
- Cloud height and horizon height are configurable.
- Nether and End can each be void, with optional structure generation.
- Island management runs in a configured base dimension (usually overworld).

### Islands

Default types (each can be enabled or disabled in config):

| Type | Notes |
| --- | --- |
| Grass | Optional tree; grass / dirt / coarse dirt |
| Sand | Optional cactus; normal or red sand |
| Snow | Optional pumpkins and packed-ice "igloo" ring |
| Wood | Planks of a chosen wood; optional water and string |
| Garden of Glass (`gog`) | Requires **Botania** and **Garden of Glass** |

Also:

- Custom islands from structure-block `.nbt` files in the `voidislandcontrolstructures` config folder. Add the file name to the Custom Islands list. A structure block data tag `spawn_point` sets the landing block.
- Main spawn island at grid `0,0` can use bedrock, a named type, or random.
- Player islands can use a fixed type or random.
- Island size, Y level, distance between islands, bottom block (bedrock or the island's secondary block), optional starter chest.
- Optional biome paint on the island and a range around it.
- Spawn protection and a build-range that keeps visitors from wandering off the island they are visiting.
- Auto-create islands for new players (all worlds, or dedicated servers only).
- One-chunk mode (world border 16 blocks) if enabled in config or by command.
- Optional command block under new islands (impulse / repeating / chain, facing, auto, command string).
- Commands run once when the world first loads.
- Starting inventory via `/startingInv` and config `startingItems`.
- Resist / fire resist / regen buffs on island teleport, duration configurable.

Island membership is stored in the **world save** as UUIDs (`VoidIslandControlSaveData`). That is separate from `usercache.json` / `usernamecache.json`. Those caches are only used to turn a typed player name into a UUID.

### Player commands

Default command name is `/island` (configurable).

| Command | What it does |
| --- | --- |
| `/island create [type]` | Create your island. Optional type name or index. |
| `/island invite <player>` | Invite another player to your island. |
| `/island join` | Accept a recent invite. |
| `/island leave` | Leave the island and go to spawn. Last member must confirm. Inventory clear follows config. |
| `/island kick <player>` | Owner kicks a member. Their items drop at the owner. |
| `/island home` | Teleport to your island. Must be far enough away (protection range). |
| `/island spawn` | Teleport to world spawn `(0, islandY, 0)`. |
| `/island reset [type]` | New island in a new slot; inventory reset follows config. |
| `/island onechunk` | One-chunk border mode. Disabled in config by default. |
| `/island visit <player>` | **Remaster:** go to that player's island. Adventure for normal players, **survival if you are an operator**. |
| `/island spectate <player>` | **Remaster:** go to that player's island in **spectator** mode (ops included). |
| `/island list` | **Remaster:** list player names that have an island you can visit or spectate. |

Visit, spectate, and list:

- Work while the target player is **online or offline**, as long as they have an island in VIC save data and the server can resolve their name to a UUID (usercache or Mojang lookup on online-mode servers).
- `/island list` prints those names (except yourself). Tab complete on visit / spectate uses the same list.
- You cannot visit / spectate your own island (`/island home` is that).
- Going too far from the visited island, or using `/island home` / `/island spawn` / create / join / leave, drops visit state and puts you back in survival.
- `allowVisitCommand` disables visit and spectate. List still works.

### Admin commands

`/islandAdmin` (permission level 2):

| Command | What it does |
| --- | --- |
| `/islandAdmin kick <player>` | Force-remove a player from their island. |
| `/islandAdmin assign <player> <islandX> <islandY>` | Put a player on an existing island grid cell. |
| `/islandAdmin assignOwner <player>` | Make that player the island owner. |
| `/islandAdmin getIslandHere` | Print grid X/Y and member UUIDs for the island you are standing on. |
| `/islandAdmin list` | List every island, grid coords, and member UUIDs. |

### Other command

`/startingInv` — capture or manage the starting inventory used for new / reset players (see in-game usage).

## Remaster changes

Compared with Bartz24's 1.5.3 line:

- Target **Forge 1.12.2-14.23.5.2864** (Log4j / CVE-2021-44228 patched; do not ship pre-2856 Forge on a public pack).
- Build moved from ForgeGradle 2.3 to **ForgeGradle 3** (`userdev3`), mappings `stable_39` / `39-1.12`, Java 8, Gradle wrapper 4.9.
- `/island visit` is adventure for normal players, survival for operators.
- New `/island spectate` (spectator, including operators).
- New `/island list` — names of players who have a visitable island.
- Visit and spectate resolve offline players by UUID.
- Mapping-name updates so the old snapshot MCP calls compile on this toolchain (`getPath` / `getNamespace`, `getChunk`, local `WorldType` lookup).
- Garden of Glass support still compiles against Botania when that dependency is on the FG3 classpath (`fg.deobf`).

## Requirements

- Minecraft **1.12.2**
- Forge **14.23.5.2864** (or another 1.12.2 Forge **≥ 14.23.5.2856** if you knowingly change the pin)
- Java **8** to run and to build
- Optional: Botania + Garden of Glass for the `gog` island

Install the jar on **client and server**.

## Playing

1. Drop the jar into `mods` on the server and on each client.
2. Set the world type to `voidworld` (server.properties `level-type=voidworld`) or pick **Void? World** in the create-world screen.
3. Join. Use `/island create` unless auto-create is on.
4. Invite teammates with `/island invite`.
5. See who you can visit: `/island list`.
6. Visit an island: `/island visit <name>` (adventure, or survival if you are an op).
7. Spectate an island: `/island spectate <name>`.

Custom structures: save with a structure block, copy the `.nbt` into `config/voidislandcontrolstructures/`, add the name to the Custom Islands list, mark spawn with data `spawn_point`.

## Building from source

JDK 8 only. Gradle wrapper 4.9.

```bash
export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64
export PATH="$JAVA_HOME/bin:$PATH"
./gradlew --version    # Gradle 4.9, Java 1.8
./gradlew build
```

There is no `setupDecompWorkspace` on ForgeGradle 3. Use `./gradlew genIntellijRuns` after a successful resolve if you want IDEA run configs.

Botania is pulled with CurseMaven + `fg.deobf`, or a local jar remapped with `fg.deobf(file("libs/Botania-r1.10-363.jar"))`. Do not use a pre-deobfed snapshot jar from the FG 2.3 era.

## Pack notes

- Pin Forge to **2864** (or at least **2856+**) so the Log4j hole is closed.
- VIC save data survives usercache wipes. Command names may not, until the cache or Mojang can resolve the name again.
- Offline-mode servers can only resolve names that already exist in usercache unless the player is online.

## Copying a world to another server

Island membership lives **in the world** (`world/data/`, VIC save, UUIDs).  
Player **names** do not. Those live next to the server jar:

- `usercache.json` (vanilla)
- `usernamecache.json` (Forge)

If you copy only the world folder onto a test or new host, `/islandAdmin list` will still show every island and UUID. `/island list`, `/island visit <name>`, and `/island spectate <name>` will only show names the new server has already seen. Everyone else looks “missing” even though the island is there.

When you clone a world:

1. Copy the world folder.
2. Copy `usercache.json` and `usernamecache.json` from the **old server root** (same directory as the Forge jar) into the **new server root**.
3. Restart the new server.

Do not put those two JSON files only inside the world folder. They will be ignored.

## Credits

- **Bartz24** — original Void Island Control
- Forge / FML / MCP
- Vazkii / williewillus — Botania and Garden of Glass (optional island type)
- kreezxil — remaster, FG3 port, visit / spectate / list, offline visit, op-survival visit
