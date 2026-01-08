package com.stoinkcraft.earning.collections;

import com.stoinkcraft.earning.jobsites.JobSite;
import com.stoinkcraft.earning.jobsites.JobSiteType;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.AbstractItem;
import xyz.xenondevs.invui.item.impl.SimpleItem;
import xyz.xenondevs.invui.window.Window;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * Reusable GUI for viewing collections at any JobSite.
 * Call from your JobSite's main menu.
 */
public class CollectionsGui {

    private final JobSite jobSite;
    private final Player opener;
    private final Runnable backAction;
    private Window currentWindow;

    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getNumberInstance(Locale.US);

    // ==================== Color Constants ====================
    private static final String HEADER = "§8§l» §6§l";
    private static final String SUB_HEADER = "§8§l» §e";
    private static final String BULLET = " §7• ";
    private static final String CHECKMARK = "§a✔ ";
    private static final String CROSS = "§c✖ ";
    private static final String ARROW = "§e▶ ";
    private static final String DIVIDER = " ";

    /**
     * Create a new CollectionGui
     *
     * @param jobSite    The jobsite to show collections for
     * @param opener     The player viewing the GUI
     * @param backAction Action to run when back button is clicked (typically reopens main menu)
     */
    public CollectionsGui(JobSite jobSite, Player opener, Runnable backAction) {
        this.jobSite = jobSite;
        this.opener = opener;
        this.backAction = backAction;
    }

    // ==================== Collection List ====================

    /**
     * Open the collection list showing all collections for this jobsite
     */
    public void openCollectionList() {
        List<CollectionType> collections = CollectionType.getByJobSiteType(jobSite.getType());

        Gui.Builder.Normal builder = Gui.normal()
                .setStructure(
                        "# # # # ? # # # #",
                        ". . . . . . . . .",
                        ". . . . . . . . .",
                        "# # # # # # # < #"
                )
                .addIngredient('#', filler())
                .addIngredient('?', createCollectionListHelp())
                .addIngredient('<', backButton(backAction));

        Gui gui = builder.build();

        // Add collection items
        for (CollectionType type : collections) {
            gui.addItems(createCollectionListItem(type));
        }

        open(gui, "§8" + formatJobSiteName(jobSite.getType()) + " Collections");
    }

    private SimpleItem createCollectionListHelp() {
        int totalCollections = CollectionType.getByJobSiteType(jobSite.getType()).size();
        int completedCollections = (int) CollectionType.getByJobSiteType(jobSite.getType()).stream()
                .filter(type -> jobSite.getData().getCollectionLevel(type) >= CollectionRegistry.MAX_LEVEL)
                .count();

        return new SimpleItem(new ItemBuilder(Material.OAK_SIGN)
                .setDisplayName(HEADER + "Collections §8«")
                .addLoreLines(DIVIDER)
                .addLoreLines(SUB_HEADER + "Overview §8«")
                .addLoreLines(BULLET + "§fTotal Collections: §a" + totalCollections)
                .addLoreLines(BULLET + "§fCompleted: §a" + completedCollections + "§7/§f" + totalCollections)
                .addLoreLines(DIVIDER)
                .addLoreLines(SUB_HEADER + "How It Works §8«")
                .addLoreLines(BULLET + "§fCollect resources to §alevel up")
                .addLoreLines(BULLET + "§fEach level grants §eJobsite XP")
                .addLoreLines(BULLET + "§fMax level: §6" + CollectionRegistry.MAX_LEVEL)
                .addLoreLines(DIVIDER)
                .addLoreLines(ARROW + "Click a collection for details")
        );
    }

