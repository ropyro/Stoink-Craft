package com.stoinkcraft.serialization;

import org.bukkit.Bukkit;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Standardized logging utility for storage operations.
 * <p>
 * Provides consistent logging levels:
 * <ul>
 *   <li>{@code severe()} - Data corruption, unrecoverable failure</li>
 *   <li>{@code warning()} - Recovered from backup, deprecated usage</li>
 *   <li>{@code info()} - Normal load/save operations</li>
 * </ul>
 */
public final class StorageLogger {

    private static final String PREFIX = "[Storage] ";
    private static final Logger logger = Bukkit.getLogger();

    private StorageLogger() {
        // Utility class - prevent instantiation
    }

    // ======= SEVERE (Data corruption, unrecoverable failure) =======

    /**
     * Logs a severe error - data corruption or unrecoverable failure.
     *
     * @param message The error message
     */
    public static void severe(String message) {
        logger.severe(PREFIX + message);
    }

    /**
     * Logs a severe error with exception - data corruption or unrecoverable failure.
     *
     * @param message The error message
     * @param throwable The exception that caused the error
     */
    public static void severe(String message, Throwable throwable) {
        logger.log(Level.SEVERE, PREFIX + message, throwable);
    }

    /**
     * Logs a severe error for enterprise operations.
     *
     * @param enterpriseName The name of the enterprise
     * @param operation The operation that failed (e.g., "save", "load")
     * @param throwable The exception that caused the error
     */
    public static void severeEnterprise(String enterpriseName, String operation, Throwable throwable) {
        logger.log(Level.SEVERE, PREFIX + "Failed to " + operation + " enterprise: " + enterpriseName, throwable);
    }

    /**
     * Logs a severe error for job site operations.
     *
     * @param jobSiteType The type of job site (e.g., "Skyrise", "Quarry")
     * @param enterpriseId The enterprise ID
     * @param throwable The exception that caused the error
     */
    public static void severeJobSite(String jobSiteType, String enterpriseId, Throwable throwable) {
        logger.log(Level.SEVERE, PREFIX + "Failed to load " + jobSiteType + " data for: " + enterpriseId, throwable);
    }

    // ======= WARNING (Recovered from backup, deprecated usage) =======

    /**
     * Logs a warning - recoverable issue or deprecated usage.
     *
     * @param message The warning message
     */
    public static void warning(String message) {
        logger.warning(PREFIX + message);
    }

    /**
     * Logs a warning with exception.
     *
     * @param message The warning message
     * @param throwable The exception related to the warning
     */
    public static void warning(String message, Throwable throwable) {
        logger.log(Level.WARNING, PREFIX + message, throwable);
    }

    /**
     * Logs a warning about backup recovery.
     *
     * @param fileName The name of the file that was restored from backup
     */
    public static void warnRestoredFromBackup(String fileName) {
        logger.warning(PREFIX + "Restored from backup: " + fileName);
    }

    /**
     * Logs a warning about deprecated API usage.
     *
     * @param deprecatedClass The deprecated class name
     * @param replacement The suggested replacement
     */
    public static void warnDeprecatedUsage(String deprecatedClass, String replacement) {
        logger.warning(PREFIX + "Deprecated usage: " + deprecatedClass + ". Use " + replacement + " instead.");
    }

    // ======= INFO (Normal load/save operations) =======

    /**
     * Logs an info message - normal operations.
     *
     * @param message The info message
     */
    public static void info(String message) {
        logger.info(PREFIX + message);
    }

    /**
     * Logs the count of saved enterprises.
     *
     * @param count The number of enterprises saved
     */
    public static void infoSavedEnterprises(int count) {
        logger.info(PREFIX + "Saved " + count + " enterprises with job sites.");
    }

    /**
     * Logs the count of loaded enterprises.
     *
     * @param loaded The number of enterprises successfully loaded
     * @param failed The number of enterprises that failed to load
     * @param withJobSites Whether job sites were loaded
     */
    public static void infoLoadedEnterprises(int loaded, int failed, boolean withJobSites) {
        String jobSiteInfo = withJobSites ? " with job sites" : " (job sites pending)";
        logger.info(PREFIX + "Loaded " + loaded + " enterprises" + jobSiteInfo + ". Failed: " + failed);
    }

    /**
     * Logs successful enterprise deletion.
     *
     * @param enterpriseName The name of the deleted enterprise
     */
    public static void infoDeletedEnterprise(String enterpriseName) {
        logger.info(PREFIX + "Deleted enterprise folder: " + enterpriseName);
    }

    /**
     * Logs an attempt to load from backup.
     *
     * @param itemType What is being loaded (e.g., "enterprise", "Skyrise data")
     */
    public static void infoAttemptingBackup(String itemType) {
        logger.info(PREFIX + "Attempting to load " + itemType + " from backup...");
    }

    /**
     * Logs successful backup load.
     */
    public static void infoBackupLoadSuccessful() {
        logger.info(PREFIX + "Successfully loaded from backup!");
    }

    // ======= MIGRATION =======

    /**
     * Logs migration-related info.
     *
     * @param message The migration message
     */
    public static void infoMigration(String message) {
        logger.info(PREFIX + "[Migration] " + message);
    }

    /**
     * Logs migration warning banner.
     *
     * @param yamlCount The number of YAML files to migrate
     */
    public static void warnMigrationNeeded(int yamlCount) {
        logger.warning("==============================================");
        logger.warning(PREFIX + "Found " + yamlCount + " YAML enterprises to migrate!");
        logger.warning(PREFIX + "Run '/se migrate' to convert to JSON");
        logger.warning("==============================================");
    }
}
