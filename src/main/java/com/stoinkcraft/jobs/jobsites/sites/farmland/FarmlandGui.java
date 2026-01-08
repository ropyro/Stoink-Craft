package com.stoinkcraft.jobs.jobsites.sites.farmland;

import com.stoinkcraft.StoinkCore;
import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.jobs.collections.CollectionsGui;
import com.stoinkcraft.jobs.contracts.ActiveContract;
import com.stoinkcraft.jobs.contracts.ContractDefinition;
import com.stoinkcraft.jobs.contracts.rewards.CompositeReward;
import com.stoinkcraft.jobs.contracts.rewards.DescribableReward;
import com.stoinkcraft.jobs.contracts.rewards.Reward;
import com.stoinkcraft.jobs.jobsites.*;
import com.stoinkcraft.jobs.jobsites.components.generators.CropGenerator;
import com.stoinkcraft.jobs.jobsites.components.generators.HoneyGenerator;
import com.stoinkcraft.jobs.jobsites.components.generators.PassiveMobGenerator;
import com.stoinkcraft.jobs.jobsites.components.unlockable.Unlockable;
import com.stoinkcraft.jobs.jobsites.components.unlockable.UnlockableProgress;
import com.stoinkcraft.jobs.jobsites.components.unlockable.UnlockableState;
import com.stoinkcraft.utils.ChatUtils;
import com.stoinkcraft.utils.SCConstants;
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

public class FarmlandGui {

    private final FarmlandSite farmlandSite;
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

    public FarmlandGui(FarmlandSite farmlandSite, Player opener) {
        this.farmlandSite = farmlandSite;
        this.opener = opener;
    }

    // ==================== Main Menu ====================

    public void openWindow() {
        Gui gui = Gui.normal()
                .setStructure(
                        "# # # # ? # # # #",
                        "# # # F A B # # #",
                        "# # # # # # # # #",
                        "# 1 2 . . # O C #",
                        "# # # # # # # # #"
                )
                .addIngredient('#', filler())
                .addIngredient('?', createHelpItem())
                .addIngredient('F', menuButton(Material.IRON_HOE, "§6Crops Manager",
                        List.of("§7Manage your crop selection", "§7and growth upgrades", DIVIDER, ARROW + "Click to open"),
                        this::openCropMenu))
                .addIngredient('A', menuButton(Material.BEEF, "§6Animal Manager",
                        List.of("§7Manage your animal selection", "§7and spawn upgrades", DIVIDER, ARROW + "Click to open"),
                        this::openAnimalMenu))
                .addIngredient('B', menuButton(Material.HONEYCOMB, "§6Honey Manager",
                        List.of("§7Manage honey production", "§7and bee upgrades", DIVIDER, ARROW + "Click to open"),
                        this::openHoneyMenu))
                .addIngredient('C', menuButton(Material.GOLD_INGOT, "§6Contracts",
                        List.of("§7View your active contracts", "§7and claim rewards", DIVIDER, ARROW + "Click to open"),
                        this::openContractList))
                .addIngredient('1', createUnlockableItem(farmlandSite.getBarnStructure()))
                .addIngredient('2', createUnlockableItem(farmlandSite.getBeeHiveStructure()))
                .addIngredient('O', menuButton(Material.BOOK, "§5Collections",  // NEW
                        List.of("§7View your collection progress", "§7and earn bonus XP", DIVIDER, ARROW + "Click to open"),
                        this::openCollections))
                .build();

        open(gui, "§8Farmland Menu");
    }

    private void openCollections() {
        CollectionsGui collectionGui = new CollectionsGui(farmlandSite, opener, this::openWindow);
        collectionGui.openCollectionList();
    }

