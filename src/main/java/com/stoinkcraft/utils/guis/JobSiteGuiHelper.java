package com.stoinkcraft.utils.guis;

import com.stoinkcraft.StoinkCore;
import com.stoinkcraft.jobs.contracts.ActiveContract;
import com.stoinkcraft.jobs.contracts.ContractDefinition;
import com.stoinkcraft.jobs.contracts.rewards.CompositeReward;
import com.stoinkcraft.jobs.contracts.rewards.DescribableReward;
import com.stoinkcraft.jobs.contracts.rewards.Reward;
import com.stoinkcraft.jobs.jobsites.JobSite;
import com.stoinkcraft.jobs.jobsites.JobSiteUpgrade;
import com.stoinkcraft.jobs.jobsites.components.unlockable.Unlockable;
import com.stoinkcraft.jobs.jobsites.components.unlockable.UnlockableProgress;
import com.stoinkcraft.jobs.jobsites.components.unlockable.UnlockableState;
import com.stoinkcraft.utils.ChatUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.AbstractItem;
import xyz.xenondevs.invui.item.impl.SimpleItem;

import java.util.List;

/**
 * Centralized helper class for JobSite GUI components.
 * Provides consistent styling, reusable item builders, and upgrade display logic.
 */
public class JobSiteGuiHelper {

    // ==================== Theme Configuration ====================

    public enum Theme {
        FARMLAND("§6", "§e", "§6§l", "§e"),      // Orange/Yellow
        QUARRY("§6", "§e", "§6§l", "§e"),        // Orange/Yellow
        GRAVEYARD("§5", "§d", "§5§l", "§d");     // Purple/Pink

        public final String primary;
        public final String secondary;
        public final String headerColor;
        public final String subHeaderColor;

        Theme(String primary, String secondary, String headerColor, String subHeaderColor) {
            this.primary = primary;
            this.secondary = secondary;
            this.headerColor = headerColor;
            this.subHeaderColor = subHeaderColor;
        }
    }

    // ==================== Styling Constants ====================

    public static final String BULLET = " §7• ";
    public static final String CHECKMARK = "§a✔ ";
    public static final String CROSS = "§c✖ ";
    public static final String ARROW = "§e▶ ";
    public static final String DIVIDER = " ";

    public static String header(Theme theme, String text) {
        return "§8§l» " + theme.headerColor + text + " §8«";
    }

    public static String subHeader(Theme theme, String text) {
        return "§8§l» " + theme.subHeaderColor + text + " §8«";
    }

    public static String header(String text) {
        return header(Theme.FARMLAND, text);
    }

    public static String subHeader(String text) {
        return subHeader(Theme.FARMLAND, text);
    }

    // ==================== Common Items ====================

