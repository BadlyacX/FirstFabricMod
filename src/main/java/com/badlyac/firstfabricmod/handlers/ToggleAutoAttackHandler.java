package com.badlyac.firstfabricmod.handlers;


import com.badlyac.firstfabricmod.utils.MsgUtils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import org.lwjgl.glfw.GLFW;

import java.util.Objects;


public class ToggleAutoAttackHandler {
    public static boolean isAutoAttackEnabled = false;
    private static int tickCooldown = 0;
    private static final int ATTACK_INTERVAL_TICKS = 18;
    private static final KeyBinding toggleAttackKey = KeyBindingHelper.registerKeyBinding(
            new KeyBinding(
                    "key.firstfabric.toggle_auto_attack",
                    InputUtil.Type.KEYSYM,
                    GLFW.GLFW_KEY_F9,
                    "category.firstfabric.misc"
            ));

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleAttackKey.wasPressed()) {
                isAutoAttackEnabled = !isAutoAttackEnabled;
                MsgUtils.sendMsg(client, "Auto Attack: " + (isAutoAttackEnabled ? "§aON" : "§cOFF"), true);
            }
            if (isAutoAttackEnabled) {
                handleAutoAttack(client);
            }
        });
    }

    private static void handleAutoAttack(MinecraftClient client) {
        if (client.player == null || client.world == null) return;

        tickCooldown++;
        if (tickCooldown >= ATTACK_INTERVAL_TICKS) {
            tickCooldown = 0;

            HitResult hitResult = client.crosshairTarget;
            if (hitResult instanceof EntityHitResult entityHit) {
                Entity target = entityHit.getEntity();

                if (client.interactionManager != null) {
                    client.interactionManager.attackEntity(client.player, target);
                    client.player.swingHand(client.player.getActiveHand());
                }
            }
        }
    }
}