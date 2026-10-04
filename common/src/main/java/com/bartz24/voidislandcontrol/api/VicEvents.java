package com.bartz24.voidislandcontrol.api;

import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Loader-neutral addon bus. Register from either NeoForge or Fabric code.
 * Inherited VIC code stays GPL-3.0-only. Do not relicense it as "or later".
 */
public final class VicEvents {
    public static final Bus<BiConsumer<ServerPlayer, IslandPos>> CREATE = new Bus<>();
    public static final Bus<BiConsumer<ServerPlayer, IslandPos>> INVITE = new Bus<>();
    public static final Bus<BiConsumer<ServerPlayer, IslandPos>> LEAVE = new Bus<>();
    public static final Bus<BiConsumer<ServerPlayer, IslandPos>> HOME = new Bus<>();
    public static final Bus<BiConsumer<ServerPlayer, IslandPos>> RESET = new Bus<>();
    public static final Bus<BiConsumer<ServerPlayer, IslandPos>> VISIT = new Bus<>();
    public static final Bus<java.util.function.Consumer<ServerPlayer>> SPAWN = new Bus<>();

    private VicEvents() {
    }

    public static final class Bus<T> {
        private final List<T> listeners = new ArrayList<>();

        public void register(T listener) {
            listeners.add(listener);
        }

        public List<T> listeners() {
            return listeners;
        }
    }
}
