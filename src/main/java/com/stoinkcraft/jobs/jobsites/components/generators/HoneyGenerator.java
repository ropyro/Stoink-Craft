package com.stoinkcraft.jobs.jobsites.components.generators;

import com.stoinkcraft.jobs.jobsites.JobSite;
import com.stoinkcraft.jobs.jobsites.components.JobSiteGenerator;
import com.stoinkcraft.jobs.jobsites.components.JobSiteHologram;
import com.stoinkcraft.jobs.jobsites.sites.farmland.FarmlandSite;
import com.stoinkcraft.utils.TimeUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Beehive;
import org.bukkit.util.Vector;

import java.util.List;

public class HoneyGenerator extends JobSiteGenerator {

    private boolean initialized = false;

    private final Location hiveLocation;
    private final World world;

    private static final int MAX_HONEY = 5;
    private static final int BASE_GENERATION_SECONDS = 300; // 5 minutes base
    private static final int MIN_GENERATION_SECONDS = 30;   // 30 seconds minimum

    private int honeyLevel = 0;
    private int ticksSinceLastHoney = 0;



    private JobSiteHologram hologram;
    private final String hologramId;
    private static final Vector HOLOGRAM_OFFSET = new Vector(0.5, 1.6, 0.5);

    public HoneyGenerator(Location hiveLocation, JobSite parent) {
        super(parent);
        this.hiveLocation = hiveLocation.clone();
        this.world = hiveLocation.getWorld();

        this.hologramId =
                parent.getEnterprise().getID() + "_honey_" +
                        hiveLocation.getBlockX() + "_" +
                        hiveLocation.getBlockY() + "_" +
                        hiveLocation.getBlockZ();

        this.hologram = new JobSiteHologram(
                parent,
                hologramId,
                hiveLocation.clone().subtract(parent.getSpawnPoint().clone()).add(HOLOGRAM_OFFSET).toVector(),
                List.of()
        );

        parent.addComponent(hologram);
    }

    public JobSiteHologram getHologram() {
        return hologram;
    }

    @Override
    public void build() {
        super.build();
        syncHiveBlock();
    }

    @Override
    public void tick() {
        super.tick();
        FarmlandSite site = (FarmlandSite) getParent();

        if(!initialized){
            readHoneyFromBlock();
            updateHologramText(site);
            initialized = true;
        }

        if (!site.areBeeHivesBuilt()) return;

        if (!TimeUtils.isDay(world)) return;
        if (honeyLevel >= MAX_HONEY) return;


        ticksSinceLastHoney++;

        if (ticksSinceLastHoney >= getGenerationIntervalTicks()) {
            ticksSinceLastHoney = 0;
            honeyLevel++;
            syncHiveBlock();
        }

        updateHologramText(site);
    }

    private void updateHologramText(FarmlandSite site) {
        int percent = (int) ((honeyLevel / (double) MAX_HONEY) * 100);
        List<String> lines = List.of();
        if (!site.areBeeHivesBuilt()) {
            lines = List.of("");
        } else if(percent == 100){
                lines = List.of("#ICON: HONEYCOMB");
        }
        hologram.setLines(0, lines);
    }

    private String getProgressBar() {
        int percent = (int) ((honeyLevel / (double) MAX_HONEY) * 100);

        int bars = percent / 10;
        String bar = ChatColor.GOLD + "█".repeat(bars)
                + ChatColor.GRAY + "█".repeat(10 - bars);

        return bar + ChatColor.WHITE + " " + percent + "%";
    }

    private void readHoneyFromBlock() {
        Block block = hiveLocation.getBlock();

        if (!(block.getBlockData() instanceof Beehive hive)) {
            honeyLevel = 0;
            return;
        }

        honeyLevel = Math.min(hive.getHoneyLevel(), MAX_HONEY);
    }

    /* =========================
       VISUAL SYNC (Bukkit ONLY)
       ========================= */

    private void syncHiveBlock() {
        Block block = hiveLocation.getBlock();

        if (!(block.getBlockData() instanceof Beehive hive))
            return;

        hive.setHoneyLevel(honeyLevel);
        block.setBlockData(hive, false);
    }

    /* =========================
       HARVEST INTEGRATION
       ========================= */

    public boolean canHarvest() {
        return honeyLevel >= MAX_HONEY;
    }

    /** Called by event handler */
    public void consumeHoney() {
        honeyLevel = 0;
        syncHiveBlock();
        if(getParent() instanceof FarmlandSite farmlandSite)
            updateHologramText(farmlandSite);
    }

    /* =========================
       TIMING
       ========================= */

    private int getGenerationIntervalTicks() {
        int speedLevel = getSpeedLevel();
        // Each level reduces by 8% of base
        int reduction = (int) (BASE_GENERATION_SECONDS * 0.08 * speedLevel);
        return Math.max(MIN_GENERATION_SECONDS, BASE_GENERATION_SECONDS - reduction);
    }

    private int getSpeedLevel() {
        return ((FarmlandSite) getParent())
                .getData()
                .getLevel("honey_speed");
    }

    public Location getHiveLocation() {
        return hiveLocation;
    }
}