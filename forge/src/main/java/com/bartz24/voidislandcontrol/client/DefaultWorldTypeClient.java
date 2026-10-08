package com.bartz24.voidislandcontrol.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraftforge.client.event.ScreenEvent;

import java.util.function.Consumer;

/**
 * Forge only. Path: forge/src/main/java/com/bartz24/voidislandcontrol/client/DefaultWorldTypeClient.java
 * Decision is in common DefaultWorldType.
 *
 * Adapted from Ex Deorum ClientHandler.onScreenOpen (GPL-3.0-or-later, thedarkcolour, 2024).
 * The warning is drawn only while the selected preset is exdeorum:void_world.
 */
public final class DefaultWorldTypeClient {
    public static final Consumer<ScreenEvent.Opening> HOOK = DefaultWorldTypeClient::onScreenOpen;
    public static final Consumer<ScreenEvent.Render.Post> DRAW = DefaultWorldTypeClient::onDraw;

    private static final Component WARNING = Component.literal(
            "Ex Deorum void world is selected. Turn off set_void_world_as_default in config/exdeorum-common.toml so Void Island Control is the default world type.");

    private DefaultWorldTypeClient() {
    }

    public static void onScreenOpen(ScreenEvent.Opening event) {
        if (!(event.getNewScreen() instanceof CreateWorldScreen screen)) return;
        apply(screen.getUiState());
    }

    public static void onDraw(ScreenEvent.Render.Post event) {
        if (!(event.getScreen() instanceof CreateWorldScreen screen)) return;
        if (!isExDeorumVoid(screen.getUiState())) return;
        GuiGraphics graphics = event.getGuiGraphics();
        graphics.drawWordWrap(Minecraft.getInstance().font, WARNING, 8, screen.height - 28, screen.width - 16, 0xFFFF55);
    }

    static void apply(WorldCreationUiState ui) {
        WorldCreationUiState.WorldTypeEntry current = ui.getWorldType();
        if (current == null || current.preset() == null || current.preset().unwrapKey().isEmpty()) return;
        if (!DefaultWorldType.shouldReplace(current.preset().unwrapKey().get().location())) return;
        ui.getSettings().worldgenLoadContext().registryOrThrow(Registries.WORLD_PRESET)
                .getHolder(ResourceKey.create(Registries.WORLD_PRESET, DefaultWorldType.VOID))
                .ifPresent(holder -> ui.setWorldType(new WorldCreationUiState.WorldTypeEntry(holder)));
    }

    private static boolean isExDeorumVoid(WorldCreationUiState ui) {
        WorldCreationUiState.WorldTypeEntry current = ui.getWorldType();
        if (current == null || current.preset() == null || current.preset().unwrapKey().isEmpty()) return false;
        return current.preset().unwrapKey().get().location().equals(DefaultWorldType.EX_DEORUM_VOID);
    }
}
