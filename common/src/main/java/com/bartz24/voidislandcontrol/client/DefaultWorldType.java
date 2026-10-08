package com.bartz24.voidislandcontrol.client;

import com.bartz24.voidislandcontrol.config.VicConfig;
import net.minecraft.resources.ResourceLocation;

/**
 * Common create-world decision. No Forge or Fabric imports.
 * Each loader registers its own screen-open event and calls {@link #shouldReplace}.
 */
public final class DefaultWorldType {
    public static final ResourceLocation VOID = new ResourceLocation("voidislandcontrol", "void");
    public static final ResourceLocation EX_DEORUM_VOID = new ResourceLocation("exdeorum", "void_world");
    public static final ResourceLocation VANILLA_NORMAL = new ResourceLocation("minecraft", "normal");

    private static ResourceLocation originalDefault;

    private DefaultWorldType() {
    }

    public static boolean shouldReplace(ResourceLocation current) {
        if (!VicConfig.islandSettings.defaultVoidWorld || current == null) return false;
        if (originalDefault == null) originalDefault = current;
        return current.equals(originalDefault) || current.equals(VANILLA_NORMAL) || current.equals(EX_DEORUM_VOID);
    }
}
