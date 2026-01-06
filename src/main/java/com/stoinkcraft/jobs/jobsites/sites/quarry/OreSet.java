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
            0, // required level
            Map.of(
                    BlockTypes.COBBLESTONE, 60,
                    BlockTypes.COAL_ORE, 25,
                    BlockTypes.IRON_ORE, 12,
                    BlockTypes.DIAMOND_ORE, 3
            )
    ),

    STONE_VARIETIES(
            "Stone Varieties",
            Material.STONE,
            "Decorative stone blocks",
            5,
            Map.of(
                    BlockTypes.STONE, 40,
                    BlockTypes.GRANITE, 20,
                    BlockTypes.DIORITE, 20,
                    BlockTypes.ANDESITE, 20
            )
    ),

    COPPER_COLLECTION(
            "Copper Collection",
            Material.COPPER_INGOT,
            "Copper-focused mining",
            10,
            Map.of(
                    BlockTypes.STONE, 50,
                    BlockTypes.COPPER_ORE, 30,
                    BlockTypes.RAW_COPPER_BLOCK, 15,
                    BlockTypes.COPPER_BLOCK, 5
            )
    ),

    PRECIOUS_METALS(
            "Precious Metals",
            Material.GOLD_INGOT,
            "Gold and valuable ores",
            15,
            Map.of(
                    BlockTypes.STONE, 45,
                    BlockTypes.GOLD_ORE, 25,
                    BlockTypes.IRON_ORE, 20,
                    BlockTypes.EMERALD_ORE, 10
            )
    ),

    DEEP_MINERALS(
            "Deep Minerals",
            Material.DEEPSLATE,
            "Deepslate ore variants",
            20,
            Map.of(
                    BlockTypes.DEEPSLATE, 40,
                    BlockTypes.DEEPSLATE_COAL_ORE, 20,
                    BlockTypes.DEEPSLATE_IRON_ORE, 20,
                    BlockTypes.DEEPSLATE_DIAMOND_ORE, 10,
                    BlockTypes.DEEPSLATE_LAPIS_ORE, 10
            )
    ),

    NETHER_RESOURCES(
            "Nether Resources",
            Material.NETHERRACK,
            "Nether-themed blocks",
            25,
            Map.of(
                    BlockTypes.NETHERRACK, 40,
                    BlockTypes.NETHER_GOLD_ORE, 25,
                    BlockTypes.NETHER_QUARTZ_ORE, 25,
                    BlockTypes.ANCIENT_DEBRIS, 10
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
