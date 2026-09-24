package com.bartz24.voidislandcontrol.api;

import com.bartz24.voidislandcontrol.config.ConfigOptions;
import net.minecraft.nbt.NBTTagCompound;

public class VisitPerms {
    public boolean allowBlockInteract;
    public boolean allowItemUse;
    public boolean allowEntityInteract;
    public boolean allowAttack;
    public boolean allowPickup;
    public boolean allowBlockHarvest;
    public boolean allowBlockPlace;

    public static final String[] FLAGS = new String[] {
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
        if (ConfigOptions.commandSettings == null || ConfigOptions.commandSettings.visitSettings == null)
            return p;
        ConfigOptions.CommandSettings.VisitSettings v = ConfigOptions.commandSettings.visitSettings;
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
        if (flag.equalsIgnoreCase("allowBlockInteract"))
            allowBlockInteract = value;
        else if (flag.equalsIgnoreCase("allowItemUse"))
            allowItemUse = value;
        else if (flag.equalsIgnoreCase("allowEntityInteract"))
            allowEntityInteract = value;
        else if (flag.equalsIgnoreCase("allowAttack"))
            allowAttack = value;
        else if (flag.equalsIgnoreCase("allowPickup"))
            allowPickup = value;
        else if (flag.equalsIgnoreCase("allowBlockHarvest"))
            allowBlockHarvest = value;
        else if (flag.equalsIgnoreCase("allowBlockPlace"))
            allowBlockPlace = value;
        else
            throw new IllegalArgumentException(flag);
    }

    public boolean get(String flag) {
        if (flag.equalsIgnoreCase("allowBlockInteract"))
            return allowBlockInteract;
        if (flag.equalsIgnoreCase("allowItemUse"))
            return allowItemUse;
        if (flag.equalsIgnoreCase("allowEntityInteract"))
            return allowEntityInteract;
        if (flag.equalsIgnoreCase("allowAttack"))
            return allowAttack;
        if (flag.equalsIgnoreCase("allowPickup"))
            return allowPickup;
        if (flag.equalsIgnoreCase("allowBlockHarvest"))
            return allowBlockHarvest;
        if (flag.equalsIgnoreCase("allowBlockPlace"))
            return allowBlockPlace;
        throw new IllegalArgumentException(flag);
    }

    public NBTTagCompound write() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setBoolean("allowBlockInteract", allowBlockInteract);
        nbt.setBoolean("allowItemUse", allowItemUse);
        nbt.setBoolean("allowEntityInteract", allowEntityInteract);
        nbt.setBoolean("allowAttack", allowAttack);
        nbt.setBoolean("allowPickup", allowPickup);
        nbt.setBoolean("allowBlockHarvest", allowBlockHarvest);
        nbt.setBoolean("allowBlockPlace", allowBlockPlace);
        return nbt;
    }

    public static VisitPerms read(NBTTagCompound nbt) {
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