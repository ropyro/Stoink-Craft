package com.stoinkcraft.jobs.jobsites.sites.quarry;

import com.stoinkcraft.StoinkCore;
import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.jobs.contracts.ActiveContract;
import com.stoinkcraft.jobs.contracts.ContractDefinition;
import com.stoinkcraft.jobs.contracts.rewards.CompositeReward;
import com.stoinkcraft.jobs.contracts.rewards.DescribableReward;
import com.stoinkcraft.jobs.contracts.rewards.Reward;
import com.stoinkcraft.jobs.jobsites.JobSiteType;
import com.stoinkcraft.jobs.jobsites.JobSiteUpgrade;
import com.stoinkcraft.jobs.jobsites.JobsiteLevelHelper;
import com.stoinkcraft.jobs.jobsites.components.unlockable.Unlockable;
import com.stoinkcraft.jobs.jobsites.components.unlockable.UnlockableProgress;
import com.stoinkcraft.jobs.jobsites.components.unlockable.UnlockableState;
import com.stoinkcraft.utils.ChatUtils;
import com.stoinkcraft.utils.guis.ClickableAutoUpdateItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.AbstractItem;
import xyz.xenondevs.invui.item.impl.SimpleItem;
import xyz.xenondevs.invui.window.Window;

import java.util.List;

public class QuarryGui {

    private final QuarrySite quarrySite;
    private final Player opener;
    private Window currentWindow;

    // ==================== Color Constants ====================
    private static final String HEADER = "§8§l» §6§l";
    private static final String SUB_HEADER = "§8§l» §e";
    private static final String BULLET = " §7• ";
    private static final String CHECKMARK = "§a✔ ";
    private static final String CROSS = "§c✖ ";
    private static final String ARROW = "§e▶ ";
    private static final String DIVIDER = " ";

    public QuarryGui(QuarrySite quarrySite, Player opener) {
        this.quarrySite = quarrySite;
        this.opener = opener;
    }

    // ==================== Main Menu ====================

    public void openWindow() {
        Gui gui = Gui.normal()
                .setStructure(
                        "# # # # ? # # # #",
                        "# # # O U P # # #",
                        "# # # # # # # # #",
                        "# # # # C # # # #",
                        "# # # # # # # # #"
                )
                .addIngredient('#', filler())
                .addIngredient('?', createHelpItem())
                .addIngredient('O', menuButton(Material.DIAMOND_ORE, "§6Ore Sets",
                        List.of("§7Choose which ores to mine", DIVIDER, ARROW + "Click to open"),
                        this::openOreSetMenu))
                .addIngredient('U', menuButton(Material.ANVIL, "§6Upgrades",
                        List.of("§7Upgrade your quarry", DIVIDER, ARROW + "Click to open"),
                        this::openUpgradeMenu))
                .addIngredient('P', menuButton(Material.BEACON, "§6Power Cell",
                        List.of("§7Manage the Power Cell", DIVIDER, ARROW + "Click to open"),
                        this::openPowerCellMenu))
                .addIngredient('C', menuButton(Material.GOLD_INGOT, "§6Contracts",
                        List.of("§7View your active contracts", DIVIDER, ARROW + "Click to open"),
                        this::openContractList))
                .build();

        open(gui, "§8Quarry Menu");
    }

