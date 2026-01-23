package com.stoinkcraft.jobsites.sites.sites.farmland;

import com.stoinkcraft.StoinkCore;
import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.jobsites.collections.CollectionsGui;
import com.stoinkcraft.jobsites.contracts.ActiveContract;
import com.stoinkcraft.jobsites.sites.*;
import com.stoinkcraft.jobsites.sites.components.generators.CropGenerator;
import com.stoinkcraft.jobsites.sites.components.generators.GreenhouseGenerator;
import com.stoinkcraft.jobsites.sites.components.generators.HoneyGenerator;
import com.stoinkcraft.jobsites.sites.components.generators.PassiveMobGenerator;
import com.stoinkcraft.utils.SCConstants;
import com.stoinkcraft.utils.guis.JobSiteGuiHelper;
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

public class FarmlandGui {

    private static final JobSiteGuiHelper.Theme THEME = JobSiteGuiHelper.Theme.FARMLAND;

    private final FarmlandSite farmlandSite;
    private final Player opener;
    private Window currentWindow;

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
                .addIngredient('F', menuButton(Material.GLASS, "§6Greenhouses",
                        List.of("§7Manage your 3 greenhouses", "§7Each with its own crops and upgrades", DIVIDER, ARROW + "Click to open"),
                        this::openGreenhouseMenu))
                .addIngredient('A', menuButton(Material.BEEF, "§6Animal Manager",
                        List.of("§7Manage your animal selection", "§7and spawn upgrades", DIVIDER, ARROW + "Click to open"),
                        this::openAnimalMenu))
                .addIngredient('B', menuButton(Material.HONEYCOMB, "§6Honey Manager",
                        List.of("§7Manage honey production", "§7and bee upgrades", DIVIDER, ARROW + "Click to open"),
                        this::openHoneyMenu))
                .addIngredient('C', menuButton(Material.GOLD_INGOT, "§6Contracts",
                        List.of("§7View your active contracts", "§7and claim rewards", DIVIDER, ARROW + "Click to open"),
                        this::openContractList))
                .addIngredient('1', createUnlockableStructureItem(
                        farmlandSite,
                        farmlandSite.getBarnStructure(),
                        THEME,
                        "Expands animal housing capacity",
                        Material.COBWEB,
                        Material.SCAFFOLDING,
                        Material.OAK_LOG
                ))
                .addIngredient('2', createUnlockableStructureItem(
                        farmlandSite,
                        farmlandSite.getBeeHiveStructure(),
                        THEME,
                        "Unlocks honey production",
                        Material.COBWEB,
                        Material.SCAFFOLDING,
                        Material.BEE_NEST
                ))
                .addIngredient('O', menuButton(Material.BOOK, "§5Collections",
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
                .setDisplayName(header(THEME, "Farmland Help"))
                .addLoreLines(DIVIDER)
                .addLoreLines(subHeader(THEME, "Jobsite Progress"))
                .addLoreLines(BULLET + "§fLevel: §a" + level)
                .addLoreLines(BULLET + "§fXP to next: §a" + String.format("%,d", xpToNext) + " XP")
                .addLoreLines(DIVIDER)
                .addLoreLines(subHeader(THEME, "How XP Works"))
                .addLoreLines(BULLET + "§fComplete §adaily §fand §aweekly §fcontracts")
                .addLoreLines(BULLET + "§fXP increases your §aFarmland Level")
                .addLoreLines(BULLET + "§fHigher levels unlock §abetter rewards")
                .addLoreLines(DIVIDER)
                .addLoreLines(subHeader(THEME, "Upgrades & Unlocks"))
                .addLoreLines(BULLET + "§fCrop upgrades increase §agrowth speed")
                .addLoreLines(BULLET + "§fAnimal upgrades increase §aspawn rate")
                .addLoreLines(BULLET + "§fStructures unlock §anew features")
                .addLoreLines(DIVIDER)
                .addLoreLines(subHeader(THEME, "Earning Money"))
                .addLoreLines(BULLET + "§fHarvest crops and butcher animals")
                .addLoreLines(BULLET + "§fComplete contracts for §6money")
                .addLoreLines(BULLET + "§fYou receive §a" + playerSplit + "% §f| Enterprise §a" + enterpriseSplit + "%")
        );
    }

    // ==================== Greenhouse Menu ====================