    private SimpleItem createHelpItem() {
        int xp = farmlandSite.getData().getXp();
        int level = JobsiteLevelHelper.getLevelFromXp(xp);
        int xpToNext = JobsiteLevelHelper.getXpToNextLevel(xp);
        int playerSplit = (int) (SCConstants.PLAYER_PAY_SPLIT_PERCENTAGE * 100);
        int enterpriseSplit = 100 - playerSplit;

        return new SimpleItem(new ItemBuilder(Material.OAK_SIGN)
                .setDisplayName(HEADER + "Farmland Help §8«")
                .addLoreLines(DIVIDER)
                .addLoreLines(SUB_HEADER + "Jobsite Progress §8«")
                .addLoreLines(BULLET + "§fLevel: §a" + level)
                .addLoreLines(BULLET + "§fXP to next: §a" + String.format("%,d", xpToNext) + " XP")
                .addLoreLines(DIVIDER)
                .addLoreLines(SUB_HEADER + "How XP Works §8«")
                .addLoreLines(BULLET + "§fComplete §adaily §fand §aweekly §fcontracts")
                .addLoreLines(BULLET + "§fXP increases your §aFarmland Level")
                .addLoreLines(BULLET + "§fHigher levels unlock §abetter rewards")
                .addLoreLines(DIVIDER)
                .addLoreLines(SUB_HEADER + "Upgrades & Unlocks §8«")
                .addLoreLines(BULLET + "§fCrop upgrades increase §agrowth speed")
                .addLoreLines(BULLET + "§fAnimal upgrades increase §aspawn rate")
                .addLoreLines(BULLET + "§fStructures unlock §anew features")
                .addLoreLines(DIVIDER)
                .addLoreLines(SUB_HEADER + "Earning Money §8«")
                .addLoreLines(BULLET + "§fHarvest crops and butcher animals")
                .addLoreLines(BULLET + "§fComplete contracts for §6money")
                .addLoreLines(BULLET + "§fYou receive §a" + playerSplit + "% §f| Enterprise §a" + enterpriseSplit + "%")
        );
    }

    // ==================== Unlockable Item (Auto-Updating) ====================

    private Item createUnlockableItem(Unlockable unlockable) {
        ClickableAutoUpdateItem item = new ClickableAutoUpdateItem(
                20, // 1 second update interval
                () -> buildUnlockableItemProvider(unlockable),
                (player, event) -> handleUnlockableClick(unlockable, player)
        );
        item.start();
        return item;
    }

