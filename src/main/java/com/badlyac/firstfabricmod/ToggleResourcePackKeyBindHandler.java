package com.badlyac.firstfabricmod;

import com.badlyac.firstfabricmod.mixin.MinecraftClientAccessor;
import com.badlyac.firstfabricmod.mixin.ResourcePackManagerAccessor;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.resource.ResourcePackProfile;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ToggleResourcePackKeyBindHandler {

    private static final MinecraftClient client = MinecraftClient.getInstance();

    private static final KeyBinding togglePackKey = KeyBindingHelper.registerKeyBinding(
            new KeyBinding(
                    "key.firstfabric.toggle_pack",
                    InputUtil.Type.KEYSYM,
                    GLFW.GLFW_KEY_K,
                    "category.firstfabric.misc"
            )
    );

    private static final String TARGET_PACK = "file/Xray_Ultimate_1.20.5_v5.0.1.zip";
    private static List<String> originalPacks = null;
    private static boolean usingCustom = false;

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (togglePackKey.wasPressed()) {
                toggleResourcePack();
            }
        });

        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(
                new SimpleSynchronousResourceReloadListener() {
                    @Override
                    public Identifier getFabricId() {
                        return Identifier.of("firstfabric", "reload_debug");
                    }

                    @Override
                    public void reload(ResourceManager manager) {
                        System.out.println("[✔] 資源重新載入完成！");
                    }
                }
        );
    }

    private static void toggleResourcePack() {
        ResourcePackManager manager = ((MinecraftClientAccessor) client).getResourcePackManager();
        Map<String, ResourcePackProfile> profilesMap = ((ResourcePackManagerAccessor) manager).getProfiles();

        if (!usingCustom) {
            if (profilesMap.containsKey(TARGET_PACK)) {
                if (originalPacks == null) {
                    originalPacks = new ArrayList<>(client.options.resourcePacks);
                }

                List<String> newList = new ArrayList<>(originalPacks);
                if (!newList.contains(TARGET_PACK)) {
                    newList.addFirst(TARGET_PACK);
                }

                client.options.resourcePacks = newList;
                manager.setEnabledProfiles(newList);
                client.reloadResources();
                usingCustom = true;
                System.out.println("[✔] 啟用材質包：" + TARGET_PACK);
            } else {
                System.out.println("[✘] 找不到材質包：" + TARGET_PACK);
            }
        } else {
            client.options.resourcePacks = new ArrayList<>(originalPacks);
            manager.setEnabledProfiles(originalPacks);
            client.reloadResources();
            usingCustom = false;
            System.out.println("[✔] 已恢復原始材質包");
        }
    }
}