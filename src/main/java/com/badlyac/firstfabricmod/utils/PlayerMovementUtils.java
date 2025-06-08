package com.badlyac.firstfabricmod.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.util.math.BlockPos;

public class PlayerMovementUtils {
    private static final MinecraftClient client = MinecraftClient.getInstance();
    private static BlockPos targetPos = null;
    private static Direction8 currentDirection = null;

    public enum Direction8 {
        NORTH, SOUTH, EAST, WEST,
        NORTH_EAST, NORTH_WEST, SOUTH_EAST, SOUTH_WEST
    }

    public static void moveTowards(BlockPos target, Direction8 direction) {
        targetPos = target;
        currentDirection = direction;
    }

    public static void stop() {
        stopAllMovement();
        targetPos = null;
        currentDirection = null;
    }

    public static void tick() {
        if (client.player == null || targetPos == null) return;

        BlockPos playerPos = client.player.getBlockPos();
        if (playerPos.equals(targetPos)) {
            stop();
            return;
        }

        moveInDirection(currentDirection);
    }

    public static void moveInDirection(Direction8 direction) {
        stopAllMovement();

        switch (direction) {
            case NORTH -> moveForward(true);
            case SOUTH -> moveBackward(true);
            case EAST -> strafeRight(true);
            case WEST -> strafeLeft(true);
            case NORTH_EAST -> { moveForward(true); strafeRight(true); }
            case NORTH_WEST -> { moveForward(true); strafeLeft(true); }
            case SOUTH_EAST -> { moveBackward(true); strafeRight(true); }
            case SOUTH_WEST -> { moveBackward(true); strafeLeft(true); }
        }
    }

    public static void moveForward(boolean active) {
        setKey(client.options.forwardKey, active);
    }

    public static void moveBackward(boolean active) {
        setKey(client.options.backKey, active);
    }

    public static void strafeLeft(boolean active) {
        setKey(client.options.leftKey, active);
    }

    public static void strafeRight(boolean active) {
        setKey(client.options.rightKey, active);
    }

    public static void jump(boolean active) {
        setKey(client.options.jumpKey, active);
    }

    public static void stopAllMovement() {
        moveForward(false);
        moveBackward(false);
        strafeLeft(false);
        strafeRight(false);
        jump(false);
    }

    private static void setKey(KeyBinding key, boolean active) {
        key.setPressed(active);
    }
}
