package com.badlyac.firstfabricmod.manager;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class GeminiManager {
    private static final Path SCRIPT_PATH = Paths.get("gemini_server.py");
    private static Process pythonProcess;

    public static void setup() {
        extractScript();
        startPythonServer();
    }

    private static void extractScript() {
        if (Files.exists(SCRIPT_PATH)) return;

        try (InputStream in = GeminiManager.class.getClassLoader().getResourceAsStream("assets/firstfabricmod/python/gemini/gemini_server.py")) {
            if (in == null) {
                System.out.println("[GeminiManager] cannot find gemini_server.py ！");
                return;
            }
            Files.copy(in, SCRIPT_PATH);
            System.out.println("[GeminiManager] successfully copied gemini_server.py！");
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void startPythonServer() {
        try {
            ProcessBuilder pb = new ProcessBuilder("python", "gemini_server.py");
            pb.inheritIO();
            pythonProcess = pb.start();
            System.out.println("[GeminiManager] Local Gemini Flask Server has activated !");
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void stopServer() {
        if (pythonProcess != null) {
            pythonProcess.destroy();
        }
    }
}

