package com.badlyac.firstfabricmod;

import net.fabricmc.api.ClientModInitializer;

public class FirstFabricModClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ToggleResourcePackKeyBindHandler.register();
        ToggleAutoAttackHandler.register();
    }
}