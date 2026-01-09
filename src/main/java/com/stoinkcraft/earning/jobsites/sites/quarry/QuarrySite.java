package com.stoinkcraft.earning.jobsites.sites.quarry;

import com.stoinkcraft.StoinkCore;
import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.earning.jobsites.JobSite;
import com.stoinkcraft.earning.jobsites.JobSiteType;
import com.stoinkcraft.earning.jobsites.JobSiteUpgrade;
import com.stoinkcraft.earning.jobsites.components.JobSiteHologram;
import com.stoinkcraft.earning.jobsites.components.JobSiteNPC;
import com.stoinkcraft.earning.jobsites.components.generators.MineGenerator;
import com.stoinkcraft.earning.jobsites.components.structures.PowerCellStructure;
import com.stoinkcraft.earning.jobsites.components.unlockable.UnlockableState;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import java.io.File;
import java.util.*;

public class QuarrySite extends JobSite {

    public static final Vector WELCOME_HOLOGRAM_OFFSET =
            new Vector(-3.5, 3, 0.5);

    public static final Vector MINE_CORNER_1_OFFSET =
            new Vector(-4, -1, -4);

    public static final Vector MINE_CORNER_2_OFFSET =
            new Vector(-25, -21, 17);

    public static final Vector MINER_BOB_OFFSET =
            new Vector(-0.5, 0, 4.5);

    // Deprecated - use ConfigLoader.getGenerators().getMineBaseRegenIntervalSeconds() instead
    @Deprecated
    public static final long DEFAULT_REGEN_INTERVAL_SECONDS = 60L * 60L * 2L; // 7200 seconds

    // XP per geode block mined (bonus XP for finding geodes)
    public static final int GEODE_XP_REWARD = 15;

    private static final String MINER_BOB_TEXTURE =  "ewogICJ0aW1lc3RhbXAiIDogMTY4MDE3NzE1NzgxMSwKICAicHJvZmlsZUlkIiA6ICJkOGNkMTNjZGRmNGU0Y2IzODJmYWZiYWIwOGIyNzQ4OSIsCiAgInByb2ZpbGVOYW1lIiA6ICJaYWNoeVphY2giLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTY5YmRmYmIzZGRiNTA3YThjYjZiZTUyMDU3MWU0YjZmMjExNjBjMzUwNTlkMTFmZGYzYTI5NjI5ZmU1N2Y1MCIKICAgIH0KICB9Cn0=";
    private static final String MINER_BOB_SIGNATURE = "FGpddwZXQZ8jfAyCYxXQg5k9u6xJBszvw/vsIHV8sPjnsyBJDChZwHKXfc5bo5Pro1mfQZwJ5oQqtFlRbR4Mp2hODJmj96C9HPMFIjYTMXXGHo0FcHQ8fJPz93/qnhMIZquDXdgqVzcFYz0ga+DwkPhsEUSiDx/qzka7nESev5Dd+/V1gpIT8a0MJL25xAov/yUXEZ71I2v01SeMa9nqhiDxCpiu8VhpIR2BLdDGPSMqQz4uL+rzYWXkzjByWZpoSRRLf5cMqWcBFaoRLlR6ILAhhOdNPBPll9UDcfRVUestDxo0xxfgPddgPWPAik8RbDG3sNooGFfjviAcI3XzSO5k/esE+QJ97WRR/FQ9da94Zn4dsS6nEWV4n6n6L3cqsRyoDeboT5n0Y6HXiyudsfmIHHho12uttY8jkPtx+/ZDC/aJWLMAW89n5TqWzhv6s6q9qdiidwmw2rIBwPUx9fiVnWHiDBsL0V3/c2QHlfaqJ2j3sP/BpXnS2chQGI9Ub40Igm1MJHH5J1zs/oNLblYQ0McWoEx57oXOVx5qSnc4TzL36zB5AqSf0NGjF68cS+FP0YD3YjWWBABdZ9E+C34C38Ps9OCDM42GuQHC2wADHh0heidDr4VAABYA1MqpP7cCNb0hzTwwJtgijjCQ9ni63zbBdHR1clcN/4bEhT8=";