    private AbstractItem createCollectionListItem(CollectionType type) {
        return new AbstractItem() {
            @Override
            public ItemProvider getItemProvider() {
                long count = jobSite.getData().getCollectionCount(type);
                int level = jobSite.getData().getCollectionLevel(type);
                boolean maxed = level >= CollectionRegistry.MAX_LEVEL;

                ItemBuilder item = new ItemBuilder(type.getIcon());

                // Title with level indicator
                if (maxed) {
                    item.setDisplayName("§6§l" + type.getDisplayName() + " §7(§aMAX§7)");
                } else if (level > 0) {
                    item.setDisplayName("§e" + type.getDisplayName() + " §7(Lv. §a" + level + "§7)");
                } else {
                    item.setDisplayName("§e" + type.getDisplayName() + " §7(Lv. §70§7)");
                }

                item.addLoreLines(DIVIDER);

                // Progress section
                item.addLoreLines(SUB_HEADER + "Progress §8«");
                item.addLoreLines(BULLET + "§fLevel: §a" + level + "§7/§f" + CollectionRegistry.MAX_LEVEL);
                item.addLoreLines(BULLET + "§fCollected: §e" + NUMBER_FORMAT.format(count));

                if (!maxed) {
                    long nextThreshold = CollectionRegistry.getThresholdForLevel(level + 1);
                    item.addLoreLines(BULLET + "§fNext Level: §e" + NUMBER_FORMAT.format(nextThreshold));
                    item.addLoreLines(BULLET + createProgressBar(count, level));
                } else {
                    item.addLoreLines(BULLET + "§a§lCOMPLETED!");
                }

                item.addLoreLines(DIVIDER);
                item.addLoreLines(ARROW + "Click to view all levels");

                return item;
            }

            @Override
            public void handleClick(@NotNull ClickType click, @NotNull Player p, @NotNull InventoryClickEvent e) {
                openCollectionDetail(type);
            }
        };
    }

    // ==================== Collection Detail ====================

    /**
     * Open detailed view for a specific collection showing all 27 levels
     */
    private void openCollectionDetail(CollectionType type) {
        long count = jobSite.getData().getCollectionCount(type);
        int currentLevel = jobSite.getData().getCollectionLevel(type);

        Gui.Builder.Normal builder = Gui.normal()
                .setStructure(
                        "# # # # ? # # # #",
                        "# # # # S # # # #",
                        ". . . . . . . . .",
                        ". . . . . . . . .",
                        ". . . . . . . . .",
                        "# # # # # # # < #"
                )
                .addIngredient('#', filler())
                .addIngredient('?', createCollectionDetailHelp(type))
                .addIngredient('S', createSummaryItem(type))
                .addIngredient('<', backButton(this::openCollectionList));

        Gui gui = builder.build();

        // Add level items (1-27, fits in 3 rows of 9)
        for (int level = 1; level <= CollectionRegistry.MAX_LEVEL; level++) {
            gui.addItems(createLevelItem(type, level, currentLevel, count));
        }

        open(gui, "§8" + type.getDisplayName() + " Collection");
    }

    private SimpleItem createCollectionDetailHelp(CollectionType type) {
        return new SimpleItem(new ItemBuilder(Material.OAK_SIGN)
                .setDisplayName(HEADER + type.getDisplayName() + " Collection §8«")
                .addLoreLines(DIVIDER)
                .addLoreLines(SUB_HEADER + "Level Rewards §8«")
                .addLoreLines(BULLET + "§fEach level grants §aJobsite XP")
                .addLoreLines(BULLET + "§fXP scales with level")
                .addLoreLines(BULLET + "§fMax Level: §6" + CollectionRegistry.MAX_LEVEL)
                .addLoreLines(DIVIDER)
                .addLoreLines(SUB_HEADER + "Legend §8«")
                .addLoreLines(BULLET + "§a■ §7Completed")
                .addLoreLines(BULLET + "§e■ §7In Progress")
                .addLoreLines(BULLET + "§8■ §7Locked")
        );
    }

    private SimpleItem createSummaryItem(CollectionType type) {
        long count = jobSite.getData().getCollectionCount(type);
        int level = jobSite.getData().getCollectionLevel(type);
        boolean maxed = level >= CollectionRegistry.MAX_LEVEL;

        // Calculate total XP earned from this collection
        int totalXpEarned = 0;
        for (int i = 1; i <= level; i++) {
            totalXpEarned += CollectionRegistry.getXpRewardForLevel(i);
        }

        ItemBuilder item = new ItemBuilder(type.getIcon());
        item.setDisplayName("§6§l" + type.getDisplayName());
        item.addLoreLines(DIVIDER);

        item.addLoreLines(SUB_HEADER + "Statistics §8«");
        item.addLoreLines(BULLET + "§fTotal Collected: §e" + NUMBER_FORMAT.format(count));
        item.addLoreLines(BULLET + "§fCurrent Level: §a" + level + "§7/§f" + CollectionRegistry.MAX_LEVEL);
        item.addLoreLines(BULLET + "§fXP Earned: §a" + NUMBER_FORMAT.format(totalXpEarned));
        item.addLoreLines(DIVIDER);

        if (maxed) {
            item.addLoreLines(CHECKMARK + "§a§lCOLLECTION COMPLETE!");
        } else {
            long remaining = CollectionRegistry.getRemainingToNextLevel(count);
            item.addLoreLines(SUB_HEADER + "Next Level §8«");
            item.addLoreLines(BULLET + "§fNeed: §e" + NUMBER_FORMAT.format(remaining) + " §7more");
            item.addLoreLines(BULLET + createProgressBar(count, level));
        }

        return new SimpleItem(item);
    }

