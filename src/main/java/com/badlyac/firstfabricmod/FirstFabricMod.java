package com.badlyac.firstfabricmod;

import net.fabricmc.api.ModInitializer;

public class FirstFabricMod implements ModInitializer {
    public static final String MOD_ID = "firstfabricmod";

    // This logger is used to write text to the console and the log file.
    // It is considered best practice to use your mod id as the logger's name.
    // That way, it's clear which mod wrote info, warnings, and errors.

    @Override
    public void onInitialize() {
        ToggleResourcePackKeyBindHandler.register();
    }
}