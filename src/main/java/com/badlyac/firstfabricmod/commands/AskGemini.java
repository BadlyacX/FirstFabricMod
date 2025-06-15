package com.badlyac.firstfabricmod.commands;

import com.badlyac.firstfabricmod.utils.MsgUtils;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class AskGemini {

    private static final MinecraftClient minecraftClient = MinecraftClient.getInstance();

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(
                ClientCommandManager.literal("askgemini")
                        .then(ClientCommandManager.argument("input", StringArgumentType.greedyString())
                                .executes(context -> {
                                    String input = StringArgumentType.getString(context, "input");

                                    if (input.equalsIgnoreCase("reset")) {
                                        sendSimplePost("http://localhost:5000/reset", "§6[Gemini] Chat has been reset");
                                    } else if (input.equalsIgnoreCase("shutdown")) {
                                        sendSimplePost("http://localhost:5000/shutdown", "§c[Gemini] AI Server has been shutdown");
                                    } else {
                                        sendAskRequest(input);
                                    }

                                    return 1;
                                }))
        );
    }

    private static void sendAskRequest(String question) {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:5000/ask"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{\"question\":\"" + question + "\"}"))
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    String body = response.body();
                    try {
                        JsonObject json = JsonParser.parseString(body).getAsJsonObject();
                        String reply = json.get("reply").getAsString();

                        MinecraftClient.getInstance().execute(() -> {
                            MsgUtils.sendMsg(minecraftClient, "§cGemini: §r" + reply, false);
                        });
                    } catch (Exception e) {
                        MinecraftClient.getInstance().execute(() -> {
                            MsgUtils.sendMsg(minecraftClient, "§4[ERROR] Gemini response cannot be parsed", false);
                        });
                    }
                });
    }

    private static void sendResetRequest() {
        sendSimplePost("http://localhost:5000/reset", "§6[Gemini] chat has been reset！");
    }

    private static void sendShutdownRequest() {
        sendSimplePost("http://localhost:5000/shutdown", "§c[Gemini] AI Server has been shutdown");
    }

    private static void sendSimplePost(String url, String confirmMessage) {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.discarding())
                .thenAccept(response -> {
                    MinecraftClient.getInstance().execute(() ->
                            MsgUtils.sendMsg(minecraftClient, confirmMessage, false)
                    );
                });
    }
}