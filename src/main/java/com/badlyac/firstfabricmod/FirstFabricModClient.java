package com.badlyac.firstfabricmod;

import com.badlyac.firstfabricmod.handler.ToggleAutoAttackHandler;
import com.badlyac.firstfabricmod.handler.ToggleResourcePackKeyBindHandler;
import com.badlyac.firstfabricmod.manager.GeminiManager;
import net.fabricmc.api.ClientModInitializer;

public class FirstFabricModClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ToggleResourcePackKeyBindHandler.register();
        ToggleAutoAttackHandler.register();
        GeminiManager.setup();
    }
}