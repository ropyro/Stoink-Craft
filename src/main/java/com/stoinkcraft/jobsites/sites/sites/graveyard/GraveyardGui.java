package com.stoinkcraft.jobsites.sites.sites.graveyard;

import com.stoinkcraft.StoinkCore;
import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.jobsites.collections.CollectionsGui;
import com.stoinkcraft.jobsites.contracts.ActiveContract;
import com.stoinkcraft.jobsites.sites.JobSiteType;
import com.stoinkcraft.jobsites.sites.JobSiteUpgrade;
import com.stoinkcraft.jobsites.sites.JobsiteLevelHelper;
import com.stoinkcraft.jobsites.sites.components.generators.TombstoneGenerator;
import com.stoinkcraft.jobsites.sites.components.structures.MausoleumStructure;
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

public class GraveyardGui {

    private static final JobSiteGuiHelper.Theme THEME = JobSiteGuiHelper.Theme.GRAVEYARD;

    private final GraveyardSite graveyardSite;
    private final Player opener;
    private Window currentWindow;

    public GraveyardGui(GraveyardSite graveyardSite, Player opener) {
        this.graveyardSite = graveyardSite;
        this.opener = opener;
    }

    // ==================== Main Menu ====================

    public void openWindow() {
        Gui gui = Gui.normal()
                .setStructure(
                        "# # # # # # # # #",
                        "# # # # ? # # # #",
                        "# # # # # # # # #",
                        "# # T A M U # # #",
                        "# # # C # L # # #",
                        "# # # # # # # # #"
                )
                .addIngredient('#', filler())
                .addIngredient('?', createHelpItem())
                .addIngredient('T', menuButton(Material.MOSSY_COBBLESTONE, "§5Tombstones",
                        List.of("§7Purchase and manage tombstones", "§7Configure mob attunements", DIVIDER, ARROW + "Click to open"),
                        this::openTombstoneMenu))
                .addIngredient('A', menuButton(Material.ZOMBIE_HEAD, "§5Attunements",
                        List.of("§7Attune tombstones to spawn", "§7specific mob types", DIVIDER, ARROW + "Click to open"),
                        this::openAttunementMenu))
                .addIngredient('M', menuButton(Material.COBWEB, "§5Mausoleum",
                        List.of("§7Manage the Mausoleum", "§7and spider hordes", DIVIDER, ARROW + "Click to open"),
                        this::openMausoleumMenu))
                .addIngredient('U', menuButton(Material.EXPERIENCE_BOTTLE, "§5Upgrades",
                        List.of("§7Purchase graveyard upgrades", DIVIDER, ARROW + "Click to open"),
                        this::openUpgradeMenu))
                .addIngredient('C', menuButton(Material.GOLD_INGOT, "§5Contracts",
                        List.of("§7View your active contracts", "§7and claim rewards", DIVIDER, ARROW + "Click to open"),
                        this::openContractList))
                .addIngredient('L', menuButton(Material.BOOK, "§5Collections",
                        List.of("§7View your collection progress", "§7and earn bonus XP", DIVIDER, ARROW + "Click to open"),
                        this::openCollections))
                .build();

        open(gui, "§8Graveyard Menu");
    }

    private void openCollections() {
        CollectionsGui collectionGui = new CollectionsGui(graveyardSite, opener, this::openWindow);
        collectionGui.openCollectionList();
    }

    private SimpleItem createHelpItem() {
        GraveyardData data = graveyardSite.getData();
        int xp = data.getXp();
        int level = JobsiteLevelHelper.getLevelFromXp(xp);
        int xpToNext = JobsiteLevelHelper.getXpToNextLevel(xp);
        int souls = data.getSouls();

        return new SimpleItem(new ItemBuilder(Material.OAK_SIGN)
                .setDisplayName(header(THEME, "Graveyard Help"))
                .addLoreLines(DIVIDER)
                .addLoreLines(subHeader(THEME, "Jobsite Progress"))
                .addLoreLines(BULLET + "§fLevel: §a" + level)
                .addLoreLines(BULLET + "§fXP to next: §a" + String.format("%,d", xpToNext) + " XP")
                .addLoreLines(BULLET + "§fSouls: §d" + String.format("%,d", souls) + " ✦")
                .addLoreLines(DIVIDER)
                .addLoreLines(subHeader(THEME, "How It Works"))
                .addLoreLines(BULLET + "§fSlay undead mobs for §acontract progress")
                .addLoreLines(BULLET + "§fMobs have a chance to drop §dSouls")
                .addLoreLines(BULLET + "§fUse Souls to §aattune tombstones")
                .addLoreLines(DIVIDER)
                .addLoreLines(subHeader(THEME, "Tombstones"))
                .addLoreLines(BULLET + "§fPurchase more with §6money")
                .addLoreLines(BULLET + "§fAttune to spawn §cspecific mobs")
                .addLoreLines(BULLET + "§fHigher level = §amore tombstones")
        );
    }

