package com.stoinkcraft.earning.jobsites.sites.graveyard;

import com.fastasyncworldedit.core.FaweAPI;
import com.stoinkcraft.StoinkCore;
import com.stoinkcraft.config.ConfigLoader;
import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.earning.contracts.ContractContext;
import com.stoinkcraft.earning.jobsites.JobSite;
import com.stoinkcraft.earning.jobsites.JobSiteType;
import com.stoinkcraft.earning.jobsites.JobSiteUpgrade;
import com.stoinkcraft.earning.jobsites.components.JobSiteHologram;
import com.stoinkcraft.earning.jobsites.components.JobSiteNPC;
import com.stoinkcraft.earning.jobsites.components.generators.TombstoneGenerator;
import com.stoinkcraft.earning.jobsites.components.structures.MausoleumStructure;
import com.stoinkcraft.earning.jobsites.components.unlockable.UnlockableState;
import com.stoinkcraft.utils.ChatUtils;
import com.stoinkcraft.utils.RegionUtils;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GraveyardSite extends JobSite {

    /**
     * Holograms
     */
    private String welcomeHologramId;
    public static Vector welcomeHologramOffset = new Vector(-8.5, 3, 0.5);

    /**
     * Grave Keeper NPC
     */
    private JobSiteNPC graveKeeper;
    private String graveKeeperTexture = "ewogICJ0aW1lc3RhbXAiIDogMTcxNTcyMTkyMzQ2OCwKICAicHJvZmlsZUlkIiA6ICJmNmYwNmE2NGQ1Yzg0MjIzOGE3NjIwYTUxNzczOWI0ZCIsCiAgInByb2ZpbGVOYW1lIiA6ICJBbmFEaWFtb25kIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzJiYjZjNDhlOWFhNTJhNDU5YmY1NTA3MGI5MjRlYzJlOWVhMTUyODI2MzRlMzRmYjJjNzMxZjlhMzI3OTllOSIKICAgIH0KICB9Cn0="; // Add your skin texture
    private String graveKeeperSignature = "cO+T+xl0LkrgVuygADGM8L+W5TmK8jef350p5tZLsoubfbNAE8CIMVsScwIbyAUa9wuUXOcQLTmduneS/O3uvzDYy3gAfBKauVVPGJF4pxUpH7zLb6SD4ATZTtJhsWcevB4jCHNbo3fb+4sTtMefMoEyejsu5MPRNCKhhrMIY5rNiIredQ36I7rnZmt2JVnXkCksSfYMqcZXChYDxKL+2+S7EURLHc3Bmry+2VyVArQrLX59wh+G6ziufZgQagSNtz2Rdtk5/TiscrDW5Ot0ibaoBGikAuCDeNyzWPQ0qbg62P/ue7a/KbGP97J099BFkxsvXB/7KeADZD/uuLPliWZLyj+Zq2WP+Oie/DlGDL7FnBSZaOOYEc8IRGZHvdpHWxzS5B+J5m7YXweoorAA8k1dmN7rdPl9MSXee7OU2obMm0QvQi8aK9fDU9w7os3yOsy/oM0FRYxQE4xfzZOsbwF76+Jggv5FNPGTo5/klX1bnCY4q3mcLXfuKF25ncr2TDrwRq0OP8Rgsn6lC46OqSLFmvNLEGuTngY9qvqTF7IhVoJ1lr2L26NX67EmrkO3oBWb6EPnfIN7y+/LlH1znJXSxI5Mt+D8SPB8ejWYZaW4J8qhnlrVSXbfulHclHgcRxLCCErtwtSgD4yEjFc7OBJc5Sr6T/eo3W/Qi1EFzTU="; // Add your skin signature
    public static Vector graveKeeperOffset = new Vector(-2.5, 0, -1.5);

    /**
     * Tombstone Generators (27 total)
     */
    public static final int TOTAL_TOMBSTONES = 27;
    public static final int STARTING_TOMBSTONES = 4;

    // Tombstone pricing - exponential scaling
    private static final int BASE_TOMBSTONE_COST = 2_000;
    private static final double TOMBSTONE_COST_MULTIPLIER = 1.12; // Gentler curve

    /**
     * Soul drop chance (0.0 - 1.0)
     */
    public static final double SOUL_DROP_CHANCE = 0.25;

    private final List<TombstoneGenerator> tombstoneGenerators = new ArrayList<>();

    // Tombstone locations - adjust these based on your schematic
    public static final List<Vector> TOMBSTONE_OFFSETS = List.of(
            // Row 1
            new Vector(-9, 0.5, -4.5),
            new Vector(-13, 0.5, -4.5),
            new Vector(-17, 0.5, -4.5),
            new Vector(-21, 0.5, -4.5),
            new Vector(-25, 0.5, -4.5),
            new Vector(-29, 0.5, -4.5),
            new Vector(-33, 0.5, -4.5),
            new Vector(-37, 0.5, -4.5),

            // Row 2
            new Vector(-9, 0.5, 5.5),
            new Vector(-13, 0.5, 5.5),
            new Vector(-17, 0.5, 5.5),
            new Vector(-21, 0.5, 5.5),
            new Vector(-25, 0.5, 5.5),
            new Vector(-33, 0.5, 5.5),
            new Vector(-37, 0.5, 5.5),

            // Row 3
            new Vector(-9, 0.5, 10.5),
            new Vector(-13, 0.5, 10.5),
            new Vector(-21, 0.5, 10.5),
            new Vector(-25, 0.5, 10.5),

            // Row 4
            new Vector(-9, 0.5, 20),
            new Vector(-13, 0.5, 20),

            // Row 5
            new Vector(-9, 0.5, 28.3),
            new Vector(-17, 0.5, 28.3),
            new Vector(-21, 0.5, 28.3),
            new Vector(-29, 0.5, 28.3),
            new Vector(-33, 0.5, 28.3),
            new Vector(-37, 0.5, 28.3)
//
//            new Vector(-1, 30.5 -10),
//            new Vector(1, 30.5 -10),
//            new Vector(3, 30.5 -10),
//            new Vector(5, 30.5 -10)
    );

    /**
     * Mausoleum
     */
    private MausoleumStructure mausoleumStructure;
    public static Vector mausoleumHordeSpawnOffset = new Vector(-24, -8, 14); // Adjust based on schematic

    public GraveyardSite(Enterprise enterprise, Location spawnPoint, GraveyardData data) {
        super(enterprise, JobSiteType.GRAVEYARD, spawnPoint,
                new File(StoinkCore.getInstance().getDataFolder(), "/schematics/graveyard.schem"),
                data, data.isBuilt());

        welcomeHologramId = enterprise.getID() + "_" + JobSiteType.GRAVEYARD.name() + "_welcome";

        // Create tombstone generators
        for (int i = 0; i < TOTAL_TOMBSTONES && i < TOMBSTONE_OFFSETS.size(); i++) {
            Vector offset = TOMBSTONE_OFFSETS.get(i);
            Location tombstoneLoc = spawnPoint.clone().add(offset);
            TombstoneGenerator generator = new TombstoneGenerator(tombstoneLoc, this, i);

            // Enable if already purchased
            if (i < data.getTombstonesPurchased()) {
                generator.setEnabled(true);
            }

            tombstoneGenerators.add(generator);
        }

        graveKeeper = createGraveKeeper(this);

        mausoleumStructure = new MausoleumStructure(this, mausoleumHordeSpawnOffset);

        registerUpgrades();
        registerComponents();
    }

    private void registerComponents() {
        List<String> welcomeHologramLines = new ArrayList<>();
        welcomeHologramLines.add(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "Welcome to the Graveyard");
        welcomeHologramLines.add(ChatColor.WHITE + "Here you will slay the undead");
        welcomeHologramLines.add(ChatColor.WHITE + "to complete combat contracts!");
        welcomeHologramLines.add(ChatColor.WHITE + "Chat with the Grave Keeper to purchase");
        welcomeHologramLines.add(ChatColor.WHITE + "tombstones and upgrade your graveyard!");
        addComponent(new JobSiteHologram(this, welcomeHologramId, welcomeHologramOffset, welcomeHologramLines));

        tombstoneGenerators.forEach(this::addComponent);
        addComponent(graveKeeper);
        addComponent(mausoleumStructure);
    }

    private void registerUpgrades() {

        // =========================
        // SPAWN SPEED
        // =========================
        // Reduces time between spawns at each tombstone
        // Base: 30 seconds, Min: 5 seconds
        // Each level = -2.5 seconds

        upgrades.add(new JobSiteUpgrade(
                "spawn_speed",
                "Spawn Speed",
                10,                    // max level
                1,                     // base jobsite level
                3,                     // +3 per level
                // Level 1 @ JS1, Level 2 @ JS4... Level 10 @ JS28
                lvl -> 2500 + (lvl * 2000), // 4500, 6500, 8500... 22500
                site -> true,
                (site, lvl) -> {}
        ));

        // =========================
        // TOMBSTONE CAPACITY
        // =========================
        // Increases max purchasable tombstones beyond level-based cap
        // Each level = +2 additional tombstone slots

        upgrades.add(new JobSiteUpgrade(
                "tombstone_capacity",
                "Tombstone Capacity",
                10,
                3,                     // base level
                2,                     // +2 per level
                // Level 1 @ JS3, Level 2 @ JS5... Level 10 @ JS21
                lvl -> 4000 + (lvl * 3000), // 7000, 10000, 13000... 34000
                site -> true,
                (site, lvl) -> {}
        ));

        // =========================
        // MAUSOLEUM HORDE FREQUENCY
        // =========================
        // Reduces time between spider horde spawns
        // Requires Mausoleum to be built

        upgrades.add(new JobSiteUpgrade(
                "mausoleum_spawn_speed",
                "Horde Frequency",
                10,
                ConfigLoader.getStructures().getMausoleumRequiredLevel() + 2, // JS 17
                2,                     // +2 per level
                // Level 1 @ JS17, Level 2 @ JS19... Level 10 @ JS35
                lvl -> 8000 + (lvl * 6000), // 14000, 20000, 26000... 68000
                site -> site.getData().getUnlockableState("mausoleum") == UnlockableState.UNLOCKED,
                (site, lvl) -> {}
        ));

        // =========================
        // MAUSOLEUM HORDE SIZE
        // =========================
        // Increases spiders per horde (more risk, more reward)
        // Each level = +3 spiders

        upgrades.add(new JobSiteUpgrade(
                "mausoleum_horde_size",
                "Horde Size",
                10,
                ConfigLoader.getStructures().getMausoleumRequiredLevel() + 2, // JS 17
                2,                     // +2 per level
                // Level 1 @ JS17, Level 2 @ JS19... Level 10 @ JS35
                lvl -> 6000 + (lvl * 5000), // 11000, 16000, 21000... 56000
                site -> site.getData().getUnlockableState("mausoleum") == UnlockableState.UNLOCKED,
                (site, lvl) -> {}
        ));

        // =========================
        // SOUL HARVEST CHANCE (NEW)
        // =========================
        // Increases soul drop rate
        // Base: 25%, Max: 50% at level 10

        upgrades.add(new JobSiteUpgrade(
                "soul_harvest",
                "Soul Harvest",
                10,
                5,                     // base level
                2,                     // +2 per level
                // Level 1 @ JS5, Level 2 @ JS7... Level 10 @ JS23
                lvl -> 3000 + (lvl * 2500), // 5500, 8000, 10500... 28000
                site -> true,
                (site, lvl) -> {}
        ));
    }

    private JobSiteNPC createGraveKeeper(GraveyardSite graveyardSite) {
        return new JobSiteNPC(this,
                ChatColor.DARK_PURPLE + "Grave Keeper",
                graveKeeperOffset,
                graveKeeperTexture, graveKeeperSignature) {
            @Override
            public void onRightClick(NPCRightClickEvent event) {
                super.onRightClick(event);
                Player player = event.getClicker();
                new GraveyardGui(graveyardSite, player).openWindow();
                ChatUtils.sendMessage(player, ChatColor.DARK_PURPLE + "Opening Graveyard Management...");
            }
        };
    }

    // ==================== Tombstone Management ====================

    /**
     * Get the maximum number of tombstones that can be purchased at current level
     */
    public int getMaxPurchasableTombstones() {
        int level = getLevel();
        int capacityLevel = getData().getLevel("tombstone_capacity");

        // Base formula: starts at 4, gains roughly 1 per 1.5 levels
        // Level 1 = 4, Level 15 = ~13, Level 30 = ~23
        int baseCapacity = STARTING_TOMBSTONES + (int) ((level - 1) * 0.65);

        // Capacity upgrade adds extra slots (+2 per level)
        int bonusCapacity = capacityLevel * 2;

        return Math.min(TOTAL_TOMBSTONES, baseCapacity + bonusCapacity);
    }

    /**
     * Get the cost to purchase the next tombstone
     */
    public int getNextTombstoneCost() {
        int owned = getData().getTombstonesPurchased();
        int purchasedBeyondStart = Math.max(0, owned - STARTING_TOMBSTONES);
        return (int) (BASE_TOMBSTONE_COST * Math.pow(TOMBSTONE_COST_MULTIPLIER, purchasedBeyondStart));
    }


    /**
     * Purchase the next tombstone
     */
    public boolean purchaseTombstone(Player player) {
        GraveyardData data = getData();
        int owned = data.getTombstonesPurchased();

        if (owned >= getMaxPurchasableTombstones()) {
            return false;
        }

        if (owned >= TOTAL_TOMBSTONES) {
            return false;
        }

        int cost = getNextTombstoneCost();
        if (!StoinkCore.getEconomy().has(player, cost)) {
            return false;
        }

        StoinkCore.getEconomy().withdrawPlayer(player, cost);
        data.incrementTombstonesPurchased();

        // Enable the newly purchased tombstone
        if (owned < tombstoneGenerators.size()) {
            tombstoneGenerators.get(owned).setEnabled(true);
        }

        return true;
    }

    // ==================== Soul Handling ====================
    /**
     * Called when a graveyard mob is killed - handles soul drops
     */
    public void onMobKilled(Player killer, EntityType entityType) {
        if (Math.random() < getSoulDropChance()) {
            getData().addSouls(1);
            killer.sendMessage(ChatColor.LIGHT_PURPLE + "+1 Soul " + ChatColor.DARK_PURPLE + "✦ Graveyard");

            ContractContext soulContext = new ContractContext(
                    killer,
                    JobSiteType.GRAVEYARD,
                    "SOUL",
                    1
            );
            StoinkCore.getInstance().getContractManager()
                    .handleContext(enterprise, soulContext);
        }
    }

    public double getSoulDropChance() {
        int harvestLevel = getData().getLevel("soul_harvest");
        // Base 25% + 2.5% per level = max 50%
        return SOUL_DROP_CHANCE + (harvestLevel * 0.025);
    }

    // ==================== Overrides ====================

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    public void build() {
        super.build();
    }

    @Override
    public void disband() {
        super.disband();
    }

    @Override
    public GraveyardData getData() {
        return (GraveyardData) super.getData();
    }

    // ==================== Getters ====================

    public List<TombstoneGenerator> getTombstoneGenerators() {
        return tombstoneGenerators;
    }

    public MausoleumStructure getMausoleumStructure() {
        return mausoleumStructure;
    }

    public JobSiteNPC getGraveKeeper() {
        return graveKeeper;
    }

    public int getActiveTombstoneCount() {
        return (int) tombstoneGenerators.stream().filter(TombstoneGenerator::isEnabled).count();
    }
}
