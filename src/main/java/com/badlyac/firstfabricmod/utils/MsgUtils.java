package com.badlyac.firstfabricmod.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.util.Objects;

public class MsgUtils {

    public static void sendMsg(MinecraftClient client, String str, boolean overlay) {
        Objects.requireNonNull(client.player).sendMessage(Text.literal(str), overlay);
    }
}
