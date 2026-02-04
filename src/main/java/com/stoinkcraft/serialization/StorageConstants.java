package com.stoinkcraft.serialization;

import com.stoinkcraft.jobsites.sites.JobSiteType;

/**
 * Centralized constants for enterprise/jobsite storage paths and filenames.
 * All storage-related classes should reference these constants to maintain consistency.
 */
public final class StorageConstants {

    private StorageConstants() {
        // Utility class - prevent instantiation
    }

    // Directory names
    public static final String ENTERPRISES_DIR = "Enterprises";
    public static final String JOBSITES_SUBDIR = "jobsites";
    public static final String CONTRACTS_SUBDIR = "contracts";

    // Enterprise files
    public static final String ENTERPRISE_FILE = "enterprise.json";
    public static final String ENTERPRISE_YAML_FILE = "enterprise.yml";
    public static final String PRICE_HISTORY_YAML_FILE = "pricehistory.yml";

    // JobSite files
    public static final String SKYRISE_FILE = "skyrise.json";
    public static final String QUARRY_FILE = "quarry.json";
    public static final String FARMLAND_FILE = "farmland.json";
    public static final String GRAVEYARD_FILE = "graveyard.json";

    // Contract files
    public static final String CONTRACTS_FILE = "contracts.json";

    // Backup suffix (legacy single backup)
    public static final String BACKUP_SUFFIX = ".backup";

    // Schema versions for future migrations
    public static final int ENTERPRISE_SCHEMA_VERSION = 1;
    public static final int JOBSITE_SCHEMA_VERSION = 1;

    /**
     * Get the JSON filename for a specific job site type.
     *
     * @param type The job site type
     * @return The filename (e.g., "skyrise.json")
     */
    public static String getJobSiteFileName(JobSiteType type) {
        return switch (type) {
            case SKYRISE -> SKYRISE_FILE;
            case QUARRY -> QUARRY_FILE;
            case FARMLAND -> FARMLAND_FILE;
            case GRAVEYARD -> GRAVEYARD_FILE;
        };
    }
}
