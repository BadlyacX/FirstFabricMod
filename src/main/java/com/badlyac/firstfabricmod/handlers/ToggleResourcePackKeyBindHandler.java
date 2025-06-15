package com.badlyac.firstfabricmod.handlers;

import com.badlyac.firstfabricmod.FirstFabricMod;
import com.badlyac.firstfabricmod.utils.MsgUtils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class ToggleResourcePackKeyBindHandler {

    private static final String PACK_ID = "xray";
    private static final String id = FirstFabricMod.id();
    private static final Identifier PACK_IDENTIFIER = Identifier.of(id, PACK_ID);

    private static final KeyBinding togglePackKey = KeyBindingHelper.registerKeyBinding(
            new KeyBinding(
                    "key.firstfabric.toggle_pack",
                    InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_K,
                    "category.firstfabric.misc"
            )
    );

    public static void register() {
        ModContainer modContainer = FabricLoader.getInstance().getModContainer(id).orElseThrow();
        ResourceManagerHelper.registerBuiltinResourcePack(
                PACK_IDENTIFIER,
                modContainer,
                ResourcePackActivationType.NORMAL
        );

        ClientTickEvents.END_CLIENT_TICK.register(minecraftClient -> {
            if (togglePackKey.wasPressed()) {
                toggleXRay(minecraftClient);
            }
        });
    }

    private static void toggleXRay(MinecraftClient client) {
        var packManager = client.getResourcePackManager();
        var enabledPacks = packManager.getEnabledIds();

        if (enabledPacks.contains(PACK_IDENTIFIER.toString())) {
            packManager.disable(PACK_IDENTIFIER.toString());
            MsgUtils.sendMsg(client, "§cX-Ray 關閉！", false);
        } else {
            packManager.enable(PACK_IDENTIFIER.toString());
            MsgUtils.sendMsg(client, "§aX-Ray 開啟！", false);
        }

        client.reloadResources();
    }
}
