# Void Island Control for 1.20.1

Multiplayer void-island world control for Minecraft 1.20.1, NeoForge and Fabric. Same feature set as the 1.12.2 remaster: island teams, visit / spectate / list / permissions, starter inventory, custom structure islands, optional void nether and end.

Original CurseForge project: https://www.curseforge.com/minecraft/mc-mods/void-island-control

## License and attribution (GPLv3)

This project is licensed under the GNU General Public License v3.0 only. The full license text is in [`LICENSE.md`](LICENSE.md). Do not add "or later" to inherited Void Island Control code.

Original work:

- Author: Bartz24
- CurseForge: https://www.curseforge.com/minecraft/mc-mods/void-island-control
- Original source: https://github.com/Bartz24/VoidIslandControl

Remaster and 1.20.1 port:

- Maintainer: kreezxil
- Source: https://github.com/kreezxil/VoidIslandControl

You may copy, modify, and redistribute this mod under GPLv3. Modified versions must keep the license, keep copyright notices, and mark the changes.

## What this mod does

Void Island Control adds a world preset for a customizable, multiplayer void world. Players create and manage isolated islands, invite teammates, and run skyblock-style play.

- Multiplayer island create, invite, join, leave, kick, home, spawn, reset, and one-chunk mode.
- Default island types: Grass, Sand, Snow, Wood, and Garden of Glass. Each type is configurable.
- Custom islands from structure `.nbt` files in the `config/voidislandcontrolstructures` folder.
- Config for bottom blocks, spawn protection, island size and distance, void nether / end with optional structures, one-chunk mode, starter chest, starting inventory, command blocks on new islands, and commands run when the world first loads.
- Visit / spectate / list / per-island permission flags from the 1.12.2 remaster.

Install the matching jar on the client and the server. NeoForge and Fabric are separate jars. A Fabric client cannot join a NeoForge server.

### World preset

1.20.1 has no world types. Create a world with preset **Void Island** (`voidislandcontrol:void`) or, on a dedicated server, set `level-type=voidislandcontrol:void`.

- `voidislandcontrol:void` voids overworld, nether, and end. Nether and end still place structures when the preset's `structures` flag is true (fortresses, end cities) without terrain. That matches `netherVoid`, `endVoid`, `netherVoidStructures`, and `endVoidStructures` defaulting to true.
- `voidislandcontrol:void_overworld` voids only the overworld. Use this when nether or end should stay vanilla.

Cloud and horizon values remain in config for pack authors. The 1.20.1 client does not expose the old world-type horizon hook.

Island management runs in `worldGenSettings.baseDimension` (0 overworld, -1 nether, 1 end).

### Islands

| Type | Notes |
| --- | --- |
| Grass | Optional tree; grass / dirt / coarse dirt |
| Sand | Optional cactus; normal or red sand |
| Snow | Optional pumpkins and packed-ice igloo ring |
| Wood | Planks of a chosen wood; optional water and string |
| Garden of Glass (`gog`) | Pebble island. Livingrock, pebble, and water bowl are used when Botania is installed. No hard dependency. |

Also:

- Custom islands from `.nbt` files in `config/voidislandcontrolstructures`. Add the file name (without `.nbt`) to `customIslands`.
- Main spawn island at grid `0,0` can use bedrock, a named type, or random.
- Player islands can use a fixed type or random.
- Island size, Y level, distance, bottom block (bedrock or the island's secondary block), optional starter chest.
- Spawn protection and a build range that keeps players on their own island.
- Auto-create islands for new players (all worlds, or dedicated servers only).
- One-chunk mode (world border 16 blocks) if enabled in config or by command.
- Optional command block under new islands (impulse / repeating / chain, auto, command string).
- Commands run once when the world first loads.
- Starting inventory via `/startingInv` and config `startingItems`.
- Resistance, fire resistance, and regeneration on island teleport.

Island membership is stored in the world save as UUIDs (`vic_data`). Names resolve through the server profile cache, so visit works while the owner is offline.

1.12.2 worlds cannot be opened on 1.20.1. This is a new-world port.

### Player commands

Default command name is `/island` (configurable).

| Command | What it does |
| --- | --- |
| `/island create [type]` | Create your island. Optional type name or index. |
| `/island invite <player>` | Invite another online player to your island. |
| `/island join` | Accept a recent invite (about 60 seconds). |
| `/island leave` | Leave the island and go to spawn. Last member must confirm. Inventory clear follows config. |
| `/island kick <player>` | Owner kicks a member. Their items drop. |
| `/island home` | Teleport to your island. Must be at least `protectionBuildRange` blocks away. |
| `/island spawn` | Teleport to world spawn. |
| `/island reset [type]` | New island in a new slot. Inventory reset follows config. |
| `/island onechunk` | One-chunk border mode. Disabled in config by default. |
| `/island visit <player>` | Visible body on that island. Adventure by default; survival if you are an operator or allowed to harvest/place. |
| `/island spectate <player>` | Ghost cam on that island. |
| `/island list` | Names that have an island you can visit or spectate. |
| `/island permission <player> <flag> <true/false>` | Owner-only whitelist flags for that player on your island. |

Visit, spectate, list, and permissions:

- Visit / spectate work while the target is online or offline if VIC has their island UUID and the name resolves.
- You cannot visit / spectate your own island.
- A visitor is a visible player. Spectator stays the invisible cam.
- Default visit is walk-and-talk only.
- `/island home` / `spawn` / create / join / leave ends the visit and returns survival.
- `allowVisitCommand` disables visit and spectate. List still works.
- `allowPerPlayerOverrides` must be true or `/island permission` does nothing. Overrides are stored on the island, keyed by visitor UUID.

Global visit flags (`commandSettings.visitSettings`, all default false):

| Flag | If true |
| --- | --- |
| `allowBlockInteract` | Chests / machines / right-click blocks |
| `allowItemUse` | Use items |
| `allowEntityInteract` | Frames, villagers, armor stands |
| `allowAttack` | Attack entities |
| `allowPickup` | Pick up items |
| `allowBlockHarvest` | Break / harvest blocks (visitor is put in survival) |
| `allowBlockPlace` | Place blocks (visitor is put in survival) |
| `allowPerPlayerOverrides` | Owners may set the flags above per visitor |

Island lockdown (`islandSettings`, default off):

| Option | Default | Meaning |
| --- | --- | --- |
| `islandLockdown` | false | Yank players back and show the too-far message |
| `islandLockdownRange` | 500 | Distance from island center that triggers it |

Ops are not leashed.

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

`/startingInv` (permission level 2) captures your inventory into `startingItems`. Item form is `namespace:path*count` or `namespace:path{snbt}*count`. 1.20.1 has no item metadata.

### Config

`config/voidislandcontrol.json` is written on first launch. Keys match the 1.12.2 remaster. `worldGenType` is kept so old pack configs still parse; the 1.20.1 world is selected with the preset, not that string.

### Addon API

`com.bartz24.voidislandcontrol.api.IslandManager` and `VicEvents` are the shared API. Register listeners on `VicEvents.CREATE`, `INVITE`, `LEAVE`, `HOME`, `RESET`, `VISIT`, and `SPAWN`. The bus is loader-neutral.

### 1.20.1 notes

- Two jars: NeoForge (`neoforge`) and Fabric (`fabric`). There is no Forge loader build.
- Layout follows the MultiLoader template: shared code in `common`, loader entry points in `neoforge` and `fabric`.
- Garden of Glass no longer calls Botania internals. The pebble is placed by this mod. Botania items are used only if that mod is present.
