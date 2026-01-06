package com.stoinkcraft.jobs.jobsites.components;

import com.stoinkcraft.jobs.jobsites.JobSite;
import com.stoinkcraft.jobs.jobsites.JobSiteData;
import com.stoinkcraft.jobs.jobsites.JobSiteType;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.trait.LookClose;
import net.citizensnpcs.trait.SkinTrait;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Vector;

public class JobSiteNPC implements JobSiteComponent{

    private JobSite parent;
    private NPC npc;
    private String name;
    private Vector offset;
    private String skinTexture;
    private String skinSignature;

    private int citizensId;

    public JobSiteNPC(JobSite parent, String name, Vector offset, String skinTexture, String skinSignature){
        this.parent = parent;
        this.name = name;
        this.offset = offset;
        this.skinTexture = skinTexture;
        this.skinSignature = skinSignature;

        this.citizensId = parent.getData().getNpcId(name);

        JobSiteData data = parent.getData();
        if (data.getNpcId(name) != -1) {
            NPCRegistry registry = CitizensAPI.getNPCRegistry();
            npc = registry.getById(data.getNpcId(name));
            if (npc == null) {
                data.setNpc(name, -1);
            }
        }
    }

    @Override
    public void tick() {

    }

    @Override
    public void build() {
        if(npc == null)
            createNpc();
    }

    @Override
    public void disband() {
        removeNpc();
    }

    @Override
    public void levelUp() {

    }

    public void onRightClick(NPCRightClickEvent event){

    }

    private void removeNpc() {
        NPCRegistry registry = CitizensAPI.getNPCRegistry();

        int id = parent.getData().getNpcId(name);
        if (id == -1) return;

        NPC npc = registry.getById(id);
        if (npc != null) {
            npc.despawn();
            npc.destroy();
        }

        parent.getData().setNpc(name,-1);
        this.npc = null;
    }

    private void createNpc() {
        NPCRegistry registry = CitizensAPI.getNPCRegistry();

        // Create the NPC
        npc = registry.createNPC(EntityType.PLAYER, getName());

        // Spawn at the desired location
        Location npcLocation = parent.getSpawnPoint().clone().add(offset);
        npc.spawn(npcLocation);

        //set skin texture
        npc.getOrAddTrait(SkinTrait.class).setSkinPersistent("FarmerJoe", skinSignature, skinTexture);

        npc.getNavigator().getDefaultParameters().stationaryTicks(Integer.MAX_VALUE);
        npc.getOrAddTrait(LookClose.class).toggle();

        // Store reference to this job site in NPC's data
        npc.data().setPersistent(NPC.Metadata.NAMEPLATE_VISIBLE, true);
        npc.data().setPersistent("ENTERPRISE_ID", parent.getEnterprise().getID().toString());
        npc.data().setPersistent("JOBSITE_TYPE", parent.getType().name());

        parent.getData().setNpc(name, npc.getId());
    }

    public NPC getNpc() {
        return npc;
    }

    public int getCitizensId() {
        return citizensId;
    }

    public String getName() {
        return name;
    }
}