    private ItemProvider buildUnlockableItemProvider(Unlockable unlockable) {
        UnlockableState state = unlockable.getUnlockState();
        int jobsiteLevel = farmlandSite.getLevel();

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
                UnlockableProgress progress = farmlandSite.getData()
                        .getUnlockableProgress(unlockable.getUnlockableId());
                long remaining = progress.getRemainingSeconds();

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
                if (farmlandSite.purchaseUnlockable(unlockable, player)) {
                    sendSuccess(player, unlockable.getDisplayName() + " construction started!");
                } else if (farmlandSite.getLevel() < unlockable.getRequiredJobsiteLevel()) {
                    sendError(player, "You need Farmland Level " + unlockable.getRequiredJobsiteLevel() + "!");
                } else if (!StoinkCore.getEconomy().has(player, unlockable.getCost())) {
                    sendError(player, "You need $" + String.format("%,d", unlockable.getCost()) + "!");
                } else {
                    sendError(player, "Requirements not met!");
                }
            }
            case BUILDING -> {
                UnlockableProgress progress = farmlandSite.getData()
                        .getUnlockableProgress(unlockable.getUnlockableId());
                sendInfo(player, "Under construction - " + ChatUtils.formatDuration(progress.getRemainingSeconds()) + " remaining");
            }
            case UNLOCKED -> {
                sendInfo(player, unlockable.getDisplayName() + " is already built!");
            }
        }
    }

    private Material getMaterialForState(Unlockable unlockable, UnlockableState state) {
        String id = unlockable.getUnlockableId();

        return switch (state) {
            case LOCKED -> Material.COBWEB;
            case BUILDING -> Material.SCAFFOLDING;
            case UNLOCKED -> switch (id) {
                case "barn" -> Material.OAK_LOG;
                case "beehive" -> Material.BEE_NEST;
                default -> Material.EMERALD_BLOCK;
            };
        };
    }

    private String getDescriptionForUnlockable(Unlockable unlockable) {
        return switch (unlockable.getUnlockableId()) {
            case "barn" -> "§7Expands animal housing capacity";
            case "beehive" -> "§7Unlocks honey production";
            default -> "§7A farmland structure";
        };
    }

    private String createConstructionProgressBar(Unlockable unlockable, UnlockableProgress progress) {
        long totalDuration = unlockable.getBuildTimeMillis();
        long remaining = progress.getRemainingSeconds();
        long elapsed = totalDuration - remaining;

        int percent = (int) Math.min(100, (elapsed * 100) / Math.max(1, totalDuration));
        int bars = percent / 10;

        String bar = "§a" + "▌".repeat(bars) + "§7" + "▌".repeat(10 - bars);
        return "   " + bar + " §f" + percent + "%";
    }

    // ==================== Crop Menu ====================

    private void openCropMenu() {
        Gui gui = Gui.normal()
                .setStructure(
                        "# # # # ? # # # #",
                        "# W C P B # # G #",
                        "# # # # # # # < #"
                )
                .addIngredient('#', filler())
                .addIngredient('?', createSubMenuHelp("Crops Manager",
                        "Select which crop to grow and",
                        "upgrade your growth speed."))
                .addIngredient('W', createCropItem(Material.WHEAT, CropGenerator.CropGeneratorType.WHEAT, null))
                .addIngredient('C', createCropItem(Material.CARROT, CropGenerator.CropGeneratorType.CARROT, "unlock_carrot"))
                .addIngredient('P', createCropItem(Material.POTATO, CropGenerator.CropGeneratorType.POTATO, "unlock_potato"))
                .addIngredient('B', createCropItem(Material.BEETROOT, CropGenerator.CropGeneratorType.BEETROOT, "unlock_beetroot"))
                .addIngredient('G', createUpgradeItem("crop_growth_speed", Material.WATER_BUCKET, "Growth Speed",
                        "Increases how fast crops grow"))
                .addIngredient('<', backButton(this::openWindow))
                .build();

        open(gui, "§8Crops Manager");
    }

    private AbstractItem createCropItem(Material mat, CropGenerator.CropGeneratorType type, String unlockUpgradeId) {
        return new AbstractItem() {
            @Override
            public ItemProvider getItemProvider() {
                JobSiteUpgrade upgrade = unlockUpgradeId != null ? findUpgrade(unlockUpgradeId) : null;
                boolean unlocked = upgrade == null || farmlandSite.getData().getLevel(unlockUpgradeId) > 0;
                boolean selected = farmlandSite.getData().getCurrentCropType() == type;
                int jobsiteLevel = farmlandSite.getLevel();

                ItemBuilder item = new ItemBuilder(mat);

                if (!unlocked) {
                    item.setDisplayName("§c" + formatName(type.name()) + " §8(Locked)");
                    item.addLoreLines("§7Unlock this crop type");
                    item.addLoreLines(DIVIDER);
                    item.addLoreLines(SUB_HEADER + "Requirements §8«");
                    item.addLoreLines(BULLET + "§fCost: §6$" + String.format("%,d", upgrade.cost(1)));
                    item.addLoreLines(BULLET + "§fRequired Level: §e" + upgrade.requiredJobsiteLevel());
                    item.addLoreLines(DIVIDER);

                    if (jobsiteLevel < upgrade.requiredJobsiteLevel()) {
                        item.addLoreLines(CROSS + "§cYour level: §f" + jobsiteLevel);
                    } else if (!upgrade.canUnlock(farmlandSite)) {
                        item.addLoreLines(CROSS + "§cPrevious crop not unlocked");
                    } else {
                        item.addLoreLines(CHECKMARK + "§aReady to unlock!");
                    }

                    item.addLoreLines(DIVIDER);
                    item.addLoreLines(ARROW + "Click to unlock");

                } else if (selected) {
                    item.setDisplayName("§a" + formatName(type.name()) + " §8(Active)");
                    item.addLoreLines("§7Currently growing this crop");
                    item.addLoreLines(DIVIDER);
                    item.addLoreLines(CHECKMARK + "§aSelected");

                } else {
                    item.setDisplayName("§e" + formatName(type.name()));
                    item.addLoreLines("§7Click to grow this crop");
                    item.addLoreLines(DIVIDER);
                    item.addLoreLines(ARROW + "Click to select");
                }

                return item;
            }

            @Override
            public void handleClick(@NotNull ClickType click, @NotNull Player p, @NotNull InventoryClickEvent e) {
                JobSiteUpgrade upgrade = unlockUpgradeId != null ? findUpgrade(unlockUpgradeId) : null;
                boolean unlocked = upgrade == null || farmlandSite.getData().getLevel(unlockUpgradeId) > 0;

                if (!unlocked) {
                    if (farmlandSite.purchaseUpgrade(upgrade, p)) {
                        sendSuccess(p, formatName(type.name()) + " unlocked!");
                    } else {
                        sendPurchaseError(p, upgrade);
                    }
                } else {
                    farmlandSite.getCropGenerator().setCropType(type);
                    sendSuccess(p, "Now growing " + formatName(type.name()) + "!");
                }

                // Re-open menu to refresh all items
                openCropMenu();
            }
        };
    }

    // ==================== Animal Menu ====================

    private void openAnimalMenu() {
        Gui gui = Gui.normal()
                .setStructure(
                        "# # # # ? # # # #",
                        "# C S P H # M K #",
                        "# # # # # # # < #"
                )
                .addIngredient('#', filler())
                .addIngredient('?', createSubMenuHelp("Animal Manager",
                        "Select which animal to spawn and",
                        "upgrade spawn speed and capacity."))
                .addIngredient('C', createMobItem(Material.COW_SPAWN_EGG, PassiveMobGenerator.PassiveMobType.COW, null))
                .addIngredient('S', createMobItem(Material.SHEEP_SPAWN_EGG, PassiveMobGenerator.PassiveMobType.SHEEP, "unlock_sheep"))
                .addIngredient('P', createMobItem(Material.PIG_SPAWN_EGG, PassiveMobGenerator.PassiveMobType.PIG, "unlock_pig"))
                .addIngredient('H', createMobItem(Material.CHICKEN_SPAWN_EGG, PassiveMobGenerator.PassiveMobType.CHICKEN, "unlock_chicken"))
                .addIngredient('M', createUpgradeItem("mob_spawn_speed", Material.FEATHER, "Spawn Speed",
                        "Increases animal spawn rate"))
                .addIngredient('K', createUpgradeItem("mob_capacity", Material.CHEST, "Capacity",
                        "Increases max animals"))
                .addIngredient('<', backButton(this::openWindow))
                .build();

        open(gui, "§8Animal Manager");
    }

    private AbstractItem createMobItem(Material mat, PassiveMobGenerator.PassiveMobType type, String unlockUpgradeId) {
        return new AbstractItem() {
            @Override
            public ItemProvider getItemProvider() {
                JobSiteUpgrade upgrade = unlockUpgradeId != null ? findUpgrade(unlockUpgradeId) : null;
                boolean unlocked = upgrade == null || farmlandSite.getData().getLevel(unlockUpgradeId) > 0;
                boolean selected = farmlandSite.getData().getCurrentMobType() == type;
                int jobsiteLevel = farmlandSite.getLevel();

                ItemBuilder item = new ItemBuilder(mat);

                if (!unlocked) {
                    item.setDisplayName("§c" + type.getDisplayName() + " §8(Locked)");
                    item.addLoreLines("§7Unlock this animal type");
                    item.addLoreLines(DIVIDER);
                    item.addLoreLines(SUB_HEADER + "Requirements §8«");
                    item.addLoreLines(BULLET + "§fCost: §6$" + String.format("%,d", upgrade.cost(1)));
                    item.addLoreLines(BULLET + "§fRequired Level: §e" + upgrade.requiredJobsiteLevel());
                    item.addLoreLines(DIVIDER);

                    if (jobsiteLevel < upgrade.requiredJobsiteLevel()) {
                        item.addLoreLines(CROSS + "§cYour level: §f" + jobsiteLevel);
                    } else if (!upgrade.canUnlock(farmlandSite)) {
                        item.addLoreLines(CROSS + "§cPrevious animal not unlocked");
                    } else {
                        item.addLoreLines(CHECKMARK + "§aReady to unlock!");
                    }

                    item.addLoreLines(DIVIDER);
                    item.addLoreLines(ARROW + "Click to unlock");

                } else if (selected) {
                    item.setDisplayName("§a" + type.getDisplayName() + " §8(Active)");
                    item.addLoreLines("§7Currently spawning this animal");
                    item.addLoreLines(DIVIDER);
                    item.addLoreLines(CHECKMARK + "§aSelected");

                } else {
                    item.setDisplayName("§e" + type.getDisplayName());
                    item.addLoreLines("§7Click to spawn this animal");
                    item.addLoreLines(DIVIDER);
                    item.addLoreLines(ARROW + "Click to select");
                }

                return item;
            }

            @Override
            public void handleClick(@NotNull ClickType click, @NotNull Player p, @NotNull InventoryClickEvent e) {
                JobSiteUpgrade upgrade = unlockUpgradeId != null ? findUpgrade(unlockUpgradeId) : null;
                boolean unlocked = upgrade == null || farmlandSite.getData().getLevel(unlockUpgradeId) > 0;

                if (!unlocked) {
                    if (farmlandSite.purchaseUpgrade(upgrade, p)) {
                        sendSuccess(p, type.getDisplayName() + " unlocked!");
                    } else {
                        sendPurchaseError(p, upgrade);
                    }
                } else {
                    farmlandSite.getMobGenerator().setMobType(type);
                    sendSuccess(p, "Now spawning " + type.getDisplayName() + "!");
                }

                // Re-open menu to refresh all items
                openAnimalMenu();
            }
        };
    }

    // ==================== Honey Menu ====================

    private void openHoneyMenu() {
        boolean beehivesBuilt = farmlandSite.areBeeHivesBuilt();

        Gui gui = Gui.normal()
                .setStructure(
                        "# # # # ? # # # #",
                        "# # # H S # # # #",
                        "# # # # # # # < #"
                )
                .addIngredient('#', filler())
                .addIngredient('?', createSubMenuHelp("Honey Manager",
                        beehivesBuilt ? "Manage your honey production." : "§cBuild the Bee Hives first!"))
                .addIngredient('H', createHoneyStatusItem())
                .addIngredient('S', createUpgradeItem("honey_speed", Material.SUGAR, "Honey Speed",
                        "Increases honey generation rate"))
                .addIngredient('<', backButton(this::openWindow))
                .build();

        open(gui, "§8Honey Manager");
    }

    private AbstractItem createHoneyStatusItem() {
        return new AbstractItem() {
            @Override
            public ItemProvider getItemProvider() {
                boolean built = farmlandSite.areBeeHivesBuilt();
                ItemBuilder item = new ItemBuilder(Material.HONEY_BOTTLE);

                if (!built) {
                    item.setDisplayName("§cHoney Production §8(Locked)");
                    item.addLoreLines("§7Build the Bee Hives to unlock");
                    item.addLoreLines(DIVIDER);
                    item.addLoreLines(CROSS + "§cBee Hives not built");
                } else {
                    List<HoneyGenerator> generators = farmlandSite.getHoneyGenerators();
                    long readyCount = generators.stream().filter(HoneyGenerator::canHarvest).count();

                    item.setDisplayName("§6Honey Production");
                    item.addLoreLines("§7Your beehives are producing honey");
                    item.addLoreLines(DIVIDER);
                    item.addLoreLines(SUB_HEADER + "Status §8«");
                    item.addLoreLines(BULLET + "§fTotal Hives: §a" + generators.size());
                    item.addLoreLines(BULLET + "§fReady to Harvest: §a" + readyCount);
                    item.addLoreLines(DIVIDER);
                    item.addLoreLines("§7Right-click hives with a bottle");
                    item.addLoreLines("§7to collect honey!");
                }

                return item;
            }

            @Override
            public void handleClick(@NotNull ClickType click, @NotNull Player p, @NotNull InventoryClickEvent e) {
                if (!farmlandSite.areBeeHivesBuilt()) {
                    sendError(p, "Build the Bee Hives first!");
                }
            }
        };
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

                int currentLevel = farmlandSite.getData().getLevel(upgradeId);
                int maxLevel = upgrade.maxLevel();
                int jobsiteLevel = farmlandSite.getLevel();
                boolean maxed = currentLevel >= maxLevel;

                ItemBuilder item = new ItemBuilder(mat);
                item.setDisplayName("§6" + name);
                item.addLoreLines("§7" + description);
                item.addLoreLines(DIVIDER);

                // Level display
                item.addLoreLines(SUB_HEADER + "Progress §8«");
                item.addLoreLines(BULLET + "§fLevel: §a" + currentLevel + "§7/§f" + maxLevel);
                item.addLoreLines(BULLET + createLevelBar(currentLevel, maxLevel));
                item.addLoreLines(DIVIDER);

                if (maxed) {
                    item.addLoreLines(CHECKMARK + "§aMax Level Reached!");
                } else {
                    item.addLoreLines(SUB_HEADER + "Next Level §8«");
                    item.addLoreLines(BULLET + "§fCost: §6$" + String.format("%,d", upgrade.cost(currentLevel + 1)));
                    item.addLoreLines(BULLET + "§fRequired Level: §e" + upgrade.requiredJobsiteLevel());
                    item.addLoreLines(DIVIDER);

                    if (jobsiteLevel < upgrade.requiredJobsiteLevel()) {
                        item.addLoreLines(CROSS + "§cYour level: §f" + jobsiteLevel);
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

                int currentLevel = farmlandSite.getData().getLevel(upgradeId);
                if (currentLevel >= upgrade.maxLevel()) {
                    sendInfo(p, name + " is already at max level!");
                    return;
                }

                if (farmlandSite.purchaseUpgrade(upgrade, p)) {
                    int newLevel = farmlandSite.getData().getLevel(upgradeId);
                    sendSuccess(p, name + " upgraded to level " + newLevel + "!");
                } else {
                    sendPurchaseError(p, upgrade);
                }

                notifyWindows();
            }
        };
    }

    private String createLevelBar(int current, int max) {
        int filled = (int) ((current / (double) max) * 10);
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
                        "money and Farmland XP."))
                .addIngredient('<', backButton(this::openWindow))
                .build();

        List<ActiveContract> contracts = core.getContractManager()
                .getContracts(enterprise, JobSiteType.FARMLAND);

        contracts.forEach(contract -> gui.addItems(createContractItem(contract)));

        open(gui, "§8Farmland Contracts");
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
            item.addLoreLines("§7" + line);
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
        return farmlandSite.getUpgrades().stream()
                .filter(u -> u.id().equals(id))
                .findFirst()
                .orElse(null);
    }

    private String formatName(String name) {
        return name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
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
        int jobsiteLevel = farmlandSite.getLevel();

        if (jobsiteLevel < upgrade.requiredJobsiteLevel()) {
            sendError(p, "You need Farmland Level " + upgrade.requiredJobsiteLevel() + "!");
        } else if (!StoinkCore.getEconomy().has(p, upgrade.cost(farmlandSite.getData().getLevel(upgrade.id()) + 1))) {
            sendError(p, "Insufficient funds!");
        } else if (!upgrade.canUnlock(farmlandSite)) {
            sendError(p, "Requirements not met!");
        } else {
            sendError(p, "Unable to purchase!");
        }
    }
}