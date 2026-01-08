package com.stoinkcraft.jobs.jobsites.sites.quarry;

import com.stoinkcraft.StoinkCore;
import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.jobs.collections.CollectionsGui;
import com.stoinkcraft.jobs.contracts.ActiveContract;
import com.stoinkcraft.jobs.jobsites.JobSiteType;
import com.stoinkcraft.jobs.jobsites.JobSiteUpgrade;
import com.stoinkcraft.jobs.jobsites.JobsiteLevelHelper;
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

import java.util.List;

import static com.stoinkcraft.utils.guis.JobSiteGuiHelper.*;

public class QuarryGui {

    private static final Theme THEME = Theme.QUARRY;

    private final QuarrySite quarrySite;
    private final Player opener;
    private Window currentWindow;

    public QuarryGui(QuarrySite quarrySite, Player opener) {
        this.quarrySite = quarrySite;
        this.opener = opener;
    }

    // ==================== Main Menu ====================

    public void openWindow() {
        Gui gui = Gui.normal()
                .setStructure(
                        "# # # # ? # # # #",
                        "# O U P # # L C #",
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
                .addIngredient('L', menuButton(Material.BOOK, "§5Collections",
                        List.of("§7View your collection progress", "§7and earn bonus XP", DIVIDER, ARROW + "Click to open"),
                        this::openCollections))
                .build();

        open(gui, "§8Quarry Menu");
    }

    private void openCollections() {
        CollectionsGui collectionGui = new CollectionsGui(quarrySite, opener, this::openWindow);
        collectionGui.openCollectionList();
    }

    private SimpleItem createHelpItem() {
        int xp = quarrySite.getData().getXp();
        int level = JobsiteLevelHelper.getLevelFromXp(xp);
        int xpToNext = JobsiteLevelHelper.getXpToNextLevel(xp);
        long regenTime = quarrySite.getMineGenerator().getRemainingSeconds();

        return new SimpleItem(new ItemBuilder(Material.OAK_SIGN)
                .setDisplayName(header(THEME, "Quarry Help"))
                .addLoreLines(DIVIDER)
                .addLoreLines(subHeader(THEME, "Quarry Status"))
                .addLoreLines(BULLET + "§fLevel: §a" + level)
                .addLoreLines(BULLET + "§fXP to next: §a" + String.format("%,d", xpToNext) + " XP")
                .addLoreLines(BULLET + "§fRegen in: §e" + formatDurationSeconds(regenTime))
                .addLoreLines(BULLET + "§fOre Set: §b" + quarrySite.getData().getCurrentOreSet().getDisplayName())
                .addLoreLines(DIVIDER)
                .addLoreLines(subHeader(THEME, "How It Works"))
                .addLoreLines(BULLET + "§fMine ores to complete §acontracts")
                .addLoreLines(BULLET + "§fFind §dgeodes §ffor bonus XP!")
                .addLoreLines(BULLET + "§fQuarry regenerates periodically")
                .addLoreLines(DIVIDER)
                .addLoreLines(subHeader(THEME, "Power Cell"))
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
                .addIngredient('?', createSubMenuHelp(THEME, "Ore Sets",
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
                JobSiteUpgrade upgrade = unlockUpgradeId != null ? findUpgrade(quarrySite, unlockUpgradeId) : null;

                ItemBuilder item = new ItemBuilder(oreSet.getIcon());

                if (!unlocked && upgrade != null) {
                    int requiredLevel = upgrade.getRequiredJobsiteLevel(1);

                    item.setDisplayName("§c" + oreSet.getDisplayName() + " §8(Locked)");
                    item.addLoreLines("§7" + oreSet.getDescription());
                    item.addLoreLines(DIVIDER);
                    item.addLoreLines(subHeader(THEME, "Requirements"));
                    item.addLoreLines(BULLET + "§fCost: §6$" + String.format("%,d", upgrade.cost(1)));
                    item.addLoreLines(BULLET + "§fRequired Level: §e" + requiredLevel);
                    item.addLoreLines(DIVIDER);

                    if (jobsiteLevel < requiredLevel) {
                        item.addLoreLines(CROSS + "§cYour level: §f" + jobsiteLevel);
                    } else if (!upgrade.canPurchase(quarrySite, 1)) {
                        item.addLoreLines(CROSS + "§cPrevious set not unlocked");
                    } else {
                        item.addLoreLines(CHECKMARK + "§aReady to unlock!");
                    }

                    item.addLoreLines(DIVIDER);
                    item.addLoreLines(subHeader(THEME, "Ore Contents"));
                    addOreContentsLore(item, oreSet);
                    item.addLoreLines(DIVIDER);
                    item.addLoreLines(ARROW + "Click to unlock");

                } else if (selected) {
                    item.setDisplayName("§a" + oreSet.getDisplayName() + " §8(Active)");
                    item.addLoreLines("§7" + oreSet.getDescription());
                    item.addLoreLines(DIVIDER);
                    item.addLoreLines(CHECKMARK + "§aCurrently selected");
                    item.addLoreLines(DIVIDER);
                    item.addLoreLines(subHeader(THEME, "Ore Contents"));
                    addOreContentsLore(item, oreSet);

                } else {
                    item.setDisplayName("§e" + oreSet.getDisplayName());
                    item.addLoreLines("§7" + oreSet.getDescription());
                    item.addLoreLines(DIVIDER);
                    item.addLoreLines(subHeader(THEME, "Ore Contents"));
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
                    JobSiteUpgrade upgrade = findUpgrade(quarrySite, unlockUpgradeId);

                    if (upgrade != null && quarrySite.purchaseUpgrade(upgrade, p)) {
                        sendSuccess(p, oreSet.getDisplayName() + " unlocked!");
                    } else if (upgrade != null) {
                        sendUpgradePurchaseError(p, quarrySite, upgrade);
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

    // ==================== Upgrade Menu ====================

    private void openUpgradeMenu() {
        Gui gui = Gui.normal()
                .setStructure(
                        "# # # # ? # # # #",
                        "# # # R # # # # #",
                        "# # # # # # # < #"
                )
                .addIngredient('#', filler())
                .addIngredient('?', createSubMenuHelp(THEME, "Upgrades",
                        "Upgrade your quarry's efficiency."))
                .addIngredient('R', createUpgradeItem(
                        quarrySite,
                        "regen_speed",
                        Material.CLOCK,
                        "Regeneration Speed",
                        "Decreases time between mine regens",
                        THEME,
                        this::openUpgradeMenu
                ))
                .addIngredient('<', backButton(this::openWindow))
                .build();

        open(gui, "§8Quarry Upgrades");
    }

    // ==================== Power Cell Menu ====================

    private void openPowerCellMenu() {
        boolean built = quarrySite.isPowerCellBuilt();
        int powerLevel = quarrySite.getData().getLevel("power_level");

        String statusLine = built
                ? "Power Cell is operational!"
                : "§cBuild the Power Cell first!";
        String effectLine = built && powerLevel > 0
                ? "Current Haste Level: " + toRoman(powerLevel)
                : "";

        Gui gui = Gui.normal()
                .setStructure(
                        "# # # # ? # # # #",
                        "# # S # P # U # #",
                        "# # # # # # # < #"
                )
                .addIngredient('#', filler())
                .addIngredient('?', createSubMenuHelp(THEME, "Power Cell", statusLine, effectLine))
                .addIngredient('S', createPowerCellStatusItem())
                .addIngredient('P', createUnlockableStructureItem(
                        quarrySite,
                        quarrySite.getPowerCell(),
                        THEME,
                        "Provides Haste effect to miners",
                        Material.COBWEB,
                        Material.SCAFFOLDING,
                        Material.BEACON
                ))
                .addIngredient('U', createPowerLevelUpgradeItem())
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
                    item.addLoreLines("§7Build the Power Cell structure first");
                    item.addLoreLines(DIVIDER);
                    item.addLoreLines(CROSS + "§cStructure not built");
                } else {
                    item.setDisplayName("§6Power Cell Status");
                    item.addLoreLines("§7Provides Haste to miners");
                    item.addLoreLines(DIVIDER);
                    item.addLoreLines(subHeader(THEME, "Status"));

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

    /**
     * Custom upgrade item for Power Level that requires Power Cell to be built.
     */
    private AbstractItem createPowerLevelUpgradeItem() {
        return new AbstractItem() {
            @Override
            public ItemProvider getItemProvider() {
                JobSiteUpgrade upgrade = findUpgrade(quarrySite, "power_level");
                if (upgrade == null) {
                    return new ItemBuilder(Material.BARRIER).setDisplayName("§cUpgrade not found");
                }

                boolean powerCellBuilt = quarrySite.isPowerCellBuilt();
                int currentLevel = quarrySite.getData().getLevel("power_level");
                int maxLevel = upgrade.maxLevel();
                int jobsiteLevel = quarrySite.getLevel();
                int nextLevel = currentLevel + 1;
                boolean maxed = currentLevel >= maxLevel;

                ItemBuilder item = new ItemBuilder(Material.BEACON);
                item.setDisplayName("§6Power Level");
                item.addLoreLines("§7Increases Haste effect strength");
                item.addLoreLines(DIVIDER);

                if (!powerCellBuilt) {
                    item.addLoreLines(CROSS + "§cBuild the Power Cell first!");
                    return item;
                }

                // Current Progress
                item.addLoreLines(subHeader(THEME, "Progress"));
                item.addLoreLines(BULLET + "§fLevel: §a" + currentLevel + "§7/§f" + maxLevel);
                item.addLoreLines(BULLET + createLevelBar(currentLevel, maxLevel));

                if (currentLevel > 0) {
                    item.addLoreLines(BULLET + "§fCurrent Effect: §aHaste " + toRoman(currentLevel));
                }
                item.addLoreLines(DIVIDER);

                if (maxed) {
                    item.addLoreLines(CHECKMARK + "§aMax Level Reached!");
                    item.addLoreLines(BULLET + "§fMax Effect: §aHaste " + toRoman(maxLevel));
                } else {
                    int nextLevelCost = upgrade.cost(nextLevel);
                    int nextLevelRequiredJS = upgrade.getRequiredJobsiteLevel(nextLevel);

                    item.addLoreLines(subHeader(THEME, "Next Level"));
                    item.addLoreLines(BULLET + "§fCost: §6$" + String.format("%,d", nextLevelCost));
                    item.addLoreLines(BULLET + "§fRequired Level: §e" + nextLevelRequiredJS);
                    item.addLoreLines(BULLET + "§fEffect: §aHaste " + toRoman(nextLevel));
                    item.addLoreLines(DIVIDER);

                    if (jobsiteLevel < nextLevelRequiredJS) {
                        item.addLoreLines(CROSS + "§cYour level: §f" + jobsiteLevel + " §7(need §e" + nextLevelRequiredJS + "§7)");
                    } else if (!upgrade.canPurchase(quarrySite, nextLevel)) {
                        item.addLoreLines(CROSS + "§cRequirements not met");
                    } else {
                        item.addLoreLines(CHECKMARK + "§aReady to upgrade!");
                    }

                    item.addLoreLines(DIVIDER);
                    item.addLoreLines(ARROW + "Click to upgrade");
                }

                return item;
            }

            @Override
            public void handleClick(@NotNull ClickType click, @NotNull Player p, @NotNull InventoryClickEvent e) {
                if (!quarrySite.isPowerCellBuilt()) {
                    sendError(p, "Build the Power Cell first!");
                    return;
                }

                JobSiteUpgrade upgrade = findUpgrade(quarrySite, "power_level");
                if (upgrade == null) return;

                int currentLevel = quarrySite.getData().getLevel("power_level");
                if (currentLevel >= upgrade.maxLevel()) {
                    sendInfo(p, "Power Level is already at max!");
                    return;
                }

                if (quarrySite.purchaseUpgrade(upgrade, p)) {
                    int newLevel = quarrySite.getData().getLevel("power_level");
                    sendSuccess(p, "Power Level upgraded to " + newLevel + "! (Haste " + toRoman(newLevel) + ")");
                } else {
                    sendUpgradePurchaseError(p, quarrySite, upgrade);
                }

                openPowerCellMenu();
            }
        };
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
                .addIngredient('?', createSubMenuHelp(THEME, "Contracts",
                        "Complete contracts to earn",
                        "money and Quarry XP."))
                .addIngredient('<', backButton(this::openWindow))
                .build();

        List<ActiveContract> contracts = core.getContractManager()
                .getContracts(enterprise, JobSiteType.QUARRY);

        contracts.forEach(contract -> gui.addItems(createContractItem(contract, THEME)));

        open(gui, "§8Quarry Contracts");
    }

    // ==================== Window Management ====================

    private void open(Gui gui, String title) {
        currentWindow = Window.single()
                .setViewer(opener)
                .setTitle(title)
                .setGui(gui)
                .build();
        currentWindow.open();
    }
}