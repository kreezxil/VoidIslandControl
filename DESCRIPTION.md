# Void Island Control

Multiplayer void-island world control for Minecraft 1.20.1 on NeoForge and Fabric.

Original: Bartz24, https://www.curseforge.com/minecraft/mc-mods/void-island-control

Port: kreezxil. GPLv3 only. Do not add "or later".

Pick the Void Island world preset, or set a dedicated server to `level-type=voidislandcontrol:void`. Use `/island create` unless auto-create is on. Config is `config/voidislandcontrol.json`.

Commands: create, invite, join, leave, kick, home, spawn, reset, onechunk, visit, spectate, list, permission. Admin: `/islandAdmin`. Starting inventory: `/startingInv`.

Island types: grass, sand, snow, wood, gog, plus custom structure nbt files in `config/voidislandcontrolstructures`. Visit defaults to walk-and-talk. Owners can whitelist flags per visitor when `allowPerPlayerOverrides` is true.

NeoForge and Fabric are separate jars. Same features on both.
