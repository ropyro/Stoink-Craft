package com.stoinkcraft.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.logging.Logger;

/**
 * Configuration holder for structure-related settings.
 * Manages build times, costs, rewards, and other parameters for all structures.
 */
public class StructureConfig {

    private final Logger logger;

    // Barn
    private int barnRequiredLevel;
    private int barnCost;
    private long barnBuildTimeMillis;
    private int barnCompletionXp;
    private String barnSchematic;

    // BeeHive
    private int beehiveRequiredLevel;
    private int beehiveCost;
    private long beehiveBuildTimeMillis;
    private int beehiveCompletionXp;
    private String beehiveSchematic;

    // PowerCell
    private int powercellRequiredLevel;
    private int powercellCost;
    private long powercellBuildTimeMillis;
    private int powercellCompletionXp;
    private String powercellSchematic;
    private int powercellEffectCheckIntervalTicks;

    // Mausoleum
    private int mausoleumRequiredLevel;
    private int mausoleumCost;
    private long mausoleumBuildTimeMillis;
    private int mausoleumCompletionXp;
    private String mausoleumSchematic;

    // Mausoleum Horde
    private int mausoleumBaseHordeIntervalSeconds;
    private int mausoleumMinHordeIntervalSeconds;
    private int mausoleumHordeIntervalReductionPerLevel;
    private int mausoleumBaseHordeSize;
    private int mausoleumSpidersPerUpgrade;
    private int mausoleumMaxHordeSize;

    // Mausoleum Rewards
    private int mausoleumXpPerSpider;
    private int mausoleumMoneyPerSpider;

    public StructureConfig(JavaPlugin plugin, File configFile) {
        this.logger = plugin.getLogger();
        load(configFile);
    }

    private void load(File configFile) {
        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        // Barn
        barnRequiredLevel = config.getInt("structures.barn.required-level", 10);
        barnCost = config.getInt("structures.barn.cost", 50000);
        barnBuildTimeMillis = config.getLong("structures.barn.build-time-seconds", 900) * 1000;
        barnCompletionXp = config.getInt("structures.barn.completion-xp", 500);
        barnSchematic = config.getString("structures.barn.schematic", "barn.schem");

        // BeeHive
        beehiveRequiredLevel = config.getInt("structures.beehive.required-level", 20);
        beehiveCost = config.getInt("structures.beehive.cost", 150000);
        beehiveBuildTimeMillis = config.getLong("structures.beehive.build-time-seconds", 2700) * 1000;
        beehiveCompletionXp = config.getInt("structures.beehive.completion-xp", 2000);
        beehiveSchematic = config.getString("structures.beehive.schematic", "beehives.schem");

        // PowerCell
        powercellRequiredLevel = config.getInt("structures.powercell.required-level", 10);
        powercellCost = config.getInt("structures.powercell.cost", 75000);
        powercellBuildTimeMillis = config.getLong("structures.powercell.build-time-seconds", 1200) * 1000;
        powercellCompletionXp = config.getInt("structures.powercell.completion-xp", 750);
        powercellSchematic = config.getString("structures.powercell.schematic", "powercell.schem");
        powercellEffectCheckIntervalTicks = config.getInt("structures.powercell.effect-check-interval-ticks", 60);

        // Mausoleum
        mausoleumRequiredLevel = config.getInt("structures.mausoleum.required-level", 15);
        mausoleumCost = config.getInt("structures.mausoleum.cost", 175000);
        mausoleumBuildTimeMillis = config.getLong("structures.mausoleum.build-time-seconds", 1800) * 1000;
        mausoleumCompletionXp = config.getInt("structures.mausoleum.completion-xp", 1500);
        mausoleumSchematic = config.getString("structures.mausoleum.schematic", "mausoleum.schem");

        // Mausoleum Horde
        mausoleumBaseHordeIntervalSeconds = config.getInt("structures.mausoleum.horde.base-interval-seconds", 600);
        mausoleumMinHordeIntervalSeconds = config.getInt("structures.mausoleum.horde.min-interval-seconds", 180);
        mausoleumHordeIntervalReductionPerLevel = config.getInt("structures.mausoleum.horde.interval-reduction-per-level", 42);
        mausoleumBaseHordeSize = config.getInt("structures.mausoleum.horde.base-size", 6);
        mausoleumSpidersPerUpgrade = config.getInt("structures.mausoleum.horde.spiders-per-upgrade", 2);
        mausoleumMaxHordeSize = config.getInt("structures.mausoleum.horde.max-size", 30);

        // Mausoleum Rewards
        mausoleumXpPerSpider = config.getInt("structures.mausoleum.rewards.xp-per-spider", 12);
        mausoleumMoneyPerSpider = config.getInt("structures.mausoleum.rewards.money-per-spider", 40);

        validate();

        logger.info("Structure config loaded successfully");
    }

