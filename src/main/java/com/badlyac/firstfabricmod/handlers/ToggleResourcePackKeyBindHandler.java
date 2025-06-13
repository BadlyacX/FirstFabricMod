package com.badlyac.firstfabricmod.handlers;

import com.badlyac.firstfabricmod.FirstFabricModClient;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

import net.minecraft.resource.ResourcePackManager;
import net.minecraft.resource.ResourcePackProfile;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;


public class ToggleResourcePackKeyBindHandler {

    private static final String PACK_ID = "xray";
    private static boolean xrayEnabled = false;
    private static final KeyBinding togglePackKey = KeyBindingHelper.registerKeyBinding(
            new KeyBinding(
                    "key.firstfabric.toggle_pack",
                    InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_K,
                    "category.firstfabric.misc"
            )
    );

//    public static void register(String packId) {
//        ResourceManagerHelper.registerBuiltinResourcePack(
//                (PACK_ID),
//                FirstFabricModClient.class,
//                "resourcepacks/xray",
//                Text.literal("內建 X-Ray 資源包"),
//                ResourcePackProfile.InsertionPosition.TOP
//        );
//    }
}