    private SimpleItem createHelpItem() {
        int xp = quarrySite.getData().getXp();
        int level = JobsiteLevelHelper.getLevelFromXp(xp);
        int xpToNext = JobsiteLevelHelper.getXpToNextLevel(xp);
        long regenTime = quarrySite.getMineGenerator().getRemainingSeconds();

        return new SimpleItem(new ItemBuilder(Material.OAK_SIGN)
                .setDisplayName(HEADER + "Quarry Help §8«")
                .addLoreLines(DIVIDER)
                .addLoreLines(SUB_HEADER + "Quarry Status §8«")
                .addLoreLines(BULLET + "§fLevel: §a" + level)
                .addLoreLines(BULLET + "§fXP to next: §a" + String.format("%,d", xpToNext) + " XP")
                .addLoreLines(BULLET + "§fRegen in: §e" + formatTime(regenTime))
                .addLoreLines(BULLET + "§fOre Set: §b" + quarrySite.getData().getCurrentOreSet().getDisplayName())
                .addLoreLines(DIVIDER)
                .addLoreLines(SUB_HEADER + "How It Works §8«")
                .addLoreLines(BULLET + "§fMine ores to complete §acontracts")
                .addLoreLines(BULLET + "§fFind §dgeodes §ffor bonus XP!")
                .addLoreLines(BULLET + "§fQuarry regenerates periodically")
                .addLoreLines(DIVIDER)
                .addLoreLines(SUB_HEADER + "Power Cell §8«")
                .addLoreLines(BULLET + "§fBuild the Power Cell for §eHaste")
                .addLoreLines(BULLET + "§fUpgrade for stronger effects")
        );
    }

    // ==================== Ore Set Menu ====================

    private void openOreSetMenu() {
        Gui gui = Gui.normal()
                .setStructure(
                        "# # # # ? # # # #",
                        "# 1 2 3 4 5 6 # #",
                        "# # # # # # # < #"
                )
                .addIngredient('#', filler())
                .addIngredient('?', createSubMenuHelp("Ore Sets",
                        "Select which ore set to mine.",
                        "Different sets have different ores!"))
                .addIngredient('1', createOreSetItem(OreSet.MINING_BASICS))
                .addIngredient('2', createOreSetItem(OreSet.STONE_VARIETIES))
                .addIngredient('3', createOreSetItem(OreSet.COPPER_COLLECTION))
                .addIngredient('4', createOreSetItem(OreSet.PRECIOUS_METALS))
                .addIngredient('5', createOreSetItem(OreSet.DEEP_MINERALS))
                .addIngredient('6', createOreSetItem(OreSet.NETHER_RESOURCES))
                .addIngredient('<', backButton(this::openWindow))
                .build();

        open(gui, "§8Ore Sets");
    }

