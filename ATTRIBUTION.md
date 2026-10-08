# Void Island Control 
...is GPLv3-only for its own code. See LICENSE.md.

## The selectable world-preset
...approach is adapted from SkyblockBuilder, which is Apache-2.0.

- Project: SkyblockBuilder
- Author: MelanX / ChaoticTrials
- Source: https://github.com/ChaoticTrials/SkyblockBuilder/tree/1.20.x
- License: Apache License 2.0

## What was adapted:

- Register the preset in `data/minecraft/tags/worldgen/world_preset/normal.json` so the create-world World Type button cycles to it. SkyblockBuilder does this in datagen (`ModTagProvider` / `WorldPresetProvider`).
- Name the button with `generator.<namespace>.<path>`. SkyblockBuilder uses `generator.skyblockbuilder.skyblock` in `assets/skyblockbuilder/lang/en_us.json`.
- Build overworld, nether, and end stems from config when the preset is chosen. That idea comes from `de.melanx.skyblockbuilder.world.presets.SkyblockPreset` (`dimensions()`, `configuredOverworldChunkGenerator()`). Void Island Control keeps its own generator and reads `config/voidislandcontrol.toml` instead of SkyblockBuilder's JSON5 config.

SkyblockBuilder is not bundled. No SkyblockBuilder source file is included in this repository.

## Default world type 
...on the create-world screen is adapted from Ex Deorum, which is GPL-3.0-or-later. Void Island Control is GPL-3.0-only, so this adaptation is GPL-3.0-only. No Ex Deorum source file is included.

- Project: Ex Deorum
- Author: thedarkcolour
- Copyright (c) 2024 thedarkcolour
- Source: https://github.com/thedarkcolour/ExDeorum/tree/1.20.1
- File: `src/main/java/thedarkcolour/exdeorum/client/ClientHandler.java` method `onScreenOpen`
- License: GNU General Public License v3.0 or later (https://www.gnu.org/licenses/gpl-3.0.html)

## What was adapted: 

on `ScreenEvent.Opening` for `CreateWorldScreen`, remember the first preset and replace it with this mod's void preset. Ex Deorum only replaces the original vanilla default (`set_void_world_as_default`). Void Island Control listens at `EventPriority.LOWEST` and also replaces `exdeorum:void_world`, so a pack that contains both mods opens on `voidislandcontrol:void`. Superflat, amplified, and single-biome are not replaced. Config key: `islandSettings.defaultVoidWorld`.
