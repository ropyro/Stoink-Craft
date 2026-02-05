package com.stoinkcraft.serialization;

import org.bukkit.Bukkit;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;

public final class BackupManager {

    public static final int MAX_BACKUPS = 3;

    public static final String BACKUP_PATTERN = ".backup.";

    private BackupManager() {
    }

    public static boolean createRotatingBackup(File file) {
        if (!file.exists()) {
            return true;
        }

        try {
            for (int i = MAX_BACKUPS - 1; i >= 1; i--) {
                File older = getBackupFile(file, i);
                File newer = getBackupFile(file, i + 1);

                if (older.exists()) {
                    if (i == MAX_BACKUPS - 1) {
                        newer.delete();
                    }
                    Files.move(older.toPath(), newer.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
            }

            File backup1 = getBackupFile(file, 1);
            Files.copy(file.toPath(), backup1.toPath(), StandardCopyOption.REPLACE_EXISTING);

            return true;

        } catch (IOException e) {
            Bukkit.getLogger().log(Level.WARNING, "Failed to create rotating backup for: " + file.getName(), e);
            return false;
        }
    }

    public static File findMostRecentBackup(File file) {
        for (int i = 1; i <= MAX_BACKUPS; i++) {
            File backup = getBackupFile(file, i);
            if (backup.exists()) {
                return backup;
            }
        }

        File legacyBackup = new File(file.getParentFile(), file.getName() + StorageConstants.BACKUP_SUFFIX);
        if (legacyBackup.exists()) {
            return legacyBackup;
        }

        return null;
    }

    public static boolean migrateLegacyBackup(File file) {
        File legacyBackup = new File(file.getParentFile(), file.getName() + StorageConstants.BACKUP_SUFFIX);

        if (!legacyBackup.exists()) {
            return false;
        }

        File backup1 = getBackupFile(file, 1);
        if (backup1.exists()) {
            legacyBackup.delete();
            return false;
        }

        try {
            Files.move(legacyBackup.toPath(), backup1.toPath(), StandardCopyOption.REPLACE_EXISTING);
            Bukkit.getLogger().info("Migrated legacy backup to rotating scheme: " + file.getName());
            return true;
        } catch (IOException e) {
            Bukkit.getLogger().log(Level.WARNING, "Failed to migrate legacy backup: " + file.getName(), e);
            return false;
        }
    }

    public static void deleteAllBackups(File file) {
        for (int i = 1; i <= MAX_BACKUPS; i++) {
            File backup = getBackupFile(file, i);
            if (backup.exists()) {
                backup.delete();
            }
        }

        File legacyBackup = new File(file.getParentFile(), file.getName() + StorageConstants.BACKUP_SUFFIX);
        if (legacyBackup.exists()) {
            legacyBackup.delete();
        }
    }

    public static File getBackupFile(File file, int number) {
        return new File(file.getParentFile(), file.getName() + BACKUP_PATTERN + number);
    }

    public static int countBackups(File file) {
        int count = 0;
        for (int i = 1; i <= MAX_BACKUPS; i++) {
            if (getBackupFile(file, i).exists()) {
                count++;
            }
        }
        return count;
    }
}