    private AbstractItem createOreSetItem(OreSet oreSet) {
        return new AbstractItem() {
            @Override
            public ItemProvider getItemProvider() {
                boolean unlocked = quarrySite.isOreSetUnlocked(oreSet);
                boolean selected = quarrySite.getData().getCurrentOreSet() == oreSet;
                int jobsiteLevel = quarrySite.getLevel();

                String unlockUpgradeId = quarrySite.getUnlockUpgradeId(oreSet);
                JobSiteUpgrade upgrade = unlockUpgradeId != null ? findUpgrade(unlockUpgradeId) : null;

                ItemBuilder item = new ItemBuilder(oreSet.getIcon());

                if (!unlocked) {
                    item.setDisplayName("§c" + oreSet.getDisplayName() + " §8(Locked)");
                    item.addLoreLines("§7" + oreSet.getDescription());
                    item.addLoreLines(DIVIDER);
                    item.addLoreLines(SUB_HEADER + "Requirements §8«");
                    item.addLoreLines(BULLET + "§fCost: §6$" + String.format("%,d", upgrade.cost(1)));
                    item.addLoreLines(BULLET + "§fRequired Level: §e" + oreSet.getRequiredLevel());
                    item.addLoreLines(DIVIDER);

                    if (jobsiteLevel < oreSet.getRequiredLevel()) {
                        item.addLoreLines(CROSS + "§cYour level: §f" + jobsiteLevel);
                    } else if (!upgrade.canUnlock(quarrySite)) {
                        item.addLoreLines(CROSS + "§cPrevious set not unlocked");
                    } else {
                        item.addLoreLines(CHECKMARK + "§aReady to unlock!");
                    }

                    item.addLoreLines(DIVIDER);
                    item.addLoreLines(SUB_HEADER + "Ore Contents §8«");
                    addOreContentsLore(item, oreSet);
                    item.addLoreLines(DIVIDER);
                    item.addLoreLines(ARROW + "Click to unlock");

                } else if (selected) {
                    item.setDisplayName("§a" + oreSet.getDisplayName() + " §8(Active)");
                    item.addLoreLines("§7" + oreSet.getDescription());
                    item.addLoreLines(DIVIDER);
                    item.addLoreLines(CHECKMARK + "§aCurrently selected");
                    item.addLoreLines(DIVIDER);
                    item.addLoreLines(SUB_HEADER + "Ore Contents §8«");
                    addOreContentsLore(item, oreSet);

                } else {
                    item.setDisplayName("§e" + oreSet.getDisplayName());
                    item.addLoreLines("§7" + oreSet.getDescription());
                    item.addLoreLines(DIVIDER);
                    item.addLoreLines(SUB_HEADER + "Ore Contents §8«");
                    addOreContentsLore(item, oreSet);
                    item.addLoreLines(DIVIDER);
                    item.addLoreLines(ARROW + "Click to select");
                }

                return item;
            }

            @Override
            public void handleClick(@NotNull ClickType click, @NotNull Player p, @NotNull InventoryClickEvent e) {
                boolean unlocked = quarrySite.isOreSetUnlocked(oreSet);

                if (!unlocked) {
                    String unlockUpgradeId = quarrySite.getUnlockUpgradeId(oreSet);
                    JobSiteUpgrade upgrade = findUpgrade(unlockUpgradeId);

                    if (quarrySite.purchaseUpgrade(upgrade, p)) {
                        sendSuccess(p, oreSet.getDisplayName() + " unlocked!");
                    } else {
                        sendPurchaseError(p, upgrade);
                    }
                } else {
                    quarrySite.getData().setCurrentOreSet(oreSet);
                    sendSuccess(p, "Ore set changed to " + oreSet.getDisplayName() + "!");
                    sendInfo(p, "Changes will apply on next mine regeneration.");
                }

                openOreSetMenu();
            }
        };
    }

    private void addOreContentsLore(ItemBuilder item, OreSet oreSet) {
        oreSet.getBlockWeights().forEach((blockType, weight) -> {
            String blockName = formatBlockName(blockType.id());
            item.addLoreLines(BULLET + "§f" + blockName + " §7(" + weight + "%)");
        });
    }

