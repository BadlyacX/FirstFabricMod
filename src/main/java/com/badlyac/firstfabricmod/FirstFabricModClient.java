package com.badlyac.firstfabricmod;

import com.badlyac.firstfabricmod.commands.AskGemini;
import com.badlyac.firstfabricmod.handlers.ToggleAutoAttackHandler;
import com.badlyac.firstfabricmod.handlers.ToggleResourcePackKeyBindHandler;
import com.badlyac.firstfabricmod.managers.GeminiManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;

public class FirstFabricModClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ToggleResourcePackKeyBindHandler.register();
        ToggleAutoAttackHandler.register();
        GeminiManager.setup();

        handleShutdownEvent();
        registerCommands();
    }

    private void registerCommands() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                AskGemini.register(dispatcher)
        );
    }

    private void handleShutdownEvent() {
        ClientLifecycleEvents.CLIENT_STOPPING.register(minecraftClient ->
                GeminiManager.stopServer()
        );
    }
}