package com.stoinkcraft.earning.collections;

import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.earning.jobsites.JobSite;
import com.stoinkcraft.earning.jobsites.JobSiteType;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * Handles collection progression, XP rewards, and announcements.
 */
public class CollectionManager {

    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getNumberInstance(Locale.US);

    /**
     * Handle collection progress from a block break.
     * Call this from your BlockBreakEvent listener.
     *
     * @param enterprise The player's enterprise
     * @param jobSite The jobsite where the block was broken
     * @param blockMaterial The material of the block broken
     * @param amount The drop amount to credit
     * @param player The player who broke the block (for sound feedback)
     */
    public static void handleBlockCollection(Enterprise enterprise, JobSite jobSite,
                                             Material blockMaterial, int amount, Player player) {
        CollectionType type = CollectionType.fromBlockMaterial(blockMaterial, jobSite.getType());
        if (type == null) return;

        processCollection(enterprise, jobSite, type, amount, player);
    }

    /**
     * Handle collection progress from an entity kill.
     * Call this from your EntityDeathEvent listener.
     *
     * @param enterprise The player's enterprise
     * @param jobSite The jobsite where the entity was killed
     * @param entityType The type of entity killed
     * @param player The player who killed the entity (for sound feedback)
     */
    public static void handleEntityCollection(Enterprise enterprise, JobSite jobSite,
                                              EntityType entityType, Player player) {
        CollectionType type = CollectionType.fromEntityType(entityType, jobSite.getType());
        if (type == null) return;

        processCollection(enterprise, jobSite, type, 1, player);
    }

    /**
     * Handle collection progress directly by type.
     * Use this for special cases like honeycomb collection.
     *
     * @param enterprise The player's enterprise
     * @param jobSite The jobsite
     * @param type The collection type
     * @param amount Amount to add
     * @param player The player (for sound feedback, can be null)
     */
    public static void handleDirectCollection(Enterprise enterprise, JobSite jobSite,
                                              CollectionType type, int amount, @Nullable Player player) {
        processCollection(enterprise, jobSite, type, amount, player);
    }

    /**
     * Core processing logic for collection progress
     */
    private static void processCollection(Enterprise enterprise, JobSite jobSite,
                                          CollectionType type, int amount, @Nullable Player player) {
        // Add progress and get any level-ups
        List<Integer> levelsAchieved = jobSite.getData().addCollectionProgress(type, amount);

        // Process each level-up
        for (int level : levelsAchieved) {
            int xpReward = CollectionRegistry.getXpRewardForLevel(level);

            // Get the new total for the announcement
            long totalCollected = jobSite.getData().getCollectionCount(type);

            // Send enterprise announcement
            sendLevelUpAnnouncement(enterprise, type, level, xpReward, totalCollected);

            // Play sound to all online members at the jobsite

            // Award XP to the jobsite
            jobSite.getData().incrementXp(xpReward);
            playLevelUpSound(enterprise, jobSite);
        }
    }

    /**
     * Send a formatted level-up announcement to all enterprise members
     */
    private static void sendLevelUpAnnouncement(Enterprise enterprise, CollectionType type,
                                                int level, int xpReward, long totalCollected) {
        String jobSiteName = formatJobSiteName(type.getJobSiteType());
        String formattedTotal = NUMBER_FORMAT.format(totalCollected);
        String formattedXp = NUMBER_FORMAT.format(xpReward);

        enterprise.sendEnterpriseMessage(
                "",
                "§6§l" + type.getDisplayName() + " Collection Level Up!",
                "",
                "§7Reached level §a" + level + "§7!",
                "§7Total collected: §e" + formattedTotal,
                "§a+ " + formattedXp + " " + jobSiteName + " XP",
                ""
        );
    }

    /**
     * Play level-up sound to members at the jobsite
     */
    public static void playLevelUpSound(Enterprise enterprise, JobSite jobSite) {
        enterprise.getOnlineMembers().stream()
                .filter(p -> jobSite.contains(p.getLocation()))
                .forEach(p -> {
                    p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);
                    p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.5f, 1.0f);
                });
    }

    /**
     * Format jobsite type for display
     */
    private static String formatJobSiteName(JobSiteType type) {
        String name = type.name();
        return name.charAt(0) + name.substring(1).toLowerCase();
    }

    // ==================== Utility Methods for GUI ====================

    /**
     * Get formatted progress string for display
     * Example: "1,234 / 5,000"
     */
//    public static String getProgressString(long currentCount, int currentLevel) {
//        if (currentLevel >= CollectionRegistry.MAX_LEVEL) {
//            return "§a§lMAX LEVEL";
//        }
//
//        long nextThreshold = CollectionRegistry.getThresholdForLevel(currentLevel + 1);
//        return NUMBER_FORMAT.format(currentCount) + " / " + NUMBER_FORMAT.format(nextThreshold);
//    }

    /**
     * Get a progress bar string
     * Example: "§a||||||||§7||||||||||" (40% progress)
     */
    public static String getProgressBar(long currentCount, int barLength) {
        double progress = CollectionRegistry.getProgressToNextLevel(currentCount);
        int filledBars = (int) (progress * barLength);
        int emptyBars = barLength - filledBars;

        return "§a" + "▌".repeat(filledBars) + "§7" + "▌".repeat(emptyBars);
    }
}