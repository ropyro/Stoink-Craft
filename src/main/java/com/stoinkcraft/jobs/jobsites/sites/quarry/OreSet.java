package com.stoinkcraft.jobs.jobsites.sites.quarry;

import com.sk89q.worldedit.function.pattern.RandomPattern;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import com.stoinkcraft.jobs.jobsites.JobSite;
import org.bukkit.Material;

import java.util.Map;

public enum OreSet {

    MINING_BASICS(
            "Mining Basics",
            Material.IRON_PICKAXE,
            "Basic ores for new miners",
            0,
            Map.of(
                    BlockTypes.COBBLESTONE, 55,
                    BlockTypes.STONE, 15,
                    BlockTypes.COAL_ORE, 18,
                    BlockTypes.IRON_ORE, 10,
                    BlockTypes.DIAMOND_ORE, 2
            )
    ),

    STONE_VARIETIES(
            "Stone Varieties",
            Material.STONE,
            "Decorative stone blocks",
            5,
            Map.of(
                    BlockTypes.STONE, 35,
                    BlockTypes.COBBLESTONE, 15,
                    BlockTypes.GRANITE, 15,
                    BlockTypes.DIORITE, 15,
                    BlockTypes.ANDESITE, 15,
                    BlockTypes.COAL_ORE, 5
            )
    ),

    COPPER_COLLECTION(
            "Copper Collection",
            Material.COPPER_INGOT,
            "Copper-focused mining",
            10,
            Map.of(
                    BlockTypes.STONE, 40,
                    BlockTypes.COPPER_ORE, 35,
                    BlockTypes.RAW_COPPER_BLOCK, 15,
                    BlockTypes.DEEPSLATE_COPPER_ORE, 7,
                    BlockTypes.COPPER_BLOCK, 3
            )
    ),

    PRECIOUS_METALS(
            "Precious Metals",
            Material.GOLD_INGOT,
            "Gold and emerald riches",
            15,
            Map.of(
                    BlockTypes.STONE, 35,
                    BlockTypes.DEEPSLATE, 15,
                    BlockTypes.GOLD_ORE, 22,
                    BlockTypes.DEEPSLATE_GOLD_ORE, 10,
                    BlockTypes.IRON_ORE, 10,
                    BlockTypes.EMERALD_ORE, 8
            )
    ),

    DEEP_MINERALS(
            "Deep Minerals",
            Material.DEEPSLATE,
            "Deepslate ore variants",
            20,
            Map.of(
                    BlockTypes.DEEPSLATE, 30,
                    BlockTypes.COBBLED_DEEPSLATE, 15,
                    BlockTypes.DEEPSLATE_COAL_ORE, 15,
                    BlockTypes.DEEPSLATE_IRON_ORE, 15,
                    BlockTypes.DEEPSLATE_DIAMOND_ORE, 12,
                    BlockTypes.DEEPSLATE_LAPIS_ORE, 8,
                    BlockTypes.DEEPSLATE_REDSTONE_ORE, 5
            )
    ),

    NETHER_RESOURCES(
            "Nether Resources",
            Material.NETHERRACK,
            "Hellish treasures",
            26,
            Map.of(
                    BlockTypes.NETHERRACK, 30,
                    BlockTypes.BLACKSTONE, 15,
                    BlockTypes.NETHER_GOLD_ORE, 22,
                    BlockTypes.NETHER_QUARTZ_ORE, 20,
                    BlockTypes.GILDED_BLACKSTONE, 5,
                    BlockTypes.ANCIENT_DEBRIS, 8
            )
    );

    private final String displayName;
    private final Material icon;
    private final String description;
    private final int requiredLevel;
    private final Map<BlockType, Integer> blockWeights;

    OreSet(String displayName, Material icon, String description, int requiredLevel, Map<BlockType, Integer> blockWeights) {
        this.displayName = displayName;
        this.icon = icon;
        this.description = description;
        this.requiredLevel = requiredLevel;
        this.blockWeights = blockWeights;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Material getIcon() {
        return icon;
    }

    public String getDescription() {
        return description;
    }

    public int getRequiredLevel() {
        return requiredLevel;
    }

    public Map<BlockType, Integer> getBlockWeights() {
        return blockWeights;
    }

    public RandomPattern toRandomPattern() {
        RandomPattern pattern = new RandomPattern();
        blockWeights.forEach((blockType, weight) ->
                pattern.add(blockType.getDefaultState(), weight));
        return pattern;
    }

    public boolean isUnlocked(JobSite site) {
        return site.getLevel() >= requiredLevel;
    }
}