    /* =========================
       COMPONENTS
       ========================= */

    private final String welcomeHologramId;
    private final MineGenerator mineGenerator;
    private final String mineRegionID;
    private final JobSiteNPC minerBob;
    private final PowerCellStructure powerCell;

    public QuarrySite(Enterprise enterprise, Location spawnPoint, QuarryData data) {
        super(
                enterprise,
                JobSiteType.QUARRY,
                spawnPoint,
                new File(
                        StoinkCore.getInstance().getDataFolder(),
                        "/schematics/quarry.schem"
                ),
                data,
                data.isBuilt()
        );

        welcomeHologramId =
                enterprise.getID() + "_" + JobSiteType.QUARRY.name() + "_welcome";

        mineRegionID =
                enterprise.getID() + "_" + JobSiteType.QUARRY.name() + "_mine";

        Location corner1 = spawnPoint.clone().add(MINE_CORNER_1_OFFSET);
        Location corner2 = spawnPoint.clone().add(MINE_CORNER_2_OFFSET);

        mineGenerator = new MineGenerator(
                corner1,
                corner2,
                this,
                mineRegionID
        );

        minerBob = createMinerBob(this);
        powerCell = new PowerCellStructure(this);

        registerUpgrades();
        registerComponents();
    }

    /* =========================
       COMPONENT REGISTRATION
       ========================= */

    private void registerComponents() {
        List<String> lines = new ArrayList<>();
        lines.add(ChatColor.AQUA + "" + ChatColor.BOLD + "Welcome to the Quarry");
        lines.add(ChatColor.WHITE + "Here you will mine ores and stone");
        lines.add(ChatColor.WHITE + "to complete resource contracts!");
        lines.add(ChatColor.WHITE + "Upgrade regen speed and unlock ores!");
        lines.add(" ");
        lines.add(ChatColor.GREEN + "Regenerates In: calculating...");

        addComponent(new JobSiteHologram(
                this,
                welcomeHologramId,
                WELCOME_HOLOGRAM_OFFSET,
                lines
        ));

        addComponent(mineGenerator);
        addComponent(minerBob);
        addComponent(powerCell);
    }

    /* =========================
       UPGRADES
       ========================= */

    private void registerUpgrades() {

        // =========================
        // REGENERATION SPEED
        // =========================
        // Reduces regen time from 2 hours down to 30 minutes at max
        // Each level = 10 minutes reduction

        upgrades.add(new JobSiteUpgrade(
                "regen_speed",
                "Regeneration Speed",
                10,                    // max level
                1,                     // base jobsite level (available immediately)
                3,                     // +3 per level
                // Level 1 @ JS1, Level 2 @ JS4, Level 3 @ JS7... Level 10 @ JS28
                lvl -> 3000 + (lvl * 2500), // 5500, 8000, 10500... 28000
                site -> true,
                (site, lvl) -> {}
        ));

        // =========================
        // POWER CELL LEVEL (Haste)
        // =========================
        // Requires Power Cell structure to be built first
        // Haste I, II, III for faster mining

        upgrades.add(new JobSiteUpgrade(
                "power_level",
                "Power Cell Level",
                3,                     // max level (Haste I, II, III)
                12,                    // base (Power Cell unlocks at 10, first upgrade at 12)
                4,                     // +4 per level
                // Level 1 @ JS12, Level 2 @ JS16, Level 3 @ JS20
                lvl -> 30_000 + (lvl * 25_000), // 55000, 80000, 105000
                site -> site.getData().getUnlockableState("powercell") == UnlockableState.UNLOCKED,
                (site, lvl) -> {
                    site.getEnterprise().sendEnterpriseMessage(
                            "§6§lPower Cell upgraded to Level " + lvl + "!",
                            "§eMiners now receive Haste " + toRoman(lvl) + "!"
                    );
                }
        ));

        // =========================
        // ORE SET UNLOCKS
        // =========================
        // Progressive unlocks gated by both level AND previous unlock

        upgrades.add(new JobSiteUpgrade(
                "unlock_stone_varieties",
                "Unlock Stone Varieties",
                1,
                5,                     // Level 5
                0,
                lvl -> 15_000,
                site -> true,
                (site, lvl) -> {}
        ));

        upgrades.add(new JobSiteUpgrade(
                "unlock_copper_collection",
                "Unlock Copper Collection",
                1,
                10,                    // Level 10
                0,
                lvl -> 35_000,
                site -> site.getData().getLevel("unlock_stone_varieties") > 0,
                (site, lvl) -> {}
        ));

        upgrades.add(new JobSiteUpgrade(
                "unlock_precious_metals",
                "Unlock Precious Metals",
                1,
                15,                    // Level 15
                0,
                lvl -> 60_000,
                site -> site.getData().getLevel("unlock_copper_collection") > 0,
                (site, lvl) -> {}
        ));

        upgrades.add(new JobSiteUpgrade(
                "unlock_deep_minerals",
                "Unlock Deep Minerals",
                1,
                20,                    // Level 20
                0,
                lvl -> 100_000,
                site -> site.getData().getLevel("unlock_precious_metals") > 0,
                (site, lvl) -> {}
        ));

        upgrades.add(new JobSiteUpgrade(
                "unlock_nether_resources",
                "Unlock Nether Resources",
                1,
                26,                    // Level 26
                0,
                lvl -> 175_000,
                site -> site.getData().getLevel("unlock_deep_minerals") > 0,
                (site, lvl) -> {}
        ));
    }

