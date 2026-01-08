package com.stoinkcraft.earning.collections;

import com.stoinkcraft.earning.jobsites.JobSiteType;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public enum CollectionType {

    // ==================== FARMLAND - CROPS ====================
    WHEAT("wheat", "Wheat", JobSiteType.FARMLAND, Material.WHEAT,
            Set.of(Material.WHEAT), Set.of()),
    CARROT("carrot", "Carrot", JobSiteType.FARMLAND, Material.CARROT,
            Set.of(Material.CARROTS), Set.of()),
    POTATO("potato", "Potato", JobSiteType.FARMLAND, Material.POTATO,
            Set.of(Material.POTATOES), Set.of()),
    BEETROOT("beetroot", "Beetroot", JobSiteType.FARMLAND, Material.BEETROOT,
            Set.of(Material.BEETROOTS), Set.of()),
    HONEYCOMB("honeycomb", "Honeycomb", JobSiteType.FARMLAND, Material.HONEYCOMB,
            Set.of(Material.HONEYCOMB), Set.of()),

    // ==================== FARMLAND - MOBS ====================
    COW("cow", "Cow", JobSiteType.FARMLAND, Material.BEEF,
            Set.of(), Set.of(EntityType.COW)),
    SHEEP("sheep", "Sheep", JobSiteType.FARMLAND, Material.WHITE_WOOL,
            Set.of(), Set.of(EntityType.SHEEP)),
    PIG("pig", "Pig", JobSiteType.FARMLAND, Material.PORKCHOP,
            Set.of(), Set.of(EntityType.PIG)),
    CHICKEN("chicken", "Chicken", JobSiteType.FARMLAND, Material.CHICKEN,
            Set.of(), Set.of(EntityType.CHICKEN)),

    // ==================== QUARRY - ORES ====================
    COAL("coal", "Coal", JobSiteType.QUARRY, Material.COAL,
            Set.of(Material.COAL_ORE, Material.DEEPSLATE_COAL_ORE), Set.of()),
    IRON("iron", "Iron", JobSiteType.QUARRY, Material.RAW_IRON,
            Set.of(Material.IRON_ORE, Material.DEEPSLATE_IRON_ORE), Set.of()),
    COPPER("copper", "Copper", JobSiteType.QUARRY, Material.RAW_COPPER,
            Set.of(Material.COPPER_ORE, Material.RAW_COPPER_BLOCK, Material.COPPER_BLOCK), Set.of()),
    GOLD("gold", "Gold", JobSiteType.QUARRY, Material.RAW_GOLD,
            Set.of(Material.GOLD_ORE, Material.DEEPSLATE_GOLD_ORE, Material.NETHER_GOLD_ORE), Set.of()),
    DIAMOND("diamond", "Diamond", JobSiteType.QUARRY, Material.DIAMOND,
            Set.of(Material.DIAMOND_ORE, Material.DEEPSLATE_DIAMOND_ORE), Set.of()),
    EMERALD("emerald", "Emerald", JobSiteType.QUARRY, Material.EMERALD,
            Set.of(Material.EMERALD_ORE, Material.DEEPSLATE_EMERALD_ORE), Set.of()),
    LAPIS("lapis", "Lapis Lazuli", JobSiteType.QUARRY, Material.LAPIS_LAZULI,
            Set.of(Material.LAPIS_ORE, Material.DEEPSLATE_LAPIS_ORE), Set.of()),
    QUARTZ("quartz", "Quartz", JobSiteType.QUARRY, Material.QUARTZ,
            Set.of(Material.NETHER_QUARTZ_ORE), Set.of()),
    ANCIENT_DEBRIS("ancient_debris", "Ancient Debris", JobSiteType.QUARRY, Material.ANCIENT_DEBRIS,
            Set.of(Material.ANCIENT_DEBRIS), Set.of()),
    STONE("stone", "Stone", JobSiteType.QUARRY, Material.STONE,
            Set.of(Material.COBBLESTONE, Material.STONE, Material.GRANITE, Material.DIORITE,
                    Material.ANDESITE, Material.DEEPSLATE, Material.NETHERRACK), Set.of()),

    // ==================== GRAVEYARD - UNDEAD ====================
    ZOMBIE("zombie", "Zombie", JobSiteType.GRAVEYARD, Material.ZOMBIE_HEAD,
            Set.of(), Set.of(EntityType.ZOMBIE)),
    SKELETON("skeleton", "Skeleton", JobSiteType.GRAVEYARD, Material.SKELETON_SKULL,
            Set.of(), Set.of(EntityType.SKELETON)),
    HUSK("husk", "Husk", JobSiteType.GRAVEYARD, Material.SAND,
            Set.of(), Set.of(EntityType.HUSK)),
    STRAY("stray", "Stray", JobSiteType.GRAVEYARD, Material.POWDER_SNOW_BUCKET,
            Set.of(), Set.of(EntityType.STRAY)),
    ZOMBIE_VILLAGER("zombie_villager", "Zombie Villager", JobSiteType.GRAVEYARD, Material.EMERALD,
            Set.of(), Set.of(EntityType.ZOMBIE_VILLAGER)),
    DROWNED("drowned", "Drowned", JobSiteType.GRAVEYARD, Material.TRIDENT,
            Set.of(), Set.of(EntityType.DROWNED)),
    WITHER_SKELETON("wither_skeleton", "Wither Skeleton", JobSiteType.GRAVEYARD, Material.WITHER_SKELETON_SKULL,
            Set.of(), Set.of(EntityType.WITHER_SKELETON));

    private final String id;
    private final String displayName;
    private final JobSiteType jobSiteType;
    private final Material icon;
    private final Set<Material> blockMaterials; // Block materials that contribute to this collection
    private final Set<EntityType> entityTypes;  // Entity types that contribute to this collection

    CollectionType(String id, String displayName, JobSiteType jobSiteType, Material icon,
                   Set<Material> blockMaterials, Set<EntityType> entityTypes) {
        this.id = id;
        this.displayName = displayName;
        this.jobSiteType = jobSiteType;
        this.icon = icon;
        this.blockMaterials = blockMaterials;
        this.entityTypes = entityTypes;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public JobSiteType getJobSiteType() {
        return jobSiteType;
    }

    public Material getIcon() {
        return icon;
    }

    public Set<Material> getBlockMaterials() {
        return blockMaterials;
    }

    public Set<EntityType> getEntityTypes() {
        return entityTypes;
    }

    /**
     * Find collection type from a block material within a specific jobsite
     */
    public static @Nullable CollectionType fromBlockMaterial(Material material, JobSiteType jobSiteType) {
        for (CollectionType type : values()) {
            if (type.jobSiteType == jobSiteType && type.blockMaterials.contains(material)) {
                return type;
            }
        }
        return null;
    }

    /**
     * Find collection type from an entity type within a specific jobsite
     */
    public static @Nullable CollectionType fromEntityType(EntityType entityType, JobSiteType jobSiteType) {
        for (CollectionType type : values()) {
            if (type.jobSiteType == jobSiteType && type.entityTypes.contains(entityType)) {
                return type;
            }
        }
        return null;
    }

    /**
     * Get all collection types for a specific jobsite
     */
    public static List<CollectionType> getByJobSiteType(JobSiteType jobSiteType) {
        return Arrays.stream(values())
                .filter(type -> type.jobSiteType == jobSiteType)
                .toList();
    }

    /**
     * Find collection type by ID
     */
    public static @Nullable CollectionType fromId(String id) {
        for (CollectionType type : values()) {
            if (type.id.equalsIgnoreCase(id)) {
                return type;
            }
        }
        return null;
    }
}