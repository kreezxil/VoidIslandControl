package com.bartz24.voidislandcontrol.api;

import com.bartz24.voidislandcontrol.config.VicConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class IslandManager {
    public static final List<IslandGen> islandGenerations = new ArrayList<>();
    public static final List<IslandPos> currentIslands = new ArrayList<>();
    public static final List<String> spawnedPlayers = new ArrayList<>();
    public static boolean worldOneChunk = false;
    public static boolean worldLoaded = false;
    public static int initialIslandDistance = 1000;

    private static final Map<UUID, VisitState> visits = new HashMap<>();
    private static final Map<UUID, JoinState> joins = new HashMap<>();
    private static final Map<UUID, Integer> leaveConfirms = new HashMap<>();

    private IslandManager() {
    }

    public static void registerIsland(IslandGen gen) {
        for (IslandGen existing : islandGenerations) {
            if (existing.identifier.equals(gen.identifier)) return;
        }
        islandGenerations.add(gen);
    }

    public static List<String> getIslandGenTypes() {
        List<String> types = new ArrayList<>();
        for (IslandGen g : islandGenerations) types.add(g.identifier);
        return types;
    }

    public static int getIndexOfIslandType(String type) {
        for (int i = 0; i < islandGenerations.size(); i++) {
            if (islandGenerations.get(i).identifier.equalsIgnoreCase(type)) return i;
        }
        try {
            int idx = Integer.parseInt(type);
            if (idx >= 0 && idx < islandGenerations.size()) return idx;
        } catch (NumberFormatException ignored) {
        }
        return -1;
    }

    public static IslandPos getNextIsland() {
        int size = (int) Math.floor(Math.sqrt(currentIslands.size()));
        if (size % 2 == 0 && size > 0) size--;
        size = (size + 1) / 2;
        for (int x = -size; x <= size; x++) {
            for (int z = -size; z <= size; z++) {
                if (!hasPosition(x, z)) return new IslandPos(x, z);
            }
        }
        return new IslandPos(size + 1, 0);
    }

    public static IslandPos getPlayerIsland(UUID playerUUID) {
        String id = playerUUID.toString();
        for (IslandPos pos : currentIslands) {
            if (pos.getPlayerUUIDs().contains(id)) return pos;
        }
        return null;
    }

    public static IslandPos getIslandAtPos(int x, int y) {
        for (IslandPos pos : currentIslands) {
            if (pos.getX() == x && pos.getY() == y) return pos;
        }
        return null;
    }

    public static boolean hasPosition(int x, int y) {
        return getIslandAtPos(x, y) != null;
    }

    public static boolean playerHasIsland(UUID playerUUID) {
        return getPlayerIsland(playerUUID) != null;
    }

    public static boolean hasPlayerSpawned(UUID playerUUID) {
        return spawnedPlayers.contains(playerUUID.toString());
    }

    public static void addPlayer(UUID playerUUID, IslandPos posAdd) {
        IslandPos pos = getIslandAtPos(posAdd.getX(), posAdd.getY());
        if (pos != null) pos.addNewPlayer(playerUUID);
    }

    public static void removePlayer(UUID playerUUID) {
        IslandPos pos = getPlayerIsland(playerUUID);
        if (pos != null) pos.removePlayer(playerUUID);
    }

    public static BlockPos worldPos(IslandPos pos) {
        int dist = VicConfig.islandSettings.islandDistance;
        return new BlockPos(pos.getX() * dist, VicConfig.islandSettings.islandYLevel, pos.getY() * dist);
    }

    public static BlockPos getSpawnOffset(IslandPos islandPos) {
        if (islandPos == null) return BlockPos.ZERO;
        if (islandPos.getX() == 0 && islandPos.getY() == 0) {
            String main = VicConfig.islandSettings.islandMainSpawnType;
            if ("bedrock".equals(main) || "random".equals(main)) return new BlockPos(0, 7, 0);
            int idx = getIndexOfIslandType(main);
            if (idx != -1) return islandGenerations.get(idx).spawnOffset;
            return BlockPos.ZERO;
        }
        int idx = getIndexOfIslandType(islandPos.getType());
        if (idx == -1) return new BlockPos(0, 2, 0);
        return islandGenerations.get(idx).spawnOffset;
    }

    public static void tpPlayerToPos(ServerPlayer player, BlockPos pos, IslandPos islandPos) {
        BlockPos offset = getSpawnOffset(islandPos);
        if (offset == null) offset = getSpawnOffset(currentIslands.isEmpty() ? null : currentIslands.get(0));
        if (offset == null) offset = BlockPos.ZERO;
        BlockPos dest = pos.offset(offset);
        ServerLevel level = player.server.getLevel(VicConfig.baseLevel());
        if (level == null) level = player.serverLevel();
        if (!VicConfig.islandSettings.forceSpawn) {
            BlockPos feet = dest.above(2);
            if (!level.getBlockState(feet).isAir() || !level.getBlockState(feet.above()).isAir()) {
                BlockPos top = level.getHeightmapPos(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING, dest);
                dest = top;
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal("Failed to spawn. Sent to top block of platform spawn."));
            }
        }
        int ticks = VicConfig.islandSettings.buffTimer;
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, ticks, 4, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, ticks, 4, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, ticks, 4, false, false));
        player.clearFire();
        player.teleportTo(level, dest.getX() + 0.5, dest.getY() + 2.6, dest.getZ() + 0.5, player.getYRot(), player.getXRot());
    }

    public static void tpPlayerToPosSpawn(ServerPlayer player, BlockPos pos, IslandPos islandPos) {
        tpPlayerToPos(player, pos, islandPos);
        BlockPos offset = getSpawnOffset(islandPos);
        if (offset == null) offset = BlockPos.ZERO;
        BlockPos bed = pos.offset(offset);
        player.setRespawnPosition(VicConfig.baseLevel(), bed, 0.0F, true, false);
    }

    public static void setStartingInv(ServerPlayer player) {
        if (!VicConfig.islandSettings.resetInventory) return;
        player.getInventory().clearContent();
        String[] items = VicConfig.islandSettings.startingItems;
        if (items == null) return;
        try {
            for (int i = 0; i < items.length && i < player.getInventory().getContainerSize(); i++) {
                ItemStack stack = parseStartingItem(items[i]);
                if (!stack.isEmpty()) player.getInventory().setItem(i, stack);
            }
        } catch (RuntimeException e) {
            player.getInventory().clearContent();
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("Error getting starting inventory. " + e.getMessage()));
        }
    }

    public static ItemStack parseStartingItem(String raw) {
        if (raw == null || raw.isBlank() || !raw.contains(":")) return ItemStack.EMPTY;
        String trimmed = raw.replace(" ", "");
        int star = trimmed.lastIndexOf('*');
        int count = 1;
        String body = trimmed;
        if (star > 0) {
            body = trimmed.substring(0, star);
            count = Integer.parseInt(trimmed.substring(star + 1));
        }
        String nbt = null;
        int brace = body.indexOf('{');
        if (brace >= 0) {
            nbt = body.substring(brace);
            body = body.substring(0, brace);
        }
        String[] parts = body.split(":");
        if (parts.length >= 3) {
            body = parts[0] + ":" + parts[1];
        }
        ResourceLocation id = ResourceLocation.tryParse(body);
        if (id == null) return ItemStack.EMPTY;
        Item item = BuiltInRegistries.ITEM.get(id);
        if (item == null || item == net.minecraft.world.item.Items.AIR) return ItemStack.EMPTY;
        ItemStack stack = new ItemStack(item, Math.max(1, count));
        if (nbt != null && !nbt.isBlank()) {
            try {
                CompoundTag tag = TagParser.parseTag(nbt);
                stack.setTag(tag);
            } catch (Exception ignored) {
            }
        }
        return stack;
    }

    public static boolean isOperator(ServerPlayer player) {
        return player.hasPermissions(2);
    }

    public static void setVisitLoc(UUID player, int x, int y, boolean spectate) {
        visits.put(player, new VisitState(x, y, spectate));
    }

    public static void removeVisitLoc(UUID player) {
        visits.remove(player);
    }

    public static boolean hasVisitLoc(UUID player) {
        return visits.containsKey(player);
    }

    public static VisitState getVisitLoc(UUID player) {
        return visits.get(player);
    }

    public static GameType visitGameType(ServerPlayer player) {
        VisitState state = visits.get(player.getUUID());
        if (state == null) return player.gameMode.getGameModeForPlayer();
        if (state.spectate) return GameType.SPECTATOR;
        if (isOperator(player)) return GameType.SURVIVAL;
        IslandPos island = getIslandAtPos(state.x, state.y);
        VisitPerms perms = island == null ? VisitPerms.fromConfig() : island.effectivePerms(player.getUUID());
        if (perms.allowBlockHarvest || perms.allowBlockPlace) return GameType.SURVIVAL;
        return GameType.ADVENTURE;
    }

    public static void setJoinLoc(UUID player, int x, int y) {
        joins.put(player, new JoinState(x, y, 20 * 60));
    }

    public static JoinState getJoinLoc(UUID player) {
        return joins.get(player);
    }

    public static void removeJoinLoc(UUID player) {
        joins.remove(player);
    }

    public static void setLeaveConfirm(UUID player) {
        leaveConfirms.put(player, 20 * 20);
    }

    public static boolean hasLeaveConfirm(UUID player) {
        return leaveConfirms.containsKey(player);
    }

    public static void removeLeaveConfirm(UUID player) {
        leaveConfirms.remove(player);
    }

    public static void tickTransient(UUID player) {
        JoinState join = joins.get(player);
        if (join != null) {
            join.ticks--;
            if (join.ticks <= 0) joins.remove(player);
        }
        Integer leave = leaveConfirms.get(player);
        if (leave != null) {
            if (leave <= 1) leaveConfirms.remove(player);
            else leaveConfirms.put(player, leave - 1);
        }
    }

    public static List<String> getKnownIslandPlayerNames(MinecraftServer server) {
        List<String> names = new ArrayList<>();
        for (IslandPos pos : currentIslands) {
            for (String raw : pos.getPlayerUUIDs()) {
                try {
                    UUID id = UUID.fromString(raw);
                    String name = resolveName(server, id);
                    if (name != null && !names.contains(name)) names.add(name);
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
        return names;
    }

    public static String resolveName(MinecraftServer server, UUID id) {
        ServerPlayer online = server.getPlayerList().getPlayer(id);
        if (online != null) return online.getGameProfile().getName();
        Optional<com.mojang.authlib.GameProfile> cached = server.getProfileCache().get(id);
        return cached.map(com.mojang.authlib.GameProfile::getName).orElse(null);
    }

    public static UUID findPlayerUUID(MinecraftServer server, String name) {
        ServerPlayer online = server.getPlayerList().getPlayerByName(name);
        if (online != null) return online.getUUID();
        Optional<com.mojang.authlib.GameProfile> cached = server.getProfileCache().get(name);
        return cached.map(com.mojang.authlib.GameProfile::getId).orElse(null);
    }

    public static final class VisitState {
        public final int x;
        public final int y;
        public final boolean spectate;

        public VisitState(int x, int y, boolean spectate) {
            this.x = x;
            this.y = y;
            this.spectate = spectate;
        }
    }

    public static final class JoinState {
        public final int x;
        public final int y;
        public int ticks;

        public JoinState(int x, int y, int ticks) {
            this.x = x;
            this.y = y;
            this.ticks = ticks;
        }
    }
}
