package com.stoinkcraft.jobsites.sites.graveyard;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import java.util.Arrays;
import java.util.List;

public enum UndeadMobType {
    // Default types (no attunement cost, available immediately)
    ZOMBIE("Zombie", EntityType.ZOMBIE, 0, 1, Material.ZOMBIE_HEAD),
    SKELETON("Skeleton", EntityType.SKELETON, 0, 1, Material.SKELETON_SKULL),

    // Tier 2 (Level 8) - Early progression
    HUSK("Husk", EntityType.HUSK, 15, 8, Material.SAND),
    STRAY("Stray", EntityType.STRAY, 15, 8, Material.POWDER_SNOW_BUCKET),

    // Tier 3 (Level 16) - Mid progression
    ZOMBIE_VILLAGER("Zombie Villager", EntityType.ZOMBIE_VILLAGER, 40, 16, Material.EMERALD),
    DROWNED("Drowned", EntityType.DROWNED, 40, 16, Material.TRIDENT),

    // Tier 4 (Level 24) - Late progression
    WITHER_SKELETON("Wither Skeleton", EntityType.WITHER_SKELETON, 80, 24, Material.WITHER_SKELETON_SKULL),

    // Random (default state - cycles through unlocked types)
    RANDOM("Random", null, 0, 1, Material.SPAWNER);

    private final String displayName;
    private final EntityType entityType;
    private final int soulCost;
    private final int requiredLevel;
    private final Material icon;

    UndeadMobType(String displayName, EntityType entityType, int soulCost, int requiredLevel, Material icon) {
        this.displayName = displayName;
        this.entityType = entityType;
        this.soulCost = soulCost;
        this.requiredLevel = requiredLevel;
        this.icon = icon;
    }

    public String getDisplayName() {
        return displayName;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public int getSoulCost() {
        return soulCost;
    }

    public int getRequiredLevel() {
        return requiredLevel;
    }

    public Material getIcon() {
        return icon;
    }

    /**
     * Get a random mob type from the default pool (Zombie/Skeleton)
     */
    public static UndeadMobType getRandomDefault() {
        return Math.random() < 0.5 ? ZOMBIE : SKELETON;
    }

    /**
     * Check if this type requires attunement (costs souls)
     */
    public boolean requiresAttunement() {
        return soulCost > 0;
    }

    public static UndeadMobType getFromEntityType(EntityType entityType){
        for(UndeadMobType undeadMobType : UndeadMobType.values()){
            if(undeadMobType.getEntityType().equals(entityType)){
                return undeadMobType;
            }
        }
        return null;
    }

    /**
     * Get all types available at a given level
     */
    public static List<UndeadMobType> getAvailableTypes(int level) {
        return Arrays.stream(values())
                .filter(type -> type != RANDOM)
                .filter(type -> type.requiredLevel <= level)
                .toList();
    }
}