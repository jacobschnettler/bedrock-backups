/**
 * Write a description of class Backups here.
 * 
 * @author Jacob Schnettler
 * @version 1.0.0
 * @date 3/18/2024
 */

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Backups
{    
    public static void createBackup(String worldFolder, String worldName, String backupFolderPath) 
    {
        try {
            // Create a Path object for the source world folder
            Path sourcePath = Paths.get(worldFolder);

            // Check if the source world folder exists
            if (Files.exists(sourcePath)) {
                // Create a Path object for the backup folder
                Path backupPath = Paths.get(backupFolderPath);

                // Create the backup folder if it doesn't exist
                if (!Files.exists(backupPath)) {
                    Files.createDirectories(backupPath);
                }

                // Create a Path object for the backup world folder
                Path backupWorldPath = backupPath.resolve(worldName + "_backup");

                // Copy the source world folder to the backup folder
                Files.walk(sourcePath)
                .forEach(source -> {
                        try {
                            Path target = backupWorldPath.resolve(sourcePath.relativize(source));
                            Files.copy(source, target);
                            System.out.println("Copied: " + source + " to " + target);
                        } catch (IOException e) {
                            System.err.println("Failed to copy: " + e.getMessage());
                        }
                    });

                System.out.println("Backup of " + worldName + " created successfully.");
            } else {
                System.out.println("Source world folder does not exist.");
            }
        } catch (IOException e) {
            System.out.println("Error creating backup: " + e.getMessage());
        }
    }

    public static void main(String args[])
    {
        String worldName = "Bedrock level";

        String world = "../worlds/" + worldName;

        String backupsFolder = "../backups/";

        int APIPort = 3000;
        
        try {
            API server = new API(APIPort);
            
            server.start();
        } catch (IOException e) {
            System.out.println("Error starting HTTP server: " + e.getMessage());
        }
        
        //System.out.println("Creating backup of " + worldName);

        //createBackup(world, worldName, backupsFolder);
    }
}
