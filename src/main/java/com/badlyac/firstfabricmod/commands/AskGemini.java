package com.badlyac.firstfabricmod.commands;


import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class AskGemini {
    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(
                ClientCommandManager.literal("askgemini")
                        .then(ClientCommandManager.argument("input", StringArgumentType.greedyString())
                                .executes(context -> {
                                    String input = StringArgumentType.getString(context, "input");

                                    if (input.equalsIgnoreCase("reset")) {
                                        sendSimplePost("http://localhost:5000/reset", "§6[Gemini] 對話已重置！");
                                    } else if (input.equalsIgnoreCase("shutdown")) {
                                        sendSimplePost("http://localhost:5000/shutdown", "§c[Gemini] AI 伺服器已關閉！");
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
                            MinecraftClient.getInstance().player.sendMessage(Text.literal("§cGemini: §r" + reply), false);
                        });
                    } catch (Exception e) {
                        MinecraftClient.getInstance().execute(() -> {
                            MinecraftClient.getInstance().player.sendMessage(Text.literal("§4[錯誤] Gemini 回覆解析失敗"), false);
                        });
                    }
                });
    }

    private static void sendResetRequest() {
        sendSimplePost("http://localhost:5000/reset", "§6[Gemini] 對話已重置！");
    }

    private static void sendShutdownRequest() {
        sendSimplePost("http://localhost:5000/shutdown", "§c[Gemini] AI 伺服器已關閉！");
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
                            MinecraftClient.getInstance().player.sendMessage(Text.literal(confirmMessage), false)
                    );
                });
    }
}
