# Attribution

Void Island Control is GPLv3-only for its own code. See LICENSE.md.

The selectable world-preset approach is adapted from SkyblockBuilder, which is Apache-2.0.

- Project: SkyblockBuilder
- Author: MelanX / ChaoticTrials
- Source: https://github.com/ChaoticTrials/SkyblockBuilder/tree/1.20.x
- License: Apache License 2.0

What was adapted, not copied wholesale:

- Register the preset in `data/minecraft/tags/worldgen/world_preset/normal.json` so the create-world World Type button cycles to it. SkyblockBuilder does this in datagen (`ModTagProvider` / `WorldPresetProvider`).
- Name the button with `generator.<namespace>.<path>`. SkyblockBuilder uses `generator.skyblockbuilder.skyblock` in `assets/skyblockbuilder/lang/en_us.json`.
- Build overworld, nether, and end stems from config when the preset is chosen. That idea comes from `de.melanx.skyblockbuilder.world.presets.SkyblockPreset` (`dimensions()`, `configuredOverworldChunkGenerator()`). Void Island Control keeps its own generator and reads `config/voidislandcontrol.toml` instead of SkyblockBuilder's JSON5 config.

SkyblockBuilder is not bundled. No SkyblockBuilder source file is included in this repository.