    private String toRoman(int num) {
        return switch (num) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            default -> String.valueOf(num);
        };
    }

    /* =========================
       ORE SET HELPERS
       ========================= */

    public boolean isOreSetUnlocked(OreSet oreSet) {
        return switch (oreSet) {
            case MINING_BASICS -> true; // Always unlocked
            case STONE_VARIETIES -> getData().getLevel("unlock_stone_varieties") > 0;
            case COPPER_COLLECTION -> getData().getLevel("unlock_copper_collection") > 0;
            case PRECIOUS_METALS -> getData().getLevel("unlock_precious_metals") > 0;
            case DEEP_MINERALS -> getData().getLevel("unlock_deep_minerals") > 0;
            case NETHER_RESOURCES -> getData().getLevel("unlock_nether_resources") > 0;
        };
    }

    public String getUnlockUpgradeId(OreSet oreSet) {
        return switch (oreSet) {
            case MINING_BASICS -> null;
            case STONE_VARIETIES -> "unlock_stone_varieties";
            case COPPER_COLLECTION -> "unlock_copper_collection";
            case PRECIOUS_METALS -> "unlock_precious_metals";
            case DEEP_MINERALS -> "unlock_deep_minerals";
            case NETHER_RESOURCES -> "unlock_nether_resources";
        };
    }

    /* =========================
       NPC CREATION
       ========================= */

    private JobSiteNPC createMinerBob(QuarrySite quarrySite) {
        return new JobSiteNPC(
                this,
                ChatColor.GOLD + "Miner Bob",
                MINER_BOB_OFFSET,
                MINER_BOB_TEXTURE,
                MINER_BOB_SIGNATURE
        ) {
            @Override
            public void onRightClick(NPCRightClickEvent event) {
                super.onRightClick(event);
                Player player = event.getClicker();
                new QuarryGui(quarrySite, player).openWindow();
            }
        };
    }

    /* =========================
       LIFECYCLE
       ========================= */

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    public QuarryData getData() {
        return (QuarryData) super.getData();
    }

    public MineGenerator getMineGenerator() {
        return mineGenerator;
    }

    public JobSiteNPC getMinerBob() {
        return minerBob;
    }

    public PowerCellStructure getPowerCell() {
        return powerCell;
    }

    public boolean isPowerCellBuilt() {
        return powerCell.isUnlocked();
    }
}