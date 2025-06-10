package com.badlyac.firstfabricmod.managers;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

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
            try {
                if (!pythonProcess.waitFor(5, TimeUnit.SECONDS)) {
                    pythonProcess.destroyForcibly();
                    System.out.println("Python process has been forcefully terminated.");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().isInterrupted();
                System.err.println("Error while stopping python process: " + e.getMessage());
            }
            pythonProcess = null;
            System.out.println("Python process has been terminated.");

            try {
                if (Files.exists(SCRIPT_PATH)) {
                    Files.delete(SCRIPT_PATH);
                    System.out.println("gemini_server.py has been deleted.");
                } else {
                    System.out.println("gemini_server.py does not exist.");
                }
            } catch (IOException e) {
                System.err.println("Error ocurred while deleting gemini_server.py: " + e.getMessage());
            }
        }
    }
}