    private String formatBlockName(String id) {
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

    // ==================== Upgrade Menu ====================

    private void openUpgradeMenu() {
        Gui gui = Gui.normal()
                .setStructure(
                        "# # # # ? # # # #",
                        "# # # R P # # # #",
                        "# # # # # # # < #"
                )
                .addIngredient('#', filler())
                .addIngredient('?', createSubMenuHelp("Upgrades",
                        "Upgrade your quarry's efficiency."))
                .addIngredient('R', createUpgradeItem("regen_speed", Material.CLOCK, "Regeneration Speed",
                        "Decreases time between mine regens"))
                .addIngredient('P', createUpgradeItem("power_level", Material.BEACON, "Power Cell Level",
                        "Increases Haste effect strength"))
                .addIngredient('<', backButton(this::openWindow))
                .build();

        open(gui, "§8Quarry Upgrades");
    }

    // ==================== Power Cell Menu ====================

    private void openPowerCellMenu() {
        boolean built = quarrySite.isPowerCellBuilt();
        int powerLevel = quarrySite.getData().getLevel("power_level");

        Gui gui = Gui.normal()
                .setStructure(
                        "# # # # ? # # # #",
                        "# # S U # 1 # # #",
                        "# # # # # # # < #"
                )
                .addIngredient('#', filler())
                .addIngredient('?', createSubMenuHelp("Power Cell",
                        built ? "Power Cell is operational!" : "§cBuild the Power Cell first!",
                        built ? "Current Haste Level: " + (powerLevel > 0 ? toRoman(powerLevel) : "None") : ""))
                .addIngredient('S', createPowerCellStatusItem())
                .addIngredient('U', createUpgradeItem("power_level", Material.BEACON, "Power Level",
                        "Increases Haste effect strength"))
                .addIngredient('1', createUnlockableItem(quarrySite.getPowerCell()))
                .addIngredient('<', backButton(this::openWindow))
                .build();

        open(gui, "§8Power Cell");
    }

    private AbstractItem createPowerCellStatusItem() {
        return new AbstractItem() {
            @Override
            public ItemProvider getItemProvider() {
                boolean built = quarrySite.isPowerCellBuilt();
                int powerLevel = quarrySite.getData().getLevel("power_level");

                ItemBuilder item = new ItemBuilder(built ? Material.SEA_LANTERN : Material.GRAY_STAINED_GLASS);

                if (!built) {
                    item.setDisplayName("§cPower Cell §8(Not Built)");
                    item.addLoreLines("§7Build the Power Cell from the main menu");
                    item.addLoreLines(DIVIDER);
                    item.addLoreLines(CROSS + "§cStructure not built");
                } else {
                    item.setDisplayName("§6Power Cell Status");
                    item.addLoreLines("§7Provides Haste to miners");
                    item.addLoreLines(DIVIDER);
                    item.addLoreLines(SUB_HEADER + "Status §8«");

                    if (powerLevel > 0) {
                        item.addLoreLines(BULLET + "§fEffect: §aHaste " + toRoman(powerLevel));
                        item.addLoreLines(BULLET + "§fRange: §aEntire Quarry");
                        item.addLoreLines(CHECKMARK + "§aOperational");
                    } else {
                        item.addLoreLines(BULLET + "§fEffect: §cNone");
                        item.addLoreLines(BULLET + "§7Upgrade to activate Haste");
                    }
                }

                return item;
            }

            @Override
            public void handleClick(@NotNull ClickType click, @NotNull Player p, @NotNull InventoryClickEvent e) {
                if (!quarrySite.isPowerCellBuilt()) {
                    sendError(p, "Build the Power Cell first!");
                }
            }
        };
    }

    // ==================== Unlockable Item (Auto-Updating) ====================

    private Item createUnlockableItem(Unlockable unlockable) {
        ClickableAutoUpdateItem item = new ClickableAutoUpdateItem(
                20,
                () -> buildUnlockableItemProvider(unlockable),
                (player, event) -> handleUnlockableClick(unlockable, player)
        );
        item.start();
        return item;
    }

    private ItemProvider buildUnlockableItemProvider(Unlockable unlockable) {
        UnlockableState state = unlockable.getUnlockState();
        int jobsiteLevel = quarrySite.getLevel();

        ItemBuilder item = new ItemBuilder(getMaterialForState(unlockable, state));
        item.setDisplayName("§6" + unlockable.getDisplayName());
        item.addLoreLines(getDescriptionForUnlockable(unlockable));
        item.addLoreLines(DIVIDER);

        switch (state) {
            case LOCKED -> {
                item.addLoreLines(SUB_HEADER + "Requirements §8«");
                item.addLoreLines(BULLET + "§fCost: §6$" + String.format("%,d", unlockable.getCost()));
                item.addLoreLines(BULLET + "§fRequired Level: §e" + unlockable.getRequiredJobsiteLevel());
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
                UnlockableProgress progress = quarrySite.getData()
                        .getUnlockableProgress(unlockable.getUnlockableId());
                long remaining = progress.getRemainingMillis();

                item.addLoreLines(SUB_HEADER + "Under Construction §8«");
                item.addLoreLines(DIVIDER);
                item.addLoreLines(BULLET + "§fTime Remaining:");
                item.addLoreLines("   §6" + ChatUtils.formatDuration(remaining));
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

    private void handleUnlockableClick(Unlockable unlockable, Player player) {
        UnlockableState state = unlockable.getUnlockState();

        switch (state) {
            case LOCKED -> {
                if (quarrySite.purchaseUnlockable(unlockable, player)) {
                    sendSuccess(player, unlockable.getDisplayName() + " construction started!");
                } else if (quarrySite.getLevel() < unlockable.getRequiredJobsiteLevel()) {
                    sendError(player, "You need Quarry Level " + unlockable.getRequiredJobsiteLevel() + "!");
                } else if (!StoinkCore.getEconomy().has(player, unlockable.getCost())) {
                    sendError(player, "You need $" + String.format("%,d", unlockable.getCost()) + "!");
                } else {
                    sendError(player, "Requirements not met!");
                }
            }
            case BUILDING -> {
                UnlockableProgress progress = quarrySite.getData()
                        .getUnlockableProgress(unlockable.getUnlockableId());
                sendInfo(player, "Under construction - " + ChatUtils.formatDuration(progress.getRemainingMillis()) + " remaining");
            }
            case UNLOCKED -> {
                sendInfo(player, unlockable.getDisplayName() + " is already built!");
            }
        }
    }

    private Material getMaterialForState(Unlockable unlockable, UnlockableState state) {
        return switch (state) {
            case LOCKED -> Material.COBWEB;
            case BUILDING -> Material.SCAFFOLDING;
            case UNLOCKED -> Material.BEACON;
        };
    }

    private String getDescriptionForUnlockable(Unlockable unlockable) {
        return "§7Provides Haste effect to miners";
    }

    private String createConstructionProgressBar(Unlockable unlockable, UnlockableProgress progress) {
        long totalDuration = unlockable.getBuildTimeMillis();
        long remaining = progress.getRemainingMillis();
        long elapsed = totalDuration - remaining;

        int percent = (int) Math.min(100, (elapsed * 100) / Math.max(1, totalDuration));
        int bars = percent / 10;

        String bar = "§a" + "▌".repeat(bars) + "§7" + "▌".repeat(10 - bars);
        return "   " + bar + " §f" + percent + "%";
    }

    // ==================== Upgrade Item ====================

    private AbstractItem createUpgradeItem(String upgradeId, Material mat, String name, String description) {
        return new AbstractItem() {
            @Override
            public ItemProvider getItemProvider() {
                JobSiteUpgrade upgrade = findUpgrade(upgradeId);
                if (upgrade == null) {
                    return new ItemBuilder(Material.BARRIER).setDisplayName("§cUpgrade not found");
                }

                int currentLevel = quarrySite.getData().getLevel(upgradeId);
                int maxLevel = upgrade.maxLevel();
                int jobsiteLevel = quarrySite.getLevel();
                boolean maxed = currentLevel >= maxLevel;

                ItemBuilder item = new ItemBuilder(mat);
                item.setDisplayName("§6" + name);
                item.addLoreLines("§7" + description);
                item.addLoreLines(DIVIDER);

                item.addLoreLines(SUB_HEADER + "Progress §8«");
                item.addLoreLines(BULLET + "§fLevel: §a" + currentLevel + "§7/§f" + maxLevel);
                item.addLoreLines(BULLET + createLevelBar(currentLevel, maxLevel));
                item.addLoreLines(DIVIDER);

                // Special info for regen speed
                if (upgradeId.equals("regen_speed")) {
                    long currentInterval = quarrySite.getMineGenerator().getRegenIntervalSeconds();
                    item.addLoreLines(BULLET + "§fCurrent Interval: §e" + formatTime(currentInterval));
                }

                // Special info for power level
                if (upgradeId.equals("power_level") && currentLevel > 0) {
                    item.addLoreLines(BULLET + "§fCurrent Effect: §aHaste " + toRoman(currentLevel));
                }

                if (maxed) {
                    item.addLoreLines(DIVIDER);
                    item.addLoreLines(CHECKMARK + "§aMax Level Reached!");
                } else {
                    item.addLoreLines(DIVIDER);
                    item.addLoreLines(SUB_HEADER + "Next Level §8«");
                    item.addLoreLines(BULLET + "§fCost: §6$" + String.format("%,d", upgrade.cost(currentLevel + 1)));
                    item.addLoreLines(BULLET + "§fRequired Level: §e" + upgrade.requiredJobsiteLevel());
                    item.addLoreLines(DIVIDER);

                    // Check power cell requirement for power_level upgrade
                    if (upgradeId.equals("power_level") && !quarrySite.isPowerCellBuilt()) {
                        item.addLoreLines(CROSS + "§cPower Cell not built");
                    } else if (jobsiteLevel < upgrade.requiredJobsiteLevel()) {
                        item.addLoreLines(CROSS + "§cYour level: §f" + jobsiteLevel);
                    } else if (!upgrade.canUnlock(quarrySite)) {
                        item.addLoreLines(CROSS + "§cRequirements not met");
                    } else {
                        item.addLoreLines(CHECKMARK + "§aRequirements met");
                    }

                    item.addLoreLines(DIVIDER);
                    item.addLoreLines(ARROW + "Click to upgrade");
                }

                return item;
            }

            @Override
            public void handleClick(@NotNull ClickType click, @NotNull Player p, @NotNull InventoryClickEvent e) {
                JobSiteUpgrade upgrade = findUpgrade(upgradeId);
                if (upgrade == null) return;

                int currentLevel = quarrySite.getData().getLevel(upgradeId);
                if (currentLevel >= upgrade.maxLevel()) {
                    sendInfo(p, name + " is already at max level!");
                    return;
                }

                if (quarrySite.purchaseUpgrade(upgrade, p)) {
                    int newLevel = quarrySite.getData().getLevel(upgradeId);
                    sendSuccess(p, name + " upgraded to level " + newLevel + "!");
                } else {
                    sendPurchaseError(p, upgrade);
                }

                notifyWindows();
            }
        };
    }

    private String createLevelBar(int current, int max) {
        int filled = max > 0 ? (int) ((current / (double) max) * 10) : 0;
        return "§a" + "■".repeat(filled) + "§7" + "■".repeat(10 - filled);
    }

    // ==================== Contract Menu ====================

    private void openContractList() {
        StoinkCore core = StoinkCore.getInstance();
        Enterprise enterprise = core.getEnterpriseManager()
                .getEnterpriseByMember(opener.getUniqueId());

        if (enterprise == null) {
            sendError(opener, "You are not in an enterprise!");
            return;
        }

        Gui gui = Gui.normal()
                .setStructure(
                        "# # # # ? # # # #",
                        "# . . . . . . . #",
                        "# . . . . . . . #",
                        "# # # # # # # < #"
                )
                .addIngredient('#', filler())
                .addIngredient('?', createSubMenuHelp("Contracts",
                        "Complete contracts to earn",
                        "money and Quarry XP."))
                .addIngredient('<', backButton(this::openWindow))
                .build();

        List<ActiveContract> contracts = core.getContractManager()
                .getContracts(enterprise, JobSiteType.QUARRY);

        contracts.forEach(contract -> gui.addItems(createContractItem(contract)));

        open(gui, "§8Quarry Contracts");
    }

    private SimpleItem createContractItem(ActiveContract contract) {
        ContractDefinition def = contract.getDefinition();
        boolean completed = contract.isCompleted();

        ItemBuilder builder = new ItemBuilder(def.displayItem());

        // Title
        if (completed) {
            builder.setDisplayName(CHECKMARK + "§a" + def.displayName());
        } else {
            builder.setDisplayName("§e" + def.displayName());
        }

        // Description
        builder.addLoreLines(DIVIDER);
        def.description().forEach(line -> builder.addLoreLines("§7" + line));

        // Progress
        builder.addLoreLines(DIVIDER);
        builder.addLoreLines(SUB_HEADER + "Progress §8«");
        builder.addLoreLines(BULLET + "§f" + contract.getProgress() + "§7/§f" + contract.getTarget());
        builder.addLoreLines(BULLET + createContractProgressBar(contract.getProgress(), contract.getTarget()));

        // Expiration
        builder.addLoreLines(DIVIDER);
        builder.addLoreLines(SUB_HEADER + "Time §8«");
        builder.addLoreLines(BULLET + "§fExpires: §e" + formatTimeRemaining(contract.getExpirationTime()));

        // Rewards
        builder.addLoreLines(DIVIDER);
        builder.addLoreLines(SUB_HEADER + "Rewards §8«");
        addRewardLore(builder, def.reward());

        return new SimpleItem(builder);
    }

    private String createContractProgressBar(int current, int target) {
        int percent = (int) ((current / (double) Math.max(1, target)) * 100);
        int bars = Math.min(10, percent / 10);
        return "§a" + "▌".repeat(bars) + "§7" + "▌".repeat(10 - bars) + " §f" + percent + "%";
    }

    private void addRewardLore(ItemBuilder builder, Reward reward) {
        if (reward instanceof CompositeReward composite) {
            composite.getRewards().forEach(r -> addRewardLore(builder, r));
            return;
        }

        if (reward instanceof DescribableReward describable) {
            describable.getLore().forEach(line -> builder.addLoreLines(BULLET + "§f" + line));
        }
    }

    private String formatTimeRemaining(long expiry) {
        long millis = expiry - System.currentTimeMillis();
        if (millis <= 0) return "§cExpired";

        long minutes = millis / 60000;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) return days + "d " + (hours % 24) + "h";
        if (hours > 0) return hours + "h " + (minutes % 60) + "m";
        return minutes + "m";
    }

    // ==================== Helper Items ====================

    private SimpleItem filler() {
        return new SimpleItem(new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).setDisplayName(" "));
    }

    private SimpleItem createSubMenuHelp(String title, String... lines) {
        ItemBuilder item = new ItemBuilder(Material.OAK_SIGN)
                .setDisplayName(HEADER + title + " §8«")
                .addLoreLines(DIVIDER);

        for (String line : lines) {
            if (line != null && !line.isEmpty()) {
                item.addLoreLines("§7" + line);
            }
        }

        return new SimpleItem(item);
    }

    private AbstractItem menuButton(Material mat, String name, List<String> lore, Runnable action) {
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

    private JobSiteUpgrade findUpgrade(String id) {
        return quarrySite.getUpgrades().stream()
                .filter(u -> u.id().equals(id))
                .findFirst()
                .orElse(null);
    }

    private String formatTime(long seconds) {
        long minutes = seconds / 60;
        long hours = minutes / 60;

        seconds %= 60;
        minutes %= 60;

        if (hours > 0) {
            return hours + "h " + minutes + "m";
        }
        return minutes + "m " + seconds + "s";
    }

    private String toRoman(int num) {
        return switch (num) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            default -> String.valueOf(num);
        };
    }

    private void open(Gui gui, String title) {
        currentWindow = Window.single()
                .setViewer(opener)
                .setTitle(title)
                .setGui(gui)
                .build();
        currentWindow.open();
    }

    // ==================== Messages ====================

    private void sendSuccess(Player p, String message) {
        ChatUtils.sendMessage(p, "§a✔ " + message);
    }

    private void sendError(Player p, String message) {
        ChatUtils.sendMessage(p, "§c✖ " + message);
    }

    private void sendInfo(Player p, String message) {
        ChatUtils.sendMessage(p, "§e" + message);
    }

    private void sendPurchaseError(Player p, JobSiteUpgrade upgrade) {
        int jobsiteLevel = quarrySite.getLevel();

        if (jobsiteLevel < upgrade.requiredJobsiteLevel()) {
            sendError(p, "You need Quarry Level " + upgrade.requiredJobsiteLevel() + "!");
        } else if (!StoinkCore.getEconomy().has(p, upgrade.cost(quarrySite.getData().getLevel(upgrade.id()) + 1))) {
            sendError(p, "Insufficient funds!");
        } else if (!upgrade.canUnlock(quarrySite)) {
            sendError(p, "Requirements not met!");
        } else {
            sendError(p, "Unable to purchase!");
        }
    }
}