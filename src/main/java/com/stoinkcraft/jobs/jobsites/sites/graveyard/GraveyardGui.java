package com.stoinkcraft.jobs.jobsites.sites.graveyard;

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
import com.stoinkcraft.jobs.jobsites.components.generators.TombstoneGenerator;
import com.stoinkcraft.jobs.jobsites.components.structures.MausoleumStructure;
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

public class GraveyardGui {

    private final GraveyardSite graveyardSite;
    private final Player opener;
    private Window currentWindow;

    // ==================== Color Constants ====================
    private static final String HEADER = "§8§l» §5§l";
    private static final String SUB_HEADER = "§8§l» §d";
    private static final String BULLET = " §7• ";
    private static final String CHECKMARK = "§a✔ ";
    private static final String CROSS = "§c✖ ";
    private static final String ARROW = "§e▶ ";
    private static final String DIVIDER = " ";

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
                        "# T A M U # S C #",
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
                .addIngredient('S', menuButton(Material.SPIDER_SPAWN_EGG, "§4Spawn Horde",
                        List.of("Cost: 350 souls"), () -> {
                            graveyardSite.getData().addSouls(-10);
                            graveyardSite.getMausoleumStructure().spawnHorde(true);
                        }))
                .build();

        open(gui, "§8Graveyard Menu");
    }

    private SimpleItem createHelpItem() {
        GraveyardData data = graveyardSite.getData();
        int xp = data.getXp();
        int level = JobsiteLevelHelper.getLevelFromXp(xp);
        int xpToNext = JobsiteLevelHelper.getXpToNextLevel(xp);
        int souls = data.getSouls();

        return new SimpleItem(new ItemBuilder(Material.OAK_SIGN)
                .setDisplayName(HEADER + "Graveyard Help §8«")
                .addLoreLines(DIVIDER)
                .addLoreLines(SUB_HEADER + "Jobsite Progress §8«")
                .addLoreLines(BULLET + "§fLevel: §a" + level)
                .addLoreLines(BULLET + "§fXP to next: §a" + String.format("%,d", xpToNext) + " XP")
                .addLoreLines(BULLET + "§fSouls: §d" + String.format("%,d", souls) + " ✦")
                .addLoreLines(DIVIDER)
                .addLoreLines(SUB_HEADER + "How It Works §8«")
                .addLoreLines(BULLET + "§fSlay undead mobs for §acontract progress")
                .addLoreLines(BULLET + "§fMobs have a chance to drop §dSouls")
                .addLoreLines(BULLET + "§fUse Souls to §aattune tombstones")
                .addLoreLines(DIVIDER)
                .addLoreLines(SUB_HEADER + "Tombstones §8«")
                .addLoreLines(BULLET + "§fPurchase more with §6money")
                .addLoreLines(BULLET + "§fAttune to spawn §cspecific mobs")
                .addLoreLines(BULLET + "§fHigher level = §amore tombstones")
        );
    }

    // ==================== Tombstone Menu ====================

    private void openTombstoneMenu() {
        GraveyardData data = graveyardSite.getData();
        int owned = data.getTombstonesPurchased();
        int max = graveyardSite.getMaxPurchasableTombstones();
        int cost = graveyardSite.getNextTombstoneCost();

        Gui gui = Gui.normal()
                .setStructure(
                        "# # # # # # # # #",
                        "# # # # ? # # # #",
                        "# # # # # # # # #",
                        "# # # # P # # # #",
                        "# # # # # # # < #"
                )
                .addIngredient('#', filler())
                .addIngredient('?', createSubMenuHelp("Tombstones",
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

                item.addLoreLines(SUB_HEADER + "Status §8«");
                item.addLoreLines(BULLET + "§fOwned: §a" + owned + "§7/§f" + total);
                item.addLoreLines(BULLET + "§fAvailable: §e" + max);
                item.addLoreLines(BULLET + createProgressBar(owned, total));
                item.addLoreLines(DIVIDER);

                if (owned >= total) {
                    item.addLoreLines(CHECKMARK + "§aAll tombstones purchased!");
                } else if (owned >= max) {
                    item.addLoreLines(CROSS + "§cLevel up to unlock more!");
                    item.addLoreLines(BULLET + "§7Upgrade Tombstone Capacity");
                } else {
                    item.addLoreLines(SUB_HEADER + "Next Purchase §8«");
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
                    sendInfo(p, "All tombstones are already purchased!");
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

                notifyWindows();
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
                //.addIngredient('P', createAttunementTypeItem(UndeadMobType.PHANTOM))
                .addIngredient('<', backButton(this::openWindow))
                .build();

        open(gui, "§8Attunements");
    }

    private SimpleItem createAttunementHelp() {
        GraveyardData data = graveyardSite.getData();
        int souls = data.getSouls();

        return new SimpleItem(new ItemBuilder(Material.OAK_SIGN)
                .setDisplayName(HEADER + "Attunements §8«")
                .addLoreLines(DIVIDER)
                .addLoreLines(SUB_HEADER + "Your Souls §8«")
                .addLoreLines(BULLET + "§d" + String.format("%,d", souls) + " ✦")
                .addLoreLines(DIVIDER)
                .addLoreLines(SUB_HEADER + "How Attunements Work §8«")
                .addLoreLines(BULLET + "§fSelect a mob type below")
                .addLoreLines(BULLET + "§fThen click a tombstone in-world")
                .addLoreLines(BULLET + "§fOr use the tombstone selector GUI")
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

                    item.addLoreLines(SUB_HEADER + "Requirements §8«");
                    item.addLoreLines(BULLET + "§fCost: §d" + type.getSoulCost() + " Souls");
                    item.addLoreLines(BULLET + "§fRequired Level: §e" + type.getRequiredLevel());
                    item.addLoreLines(DIVIDER);

                    if (!meetsLevel) {
                        item.addLoreLines(CROSS + "§cYour level: §f" + level);
                    } else if (!canAfford) {
                        item.addLoreLines(CROSS + "§cNot enough souls!");
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
        // Create a scrollable GUI showing all tombstones
        List<TombstoneGenerator> tombstones = graveyardSite.getTombstoneGenerators();

        Gui.Builder.Normal builder = Gui.normal()
                .setStructure(
                        "# # # # ? # # # #",
                        ". . . . . . . . .",
                        ". . . . . . . . .",
                        ". . . . . . . . .",
                        "# # # # # # # < #"
                )
                .addIngredient('#', filler())
                .addIngredient('?', createSubMenuHelp("Select Tombstone",
                        "Attuning to: §d" + targetType.getDisplayName(),
                        targetType.requiresAttunement() ? "Cost: §d" + targetType.getSoulCost() + " Souls" : "§aFree"))
                .addIngredient('<', backButton(this::openAttunementMenu));

        Gui gui = builder.build();

        // Add tombstone items
        for (int i = 0; i < tombstones.size(); i++) {
            TombstoneGenerator tombstone = tombstones.get(i);
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
                    sendInfo(p, "Already attuned to " + targetType.getDisplayName() + "!");
                    return;
                }

                if (tombstone.setAttunement(targetType)) {
                    if (targetType == UndeadMobType.RANDOM) {
                        sendSuccess(p, "Tombstone #" + (tombstone.getIndex() + 1) + " attunement removed!");
                    } else {
                        sendSuccess(p, "Tombstone #" + (tombstone.getIndex() + 1) + " attuned to " + targetType.getDisplayName() + "!");
                    }
                    openTombstoneSelector(targetType); // Refresh
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
        Gui gui = Gui.normal()
                .setStructure(
                        "# # # # ? # # # #",
                        "# # # M S H # # #",
                        "# # # # # # # < #"
                )
                .addIngredient('#', filler())
                .addIngredient('?', createSubMenuHelp("Mausoleum",
                        "The Mausoleum spawns spider hordes",
                        "that reward XP and money when killed."))
                .addIngredient('M', createUnlockableItem(graveyardSite.getMausoleumStructure()))
                .addIngredient('S', createMausoleumUpgradeItem("mausoleum_spawn_speed", Material.CLOCK, "Horde Frequency",
                        "Reduce time between hordes"))
                .addIngredient('H', createMausoleumUpgradeItem("mausoleum_horde_size", Material.SPIDER_EYE, "Horde Size",
                        "Increase spiders per horde"))
                .addIngredient('<', backButton(this::openWindow))
                .build();

        open(gui, "§8Mausoleum");
    }

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
        int jobsiteLevel = graveyardSite.getLevel();

        ItemBuilder item = new ItemBuilder(getMaterialForState(unlockable, state));
        item.setDisplayName("§5" + unlockable.getDisplayName());
        item.addLoreLines("§7Spawns spider hordes for rewards");
        item.addLoreLines(DIVIDER);

        switch (state) {
            case LOCKED -> {
                item.addLoreLines(SUB_HEADER + "Requirements §8«");
                item.addLoreLines(BULLET + "§fCost: §6$" + String.format("%,d", unlockable.getCost()));
                item.addLoreLines(BULLET + "§fRequired Level: §e" + unlockable.getRequiredJobsiteLevel());
                item.addLoreLines(BULLET + "§fBuild Time: §e" + ChatUtils.formatDuration(unlockable.getBuildTimeMillis()));
                item.addLoreLines(DIVIDER);

                if (jobsiteLevel < unlockable.getRequiredJobsiteLevel()) {
                    item.addLoreLines(CROSS + "§cYour level: §f" + jobsiteLevel);
                } else {
                    item.addLoreLines(CHECKMARK + "§aReady to build!");
                }

                item.addLoreLines(DIVIDER);
                item.addLoreLines(ARROW + "Click to start construction");
            }
            case BUILDING -> {
                UnlockableProgress progress = graveyardSite.getData()
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
                MausoleumStructure mausoleum = graveyardSite.getMausoleumStructure();
                item.addLoreLines(CHECKMARK + "§aConstruction Complete");
                item.addLoreLines(DIVIDER);
                item.addLoreLines(SUB_HEADER + "Status §8«");
                if (mausoleum.isHordeActive()) {
                    item.addLoreLines(BULLET + "§cHorde Active! §7(" + mausoleum.getActiveSpiderCount() + " spiders)");
                } else {
                    item.addLoreLines(BULLET + "§7Waiting for next horde...");
                }
            }
        }

        return item;
    }

    private void handleUnlockableClick(Unlockable unlockable, Player player) {
        UnlockableState state = unlockable.getUnlockState();

        switch (state) {
            case LOCKED -> {
                if (graveyardSite.purchaseUnlockable(unlockable, player)) {
                    sendSuccess(player, unlockable.getDisplayName() + " construction started!");
                } else if (graveyardSite.getLevel() < unlockable.getRequiredJobsiteLevel()) {
                    sendError(player, "You need Graveyard Level " + unlockable.getRequiredJobsiteLevel() + "!");
                } else if (!StoinkCore.getEconomy().has(player, unlockable.getCost())) {
                    sendError(player, "You need $" + String.format("%,d", unlockable.getCost()) + "!");
                } else {
                    sendError(player, "Requirements not met!");
                }
            }
            case BUILDING -> {
                UnlockableProgress progress = graveyardSite.getData()
                        .getUnlockableProgress(unlockable.getUnlockableId());
                sendInfo(player, "Under construction - " + ChatUtils.formatDuration(progress.getRemainingSeconds()) + " remaining");
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
            case UNLOCKED -> Material.STONE_BRICKS;
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

    private AbstractItem createMausoleumUpgradeItem(String upgradeId, Material mat, String name, String description) {
        return new AbstractItem() {
            @Override
            public ItemProvider getItemProvider() {
                JobSiteUpgrade upgrade = findUpgrade(upgradeId);
                if (upgrade == null) {
                    return new ItemBuilder(Material.BARRIER).setDisplayName("§cUpgrade not found");
                }

                boolean mausoleumBuilt = graveyardSite.getMausoleumStructure().isUnlocked();
                int currentLevel = graveyardSite.getData().getLevel(upgradeId);
                int maxLevel = upgrade.maxLevel();
                int jobsiteLevel = graveyardSite.getLevel();
                boolean maxed = currentLevel >= maxLevel;

                ItemBuilder item = new ItemBuilder(mat);
                item.setDisplayName("§5" + name);
                item.addLoreLines("§7" + description);
                item.addLoreLines(DIVIDER);

                if (!mausoleumBuilt) {
                    item.addLoreLines(CROSS + "§cBuild the Mausoleum first!");
                    return item;
                }

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
                if (!graveyardSite.getMausoleumStructure().isUnlocked()) {
                    sendError(p, "Build the Mausoleum first!");
                    return;
                }

                JobSiteUpgrade upgrade = findUpgrade(upgradeId);
                if (upgrade == null) return;

                int currentLevel = graveyardSite.getData().getLevel(upgradeId);
                if (currentLevel >= upgrade.maxLevel()) {
                    sendInfo(p, name + " is already at max level!");
                    return;
                }

                if (graveyardSite.purchaseUpgrade(upgrade, p)) {
                    int newLevel = graveyardSite.getData().getLevel(upgradeId);
                    sendSuccess(p, name + " upgraded to level " + newLevel + "!");
                } else {
                    sendPurchaseError(p, upgrade);
                }

                notifyWindows();
            }
        };
    }

    // ==================== Upgrade Menu ====================

    private void openUpgradeMenu() {
        Gui gui = Gui.normal()
                .setStructure(
                        "# # # # ? # # # #",
                        "# # # S C . # # #",
                        "# # # # # # # < #"
                )
                .addIngredient('#', filler())
                .addIngredient('?', createSubMenuHelp("Upgrades",
                        "Purchase upgrades to improve",
                        "your graveyard's efficiency."))
                .addIngredient('S', createUpgradeItem("spawn_speed", Material.FEATHER, "Spawn Speed",
                        "Reduce time between mob spawns"))
                .addIngredient('C', createUpgradeItem("tombstone_capacity", Material.CHEST, "Tombstone Capacity",
                        "Unlock more tombstones for purchase"))
                .addIngredient('<', backButton(this::openWindow))
                .build();

        open(gui, "§8Graveyard Upgrades");
    }

    private AbstractItem createUpgradeItem(String upgradeId, Material mat, String name, String description) {
        return new AbstractItem() {
            @Override
            public ItemProvider getItemProvider() {
                JobSiteUpgrade upgrade = findUpgrade(upgradeId);
                if (upgrade == null) {
                    return new ItemBuilder(Material.BARRIER).setDisplayName("§cUpgrade not found");
                }

                int currentLevel = graveyardSite.getData().getLevel(upgradeId);
                int maxLevel = upgrade.maxLevel();
                int jobsiteLevel = graveyardSite.getLevel();
                boolean maxed = currentLevel >= maxLevel;

                ItemBuilder item = new ItemBuilder(mat);
                item.setDisplayName("§5" + name);
                item.addLoreLines("§7" + description);
                item.addLoreLines(DIVIDER);

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

                int currentLevel = graveyardSite.getData().getLevel(upgradeId);
                if (currentLevel >= upgrade.maxLevel()) {
                    sendInfo(p, name + " is already at max level!");
                    return;
                }

                if (graveyardSite.purchaseUpgrade(upgrade, p)) {
                    int newLevel = graveyardSite.getData().getLevel(upgradeId);
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

    private String createProgressBar(int current, int max) {
        int filled = (int) ((current / (double) max) * 10);
        return "§a" + "▌".repeat(filled) + "§7" + "▌".repeat(10 - filled) + " §f" + current + "/" + max;
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
                .addIngredient('?', createSubMenuHelp("Contracts",
                        "Complete contracts to earn",
                        "money and Graveyard XP."))
                .addIngredient('<', backButton(this::openWindow))
                .build();

        List<ActiveContract> contracts = core.getContractManager()
                .getContracts(enterprise, JobSiteType.GRAVEYARD);

        contracts.forEach(contract -> gui.addItems(createContractItem(contract)));

        open(gui, "§8Graveyard Contracts");
    }

    private SimpleItem createContractItem(ActiveContract contract) {
        ContractDefinition def = contract.getDefinition();
        boolean completed = contract.isCompleted();

        ItemBuilder builder = new ItemBuilder(def.displayItem());

        // Title
        if (completed) {
            builder.setDisplayName(CHECKMARK + "§a" + def.displayName());
        } else {
            builder.setDisplayName("§d" + def.displayName());
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
            if (line.startsWith("§")) {
                item.addLoreLines(line);
            } else {
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
        return graveyardSite.getUpgrades().stream()
                .filter(u -> u.id().equals(id))
                .findFirst()
                .orElse(null);
    }

    private void open(Gui gui, String title) {
        currentWindow = Window.single()
                .setViewer(opener)
                .setTitle(title)
                .setGui(gui)
                .build();
        currentWindow.open();
    }

//    private void notifyWindows() {
//        if (currentWindow != null) {
//            currentWindow.notifyWindows();
//        }
//    }

    // ==================== Messages ====================

    private void sendSuccess(Player p, String message) {
        ChatUtils.sendMessage(p, "§a✔ " + message);
    }

    private void sendError(Player p, String message) {
        ChatUtils.sendMessage(p, "§c✖ " + message);
    }

    private void sendInfo(Player p, String message) {
        ChatUtils.sendMessage(p, "§d" + message);
    }

    private void sendPurchaseError(Player p, JobSiteUpgrade upgrade) {
        int jobsiteLevel = graveyardSite.getLevel();

        if (jobsiteLevel < upgrade.requiredJobsiteLevel()) {
            sendError(p, "You need Graveyard Level " + upgrade.requiredJobsiteLevel() + "!");
        } else if (!StoinkCore.getEconomy().has(p, upgrade.cost(graveyardSite.getData().getLevel(upgrade.id()) + 1))) {
            sendError(p, "Insufficient funds!");
        } else if (!upgrade.canUnlock(graveyardSite)) {
            sendError(p, "Requirements not met!");
        } else {
            sendError(p, "Unable to purchase!");
        }
    }
}