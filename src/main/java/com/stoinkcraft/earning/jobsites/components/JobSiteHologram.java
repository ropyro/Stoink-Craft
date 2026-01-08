package com.stoinkcraft.earning.jobsites.components;

import com.stoinkcraft.earning.jobsites.JobSite;
import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.List;

public class JobSiteHologram implements JobSiteComponent{

    private JobSite parent;
    private String id;
    private Vector offset;
    private List<String> lines;

    public JobSiteHologram(JobSite parent, String id, Vector offset, List<String> lines) {
        this.parent = parent;
        this.id = id;
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
        }catch (IllegalArgumentException e){}
        return null;
    }
}