    private void validate() {
        // Barn validation
        if (barnRequiredLevel < 0) {
            logger.severe("Invalid barn required-level: " + barnRequiredLevel + " (must be >= 0)");
            throw new IllegalArgumentException("barn required-level must be >= 0");
        }
        if (barnCost < 0) {
            logger.severe("Invalid barn cost: " + barnCost + " (must be >= 0)");
            throw new IllegalArgumentException("barn cost must be >= 0");
        }
        if (barnBuildTimeMillis <= 0) {
            logger.severe("Invalid barn build-time-seconds: " + (barnBuildTimeMillis / 1000) + " (must be > 0)");
            throw new IllegalArgumentException("barn build-time-seconds must be > 0");
        }

        // BeeHive validation
        if (beehiveRequiredLevel < 0) {
            logger.severe("Invalid beehive required-level: " + beehiveRequiredLevel + " (must be >= 0)");
            throw new IllegalArgumentException("beehive required-level must be >= 0");
        }
        if (beehiveCost < 0) {
            logger.severe("Invalid beehive cost: " + beehiveCost + " (must be >= 0)");
            throw new IllegalArgumentException("beehive cost must be >= 0");
        }
        if (beehiveBuildTimeMillis <= 0) {
            logger.severe("Invalid beehive build-time-seconds: " + (beehiveBuildTimeMillis / 1000) + " (must be > 0)");
            throw new IllegalArgumentException("beehive build-time-seconds must be > 0");
        }

        // PowerCell validation
        if (powercellRequiredLevel < 0) {
            logger.severe("Invalid powercell required-level: " + powercellRequiredLevel + " (must be >= 0)");
            throw new IllegalArgumentException("powercell required-level must be >= 0");
        }
        if (powercellCost < 0) {
            logger.severe("Invalid powercell cost: " + powercellCost + " (must be >= 0)");
            throw new IllegalArgumentException("powercell cost must be >= 0");
        }
        if (powercellBuildTimeMillis <= 0) {
            logger.severe("Invalid powercell build-time-seconds: " + (powercellBuildTimeMillis / 1000) + " (must be > 0)");
            throw new IllegalArgumentException("powercell build-time-seconds must be > 0");
        }
        if (powercellEffectCheckIntervalTicks <= 0) {
            logger.severe("Invalid powercell effect-check-interval-ticks: " + powercellEffectCheckIntervalTicks + " (must be > 0)");
            throw new IllegalArgumentException("powercell effect-check-interval-ticks must be > 0");
        }

        // Mausoleum validation
        if (mausoleumRequiredLevel < 0) {
            logger.severe("Invalid mausoleum required-level: " + mausoleumRequiredLevel + " (must be >= 0)");
            throw new IllegalArgumentException("mausoleum required-level must be >= 0");
        }
        if (mausoleumCost < 0) {
            logger.severe("Invalid mausoleum cost: " + mausoleumCost + " (must be >= 0)");
            throw new IllegalArgumentException("mausoleum cost must be >= 0");
        }
        if (mausoleumBuildTimeMillis <= 0) {
            logger.severe("Invalid mausoleum build-time-seconds: " + (mausoleumBuildTimeMillis / 1000) + " (must be > 0)");
            throw new IllegalArgumentException("mausoleum build-time-seconds must be > 0");
        }

        // Mausoleum Horde validation
        if (mausoleumBaseHordeIntervalSeconds <= 0) {
            logger.severe("Invalid mausoleum base-interval-seconds: " + mausoleumBaseHordeIntervalSeconds + " (must be > 0)");
            throw new IllegalArgumentException("mausoleum base-interval-seconds must be > 0");
        }
        if (mausoleumMinHordeIntervalSeconds <= 0 || mausoleumMinHordeIntervalSeconds > mausoleumBaseHordeIntervalSeconds) {
            logger.severe("Invalid mausoleum min-interval-seconds: " + mausoleumMinHordeIntervalSeconds);
            throw new IllegalArgumentException("mausoleum min-interval-seconds must be > 0 and <= base-interval-seconds");
        }
        if (mausoleumBaseHordeSize <= 0) {
            logger.severe("Invalid mausoleum base-size: " + mausoleumBaseHordeSize + " (must be > 0)");
            throw new IllegalArgumentException("mausoleum base-size must be > 0");
        }
        if (mausoleumMaxHordeSize < mausoleumBaseHordeSize) {
            logger.severe("Invalid mausoleum max-size: " + mausoleumMaxHordeSize + " (must be >= base-size)");
            throw new IllegalArgumentException("mausoleum max-size must be >= base-size");
        }
    }