    // ==================== Tombstone Menu ====================

    private void openTombstoneMenu() {
        Gui gui = Gui.normal()
                .setStructure(
                        "# # # # # # # # #",
                        "# # # # ? # # # #",
                        "# # # # # # # # #",
                        "# # # # P # # # #",
                        "# # # # # # # < #"
                )
                .addIngredient('#', filler())
                .addIngredient('?', createSubMenuHelp(THEME, "Tombstones",
                        "Purchase tombstones to spawn more undead.",
                        "Each tombstone spawns one mob at a time."))
                .addIngredient('P', createTombstonePurchaseItem())
                .addIngredient('<', backButton(this::openWindow))
                .build();

        open(gui, "§8Tombstone Management");
    }

    private AbstractItem createTombstonePurchaseItem() {
        return new AbstractItem() {
            @Override
            public ItemProvider getItemProvider() {
                GraveyardData data = graveyardSite.getData();
                int owned = data.getTombstonesPurchased();
                int max = graveyardSite.getMaxPurchasableTombstones();
                int total = GraveyardSite.TOTAL_TOMBSTONES;
                int cost = graveyardSite.getNextTombstoneCost();

                ItemBuilder item = new ItemBuilder(Material.MOSSY_COBBLESTONE);
                item.setDisplayName("§5Purchase Tombstone");
                item.addLoreLines("§7Activate another tombstone");
                item.addLoreLines(DIVIDER);

                item.addLoreLines(subHeader(THEME, "Status"));
                item.addLoreLines(BULLET + "§fOwned: §a" + owned + "§7/§f" + total);
                item.addLoreLines(BULLET + "§fAvailable: §e" + max);
                item.addLoreLines(BULLET + createProgressBarWithCount(owned, total));
                item.addLoreLines(DIVIDER);

                if (owned >= total) {
                    item.addLoreLines(CHECKMARK + "§aAll tombstones purchased!");
                } else if (owned >= max) {
                    item.addLoreLines(CROSS + "§cLevel up to unlock more!");
                    item.addLoreLines(BULLET + "§7Upgrade Tombstone Capacity");
                } else {
                    item.addLoreLines(subHeader(THEME, "Next Purchase"));
                    item.addLoreLines(BULLET + "§fCost: §6$" + String.format("%,d", cost));
                    item.addLoreLines(DIVIDER);
                    item.addLoreLines(ARROW + "Click to purchase");
                }

                return item;
            }

            @Override
            public void handleClick(@NotNull ClickType click, @NotNull Player p, @NotNull InventoryClickEvent e) {
                GraveyardData data = graveyardSite.getData();
                int owned = data.getTombstonesPurchased();
                int max = graveyardSite.getMaxPurchasableTombstones();

                if (owned >= GraveyardSite.TOTAL_TOMBSTONES) {
                    sendInfo(p, "All tombstones are already purchased!", THEME);
                    return;
                }

                if (owned >= max) {
                    sendError(p, "Level up or upgrade capacity to unlock more!");
                    return;
                }

                if (graveyardSite.purchaseTombstone(p)) {
                    sendSuccess(p, "Tombstone purchased! (" + (owned + 1) + "/" + GraveyardSite.TOTAL_TOMBSTONES + ")");
                } else {
                    sendError(p, "Insufficient funds! Need $" + String.format("%,d", graveyardSite.getNextTombstoneCost()));
                }

                openTombstoneMenu();
            }
        };
    }

