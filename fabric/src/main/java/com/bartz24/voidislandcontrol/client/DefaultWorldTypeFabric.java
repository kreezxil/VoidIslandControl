package com.bartz24.voidislandcontrol.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.presets.WorldPreset;

/**
 * Fabric only. Path: fabric/src/main/java/com/bartz24/voidislandcontrol/client/DefaultWorldTypeFabric.java
 */
public class DefaultWorldTypeFabric implements ClientModInitializer {
    private static final Component WARNING = Component.literal(
            "Ex Deorum void world is selected. Turn off set_void_world_as_default in config/exdeorum-common.toml so Void Island Control is the default world type.");

    @Override
    public void onInitializeClient() {
        ScreenEvents.BEFORE_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (!(screen instanceof CreateWorldScreen create)) return;
            apply(create.getUiState());
        });
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (!(screen instanceof CreateWorldScreen create)) return;
            ScreenEvents.afterRender(screen).register((current, graphics, mouseX, mouseY, tickDelta) -> {
                if (isExDeorumVoid(create.getUiState())) {
                    drawWarning(graphics, current.width, current.height);
                }
            });
        });
    }

    private static void apply(WorldCreationUiState ui) {
        var current = ui.getWorldType();
        if (current == null || current.preset() == null || current.preset().unwrapKey().isEmpty()) return;
        if (!DefaultWorldType.shouldReplace(current.preset().unwrapKey().get().location())) return;
        ui.getSettings().worldgenLoadContext().registryOrThrow(Registries.WORLD_PRESET)
                .getHolder(ResourceKey.create(Registries.WORLD_PRESET, DefaultWorldType.VOID))
                .ifPresent(holder -> ui.setWorldType(new WorldCreationUiState.WorldTypeEntry(holder)));
    }

    private static boolean isExDeorumVoid(WorldCreationUiState ui) {
        var current = ui.getWorldType();
        if (current == null || current.preset() == null || current.preset().unwrapKey().isEmpty()) return false;
        return current.preset().unwrapKey().get().location().equals(DefaultWorldType.EX_DEORUM_VOID);
    }

    private static void drawWarning(GuiGraphics graphics, int width, int height) {
        graphics.drawWordWrap(Minecraft.getInstance().font, WARNING, 8, height - 28, width - 16, 0xFFFF55);
    }
}
