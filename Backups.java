/**
 * @author Jacob Schnettler
 * @version 1.0.0
 * @date 3/18/2024
 */

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import java.util.Random;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Backups
{    
    public static void sendDiscordWebhook(String webhookUrl, String backupId) {
        try {
            URL url = URI.create(webhookUrl).toURL();

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            String timestamp = Instant.now().atZone(ZoneId.of("UTC")).format(DateTimeFormatter.ISO_INSTANT);

            String jsonPayload = String.format("{\"embeds\": [{\"title\": \"New Backup Created\", \"fields\": [{\"name\": \"Backup ID\", \"value\": \"%s\"}], \"timestamp\": \"%s\"}]}", backupId, timestamp);

            byte[] postData = jsonPayload.getBytes(StandardCharsets.UTF_8);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(postData);
                os.flush();
            }

            int responseCode = conn.getResponseCode();

            System.out.println("Discord webhook sent.");

            conn.disconnect();
        } catch (Exception e) {
            System.err.println("Error sending webhook message: " + e.getMessage());
        }
    }

    public static void createBackup(String worldFolder, String worldName, String backupFolderPath, String webhookURL) 
    {
        try {
            Path sourcePath = Paths.get(worldFolder);

            if (Files.exists(sourcePath)) {
                Path backupPath = Paths.get(backupFolderPath);

                if (!Files.exists(backupPath)) {
                    Files.createDirectories(backupPath);
                }

                Random random = new Random();

                long backupId = random.nextLong() % 10000000000L; 

                if (backupId < 0) 
                    backupId *= -1;

                Path backupWorldPath = backupPath.resolve("" + backupId);

                Files.walk(sourcePath)
                .forEach(source -> {
                        try {
                            Path target = backupWorldPath.resolve(sourcePath.relativize(source));

                            Files.copy(source, target);
                        } catch (IOException e) {
                            System.err.println("Failed to copy: " + e.getMessage());
                        }
                    });

                System.out.println("Backup of " + worldName + " created successfully.");

                sendDiscordWebhook(webhookURL, "" + backupId);
            } else {
                System.out.println("Source world folder does not exist.");
            }
        } catch (IOException e) {
            System.out.println("Error creating backup: " + e.getMessage());
        }
    }

    public static Map<String, String> readEnvFile(String filePath) throws IOException {
        Map<String, String> envMap = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("=", 2);

                if (parts.length == 2) {
                    String key = parts[0].trim();
                    String value = parts[1].trim();

                    envMap.put(key, value);
                }
            }
        }

        return envMap;
    }

    public static void main(String args[])
    {
        try {
            Map<String, String> env = readEnvFile(".env");

            String worldName = env.get("worldName");
            String world = "../worlds/" + worldName;
            String backupsFolder = "../backups/";

            String webhookURL = env.get("webhookURL");

            int APIPort = 3000;

            try {
                API server = new API(APIPort);

                server.start();
            } catch (IOException e) {
                System.out.println("Error starting HTTP server: " + e.getMessage());
            }

            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

            scheduler.scheduleAtFixedRate(() -> {
                    System.out.println("Creating backup of " + worldName);
                    createBackup(world, worldName, backupsFolder, webhookURL);
                }, 0, 6, TimeUnit.HOURS);
        } catch (IOException e) {
            System.err.println("Error reading .env file: " + e.getMessage());
        }
    }
}