    public static SimpleItem filler() {
        return new SimpleItem(new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).setDisplayName(" "));
    }

    public static SimpleItem filler(Material material) {
        return new SimpleItem(new ItemBuilder(material).setDisplayName(" "));
    }

    public static AbstractItem backButton(Runnable action) {
        return new AbstractItem() {
            @Override
            public ItemProvider getItemProvider() {
                return new ItemBuilder(Material.ARROW)
                        .setDisplayName("§c« Back")
                        .addLoreLines("§7Return to previous menu");
            }

            @Override
            public void handleClick(@NotNull ClickType click, @NotNull Player p, @NotNull InventoryClickEvent e) {
                action.run();
            }
        };
    }

    public static AbstractItem menuButton(Material mat, String name, List<String> lore, Runnable action) {
        return new AbstractItem() {
            @Override
            public ItemProvider getItemProvider() {
                ItemBuilder item = new ItemBuilder(mat).setDisplayName(name);
                lore.forEach(item::addLoreLines);
                return item;
            }

            @Override
            public void handleClick(@NotNull ClickType click, @NotNull Player p, @NotNull InventoryClickEvent e) {
                action.run();
            }
        };
    }

    public static SimpleItem createSubMenuHelp(Theme theme, String title, String... lines) {
        ItemBuilder item = new ItemBuilder(Material.OAK_SIGN)
                .setDisplayName(header(theme, title))
                .addLoreLines(DIVIDER);

        for (String line : lines) {
            if (line == null || line.isEmpty()) continue;
            if (line.startsWith("§")) {
                item.addLoreLines(line);
            } else {
                item.addLoreLines("§7" + line);
            }
        }

        return new SimpleItem(item);
    }

    // ==================== Progress Bars ====================

    public static String createLevelBar(int current, int max) {
        if (max <= 0) return "§7" + "■".repeat(10);
        int filled = (int) ((current / (double) max) * 10);
        return "§a" + "■".repeat(filled) + "§7" + "■".repeat(10 - filled);
    }

    public static String createProgressBar(int current, int max) {
        if (max <= 0) return "§7" + "▌".repeat(10) + " §f0%";
        int percent = (int) ((current / (double) max) * 100);
        int bars = Math.min(10, percent / 10);
        return "§a" + "▌".repeat(bars) + "§7" + "▌".repeat(10 - bars) + " §f" + percent + "%";
    }

    public static String createProgressBarWithCount(int current, int max) {
        if (max <= 0) return "§7" + "▌".repeat(10) + " §f0/" + max;
        int filled = (int) ((current / (double) max) * 10);
        return "§a" + "▌".repeat(filled) + "§7" + "▌".repeat(10 - filled) + " §f" + current + "/" + max;
    }

    // ==================== Time Formatting ====================

    public static String formatTimeRemaining(long expiryTimestamp) {
        long millis = expiryTimestamp - System.currentTimeMillis();
        if (millis <= 0) return "§cExpired";

        long minutes = millis / 60000;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) return days + "d " + (hours % 24) + "h";
        if (hours > 0) return hours + "h " + (minutes % 60) + "m";
        return minutes + "m";
    }

    public static String formatDurationSeconds(long seconds) {
        if (seconds <= 0) return "0s";

        long minutes = seconds / 60;
        long hours = minutes / 60;

        seconds %= 60;
        minutes %= 60;

        if (hours > 0) {
            return hours + "h " + minutes + "m";
        }
        if (minutes > 0) {
            return minutes + "m " + seconds + "s";
        }
        return seconds + "s";
    }

    public static String toRoman(int num) {
        return switch (num) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            case 6 -> "VI";
            case 7 -> "VII";
            case 8 -> "VIII";
            case 9 -> "IX";
            case 10 -> "X";
            default -> String.valueOf(num);
        };
    }

    // ==================== Messages ====================

    public static void sendSuccess(Player p, String message) {
        ChatUtils.sendMessage(p, "§a✔ " + message);
    }

    public static void sendError(Player p, String message) {
        ChatUtils.sendMessage(p, "§c✖ " + message);
    }

    public static void sendInfo(Player p, String message) {
        ChatUtils.sendMessage(p, "§e" + message);
    }

    public static void sendInfo(Player p, String message, Theme theme) {
        ChatUtils.sendMessage(p, theme.secondary + message);
    }

    // ==================== Upgrade Item Builder ====================

    /**
     * Creates an upgrade item that properly handles the new scaling level requirements.
     * Shows current level, next level requirements, and level progression roadmap.
     */
    public static AbstractItem createUpgradeItem(
            JobSite jobSite,
            String upgradeId,
            Material icon,
            String displayName,
            String description,
            Theme theme,
            Runnable onPurchaseSuccess
    ) {
        return new AbstractItem() {
            @Override
            public ItemProvider getItemProvider() {
                JobSiteUpgrade upgrade = findUpgrade(jobSite, upgradeId);
                if (upgrade == null) {
                    return new ItemBuilder(Material.BARRIER).setDisplayName("§cUpgrade not found: " + upgradeId);
                }

                int currentLevel = jobSite.getData().getLevel(upgradeId);
                int maxLevel = upgrade.maxLevel();
                int jobsiteLevel = jobSite.getLevel();
                int nextLevel = currentLevel + 1;
                boolean maxed = currentLevel >= maxLevel;

                ItemBuilder item = new ItemBuilder(icon);
                item.setDisplayName(theme.primary + displayName);
                item.addLoreLines("§7" + description);
                item.addLoreLines(DIVIDER);

                // Current Progress
                item.addLoreLines(subHeader(theme, "Progress"));
                item.addLoreLines(BULLET + "§fLevel: §a" + currentLevel + "§7/§f" + maxLevel);
                item.addLoreLines(BULLET + createLevelBar(currentLevel, maxLevel));
                item.addLoreLines(DIVIDER);

                if (maxed) {
                    item.addLoreLines(CHECKMARK + "§aMax Level Reached!");
                } else {
                    // Next Level Requirements
                    int nextLevelCost = upgrade.cost(nextLevel);
                    int nextLevelRequiredJS = upgrade.getRequiredJobsiteLevel(nextLevel);

                    item.addLoreLines(subHeader(theme, "Next Level"));
                    item.addLoreLines(BULLET + "§fCost: §6$" + String.format("%,d", nextLevelCost));
                    item.addLoreLines(BULLET + "§fRequired Level: §e" + nextLevelRequiredJS);
                    item.addLoreLines(DIVIDER);

                    // Status check
                    if (jobsiteLevel < nextLevelRequiredJS) {
                        item.addLoreLines(CROSS + "§cYour level: §f" + jobsiteLevel + " §7(need §e" + nextLevelRequiredJS + "§7)");
                    } else if (!upgrade.canPurchase(jobSite, nextLevel)) {
                        item.addLoreLines(CROSS + "§cRequirements not met");
                    } else {
                        item.addLoreLines(CHECKMARK + "§aReady to upgrade!");
                    }

//                    // Show level roadmap for multi-level upgrades
//                    if (maxLevel > 1 && upgrade.jobsiteLevelIncrement() > 0) {
//                        item.addLoreLines(DIVIDER);
//                        item.addLoreLines(subHeader(theme, "Level Roadmap"));
//                        addLevelRoadmap(item, upgrade, currentLevel, jobsiteLevel);
//                    }

                    item.addLoreLines(DIVIDER);
                    item.addLoreLines(ARROW + "Click to upgrade");
                }

                return item;
            }

            @Override
            public void handleClick(@NotNull ClickType click, @NotNull Player p, @NotNull InventoryClickEvent e) {
                JobSiteUpgrade upgrade = findUpgrade(jobSite, upgradeId);
                if (upgrade == null) return;

                int currentLevel = jobSite.getData().getLevel(upgradeId);
                if (currentLevel >= upgrade.maxLevel()) {
                    sendInfo(p, displayName + " is already at max level!");
                    return;
                }

                if (jobSite.purchaseUpgrade(upgrade, p)) {
                    int newLevel = jobSite.getData().getLevel(upgradeId);
                    sendSuccess(p, displayName + " upgraded to level " + newLevel + "!");
                    if (onPurchaseSuccess != null) {
                        onPurchaseSuccess.run();
                    }
                } else {
                    sendUpgradePurchaseError(p, jobSite, upgrade);
                }

                notifyWindows();
            }
        };
    }

    /**
     * Adds a compact level roadmap showing upcoming level requirements.
     * Shows 3-4 upcoming levels with their JS requirements.
     */
    private static void addLevelRoadmap(ItemBuilder item, JobSiteUpgrade upgrade, int currentLevel, int currentJSLevel) {
        int maxLevel = upgrade.maxLevel();
        int showCount = Math.min(4, maxLevel - currentLevel);

        StringBuilder roadmap = new StringBuilder();
        for (int i = 1; i <= showCount; i++) {
            int targetLevel = currentLevel + i;
            if (targetLevel > maxLevel) break;

            int requiredJS = upgrade.getRequiredJobsiteLevel(targetLevel);
            String levelColor = currentJSLevel >= requiredJS ? "§a" : "§c";

            if (i > 1) roadmap.append(" §7→ ");
            roadmap.append(levelColor).append("L").append(targetLevel).append("§7@§e").append(requiredJS);
        }

        if (currentLevel + showCount < maxLevel) {
            roadmap.append(" §7→ ...");
        }

        item.addLoreLines(BULLET + roadmap.toString());
    }

    /**
     * Sends appropriate error message for failed upgrade purchase.
     */
    public static void sendUpgradePurchaseError(Player p, JobSite jobSite, JobSiteUpgrade upgrade) {
        int currentLevel = jobSite.getData().getLevel(upgrade.id());
        int nextLevel = currentLevel + 1;
        int requiredJSLevel = upgrade.getRequiredJobsiteLevel(nextLevel);
        int jobsiteLevel = jobSite.getLevel();
        int cost = upgrade.cost(nextLevel);

        if (jobsiteLevel < requiredJSLevel) {
            sendError(p, "You need " + jobSite.getType().name() + " Level " + requiredJSLevel + "! (Currently: " + jobsiteLevel + ")");
        } else if (!StoinkCore.getEconomy().has(p, cost)) {
            sendError(p, "Insufficient funds! Need $" + String.format("%,d", cost));
        } else if (!upgrade.canPurchase(jobSite, nextLevel)) {
            sendError(p, "Requirements not met!");
        } else {
            sendError(p, "Unable to purchase!");
        }
    }

    // ==================== Single-Purchase Unlock Item ====================

    /**
     * Creates an item for single-purchase unlocks (like crop/mob unlocks).
     * These are upgrades with maxLevel=1 that unlock features.
     */
    public static AbstractItem createUnlockItem(
            JobSite jobSite,
            String upgradeId,
            Material icon,
            String displayName,
            String description,
            Theme theme,
            Runnable onUnlock,
            Runnable onAlreadyUnlocked
    ) {
        return new AbstractItem() {
            @Override
            public ItemProvider getItemProvider() {
                JobSiteUpgrade upgrade = findUpgrade(jobSite, upgradeId);
                if (upgrade == null) {
                    return new ItemBuilder(Material.BARRIER).setDisplayName("§cUnlock not found: " + upgradeId);
                }

                boolean unlocked = jobSite.getData().getLevel(upgradeId) > 0;
                int jobsiteLevel = jobSite.getLevel();
                int requiredLevel = upgrade.getRequiredJobsiteLevel(1);

                ItemBuilder item = new ItemBuilder(icon);

                if (unlocked) {
                    item.setDisplayName("§a" + displayName + " §7(Unlocked)");
                    item.addLoreLines("§7" + description);
                    item.addLoreLines(DIVIDER);
                    item.addLoreLines(CHECKMARK + "§aUnlocked!");
                } else {
                    item.setDisplayName("§c" + displayName + " §8(Locked)");
                    item.addLoreLines("§7" + description);
                    item.addLoreLines(DIVIDER);
                    item.addLoreLines(subHeader(theme, "Requirements"));
                    item.addLoreLines(BULLET + "§fCost: §6$" + String.format("%,d", upgrade.cost(1)));
                    item.addLoreLines(BULLET + "§fRequired Level: §e" + requiredLevel);
                    item.addLoreLines(DIVIDER);

                    if (jobsiteLevel < requiredLevel) {
                        item.addLoreLines(CROSS + "§cYour level: §f" + jobsiteLevel);
                    } else if (!upgrade.canPurchase(jobSite, 1)) {
                        item.addLoreLines(CROSS + "§cPrevious unlock required");
                    } else {
                        item.addLoreLines(CHECKMARK + "§aReady to unlock!");
                    }

                    item.addLoreLines(DIVIDER);
                    item.addLoreLines(ARROW + "Click to unlock");
                }

                return item;
            }

            @Override
            public void handleClick(@NotNull ClickType click, @NotNull Player p, @NotNull InventoryClickEvent e) {
                JobSiteUpgrade upgrade = findUpgrade(jobSite, upgradeId);
                if (upgrade == null) return;

                boolean unlocked = jobSite.getData().getLevel(upgradeId) > 0;

                if (unlocked) {
                    if (onAlreadyUnlocked != null) {
                        onAlreadyUnlocked.run();
                    }
                    return;
                }

                if (jobSite.purchaseUpgrade(upgrade, p)) {
                    sendSuccess(p, displayName + " unlocked!");
                    if (onUnlock != null) {
                        onUnlock.run();
                    }
                } else {
                    sendUpgradePurchaseError(p, jobSite, upgrade);
                }
            }
        };
    }

    // ==================== Unlockable Structure Item ====================

    /**
     * Creates an auto-updating item for unlockable structures (Barn, Power Cell, Mausoleum, etc.)
     */
    public static Item createUnlockableStructureItem(
            JobSite jobSite,
            Unlockable unlockable,
            Theme theme,
            String description,
            Material lockedMaterial,
            Material buildingMaterial,
            Material unlockedMaterial
    ) {
        ClickableAutoUpdateItem item = new ClickableAutoUpdateItem(
                20, // 1 second update interval
                () -> buildUnlockableItemProvider(jobSite, unlockable, theme, description, lockedMaterial, buildingMaterial, unlockedMaterial),
                (player, event) -> handleUnlockableClick(jobSite, unlockable, player)
        );
        item.start();
        return item;
    }

    private static ItemProvider buildUnlockableItemProvider(
            JobSite jobSite,
            Unlockable unlockable,
            Theme theme,
            String description,
            Material lockedMaterial,
            Material buildingMaterial,
            Material unlockedMaterial
    ) {
        UnlockableState state = unlockable.getUnlockState();
        int jobsiteLevel = jobSite.getLevel();

        Material mat = switch (state) {
            case LOCKED -> lockedMaterial;
            case BUILDING -> buildingMaterial;
            case UNLOCKED -> unlockedMaterial;
        };

        ItemBuilder item = new ItemBuilder(mat);
        item.setDisplayName(theme.primary + unlockable.getDisplayName());
        item.addLoreLines("§7" + description);
        item.addLoreLines(DIVIDER);

        switch (state) {
            case LOCKED -> {
                item.addLoreLines(subHeader(theme, "Requirements"));
                item.addLoreLines(BULLET + "§fCost: §6$" + String.format("%,d", unlockable.getCost()));
                item.addLoreLines(BULLET + "§fRequired Level: §e" + unlockable.getRequiredJobsiteLevel());
                item.addLoreLines(BULLET + "§fBuild Time: §e" + ChatUtils.formatDuration(unlockable.getBuildTimeMillis()));
                item.addLoreLines(DIVIDER);

                if (jobsiteLevel < unlockable.getRequiredJobsiteLevel()) {
                    item.addLoreLines(CROSS + "§cYour level: §f" + jobsiteLevel);
                } else if (!unlockable.canUnlock()) {
                    item.addLoreLines(CROSS + "§cRequirements not met");
                } else {
                    item.addLoreLines(CHECKMARK + "§aReady to build!");
                }

                item.addLoreLines(DIVIDER);
                item.addLoreLines(ARROW + "Click to start construction");
            }
            case BUILDING -> {
                UnlockableProgress progress = jobSite.getData()
                        .getUnlockableProgress(unlockable.getUnlockableId());
                long remaining = progress.getRemainingSeconds();

                item.addLoreLines(subHeader(theme, "Under Construction"));
                item.addLoreLines(DIVIDER);
                item.addLoreLines(BULLET + "§fTime Remaining:");
                item.addLoreLines("   §6" + ChatUtils.formatDurationSeconds(remaining));
                item.addLoreLines(DIVIDER);
                item.addLoreLines(createConstructionProgressBar(unlockable, progress));
            }
            case UNLOCKED -> {
                item.addLoreLines(CHECKMARK + "§aConstruction Complete");
                item.addLoreLines(DIVIDER);
                item.addLoreLines(BULLET + "§fThis structure is operational");
            }
        }

        return item;
    }

    private static void handleUnlockableClick(JobSite jobSite, Unlockable unlockable, Player player) {
        UnlockableState state = unlockable.getUnlockState();
        String siteName = jobSite.getType().name();

        switch (state) {
            case LOCKED -> {
                if (jobSite.purchaseUnlockable(unlockable, player)) {
                    sendSuccess(player, unlockable.getDisplayName() + " construction started!");
                } else if (jobSite.getLevel() < unlockable.getRequiredJobsiteLevel()) {
                    sendError(player, "You need " + siteName + " Level " + unlockable.getRequiredJobsiteLevel() + "!");
                } else if (!StoinkCore.getEconomy().has(player, unlockable.getCost())) {
                    sendError(player, "You need $" + String.format("%,d", unlockable.getCost()) + "!");
                } else {
                    sendError(player, "Requirements not met!");
                }
            }
            case BUILDING -> {
                UnlockableProgress progress = jobSite.getData()
                        .getUnlockableProgress(unlockable.getUnlockableId());
                sendInfo(player, "Under construction - " + ChatUtils.formatDurationSeconds(progress.getRemainingSeconds()) + " remaining");
            }
            case UNLOCKED -> {
                sendInfo(player, unlockable.getDisplayName() + " is already built!");
            }
        }
    }

    private static String createConstructionProgressBar(Unlockable unlockable, UnlockableProgress progress) {
        long totalDuration = unlockable.getBuildTimeMillis();
        long remaining = progress.getRemainingSeconds();
        long elapsed = totalDuration - remaining;

        int percent = (int) Math.min(100, (elapsed * 100) / Math.max(1, totalDuration));
        int bars = percent / 10;

        String bar = "§a" + "▌".repeat(bars) + "§7" + "▌".repeat(10 - bars);
        return "   " + bar + " §f" + percent + "%";
    }

    // ==================== Contract Item ====================

    public static SimpleItem createContractItem(ActiveContract contract, Theme theme) {
        ContractDefinition def = contract.getDefinition();
        boolean completed = contract.isCompleted();

        ItemBuilder builder = new ItemBuilder(def.displayItem());

        // Title
        if (completed) {
            builder.setDisplayName(CHECKMARK + "§a" + def.displayName());
        } else {
            builder.setDisplayName(theme.secondary + def.displayName());
        }

        // Description
        builder.addLoreLines(DIVIDER);
        def.description().forEach(line -> builder.addLoreLines("§7" + line));

        // Progress
        builder.addLoreLines(DIVIDER);
        builder.addLoreLines(subHeader(theme, "Progress"));
        builder.addLoreLines(BULLET + "§f" + contract.getProgress() + "§7/§f" + contract.getTarget());
        builder.addLoreLines(BULLET + createProgressBar(contract.getProgress(), contract.getTarget()));

        // Expiration
        builder.addLoreLines(DIVIDER);
        builder.addLoreLines(subHeader(theme, "Time"));
        String timeText = contract.isWeekly() ? "Weekly - " : "Daily - ";
        builder.addLoreLines(BULLET + "§f" + timeText + "§e" + formatTimeRemaining(contract.getExpirationTime()));

        // Rewards
        builder.addLoreLines(DIVIDER);
        builder.addLoreLines(subHeader(theme, "Rewards"));
        addRewardLore(builder, def.reward());

        return new SimpleItem(builder);
    }

    private static void addRewardLore(ItemBuilder builder, Reward reward) {
        if (reward instanceof CompositeReward composite) {
            composite.getRewards().forEach(r -> addRewardLore(builder, r));
            return;
        }

        if (reward instanceof DescribableReward describable) {
            describable.getLore().forEach(line -> builder.addLoreLines(BULLET + "§f" + line));
        }
    }

    // ==================== Utility Methods ====================

    public static JobSiteUpgrade findUpgrade(JobSite jobSite, String id) {
        return jobSite.getUpgrades().stream()
                .filter(u -> u.id().equals(id))
                .findFirst()
                .orElse(null);
    }

    public static String formatName(String name) {
        if (name == null || name.isEmpty()) return "";
        return name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase().replace("_", " ");
    }

    public static String formatBlockName(String id) {
        String name = id.replace("minecraft:", "").replace("_", " ");
        String[] words = name.split(" ");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1))
                        .append(" ");
            }
        }
        return result.toString().trim();
    }
}