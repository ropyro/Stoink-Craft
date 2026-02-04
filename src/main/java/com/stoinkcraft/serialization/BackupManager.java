package com.stoinkcraft.serialization;

import org.bukkit.Bukkit;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;

/**
 * Manages rotating backups for storage files.
 * <p>
 * Keeps up to 3 rotating backups:
 * <ul>
 *   <li>{@code *.backup.1} - Most recent backup</li>
 *   <li>{@code *.backup.2} - Second most recent</li>
 *   <li>{@code *.backup.3} - Oldest backup</li>
 * </ul>
 */
public final class BackupManager {

    /** Maximum number of rotating backups to keep */
    public static final int MAX_BACKUPS = 3;

    /** Backup file suffix pattern (e.g., ".backup.1") */
    public static final String BACKUP_PATTERN = ".backup.";

    private BackupManager() {
        // Utility class - prevent instantiation
    }

    /**
     * Creates a rotating backup of the specified file.
     * <p>
     * Shifts existing backups: .backup.2 -> .backup.3, .backup.1 -> .backup.2,
     * then creates a new .backup.1 from the current file.
     *
     * @param file The file to back up
     * @return true if backup was created successfully, false otherwise
     */
    public static boolean createRotatingBackup(File file) {
        if (!file.exists()) {
            return true; // Nothing to back up
        }

        try {
            // Shift existing backups (oldest gets deleted)
            for (int i = MAX_BACKUPS - 1; i >= 1; i--) {
                File older = getBackupFile(file, i);
                File newer = getBackupFile(file, i + 1);

                if (older.exists()) {
                    if (i == MAX_BACKUPS - 1) {
                        // Delete the oldest backup
                        newer.delete();
                    }
                    Files.move(older.toPath(), newer.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
            }

            // Create new .backup.1 from current file
            File backup1 = getBackupFile(file, 1);
            Files.copy(file.toPath(), backup1.toPath(), StandardCopyOption.REPLACE_EXISTING);

            return true;

        } catch (IOException e) {
            Bukkit.getLogger().log(Level.WARNING, "Failed to create rotating backup for: " + file.getName(), e);
            return false;
        }
    }

    /**
     * Finds the most recent available backup file.
     *
     * @param file The original file to find backups for
     * @return The most recent backup file, or null if no backups exist
     */
    public static File findMostRecentBackup(File file) {
        // Check rotating backups first (most recent = .backup.1)
        for (int i = 1; i <= MAX_BACKUPS; i++) {
            File backup = getBackupFile(file, i);
            if (backup.exists()) {
                return backup;
            }
        }

        // Fall back to legacy single backup
        File legacyBackup = new File(file.getParentFile(), file.getName() + StorageConstants.BACKUP_SUFFIX);
        if (legacyBackup.exists()) {
            return legacyBackup;
        }

        return null;
    }

    /**
     * Migrates legacy single .backup files to the new rotating scheme.
     * <p>
     * If a legacy .backup file exists and no rotating backups exist,
     * the legacy file is moved to .backup.1.
     *
     * @param file The original file to check for legacy backups
     * @return true if migration was performed, false if not needed or failed
     */
    public static boolean migrateLegacyBackup(File file) {
        File legacyBackup = new File(file.getParentFile(), file.getName() + StorageConstants.BACKUP_SUFFIX);

        if (!legacyBackup.exists()) {
            return false; // No legacy backup to migrate
        }

        File backup1 = getBackupFile(file, 1);
        if (backup1.exists()) {
            // Already have rotating backups, delete legacy
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

    /**
     * Cleans up all backups for a file (used when deleting data permanently).
     *
     * @param file The original file whose backups should be deleted
     */
    public static void deleteAllBackups(File file) {
        // Delete rotating backups
        for (int i = 1; i <= MAX_BACKUPS; i++) {
            File backup = getBackupFile(file, i);
            if (backup.exists()) {
                backup.delete();
            }
        }

        // Delete legacy backup if exists
        File legacyBackup = new File(file.getParentFile(), file.getName() + StorageConstants.BACKUP_SUFFIX);
        if (legacyBackup.exists()) {
            legacyBackup.delete();
        }
    }

    /**
     * Gets the backup file for a specific backup number.
     *
     * @param file The original file
     * @param number The backup number (1 = most recent, 3 = oldest)
     * @return The backup file path
     */
    public static File getBackupFile(File file, int number) {
        return new File(file.getParentFile(), file.getName() + BACKUP_PATTERN + number);
    }

    /**
     * Counts how many backups exist for a file.
     *
     * @param file The original file
     * @return The number of existing backups (0-3)
     */
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
