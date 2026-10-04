package com.bartz24.voidislandcontrol.api;

import com.bartz24.voidislandcontrol.config.VicConfig;
import net.minecraft.nbt.CompoundTag;

public class VisitPerms {
    public boolean allowBlockInteract;
    public boolean allowItemUse;
    public boolean allowEntityInteract;
    public boolean allowAttack;
    public boolean allowPickup;
    public boolean allowBlockHarvest;
    public boolean allowBlockPlace;

    public static final String[] FLAGS = new String[]{
            "allowBlockInteract",
            "allowItemUse",
            "allowEntityInteract",
            "allowAttack",
            "allowPickup",
            "allowBlockHarvest",
            "allowBlockPlace"
    };

    public static VisitPerms fromConfig() {
        VisitPerms p = new VisitPerms();
        VicConfig.VisitSettings v = VicConfig.commandSettings.visitSettings;
        if (v == null) return p;
        p.allowBlockInteract = v.allowBlockInteract;
        p.allowItemUse = v.allowItemUse;
        p.allowEntityInteract = v.allowEntityInteract;
        p.allowAttack = v.allowAttack;
        p.allowPickup = v.allowPickup;
        p.allowBlockHarvest = v.allowBlockHarvest;
        p.allowBlockPlace = v.allowBlockPlace;
        return p;
    }

    public void set(String flag, boolean value) {
        switch (flag.toLowerCase()) {
            case "allowblockinteract" -> allowBlockInteract = value;
            case "allowitemuse" -> allowItemUse = value;
            case "allowentityinteract" -> allowEntityInteract = value;
            case "allowattack" -> allowAttack = value;
            case "allowpickup" -> allowPickup = value;
            case "allowblockharvest" -> allowBlockHarvest = value;
            case "allowblockplace" -> allowBlockPlace = value;
            default -> throw new IllegalArgumentException(flag);
        }
    }

    public CompoundTag write() {
        CompoundTag nbt = new CompoundTag();
        nbt.putBoolean("allowBlockInteract", allowBlockInteract);
        nbt.putBoolean("allowItemUse", allowItemUse);
        nbt.putBoolean("allowEntityInteract", allowEntityInteract);
        nbt.putBoolean("allowAttack", allowAttack);
        nbt.putBoolean("allowPickup", allowPickup);
        nbt.putBoolean("allowBlockHarvest", allowBlockHarvest);
        nbt.putBoolean("allowBlockPlace", allowBlockPlace);
        return nbt;
    }

    public static VisitPerms read(CompoundTag nbt) {
        VisitPerms p = new VisitPerms();
        p.allowBlockInteract = nbt.getBoolean("allowBlockInteract");
        p.allowItemUse = nbt.getBoolean("allowItemUse");
        p.allowEntityInteract = nbt.getBoolean("allowEntityInteract");
        p.allowAttack = nbt.getBoolean("allowAttack");
        p.allowPickup = nbt.getBoolean("allowPickup");
        p.allowBlockHarvest = nbt.getBoolean("allowBlockHarvest");
        p.allowBlockPlace = nbt.getBoolean("allowBlockPlace");
        return p;
    }
}