    private SimpleItem createLevelItem(CollectionType type, int level, int currentLevel, long count) {
        long threshold = CollectionRegistry.getThresholdForLevel(level);
        int xpReward = CollectionRegistry.getXpRewardForLevel(level);
        boolean completed = level <= currentLevel;
        boolean isCurrentLevel = level == currentLevel + 1;

        Material material;
        String levelColor;

        if (completed) {
            material = Material.LIME_STAINED_GLASS_PANE;
            levelColor = "§a";
        } else if (isCurrentLevel) {
            material = Material.YELLOW_STAINED_GLASS_PANE;
            levelColor = "§e";
        } else {
            material = Material.GRAY_STAINED_GLASS_PANE;
            levelColor = "§8";
        }

        ItemBuilder item = new ItemBuilder(material);
        item.setDisplayName(levelColor + "Level " + level);
        item.addLoreLines(DIVIDER);

        // Threshold
        item.addLoreLines(SUB_HEADER + "Requirement §8«");
        item.addLoreLines(BULLET + "§fTotal: §e" + NUMBER_FORMAT.format(threshold));

        // XP Reward
        item.addLoreLines(DIVIDER);
        item.addLoreLines(SUB_HEADER + "Reward §8«");
        item.addLoreLines(BULLET + "§a+" + NUMBER_FORMAT.format(xpReward) + " " + formatJobSiteName(jobSite.getType()) + " XP");

        // Status
        item.addLoreLines(DIVIDER);
        if (completed) {
            item.addLoreLines(CHECKMARK + "§aCompleted!");
        } else if (isCurrentLevel) {
            double progress = CollectionRegistry.getProgressToNextLevel(count);
            int progressPercent = (int) (progress * 100);
            item.addLoreLines("§e⏳ In Progress §7(" + progressPercent + "%)");
            item.addLoreLines(BULLET + "§f" + NUMBER_FORMAT.format(count) + "§7/§f" + NUMBER_FORMAT.format(threshold));
            item.addLoreLines(BULLET + createSmallProgressBar(progress));
        } else {
            long needed = threshold - count;
            item.addLoreLines(CROSS + "§7Locked");
            item.addLoreLines(BULLET + "§7Need: §f" + NUMBER_FORMAT.format(Math.max(0, needed)) + " §7more");
        }

        return new SimpleItem(item);
    }

    // ==================== Progress Bars ====================

    private String createProgressBar(long count, int currentLevel) {
        double progress = CollectionRegistry.getProgressToNextLevel(count);
        int filled = (int) (progress * 10);
        int empty = 10 - filled;
        int percent = (int) (progress * 100);

        return "§a" + "▌".repeat(filled) + "§7" + "▌".repeat(empty) + " §f" + percent + "%";
    }

    private String createSmallProgressBar(double progress) {
        int filled = (int) (progress * 10);
        int empty = 10 - filled;

        return "§a" + "▌".repeat(filled) + "§7" + "▌".repeat(empty);
    }

    // ==================== Helper Items ====================

    private SimpleItem filler() {
        return new SimpleItem(new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).setDisplayName(" "));
    }

    private AbstractItem backButton(Runnable action) {
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

    // ==================== Utilities ====================

    private void open(Gui gui, String title) {
        currentWindow = Window.single()
                .setViewer(opener)
                .setTitle(title)
                .setGui(gui)
                .build();
        currentWindow.open();
    }

    private static String formatJobSiteName(JobSiteType type) {
        String name = type.name();
        return name.charAt(0) + name.substring(1).toLowerCase();
    }
}