    private void openGreenhouseMenu() {
        Gui gui = Gui.normal()
                .setStructure(
                        "# # # # ? # # # #",
                        "# # 1 2 3 # # # #",
                        "# # # # # # # < #"
                )
                .addIngredient('#', filler())
                .addIngredient('?', createSubMenuHelp(THEME, "Greenhouses",
                        "Manage your 3 greenhouses.",
                        "Each has its own crop and upgrades."))
                .addIngredient('1', createGreenhouseItem(1))
                .addIngredient('2', createGreenhouseItem(2))
                .addIngredient('3', createGreenhouseItem(3))
                .addIngredient('<', backButton(this::openWindow))
                .build();

        open(gui, "§8Greenhouses");
    }

    private AbstractItem createGreenhouseItem(int index) {
        return new AbstractItem() {
            @Override
            public ItemProvider getItemProvider() {
                GreenhouseGenerator greenhouse = farmlandSite.getGreenhouse(index);
                boolean unlocked = greenhouse != null && greenhouse.isUnlocked();
                int jobsiteLevel = farmlandSite.getLevel();

                ItemBuilder item = new ItemBuilder(unlocked ? Material.GREEN_STAINED_GLASS : Material.GRAY_STAINED_GLASS);

                if (!unlocked) {
                    String unlockKey = "unlock_greenhouse_" + index;
                    JobSiteUpgrade upgrade = findUpgrade(farmlandSite, unlockKey);
                    int requiredLevel = upgrade != null ? upgrade.getRequiredJobsiteLevel(1) : 0;
                    int cost = upgrade != null ? upgrade.cost(1) : 0;

                    item.setDisplayName("§c§lGreenhouse " + index + " §8(Locked)");
                    item.addLoreLines("§7Unlock this greenhouse");
                    item.addLoreLines(DIVIDER);
                    item.addLoreLines(subHeader(THEME, "Requirements"));
                    item.addLoreLines(BULLET + "§fCost: §6$" + String.format("%,d", cost));
                    item.addLoreLines(BULLET + "§fRequired Level: §e" + requiredLevel);
                    item.addLoreLines(DIVIDER);

                    if (jobsiteLevel < requiredLevel) {
                        item.addLoreLines(CROSS + "§cYour level: §f" + jobsiteLevel);
                    } else if (upgrade != null && !upgrade.canPurchase(farmlandSite, 1)) {
                        item.addLoreLines(CROSS + "§cPrevious greenhouse not unlocked");
                    } else {
                        item.addLoreLines(CHECKMARK + "§aReady to unlock!");
                    }

                    item.addLoreLines(DIVIDER);
                    item.addLoreLines(ARROW + "Click to unlock");

                } else {
                    CropGenerator.CropGeneratorType cropType = farmlandSite.getData().getGreenhouseCropType(index);
                    int growthLevel = farmlandSite.getData().getLevel(greenhouse.getGrowthSpeedUpgradeKey());

                    item.setDisplayName("§a§lGreenhouse " + index);
                    item.addLoreLines("§7Click to manage this greenhouse");
                    item.addLoreLines(DIVIDER);
                    item.addLoreLines(subHeader(THEME, "Status"));
                    item.addLoreLines(BULLET + "§fCrop: §a" + formatName(cropType.name()));
                    item.addLoreLines(BULLET + "§fGrowth Level: §a" + growthLevel);
                    item.addLoreLines(DIVIDER);
                    item.addLoreLines(ARROW + "Click to manage");
                }

                return item;
            }

            @Override
            public void handleClick(@NotNull ClickType click, @NotNull Player p, @NotNull InventoryClickEvent e) {
                GreenhouseGenerator greenhouse = farmlandSite.getGreenhouse(index);

                if (greenhouse == null || !greenhouse.isUnlocked()) {
                    // Try to unlock
                    String unlockKey = "unlock_greenhouse_" + index;
                    JobSiteUpgrade upgrade = findUpgrade(farmlandSite, unlockKey);

                    if (upgrade != null) {
                        if (farmlandSite.purchaseUpgrade(upgrade, p)) {
                            sendSuccess(p, "Greenhouse " + index + " unlocked!");
                        } else {
                            sendUpgradePurchaseError(p, farmlandSite, upgrade);
                        }
                    }
                    openGreenhouseMenu();
                } else {
                    // Open specific greenhouse GUI
                    new GreenhouseGui(farmlandSite, greenhouse, p).openWindow();
                }
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
                .addIngredient('?', createSubMenuHelp(THEME, "Animal Manager",
                        "Select which animal to spawn and",
                        "upgrade spawn speed and capacity."))
                .addIngredient('C', createMobItem(Material.COW_SPAWN_EGG, PassiveMobGenerator.PassiveMobType.COW, null))
                .addIngredient('S', createMobItem(Material.SHEEP_SPAWN_EGG, PassiveMobGenerator.PassiveMobType.SHEEP, "unlock_sheep"))
                .addIngredient('P', createMobItem(Material.PIG_SPAWN_EGG, PassiveMobGenerator.PassiveMobType.PIG, "unlock_pig"))
                .addIngredient('H', createMobItem(Material.CHICKEN_SPAWN_EGG, PassiveMobGenerator.PassiveMobType.CHICKEN, "unlock_chicken"))
                .addIngredient('M', createUpgradeItem(
                        farmlandSite,
                        "mob_spawn_speed",
                        Material.FEATHER,
                        "Spawn Speed",
                        "Increases animal spawn rate",
                        THEME,
                        this::openAnimalMenu
                ))
                .addIngredient('K', createUpgradeItem(
                        farmlandSite,
                        "mob_capacity",
                        Material.CHEST,
                        "Capacity",
                        "Increases max animals",
                        THEME,
                        this::openAnimalMenu
                ))
                .addIngredient('<', backButton(this::openWindow))
                .build();

        open(gui, "§8Animal Manager");
    }

    private AbstractItem createMobItem(Material mat, PassiveMobGenerator.PassiveMobType type, String unlockUpgradeId) {
        return new AbstractItem() {
            @Override
            public ItemProvider getItemProvider() {
                JobSiteUpgrade upgrade = unlockUpgradeId != null ? findUpgrade(farmlandSite, unlockUpgradeId) : null;
                boolean unlocked = upgrade == null || farmlandSite.getData().getLevel(unlockUpgradeId) > 0;
                boolean selected = farmlandSite.getData().getCurrentMobType() == type;
                int jobsiteLevel = farmlandSite.getLevel();

                ItemBuilder item = new ItemBuilder(mat);

                if (!unlocked) {
                    int requiredLevel = upgrade.getRequiredJobsiteLevel(1);

                    item.setDisplayName("§c" + type.getDisplayName() + " §8(Locked)");
                    item.addLoreLines("§7Unlock this animal type");
                    item.addLoreLines(DIVIDER);
                    item.addLoreLines(subHeader(THEME, "Requirements"));
                    item.addLoreLines(BULLET + "§fCost: §6$" + String.format("%,d", upgrade.cost(1)));
                    item.addLoreLines(BULLET + "§fRequired Level: §e" + requiredLevel);
                    item.addLoreLines(DIVIDER);

                    if (jobsiteLevel < requiredLevel) {
                        item.addLoreLines(CROSS + "§cYour level: §f" + jobsiteLevel);
                    } else if (!upgrade.canPurchase(farmlandSite, 1)) {
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
                JobSiteUpgrade upgrade = unlockUpgradeId != null ? findUpgrade(farmlandSite, unlockUpgradeId) : null;
                boolean unlocked = upgrade == null || farmlandSite.getData().getLevel(unlockUpgradeId) > 0;

                if (!unlocked) {
                    if (farmlandSite.purchaseUpgrade(upgrade, p)) {
                        sendSuccess(p, type.getDisplayName() + " unlocked!");
                    } else {
                        sendUpgradePurchaseError(p, farmlandSite, upgrade);
                    }
                } else {
                    farmlandSite.getMobGenerator().setMobType(type);
                    sendSuccess(p, "Now spawning " + type.getDisplayName() + "!");
                }

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
                .addIngredient('?', createSubMenuHelp(THEME, "Honey Manager",
                        beehivesBuilt ? "Manage your honey production." : "§cBuild the Bee Hives first!"))
                .addIngredient('H', createHoneyStatusItem())
                .addIngredient('S', createUpgradeItem(
                        farmlandSite,
                        "honey_speed",
                        Material.SUGAR,
                        "Honey Speed",
                        "Increases honey generation rate",
                        THEME,
                        this::openHoneyMenu
                ))
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
                    item.addLoreLines(subHeader(THEME, "Status"));
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
                        "money and Farmland XP."))
                .addIngredient('<', backButton(this::openWindow))
                .build();

        List<ActiveContract> contracts = core.getContractManager()
                .getContracts(enterprise, JobSiteType.FARMLAND);

        contracts.forEach(contract -> gui.addItems(createContractItem(contract, THEME)));

        open(gui, "§8Farmland Contracts");
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