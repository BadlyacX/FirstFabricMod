package com.badlyac.firstfabricmod;

import com.badlyac.firstfabricmod.handlers.ToggleAutoAttackHandler;
import com.badlyac.firstfabricmod.handlers.ToggleResourcePackKeyBindHandler;
import com.badlyac.firstfabricmod.managers.GeminiManager;
import net.fabricmc.api.ClientModInitializer;

public class FirstFabricModClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ToggleResourcePackKeyBindHandler.register();
        ToggleAutoAttackHandler.register();
        GeminiManager.setup();
    }
}