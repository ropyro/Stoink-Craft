package com.stoinkcraft.jobsites.sites.farmland;

import com.stoinkcraft.StoinkCore;
import com.stoinkcraft.jobsites.components.ambiance.BeeAmbience;
import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.jobsites.sites.JobSite;
import com.stoinkcraft.jobsites.sites.JobSiteType;
import com.stoinkcraft.jobsites.sites.JobSiteUpgrade;
import com.stoinkcraft.jobsites.components.GreenhouseHologram;
import com.stoinkcraft.jobsites.components.JobSiteHologram;
import com.stoinkcraft.jobsites.components.JobSiteNPC;
import com.stoinkcraft.jobsites.components.generators.GreenhouseGenerator;
import com.stoinkcraft.jobsites.components.generators.HoneyGenerator;
import com.stoinkcraft.jobsites.components.generators.PassiveMobGenerator;
import com.stoinkcraft.jobsites.components.structures.BarnStructure;
import com.stoinkcraft.jobsites.components.structures.BeeHiveStructure;
import com.stoinkcraft.jobsites.components.unlockable.UnlockableState;
import com.stoinkcraft.utils.ChatUtils;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FarmlandSite extends JobSite {

    /**
     * Holograms
     */
    private String welcomeHologramId;
    public static Vector welcomeHologramOffset = new Vector(-3.5, 3, 0.5);

    /**
     * Farmer Joe NPC
     */
    private JobSiteNPC farmerJoe;
    private String farmerJoeTexture = "ewogICJ0aW1lc3RhbXAiIDogMTc0NDA5MzMxMTMxMCwKICAicHJvZmlsZUlkIiA6ICJiOWIzY2RlZmIyZmQ0YWY1ODQxMGViZWZjY2ZmYTBhYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJpbnRlcnNlY2F0byIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9kY2Y1Mzk3YmEwNTc5ZTI3NGMxZDJhN2I4M2ExMmU1MDQ0NDBjYjQzOTgzODZkNTI4MTk5NmQ0MWMwNjc3N2M1IgogICAgfQogIH0KfQ==";
    private String farmerJoeSignature = "k+SC0/ie439qZpjQQXSiYWRP4MWW4RLOsPdhc0KB/YBmJZGs1K/KhVtRrn1KFFV263foaBjEQtE/yoX9L5VYKmgJmTxscpDlX0KwnTVpZgDOwTU0rGUxg0VElmyqxRt49FH7UzJeuFs880jzHDBxuoRw28gHOMkaiE2WtdSDOXF6KcfwyZbZ/IlybI6ydcgzsVe6L8OXJuVEFStEuaPoE27qzz4OZX5wrYpW4FtmGIkISVXIEgh4Cd+R/toaXBLV7Egz/IuWrueihUv48QXv3lbPSncCuOcpqIjfJ+JSR1CcvkypbqhKdBMko7hTH77libQrz1k79Ghtppjw7cC6/tRdPAqOtNSAPk82nHbogctI7X7RBv+5ETtKK2nw8ckTyuqikgICYwjbmDNhhuSZHodb16pQy9LaGPXqi5ti4TgMFxsY98+Yys4N1Fz0WuMl1UDm44mjmH4o1aqsjeZKem/cqZbh3rppzLGZ/4lhmooTChGfPIONGCPdpgDh1yxzw8k96RNpG0bDJo5VzQB5LzuENiHgi1vBxFXdAQii5o7XZd6SPexmmwz4BNGymebjhnQ/VSj8PfTpF/SBBEYoJF3T7WR6Y/8UpbqDCbUQJhHxRSGu+qTg5CX2nkq1hw4bhKklOGRRlC0retK7oYGJhE3aJSY8m+wLQeJGL19+A8Y=";
    public static Vector farmerJoeOffset = new Vector(-27, 0, -14);

    /**
     * Greenhouses
     */
    private Map<Integer, GreenhouseGenerator> greenhouses = new HashMap<>();
    private Map<Integer, GreenhouseHologram> greenhouseHolograms = new HashMap<>();

    // Greenhouse 1 - Always unlocked
    public static Vector greenhouse1Corner1Offset = new Vector(-28, 1, -18);
    public static Vector greenhouse1Corner2Offset = new Vector(-32, 1, -43);
    public static Vector greenhouse1HologramOffset = new Vector(-29.5, 2.5, -15.5);

    // Greenhouse 2 - Unlocked at level 8
    public static Vector greenhouse2Corner1Offset = new Vector(-38, 1, -18);
    public static Vector greenhouse2Corner2Offset = new Vector(-42, 1, -43);
    public static Vector greenhouse2HologramOffset = new Vector(-39.5, 2.5, -15.5);

    // Greenhouse 3 - Unlocked at level 15
    public static Vector greenhouse3Corner1Offset = new Vector(-48, 1, -18);
    public static Vector greenhouse3Corner2Offset = new Vector(-52, 1, -43);
    public static Vector greenhouse3HologramOffset = new Vector(-49.5, 2.5, -15.5);

    /**
     * Farm animal generator
     */
    private String mobRegionID;
    private PassiveMobGenerator mobGenerator;
    public static Vector mobGenCorner1Offset = new Vector(-47, -1, 7);
    public static Vector mobGenCorner2Offset = new Vector(-51, 5, 3);

    /**
     * Honey Generators
     */
    private List<HoneyGenerator> honeyGenerators = new ArrayList<>();
    public static final List<Vector> HONEY_HIVE_OFFSETS = List.of(
            new Vector(-10, 1, -37),
            new Vector(-9, 1, -38),
            new Vector(-9, 1, -37),
            new Vector(-10, 1, -38),

            new Vector(-10, 1, -42),
            new Vector(-10, 1, -43),
            new Vector(-9, 1, -42),
            new Vector(-9, 1, -43),

            new Vector(-6, 1, -37),
            new Vector(-5, 1, -37),
            new Vector(-4, 1, -37),
            new Vector(-6, 1, -38),
            new Vector(-5, 1, -38),
            new Vector(-4, 1, -38),

            new Vector(-6, 1, -42),
            new Vector(-5, 1, -42),
            new Vector(-4, 1, -42),
            new Vector(-6, 1, -43),
            new Vector(-5, 1, -43),
            new Vector(-4, 1, -43),

            new Vector(-1, 1, -37),
            new Vector(0, 1, -37),
            new Vector(-1, 1, -38),
            new Vector(0, 1, -38),

            new Vector(-1, 1, -42),
            new Vector(0, 1, -42),
            new Vector(-1, 1, -43),
            new Vector(0, 1, -43)
            // add more as needed
    );

    /**
     * Barn
     */
    private BarnStructure barnStructure;

    /**
     * Bee Hives
     */
    private BeeHiveStructure beeHiveStructure;

    public FarmlandSite(Enterprise enterprise, Location spawnPoint, FarmlandData data) {
        super(enterprise, JobSiteType.FARMLAND, spawnPoint,
                new File(StoinkCore.getInstance().getDataFolder(), "/schematics/farmland.schem"),
                data, data.isBuilt());

        welcomeHologramId = enterprise.getID() + "_" + JobSiteType.FARMLAND.name() + "_welcome";

        createGreenhouse(1, spawnPoint, enterprise,
                greenhouse1Corner1Offset, greenhouse1Corner2Offset, greenhouse1HologramOffset);
        createGreenhouse(2, spawnPoint, enterprise,
                greenhouse2Corner1Offset, greenhouse2Corner2Offset, greenhouse2HologramOffset);
        createGreenhouse(3, spawnPoint, enterprise,
                greenhouse3Corner1Offset, greenhouse3Corner2Offset, greenhouse3HologramOffset);

        mobRegionID = enterprise.getID() + "_" + JobSiteType.FARMLAND.name() + "_mobs";
        mobGenerator = new PassiveMobGenerator(
                spawnPoint.clone().add(mobGenCorner1Offset),
                spawnPoint.clone().add(mobGenCorner2Offset),
                this,
                mobRegionID,
                new Vector(-46.5, 3.25, 4.5)
        );

        farmerJoe = createFarmerJoe(this);

        barnStructure = new BarnStructure(this);

        beeHiveStructure = new BeeHiveStructure(this);
        for (Vector offset : HONEY_HIVE_OFFSETS) {
            Location hiveLoc = spawnPoint.clone().add(offset);
            honeyGenerators.add(new HoneyGenerator(hiveLoc, this));
        }

        registerUpgrades();
        registerComponents();
    }

    private void createGreenhouse(int index, Location spawnPoint, Enterprise enterprise,
                                   Vector corner1Offset, Vector corner2Offset, Vector hologramOffset) {
        String regionID = enterprise.getID() + "_" + JobSiteType.FARMLAND.name() + "_greenhouse_" + index;

        GreenhouseGenerator greenhouse = new GreenhouseGenerator(
                spawnPoint.clone().add(corner1Offset),
                spawnPoint.clone().add(corner2Offset),
                this,
                regionID,
                index
        );
        greenhouses.put(index, greenhouse);

        GreenhouseHologram hologram = new GreenhouseHologram(this, greenhouse, hologramOffset);
        greenhouseHolograms.put(index, hologram);
    }

    private void registerComponents(){
        List<String> welcomeHologramLines = new ArrayList<>();
        welcomeHologramLines.add(ChatColor.AQUA + "" + ChatColor.BOLD + "Welcome to the Farmland");
        welcomeHologramLines.add(ChatColor.WHITE + "Here you will harvest crops and butcher meat");
        welcomeHologramLines.add(ChatColor.WHITE + "to complete resource collection contracts!");
        welcomeHologramLines.add(ChatColor.WHITE + "Chat with Farmer Joe to upgrade your farmland's");
        welcomeHologramLines.add(ChatColor.WHITE + "crop grow speed and unlock new crops and animals!");
        addComponent(new JobSiteHologram(this, "welcome", welcomeHologramOffset, welcomeHologramLines));

        // Add all greenhouses and their holograms
        greenhouses.values().forEach(this::addComponent);
        greenhouseHolograms.values().forEach(this::addComponent);

        addComponent(mobGenerator);
        addComponent(farmerJoe);
        addComponent(barnStructure);
        addComponent(beeHiveStructure);
        addComponent(new BeeAmbience(StoinkCore.getInstance(), this, honeyGenerators));

        honeyGenerators.forEach(gen -> addComponent(gen));
    }

    private void registerUpgrades() {


        upgrades.add(new JobSiteUpgrade(
                "unlock_greenhouse_2",
                "Unlock Greenhouse 2",
                1,
                8,
                0,
                lvl -> 15_000,
                site -> true,
                (site, lvl) -> {
                    FarmlandSite farmland = (FarmlandSite) site;
                    GreenhouseGenerator gh = farmland.getGreenhouse(2);
                    GreenhouseHologram hologram = farmland.getGreenhouseHologram(2);
                    if (gh != null) gh.onUnlock();
                    if (hologram != null) hologram.onGreenhouseUnlock();
                }
        ));

        upgrades.add(new JobSiteUpgrade(
                "unlock_greenhouse_3",
                "Unlock Greenhouse 3",
                1,
                15,
                0,
                lvl -> 40_000,
                site -> site.getData().getLevel("unlock_greenhouse_2") > 0,
                (site, lvl) -> {
                    FarmlandSite farmland = (FarmlandSite) site;
                    GreenhouseGenerator gh = farmland.getGreenhouse(3);
                    GreenhouseHologram hologram = farmland.getGreenhouseHologram(3);
                    if (gh != null) gh.onUnlock();
                    if (hologram != null) hologram.onGreenhouseUnlock();
                }
        ));


        upgrades.add(new JobSiteUpgrade(
                "greenhouse_1_growth_speed",
                "Greenhouse 1 Growth Speed",
                10,
                1,
                3,
                lvl -> 1000 + (lvl * 1500),
                site -> true,
                (site, lvl) -> {}
        ));

        upgrades.add(new JobSiteUpgrade(
                "greenhouse_2_growth_speed",
                "Greenhouse 2 Growth Speed",
                10,
                8,
                3,
                lvl -> 1500 + (lvl * 1800),
                site -> site.getData().getLevel("unlock_greenhouse_2") > 0,
                (site, lvl) -> {}
        ));

        upgrades.add(new JobSiteUpgrade(
                "greenhouse_3_growth_speed",
                "Greenhouse 3 Growth Speed",
                10,
                15,
                3,
                lvl -> 2000 + (lvl * 2200),
                site -> site.getData().getLevel("unlock_greenhouse_3") > 0,
                (site, lvl) -> {}
        ));


        upgrades.add(new JobSiteUpgrade(
                "unlock_carrot",
                "Unlock Carrots",
                1,
                5,
                0,
                lvl -> 8_000,
                site -> true,
                (site, lvl) -> {}
        ));

        upgrades.add(new JobSiteUpgrade(
                "unlock_potato",
                "Unlock Potatoes",
                1,
                12,
                0,
                lvl -> 25_000,
                site -> site.getData().getLevel("unlock_carrot") > 0,
                (site, lvl) -> {}
        ));

        upgrades.add(new JobSiteUpgrade(
                "unlock_beetroot",
                "Unlock Beetroots",
                1,
                22,
                0,
                lvl -> 60_000,
                site -> site.getData().getLevel("unlock_potato") > 0,
                (site, lvl) -> {}
        ));


        upgrades.add(new JobSiteUpgrade(
                "mob_spawn_speed",
                "Animal Spawn Speed",
                10,
                3,
                2,
                lvl -> 2000 + (lvl * 2000),
                site -> true,
                (site, lvl) -> {}
        ));


        upgrades.add(new JobSiteUpgrade(
                "mob_capacity",
                "Animal Capacity",
                10,
                4,
                2,
                lvl -> 3000 + (lvl * 2500),
                site -> true,
                (site, lvl) -> {}
        ));

        upgrades.add(new JobSiteUpgrade(
                "unlock_sheep",
                "Unlock Sheep",
                1,
                10,
                0,
                lvl -> 12_000,
                site -> true,
                (site, lvl) -> {}
        ));

        upgrades.add(new JobSiteUpgrade(
                "unlock_pig",
                "Unlock Pigs",
                1,
                14,
                0,
                lvl -> 22_000,
                site -> site.getData().getLevel("unlock_sheep") > 0,
                (site, lvl) -> {}
        ));

        upgrades.add(new JobSiteUpgrade(
                "unlock_chicken",
                "Unlock Chickens",
                1,
                18,
                0,
                lvl -> 35_000,
                site -> site.getData().getLevel("unlock_pig") > 0,
                (site, lvl) -> {}
        ));

        upgrades.add(new JobSiteUpgrade(
                "unlock_horse",
                "Unlock Horses",
                1,
                24,
                0,
                lvl -> 75_000,
                site -> site.getData().getLevel("unlock_chicken") > 0,
                (site, lvl) -> {}
        ));


        upgrades.add(new JobSiteUpgrade(
                "honey_speed",
                "Honey Generation Speed",
                10,
                20,
                2,
                lvl -> 5000 + (lvl * 4000),
                site -> site.getData().getUnlockableState("beehive") == UnlockableState.UNLOCKED,
                (site, lvl) -> {}
        ));
    }

    private JobSiteNPC createFarmerJoe(FarmlandSite farmlandSite){
        return new JobSiteNPC(this,
                ChatColor.GREEN + "Farmer Joe",
                farmerJoeOffset,
                farmerJoeTexture, farmerJoeSignature){
            @Override
            public void onRightClick(NPCRightClickEvent event) {
                super.onRightClick(event);
                Player player = event.getClicker();
                new FarmlandGui(farmlandSite, player).openWindow();
                ChatUtils.sendMessage(player,ChatColor.GREEN + "Opening Farmland Upgrades...");
            }
        };
    }

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
    public FarmlandData getData() {
        return (FarmlandData)super.getData();
    }

    public boolean areBeeHivesBuilt() {
        return beeHiveStructure.isUnlocked();
    }

    public PassiveMobGenerator getMobGenerator() {
        return mobGenerator;
    }

    public Map<Integer, GreenhouseGenerator> getGreenhouses() {
        return greenhouses;
    }

    public GreenhouseGenerator getGreenhouse(int index) {
        return greenhouses.get(index);
    }

    public Map<Integer, GreenhouseHologram> getGreenhouseHolograms() {
        return greenhouseHolograms;
    }

    public GreenhouseHologram getGreenhouseHologram(int index) {
        return greenhouseHolograms.get(index);
    }

    public BarnStructure getBarnStructure() {
        return barnStructure;
    }
    public BeeHiveStructure getBeeHiveStructure(){
        return beeHiveStructure;
    }
    public List<HoneyGenerator> getHoneyGenerators() {
        return honeyGenerators;
    }
    public JobSiteNPC getFarmerJoe() {
        return farmerJoe;
    }
}
