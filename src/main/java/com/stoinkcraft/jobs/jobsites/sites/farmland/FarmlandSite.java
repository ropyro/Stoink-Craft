package com.stoinkcraft.jobs.jobsites.sites.farmland;

import com.stoinkcraft.StoinkCore;
import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.jobs.jobsites.JobSite;
import com.stoinkcraft.jobs.jobsites.JobSiteType;
import com.stoinkcraft.jobs.jobsites.resourcegenerators.generators.CropGenerator;
import com.stoinkcraft.jobs.jobsites.resourcegenerators.generators.MobGenerator;
import com.stoinkcraft.jobs.jobsites.resourcegenerators.generators.PassiveMobGenerator;
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

    private FarmlandData data;

    private String welcomeHologramName;
    public static Vector welcomeHologramOffset = new Vector(-3.5, 3, 0.5);

    private NPC farmerJoeNPC;
    public static Vector farmerJoeOffset = new Vector(-27, 0, -14); // Adjust as needed

    private String cropRegionID;

    private CropGenerator cropGenerator;
    public static Vector cropGenCorner1Offset = new Vector(-25, 0, -16);
    public static Vector cropGenCorner2Offset = new Vector(-54, 0, -45);

    private String mobRegionID;
    private PassiveMobGenerator mobGenerator;
    public static Vector mobGenCorner1Offset = new Vector(-47, -1, 7);
    public static Vector mobGenCorner2Offset = new Vector(-51, 5, 3);


    public FarmlandSite(Enterprise enterprise, Location spawnPoint, FarmlandData data) {
        super(enterprise, JobSiteType.FARMLAND, spawnPoint,
                new File(StoinkCore.getInstance().getDataFolder(), "/schematics/farmland.schem"),
                data.isBuilt());
        this.data = data;

        welcomeHologramName = enterprise.getID() + "_" + JobSiteType.FARMLAND.name() + "_welcome";
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
                data.setFarmerJoeNpcId(-1);
            }
        }
    }

    @Override
    public void tick() {
        super.tick();
        cropGenerator.tick();
        mobGenerator.tick();
    }

    @Override
    public void initializeJobs() {

    }

    @Override
    public void initializeBuild() {
        List<String> entryHoloGramLines = new ArrayList<>();
        entryHoloGramLines.add(ChatColor.AQUA + "" + ChatColor.BOLD + "Welcome to the Farmland");
        entryHoloGramLines.add(ChatColor.WHITE + "Here you will harvest crops and butcher meat");
        entryHoloGramLines.add(ChatColor.WHITE + "to complete resource collection contracts!");
        entryHoloGramLines.add(ChatColor.WHITE + "Chat with Farmer Joe to upgrade your farmland's");
        entryHoloGramLines.add(ChatColor.WHITE + "crop grow speed and unlock new crops and animals!");
        Location holoLoc = spawnPoint.clone().add(welcomeHologramOffset);
        initializeHologram(welcomeHologramName, entryHoloGramLines, holoLoc);

        cropGenerator.init();
        cropGenerator.regenerateCrops();

        if (farmerJoeNPC == null) {
            createFarmerJoeNPC();
        }

        mobGenerator.init();

        data.setBuilt(true);
    }

    @Override
    public void disband() {
        super.disband();
        RegionUtils.removeProtectedRegion(spawnPoint.getWorld(), cropGenerator.getRegionName());

        mobGenerator.clearAllMobs();

        try {
            if (DHAPI.getHologram(welcomeHologramName) != null)
                DHAPI.getHologram(welcomeHologramName).delete();
        } catch (IllegalArgumentException e) {}
        removeFarmerJoeNPC();
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
        farmerJoeNPC.data().set("jobsite", enterprise.getID());
        farmerJoeNPC.data().set("jobsitetype", JobSiteType.FARMLAND.name());

        data.setFarmerJoeNpcId(farmerJoeNPC.getId());
    }

    private void removeFarmerJoeNPC() {
        if (farmerJoeNPC != null) {
            farmerJoeNPC.destroy();
            farmerJoeNPC = null;
        }
    }

    public PassiveMobGenerator getMobGenerator() {
        return mobGenerator;
    }

    public CropGenerator getCropGenerator() {
        return cropGenerator;
    }

    public FarmlandData getData(){
        data.setBuilt(isBuilt);
        return data;
    }
}
