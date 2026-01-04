package com.stoinkcraft.jobs.jobsites.sites.farmland;

import com.stoinkcraft.StoinkCore;
import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.jobs.jobsites.JobSite;
import com.stoinkcraft.jobs.jobsites.JobSiteType;
import com.stoinkcraft.jobs.jobsites.JobSiteUpgrade;
import com.stoinkcraft.jobs.jobsites.components.JobSiteHologram;
import com.stoinkcraft.jobs.jobsites.components.generators.CropGenerator;
import com.stoinkcraft.jobs.jobsites.components.generators.PassiveMobGenerator;
import com.stoinkcraft.jobs.jobsites.components.structures.BarnStructure;
import com.stoinkcraft.utils.RegionUtils;
import eu.decentsoftware.holograms.api.DHAPI;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.trait.LookClose;
import net.citizensnpcs.trait.SkinTrait;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FarmlandSite extends JobSite {

    /**
     * Holograms
     */
    private String welcomeHologramId;
    public static Vector welcomeHologramOffset = new Vector(-3.5, 3, 0.5);

    /**
     * Farmer Joe NPC
     */
    private NPC farmerJoeNPC;
    public static Vector farmerJoeOffset = new Vector(-27, 0, -14); // Adjust as needed

    /**
     * Crop generator
     */
    private String cropRegionID;
    private CropGenerator cropGenerator;
    public static Vector cropGenCorner1Offset = new Vector(-25, 0, -16);
    public static Vector cropGenCorner2Offset = new Vector(-54, 0, -45);

    /**
     * Farm animal generator
     */
    private String mobRegionID;
    private PassiveMobGenerator mobGenerator;
    public static Vector mobGenCorner1Offset = new Vector(-47, -1, 7);
    public static Vector mobGenCorner2Offset = new Vector(-51, 5, 3);

    /**
     * Barn
     */

    private BarnStructure barnStructure;


    public FarmlandSite(Enterprise enterprise, Location spawnPoint, FarmlandData data) {
        super(enterprise, JobSiteType.FARMLAND, spawnPoint,
                new File(StoinkCore.getInstance().getDataFolder(), "/schematics/farmland.schem"),
                data, data.isBuilt());

        welcomeHologramId = enterprise.getID() + "_" + JobSiteType.FARMLAND.name() + "_welcome";

        cropRegionID = enterprise.getID() + "_" + JobSiteType.FARMLAND.name() + "_crops";
        cropGenerator = new CropGenerator(spawnPoint.clone().add(cropGenCorner1Offset),
                spawnPoint.clone().add(cropGenCorner2Offset), this, cropRegionID);

        mobRegionID = enterprise.getID() + "_" + JobSiteType.FARMLAND.name() + "_mobs";
        mobGenerator = new PassiveMobGenerator(
                spawnPoint.clone().add(mobGenCorner1Offset),
                spawnPoint.clone().add(mobGenCorner2Offset),
                this,
                mobRegionID
        );

        if (data.getFarmerJoeNpcId() != -1) {
            NPCRegistry registry = CitizensAPI.getNPCRegistry();
            farmerJoeNPC = registry.getById(data.getFarmerJoeNpcId());

            // If NPC was deleted externally, reset the ID
            if (farmerJoeNPC == null) {
                getData().setFarmerJoeNpcId(-1);
            }
        }

        barnStructure = new BarnStructure(this);

        registerUpgrades();
        registerComponents();
    }

    private void registerComponents(){
        List<String> welcomeHologramLines = new ArrayList<>();
        welcomeHologramLines.add(ChatColor.AQUA + "" + ChatColor.BOLD + "Welcome to the Farmland");
        welcomeHologramLines.add(ChatColor.WHITE + "Here you will harvest crops and butcher meat");
        welcomeHologramLines.add(ChatColor.WHITE + "to complete resource collection contracts!");
        welcomeHologramLines.add(ChatColor.WHITE + "Chat with Farmer Joe to upgrade your farmland's");
        welcomeHologramLines.add(ChatColor.WHITE + "crop grow speed and unlock new crops and animals!");
        addComponent(new JobSiteHologram(this, welcomeHologramId, welcomeHologramOffset, welcomeHologramLines));

        addComponent(cropGenerator);
        addComponent(mobGenerator);
        addComponent(barnStructure);
    }

    private void registerUpgrades() {

        // =========================
        // CROP UPGRADES
        // =========================

        upgrades.add(new JobSiteUpgrade(
                "crop_growth_speed",
                "Crop Growth Speed",
                10,
                5,
                lvl -> 5000 * lvl,
                site -> true,
                (site, lvl) -> {} // effect is read dynamically by CropGenerator
        ));

        upgrades.add(new JobSiteUpgrade(
                "unlock_carrot",
                "Unlock Carrots",
                1,
                5,
                lvl -> 25000,
                site -> true,
                (site, lvl) -> {} // unlock = upgrade level > 0
        ));

        upgrades.add(new JobSiteUpgrade(
                "unlock_potato",
                "Unlock Potatoes",
                1,
                20,
                lvl -> 75000,
                site -> site.getData().getLevel("unlock_carrot") > 0,
                (site, lvl) -> {}
        ));

        upgrades.add(new JobSiteUpgrade(
                "unlock_beetroot",
                "Unlock Beetroots",
                1,
                30,
                lvl -> 125000,
                site -> site.getData().getLevel("unlock_potato") > 0,
                (site, lvl) -> {}
        ));

// =========================
// MOB UPGRADES
// =========================

        upgrades.add(new JobSiteUpgrade(
                "mob_spawn_speed",
                "Animal Spawn Speed",
                10,
                2, // requires jobsite level 2
                lvl -> 6000 * lvl,
                site -> true,
                (site, lvl) -> {}
        ));

        upgrades.add(new JobSiteUpgrade(
                "mob_capacity",
                "Animal Capacity",
                10,
                4,
                lvl -> 9000 * lvl,
                site -> true,
                (site, lvl) -> {}
        ));

// -------------------------
// MOB UNLOCKS
// -------------------------

        upgrades.add(new JobSiteUpgrade(
                "unlock_sheep",
                "Unlock Sheep",
                1,
                3,
                lvl -> 25000,
                site -> true,
                (site, lvl) -> {}
        ));

        upgrades.add(new JobSiteUpgrade(
                "unlock_pig",
                "Unlock Pig",
                1,
                6,
                lvl -> 50000,
                site -> site.getData().getLevel("unlock_sheep") > 0,
                (site, lvl) -> {}
        ));

        upgrades.add(new JobSiteUpgrade(
                "unlock_chicken",
                "Unlock Chicken",
                1,
                10,
                lvl -> 75000,
                site -> site.getData().getLevel("unlock_pig") > 0,
                (site, lvl) -> {}
        ));

        upgrades.add(new JobSiteUpgrade(
                "unlock_horse",
                "Unlock Horse",
                1,
                18,
                lvl -> 125000,
                site -> site.getData().getLevel("unlock_chicken") > 0,
                (site, lvl) -> {}
        ));
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    public void build() {
        super.build();

        if (farmerJoeNPC == null) {
            createFarmerJoeNPC();
        }
    }

    @Override
    public void disband() {
        super.disband();
        RegionUtils.removeProtectedRegion(spawnPoint.getWorld(), cropGenerator.getRegionName());

       // mobGenerator.clearAllMobs();

        removeFarmerJoeNPC();
    }

    @Override
    public FarmlandData getData() {
        return (FarmlandData)super.getData();
    }

    private void createFarmerJoeNPC() {
        NPCRegistry registry = CitizensAPI.getNPCRegistry();

        // Create the NPC
        farmerJoeNPC = registry.createNPC(EntityType.PLAYER, ChatColor.GREEN + "Farmer Joe");

        // Spawn at the desired location
        Location npcLocation = spawnPoint.clone().add(farmerJoeOffset);
        farmerJoeNPC.spawn(npcLocation);

        // Optional: Set skin (requires a valid Minecraft username or texture data)
        String texture = "ewogICJ0aW1lc3RhbXAiIDogMTc0NDA5MzMxMTMxMCwKICAicHJvZmlsZUlkIiA6ICJiOWIzY2RlZmIyZmQ0YWY1ODQxMGViZWZjY2ZmYTBhYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJpbnRlcnNlY2F0byIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9kY2Y1Mzk3YmEwNTc5ZTI3NGMxZDJhN2I4M2ExMmU1MDQ0NDBjYjQzOTgzODZkNTI4MTk5NmQ0MWMwNjc3N2M1IgogICAgfQogIH0KfQ==";
        String signature = "k+SC0/ie439qZpjQQXSiYWRP4MWW4RLOsPdhc0KB/YBmJZGs1K/KhVtRrn1KFFV263foaBjEQtE/yoX9L5VYKmgJmTxscpDlX0KwnTVpZgDOwTU0rGUxg0VElmyqxRt49FH7UzJeuFs880jzHDBxuoRw28gHOMkaiE2WtdSDOXF6KcfwyZbZ/IlybI6ydcgzsVe6L8OXJuVEFStEuaPoE27qzz4OZX5wrYpW4FtmGIkISVXIEgh4Cd+R/toaXBLV7Egz/IuWrueihUv48QXv3lbPSncCuOcpqIjfJ+JSR1CcvkypbqhKdBMko7hTH77libQrz1k79Ghtppjw7cC6/tRdPAqOtNSAPk82nHbogctI7X7RBv+5ETtKK2nw8ckTyuqikgICYwjbmDNhhuSZHodb16pQy9LaGPXqi5ti4TgMFxsY98+Yys4N1Fz0WuMl1UDm44mjmH4o1aqsjeZKem/cqZbh3rppzLGZ/4lhmooTChGfPIONGCPdpgDh1yxzw8k96RNpG0bDJo5VzQB5LzuENiHgi1vBxFXdAQii5o7XZd6SPexmmwz4BNGymebjhnQ/VSj8PfTpF/SBBEYoJF3T7WR6Y/8UpbqDCbUQJhHxRSGu+qTg5CX2nkq1hw4bhKklOGRRlC0retK7oYGJhE3aJSY8m+wLQeJGL19+A8Y=";
        farmerJoeNPC.getOrAddTrait(SkinTrait.class).setSkinPersistent("FarmerJoe", signature, texture);

        farmerJoeNPC.getNavigator().getDefaultParameters().stationaryTicks(Integer.MAX_VALUE);
        farmerJoeNPC.getOrAddTrait(LookClose.class).toggle();

        // Store reference to this job site in NPC's data
        farmerJoeNPC.data().setPersistent(NPC.Metadata.NAMEPLATE_VISIBLE, true);
        farmerJoeNPC.data().setPersistent("ENTERPRISE_ID", enterprise.getID().toString());
        farmerJoeNPC.data().setPersistent("JOBSITE_TYPE", JobSiteType.FARMLAND.name());

        getData().setFarmerJoeNpcId(farmerJoeNPC.getId());
    }

    private void removeFarmerJoeNPC() {
        NPCRegistry registry = CitizensAPI.getNPCRegistry();

        int id = getData().getFarmerJoeNpcId();
        if (id == -1) return;

        NPC npc = registry.getById(id);
        if (npc != null) {
            npc.despawn();
            npc.destroy();
        }

        getData().setFarmerJoeNpcId(-1);
        farmerJoeNPC = null;
    }

    public PassiveMobGenerator getMobGenerator() {
        return mobGenerator;
    }

    public CropGenerator getCropGenerator() {
        return cropGenerator;
    }

    public BarnStructure getBarnStructure() {
        return barnStructure;
    }
}