    // =============================================
    // BARN GETTERS
    // =============================================

    public int getBarnRequiredLevel() {
        return barnRequiredLevel;
    }

    public int getBarnCost() {
        return barnCost;
    }

    public long getBarnBuildTimeMillis() {
        return barnBuildTimeMillis;
    }

    public int getBarnCompletionXp() {
        return barnCompletionXp;
    }

    public String getBarnSchematic() {
        return barnSchematic;
    }

    // =============================================
    // BEEHIVE GETTERS
    // =============================================

    public int getBeehiveRequiredLevel() {
        return beehiveRequiredLevel;
    }

    public int getBeehiveCost() {
        return beehiveCost;
    }

    public long getBeehiveBuildTimeMillis() {
        return beehiveBuildTimeMillis;
    }

    public int getBeehiveCompletionXp() {
        return beehiveCompletionXp;
    }

    public String getBeehiveSchematic() {
        return beehiveSchematic;
    }

    // =============================================
    // POWERCELL GETTERS
    // =============================================

    public int getPowercellRequiredLevel() {
        return powercellRequiredLevel;
    }

    public int getPowercellCost() {
        return powercellCost;
    }

    public long getPowercellBuildTimeMillis() {
        return powercellBuildTimeMillis;
    }

    public int getPowercellCompletionXp() {
        return powercellCompletionXp;
    }

    public String getPowercellSchematic() {
        return powercellSchematic;
    }

    public int getPowercellEffectCheckIntervalTicks() {
        return powercellEffectCheckIntervalTicks;
    }

    // =============================================
    // MAUSOLEUM GETTERS
    // =============================================

    public int getMausoleumRequiredLevel() {
        return mausoleumRequiredLevel;
    }

    public int getMausoleumCost() {
        return mausoleumCost;
    }

    public long getMausoleumBuildTimeMillis() {
        return mausoleumBuildTimeMillis;
    }

    public int getMausoleumCompletionXp() {
        return mausoleumCompletionXp;
    }

    public String getMausoleumSchematic() {
        return mausoleumSchematic;
    }

    // =============================================
    // MAUSOLEUM HORDE GETTERS
    // =============================================

    public int getMausoleumBaseHordeIntervalSeconds() {
        return mausoleumBaseHordeIntervalSeconds;
    }

    public int getMausoleumMinHordeIntervalSeconds() {
        return mausoleumMinHordeIntervalSeconds;
    }

    public int getMausoleumHordeIntervalReductionPerLevel() {
        return mausoleumHordeIntervalReductionPerLevel;
    }

    public int getMausoleumBaseHordeSize() {
        return mausoleumBaseHordeSize;
    }

    public int getMausoleumSpidersPerUpgrade() {
        return mausoleumSpidersPerUpgrade;
    }

    public int getMausoleumMaxHordeSize() {
        return mausoleumMaxHordeSize;
    }

    // =============================================
    // MAUSOLEUM REWARDS GETTERS
    // =============================================

    public int getMausoleumXpPerSpider() {
        return mausoleumXpPerSpider;
    }

    public int getMausoleumMoneyPerSpider() {
        return mausoleumMoneyPerSpider;
    }
}
