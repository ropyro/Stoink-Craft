package com.stoinkcraft.jobsites.components;

import com.stoinkcraft.jobsites.sites.JobSite;
import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import eu.decentsoftware.holograms.event.HologramClickEvent;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.List;

public class JobSiteHologram implements JobSiteComponent{

    private JobSite parent;
    private String id;
    private String name;
    private Vector offset;
    private List<String> lines;

    public JobSiteHologram(JobSite parent, String name, Vector offset, List<String> lines) {
        this.parent = parent;
        this.name = name;
        this.id = parent.getEnterprise().getID() + "_" + parent.getType() + "_" + name;
        this.offset = offset;
        this.lines = lines;
    }

    @Override
    public void tick() {

    }

    @Override
    public void build() {
        setHologram();
    }

    @Override
    public void disband() {
        try {
            Hologram hologram = DHAPI.getHologram(id);
            if (hologram != null)
                hologram.delete();
        } catch (IllegalArgumentException e) {}
    }

    @Override
    public void levelUp() {

    }

    public void onHologramInteract(HologramClickEvent event){

    }

    public void setHologram(){
        setHologram(lines);
    }

    public void setHologram(List<String> lines){
        setHologram(lines, parent.getSpawnPoint().clone().add(offset));
    }

    public void setHologram(List<String> lines, Location loc){
        try{
            if(DHAPI.getHologram(id) != null)
                DHAPI.getHologram(id).delete();
        }catch (IllegalArgumentException e){}
        DHAPI.createHologram(id, loc, true, lines);
    }

    public void setLines(int pageIndex, List<String> lines){
        try{
            Hologram hologram = DHAPI.getHologram(id);
            if(hologram != null)
                DHAPI.setHologramLines(hologram, pageIndex, lines);
            else
                setHologram(lines);
        }catch (IllegalArgumentException e){}
    }

    public void setLine(int pageIndex, int lineIndex, String line){
        try{
            Hologram hologram = DHAPI.getHologram(id);
            if(hologram != null)
                DHAPI.setHologramLine(hologram, pageIndex, lineIndex, line);
            else
                setHologram();
        }catch (IllegalArgumentException e){}
    }

    public void delete(){
        try{
            if(DHAPI.getHologram(id) != null)
                DHAPI.getHologram(id).delete();
        }catch (IllegalArgumentException e){}
    }

    public Hologram getHologram(){
        try{
            if(DHAPI.getHologram(id) != null)
                return DHAPI.getHologram(id);
            else setHologram();
        }catch (IllegalArgumentException e){}
        return getHologram();
    }

    public JobSite getParent() {
        return parent;
    }

    public String getId() {
        return id;
    }
}