    // ==================== Attunement Menu ====================

    private void openAttunementMenu() {
        Gui gui = Gui.normal()
                .setStructure(
                        "# # # # # # # # #",
                        "# # # # ? # # # #",
                        "# # # # # # # # #",
                        "# # R Z S H # # #",
                        "# # T V D W # < #",
                        "# # # # # # # # #"
                )
                .addIngredient('#', filler())
                .addIngredient('?', createAttunementHelp())
                .addIngredient('R', createAttunementTypeItem(UndeadMobType.RANDOM))
                .addIngredient('Z', createAttunementTypeItem(UndeadMobType.ZOMBIE))
                .addIngredient('S', createAttunementTypeItem(UndeadMobType.SKELETON))
                .addIngredient('H', createAttunementTypeItem(UndeadMobType.HUSK))
                .addIngredient('T', createAttunementTypeItem(UndeadMobType.STRAY))
                .addIngredient('V', createAttunementTypeItem(UndeadMobType.ZOMBIE_VILLAGER))
                .addIngredient('D', createAttunementTypeItem(UndeadMobType.DROWNED))
                .addIngredient('W', createAttunementTypeItem(UndeadMobType.WITHER_SKELETON))
                .addIngredient('<', backButton(this::openWindow))
                .build();

        open(gui, "§8Attunements");
    }

    private SimpleItem createAttunementHelp() {
        GraveyardData data = graveyardSite.getData();
        int souls = data.getSouls();

        return new SimpleItem(new ItemBuilder(Material.OAK_SIGN)
                .setDisplayName(header(THEME, "Attunements"))
                .addLoreLines(DIVIDER)
                .addLoreLines(subHeader(THEME, "Your Souls"))
                .addLoreLines(BULLET + "§d" + String.format("%,d", souls) + " ✦")
                .addLoreLines(DIVIDER)
                .addLoreLines(subHeader(THEME, "How Attunements Work"))
                .addLoreLines(BULLET + "§fSelect a mob type below")
                .addLoreLines(BULLET + "§fThen click a tombstone to attune")
                .addLoreLines(BULLET + "§fSouls are spent on attunement")
                .addLoreLines(DIVIDER)
                .addLoreLines("§7Attuned tombstones only spawn")
                .addLoreLines("§7that specific mob type.")
        );
    }

    private AbstractItem createAttunementTypeItem(UndeadMobType type) {
        return new AbstractItem() {
            @Override
            public ItemProvider getItemProvider() {
                GraveyardData data = graveyardSite.getData();
                int souls = data.getSouls();
                int level = graveyardSite.getLevel();
                boolean canAfford = souls >= type.getSoulCost();
                boolean meetsLevel = level >= type.getRequiredLevel();

                ItemBuilder item = new ItemBuilder(type.getIcon());
                item.setDisplayName("§5" + type.getDisplayName());

                if (type == UndeadMobType.RANDOM) {
                    item.addLoreLines("§7Random zombie or skeleton");
                    item.addLoreLines(DIVIDER);
                    item.addLoreLines(CHECKMARK + "§aFree (removes attunement)");
                } else {
                    item.addLoreLines("§7Attune tombstones to spawn " + type.getDisplayName());
                    item.addLoreLines(DIVIDER);

                    item.addLoreLines(subHeader(THEME, "Requirements"));
                    item.addLoreLines(BULLET + "§fCost: §d" + type.getSoulCost() + " Souls");
                    item.addLoreLines(BULLET + "§fRequired Level: §e" + type.getRequiredLevel());
                    item.addLoreLines(DIVIDER);

                    if (!meetsLevel) {
                        item.addLoreLines(CROSS + "§cYour level: §f" + level);
                    } else if (!canAfford) {
                        item.addLoreLines(CROSS + "§cNot enough souls! §7(" + souls + "/" + type.getSoulCost() + ")");
                    } else {
                        item.addLoreLines(CHECKMARK + "§aRequirements met!");
                    }
                }

                item.addLoreLines(DIVIDER);
                item.addLoreLines(ARROW + "Click to select for attunement");

                return item;
            }

            @Override
            public void handleClick(@NotNull ClickType click, @NotNull Player p, @NotNull InventoryClickEvent e) {
                int level = graveyardSite.getLevel();

                if (type != UndeadMobType.RANDOM && level < type.getRequiredLevel()) {
                    sendError(p, "You need Graveyard Level " + type.getRequiredLevel() + "!");
                    return;
                }

                openTombstoneSelector(type);
            }
        };
    }

