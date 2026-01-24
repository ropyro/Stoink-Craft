package com.stoinkcraft.jobsites.sites;

import org.bukkit.Material;

/**
 * Defines purchase requirements for each job site type.
 * Farmland is free and unlocked by default.
 * Other job sites require specific Farmland levels and money to purchase.
 */
public enum JobSiteRequirements {

    FARMLAND(
            JobSiteType.FARMLAND,
            0,                      // Free
            0,                      // No requirement
            null,                   // No prerequisite
            Material.WHEAT,
            "Farmland",
            "Your starting job site for farming",
            "crops and raising animals."
    ),

    QUARRY(
            JobSiteType.QUARRY,
            10_000,                 // Cost
            5,                     // Required Farmland level
            JobSiteType.FARMLAND,   // Prerequisite
            Material.DIAMOND_PICKAXE,
            "Quarry",
            "Mine valuable ores and minerals.",
            "Watch out for spiders..."
    ),

    GRAVEYARD(
            JobSiteType.GRAVEYARD,
            25_000,                // Cost
            5,                     // Required Quarry level
            JobSiteType.QUARRY,   // Prerequisite (could also require Quarry)
            Material.SKELETON_SKULL,
            "Graveyard",
            "Summon and defeat undead mobs",
            "for souls and rewards."
    );

    private final JobSiteType type;
    private final int cost;
    private final int requiredPreReqLevel;
    private final JobSiteType prerequisite;
    private final Material icon;
    private final String displayName;
    private final String descriptionLine1;
    private final String descriptionLine2;

    JobSiteRequirements(
            JobSiteType type,
            int cost,
            int requiredFarmlandLevel,
            JobSiteType prerequisite,
            Material icon,
            String displayName,
            String descriptionLine1,
            String descriptionLine2
    ) {
        this.type = type;
        this.cost = cost;
        this.requiredPreReqLevel = requiredFarmlandLevel;
        this.prerequisite = prerequisite;
        this.icon = icon;
        this.displayName = displayName;
        this.descriptionLine1 = descriptionLine1;
        this.descriptionLine2 = descriptionLine2;
    }

    public JobSiteType getType() {
        return type;
    }

    public int getCost() {
        return cost;
    }

    public int getRequiredPreReqLevel() {
        return requiredPreReqLevel;
    }

    public JobSiteType getPrerequisite() {
        return prerequisite;
    }

    public Material getIcon() {
        return icon;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescriptionLine1() {
        return descriptionLine1;
    }

    public String getDescriptionLine2() {
        return descriptionLine2;
    }

    public boolean isFree() {
        return cost == 0;
    }

    /**
     * Get requirements for a specific job site type
     */
    public static JobSiteRequirements forType(JobSiteType type) {
        for (JobSiteRequirements req : values()) {
            if (req.type == type) {
                return req;
            }
        }
        return null;
    }
}
