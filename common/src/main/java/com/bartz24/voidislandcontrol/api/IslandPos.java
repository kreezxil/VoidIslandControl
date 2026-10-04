package com.bartz24.voidislandcontrol.api;

import com.bartz24.voidislandcontrol.config.VicConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class IslandPos {
    private int posX;
    private int posY;
    private String type = "";
    private final List<String> playerUUIDs = new ArrayList<>();
    private final Map<String, VisitPerms> visitPerms = new HashMap<>();

    public IslandPos(int x, int y, UUID... ids) {
        this("", x, y, ids);
    }

    public IslandPos(String type, int x, int y, UUID... ids) {
        this.type = type == null ? "" : type;
        this.posX = x;
        this.posY = y;
        for (UUID id : ids) {
            playerUUIDs.add(id.toString());
        }
    }

    public void addNewPlayer(UUID playerUUID) {
        String id = playerUUID.toString();
        if (!playerUUIDs.contains(id)) playerUUIDs.add(id);
    }

    public void removePlayer(UUID playerUUID) {
        playerUUIDs.remove(playerUUID.toString());
    }

    public int getX() {
        return posX;
    }

    public int getY() {
        return posY;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type == null ? "" : type;
    }

    public List<String> getPlayerUUIDs() {
        return playerUUIDs;
    }

    public boolean isOwner(UUID player) {
        return !playerUUIDs.isEmpty() && playerUUIDs.get(0).equals(player.toString());
    }

    public VisitPerms getVisitPerms(UUID visitor) {
        return visitPerms.get(visitor.toString());
    }

    public VisitPerms getOrCreateVisitPerms(UUID visitor) {
        return visitPerms.computeIfAbsent(visitor.toString(), k -> VisitPerms.fromConfig());
    }

    public void setVisitPerm(UUID visitor, String flag, boolean value) {
        getOrCreateVisitPerms(visitor).set(flag, value);
    }

    public CompoundTag write() {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt("posX", posX);
        nbt.putInt("posY", posY);
        if (type != null && !type.isEmpty()) nbt.putString("type", type);
        ListTag list = new ListTag();
        for (String id : playerUUIDs) {
            CompoundTag tag = new CompoundTag();
            tag.putString("playerUUID", id);
            list.add(tag);
        }
        nbt.put("UUIDs", list);
        ListTag permList = new ListTag();
        for (Map.Entry<String, VisitPerms> e : visitPerms.entrySet()) {
            CompoundTag tag = e.getValue().write();
            tag.putString("playerUUID", e.getKey());
            permList.add(tag);
        }
        nbt.put("VisitPerms", permList);
        return nbt;
    }

    public static IslandPos read(CompoundTag nbt) {
        IslandPos pos = new IslandPos(nbt.getString("type"), nbt.getInt("posX"), nbt.getInt("posY"));
        ListTag list = nbt.getList("UUIDs", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            pos.playerUUIDs.add(list.getCompound(i).getString("playerUUID"));
        }
        ListTag permList = nbt.getList("VisitPerms", Tag.TAG_COMPOUND);
        for (int i = 0; i < permList.size(); i++) {
            CompoundTag tag = permList.getCompound(i);
            pos.visitPerms.put(tag.getString("playerUUID"), VisitPerms.read(tag));
        }
        return pos;
    }

    public VisitPerms effectivePerms(UUID visitor) {
        if (VicConfig.commandSettings.visitSettings.allowPerPlayerOverrides) {
            VisitPerms override = visitPerms.get(visitor.toString());
            if (override != null) return override;
        }
        return VisitPerms.fromConfig();
    }
}