    private void openTombstoneSelector(UndeadMobType targetType) {
        List<TombstoneGenerator> tombstones = graveyardSite.getTombstoneGenerators();

        String costLine = targetType.requiresAttunement()
                ? "Cost: §d" + targetType.getSoulCost() + " Souls"
                : "§aFree";

        Gui.Builder.Normal builder = Gui.normal()
                .setStructure(
                        "# # # # ? # # # #",
                        ". . . . . . . . .",
                        ". . . . . . . . .",
                        ". . . . . . . . .",
                        "# # # # # # # < #"
                )
                .addIngredient('#', filler())
                .addIngredient('?', createSubMenuHelp(THEME, "Select Tombstone",
                        "Attuning to: §d" + targetType.getDisplayName(),
                        costLine))
                .addIngredient('<', backButton(this::openAttunementMenu));

        Gui gui = builder.build();

        for (TombstoneGenerator tombstone : tombstones) {
            gui.addItems(createTombstoneSelectItem(tombstone, targetType));
        }

        open(gui, "§8Select Tombstone");
    }

    private AbstractItem createTombstoneSelectItem(TombstoneGenerator tombstone, UndeadMobType targetType) {
        return new AbstractItem() {
            @Override
            public ItemProvider getItemProvider() {
                boolean enabled = tombstone.isEnabled();
                UndeadMobType currentAttunement = tombstone.getAttunement();

                ItemBuilder item;
                if (!enabled) {
                    item = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE);
                    item.setDisplayName("§8Tombstone #" + (tombstone.getIndex() + 1) + " §7(Inactive)");
                    item.addLoreLines("§7Purchase this tombstone first");
                } else {
                    item = new ItemBuilder(currentAttunement.getIcon());
                    item.setDisplayName("§5Tombstone #" + (tombstone.getIndex() + 1));
                    item.addLoreLines("§7Current: §d" + currentAttunement.getDisplayName());
                    item.addLoreLines(DIVIDER);

                    if (currentAttunement == targetType) {
                        item.addLoreLines(CHECKMARK + "§aAlready attuned to this type");
                    } else {
                        item.addLoreLines(ARROW + "Click to attune to §d" + targetType.getDisplayName());
                    }
                }

                return item;
            }

            @Override
            public void handleClick(@NotNull ClickType click, @NotNull Player p, @NotNull InventoryClickEvent e) {
                if (!tombstone.isEnabled()) {
                    sendError(p, "This tombstone is not active!");
                    return;
                }

                if (tombstone.getAttunement() == targetType) {
                    sendInfo(p, "Already attuned to " + targetType.getDisplayName() + "!", THEME);
                    return;
                }

                if (tombstone.setAttunement(targetType)) {
                    if (targetType == UndeadMobType.RANDOM) {
                        sendSuccess(p, "Tombstone #" + (tombstone.getIndex() + 1) + " attunement removed!");
                    } else {
                        sendSuccess(p, "Tombstone #" + (tombstone.getIndex() + 1) + " attuned to " + targetType.getDisplayName() + "!");
                    }
                    openTombstoneSelector(targetType);
                } else {
                    GraveyardData data = graveyardSite.getData();
                    if (data.getSouls() < targetType.getSoulCost()) {
                        sendError(p, "Not enough souls! Need " + targetType.getSoulCost());
                    } else {
                        sendError(p, "Cannot attune - requirements not met!");
                    }
                }
            }
        };
    }

    // ==================== Mausoleum Menu ====================

    private void openMausoleumMenu() {
        MausoleumStructure mausoleum = graveyardSite.getMausoleumStructure();
        boolean built = mausoleum.isUnlocked();

        Gui gui = Gui.normal()
                .setStructure(
                        "# # # # ? # # # #",
                        "# # M S H # # # #",
                        "# # # # # # # < #"
                )
                .addIngredient('#', filler())
                .addIngredient('?', createMausoleumHelp())
                .addIngredient('M', createUnlockableStructureItem(
                        graveyardSite,
                        mausoleum,
                        THEME,
                        "Spawns spider hordes for rewards",
                        Material.COBWEB,
                        Material.SCAFFOLDING,
                        Material.STONE_BRICKS
                ))
                .addIngredient('S', createMausoleumUpgradeItem("mausoleum_spawn_speed", Material.CLOCK,
                        "Horde Frequency", "Reduce time between hordes"))
                .addIngredient('H', createMausoleumUpgradeItem("mausoleum_horde_size", Material.SPIDER_EYE,
                        "Horde Size", "Increase spiders per horde"))
                .addIngredient('<', backButton(this::openWindow))
                .build();

        open(gui, "§8Mausoleum");
    }

    private SimpleItem createMausoleumHelp() {
        MausoleumStructure mausoleum = graveyardSite.getMausoleumStructure();
        boolean built = mausoleum.isUnlocked();

        ItemBuilder item = new ItemBuilder(Material.OAK_SIGN)
                .setDisplayName(header(THEME, "Mausoleum"))
                .addLoreLines(DIVIDER)
                .addLoreLines("§7The Mausoleum spawns spider hordes")
                .addLoreLines("§7that reward XP and money when killed.")
                .addLoreLines(DIVIDER);

        if (built) {
            item.addLoreLines(subHeader(THEME, "Status"));
            if (mausoleum.isHordeActive()) {
                item.addLoreLines(BULLET + "§cHorde Active! §7(" + mausoleum.getActiveSpiderCount() + " spiders)");
            } else {
                long remaining = mausoleum.getRemainingSeconds();
                item.addLoreLines(BULLET + "§7Next horde in: §e" + formatDurationSeconds(remaining));
            }
        } else {
            item.addLoreLines(CROSS + "§cNot yet built");
        }

        return new SimpleItem(item);
    }

    /**
     * Custom upgrade item for Mausoleum upgrades that require the structure to be built.
     */
    private AbstractItem createMausoleumUpgradeItem(String upgradeId, Material mat, String name, String description) {
        return new AbstractItem() {
            @Override
            public ItemProvider getItemProvider() {
                JobSiteUpgrade upgrade = findUpgrade(graveyardSite, upgradeId);
                if (upgrade == null) {
                    return new ItemBuilder(Material.BARRIER).setDisplayName("§cUpgrade not found");
                }

                boolean mausoleumBuilt = graveyardSite.getMausoleumStructure().isUnlocked();
                int currentLevel = graveyardSite.getData().getLevel(upgradeId);
                int maxLevel = upgrade.maxLevel();
                int jobsiteLevel = graveyardSite.getLevel();
                int nextLevel = currentLevel + 1;
                boolean maxed = currentLevel >= maxLevel;

                ItemBuilder item = new ItemBuilder(mat);
                item.setDisplayName("§5" + name);
                item.addLoreLines("§7" + description);
                item.addLoreLines(DIVIDER);

                if (!mausoleumBuilt) {
                    item.addLoreLines(CROSS + "§cBuild the Mausoleum first!");
                    return item;
                }

                // Current Progress
                item.addLoreLines(subHeader(THEME, "Progress"));
                item.addLoreLines(BULLET + "§fLevel: §a" + currentLevel + "§7/§f" + maxLevel);
                item.addLoreLines(BULLET + createLevelBar(currentLevel, maxLevel));
                item.addLoreLines(DIVIDER);

                if (maxed) {
                    item.addLoreLines(CHECKMARK + "§aMax Level Reached!");
                } else {
                    int nextLevelCost = upgrade.cost(nextLevel);
                    int nextLevelRequiredJS = upgrade.getRequiredJobsiteLevel(nextLevel);

                    item.addLoreLines(subHeader(THEME, "Next Level"));
                    item.addLoreLines(BULLET + "§fCost: §6$" + String.format("%,d", nextLevelCost));
                    item.addLoreLines(BULLET + "§fRequired Level: §e" + nextLevelRequiredJS);
                    item.addLoreLines(DIVIDER);

                    if (jobsiteLevel < nextLevelRequiredJS) {
                        item.addLoreLines(CROSS + "§cYour level: §f" + jobsiteLevel + " §7(need §e" + nextLevelRequiredJS + "§7)");
                    } else if (!upgrade.canPurchase(graveyardSite, nextLevel)) {
                        item.addLoreLines(CROSS + "§cRequirements not met");
                    } else {
                        item.addLoreLines(CHECKMARK + "§aReady to upgrade!");
                    }

                    // Show level roadmap
                    if (upgrade.jobsiteLevelIncrement() > 0) {
                        item.addLoreLines(DIVIDER);
                        item.addLoreLines(subHeader(THEME, "Level Roadmap"));
                        addLevelRoadmap(item, upgrade, currentLevel, jobsiteLevel);
                    }

                    item.addLoreLines(DIVIDER);
                    item.addLoreLines(ARROW + "Click to upgrade");
                }

                return item;
            }

            @Override
            public void handleClick(@NotNull ClickType click, @NotNull Player p, @NotNull InventoryClickEvent e) {
                if (!graveyardSite.getMausoleumStructure().isUnlocked()) {
                    sendError(p, "Build the Mausoleum first!");
                    return;
                }

                JobSiteUpgrade upgrade = findUpgrade(graveyardSite, upgradeId);
                if (upgrade == null) return;

                int currentLevel = graveyardSite.getData().getLevel(upgradeId);
                if (currentLevel >= upgrade.maxLevel()) {
                    sendInfo(p, name + " is already at max level!", THEME);
                    return;
                }

                if (graveyardSite.purchaseUpgrade(upgrade, p)) {
                    int newLevel = graveyardSite.getData().getLevel(upgradeId);
                    sendSuccess(p, name + " upgraded to level " + newLevel + "!");
                } else {
                    sendUpgradePurchaseError(p, graveyardSite, upgrade);
                }

                openMausoleumMenu();
            }
        };
    }

    /**
     * Helper to add level roadmap to an ItemBuilder.
     */
    private void addLevelRoadmap(ItemBuilder item, JobSiteUpgrade upgrade, int currentLevel, int currentJSLevel) {
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

    // ==================== Upgrade Menu ====================

    private void openUpgradeMenu() {
        Gui gui = Gui.normal()
                .setStructure(
                        "# # # # ? # # # #",
                        "# # # S C H # # #",
                        "# # # # # # # < #"
                )
                .addIngredient('#', filler())
                .addIngredient('?', createSubMenuHelp(THEME, "Upgrades",
                        "Purchase upgrades to improve",
                        "your graveyard's efficiency."))
                .addIngredient('S', createUpgradeItem(
                        graveyardSite,
                        "spawn_speed",
                        Material.FEATHER,
                        "Spawn Speed",
                        "Reduce time between mob spawns",
                        THEME,
                        this::openUpgradeMenu
                ))
                .addIngredient('C', createUpgradeItem(
                        graveyardSite,
                        "tombstone_capacity",
                        Material.CHEST,
                        "Tombstone Capacity",
                        "Unlock more tombstones for purchase",
                        THEME,
                        this::openUpgradeMenu
                ))
                .addIngredient('H', createUpgradeItem(
                        graveyardSite,
                        "soul_harvest",
                        Material.SOUL_LANTERN,
                        "Soul Harvest",
                        "Increases soul drop chance",
                        THEME,
                        this::openUpgradeMenu
                ))
                .addIngredient('<', backButton(this::openWindow))
                .build();

        open(gui, "§8Graveyard Upgrades");
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
                        "# # # # # # # # #",
                        "# # # # ? # # # #",
                        "# # # # # # # # #",
                        "# . . . . . . . #",
                        "# . . . . . . . #",
                        "# # # # # # # < #"
                )
                .addIngredient('#', filler())
                .addIngredient('?', createSubMenuHelp(THEME, "Contracts",
                        "Complete contracts to earn",
                        "money and Graveyard XP."))
                .addIngredient('<', backButton(this::openWindow))
                .build();

        List<ActiveContract> contracts = core.getContractManager()
                .getContracts(enterprise, JobSiteType.GRAVEYARD);

        contracts.forEach(contract -> gui.addItems(createContractItem(contract, THEME)));

        open(gui, "§8Graveyard Contracts");
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