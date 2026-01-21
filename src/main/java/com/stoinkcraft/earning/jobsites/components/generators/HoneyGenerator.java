package com.stoinkcraft.earning.jobsites.components.generators;

import com.stoinkcraft.config.ConfigLoader;
import com.stoinkcraft.earning.jobsites.JobSite;
import com.stoinkcraft.earning.jobsites.components.JobSiteGenerator;
import com.stoinkcraft.earning.jobsites.components.JobSiteHologram;
import com.stoinkcraft.earning.jobsites.protection.ProtectedZone;
import com.stoinkcraft.earning.jobsites.protection.ProtectionAction;
import com.stoinkcraft.earning.jobsites.protection.ProtectionQuery;
import com.stoinkcraft.earning.jobsites.protection.ProtectionResult;
import com.stoinkcraft.earning.jobsites.sites.farmland.FarmlandSite;
import com.stoinkcraft.utils.ChatUtils;
import com.stoinkcraft.utils.TimeUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Beehive;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class HoneyGenerator extends JobSiteGenerator implements ProtectedZone {

    private boolean initialized = false;

    private final Location hiveLocation;
    private final World world;

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

    // =========================================================================
    // PROTECTION
    // =========================================================================

    @Override
    public @NotNull ProtectionResult checkProtection(@NotNull ProtectionQuery query) {
        // Check if this is our specific hive block
        Location queryLoc = query.location();
        if (queryLoc.getBlockX() != hiveLocation.getBlockX() ||
                queryLoc.getBlockY() != hiveLocation.getBlockY() ||
                queryLoc.getBlockZ() != hiveLocation.getBlockZ() ||
                !queryLoc.getWorld().equals(hiveLocation.getWorld())) {
            return ProtectionResult.ABSTAIN;
        }

        // Allow shearing this hive (harvest logic handled by PlayerInteractListener)
        if (query.action() == ProtectionAction.SHEAR) {
            return ProtectionResult.ALLOW;
        }

        // Deny breaking the hive block itself
        if (query.action() == ProtectionAction.BREAK) {
            return ProtectionResult.DENY;
        }

        return ProtectionResult.ABSTAIN;
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
        if (honeyLevel >= ConfigLoader.getGenerators().getHoneyMaxHoney()) return;


        ticksSinceLastHoney++;

        if (ticksSinceLastHoney >= getGenerationIntervalTicks()) {
            ticksSinceLastHoney = 0;
            honeyLevel++;
            syncHiveBlock();
        }

        updateHologramText(site);
    }

    private void updateHologramText(FarmlandSite site) {
        int percent = (int) ((honeyLevel / (double) ConfigLoader.getGenerators().getHoneyMaxHoney()) * 100);
        List<String> lines = List.of();
        if (!site.areBeeHivesBuilt()) {
            lines = List.of();
        } else if(percent == 100){
                lines = List.of("#ICON: HONEYCOMB");
        } else {
            lines = List.of(getProgressBar());
        }
        hologram.setLines(0, lines);
    }

    private String getProgressBar() {
        int percent = (int) ((honeyLevel / (double) ConfigLoader.getGenerators().getHoneyMaxHoney()) * 100);

        int timeRemaining = (ConfigLoader.getGenerators().getHoneyMaxHoney() - honeyLevel) * getGenerationIntervalTicks() - ticksSinceLastHoney;

        return ChatColor.GOLD + ChatUtils.formatDurationSeconds(timeRemaining);
    }

    private String getHoneyBar() {
        int maxHoney = ConfigLoader.getGenerators().getHoneyMaxHoney();
        String bar = ChatColor.GOLD + "█".repeat(honeyLevel)
                + ChatColor.GRAY + "█".repeat(maxHoney - honeyLevel);

        return bar; // + ChatColor.WHITE + " " + percent + "%";
    }

    private void readHoneyFromBlock() {
        Block block = hiveLocation.getBlock();

        if (!(block.getBlockData() instanceof Beehive hive)) {
            honeyLevel = 0;
            return;
        }

        honeyLevel = Math.min(hive.getHoneyLevel(), ConfigLoader.getGenerators().getHoneyMaxHoney());
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
        return honeyLevel >= ConfigLoader.getGenerators().getHoneyMaxHoney();
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
        int baseSeconds = ConfigLoader.getGenerators().getHoneyBaseGenerationSeconds();
        int minSeconds = ConfigLoader.getGenerators().getHoneyMinGenerationSeconds();
        double reductionPercentage = ConfigLoader.getGenerators().getHoneySpeedReductionPercentage();
        int reduction = (int) (baseSeconds * reductionPercentage * speedLevel);
        return Math.max(minSeconds, baseSeconds - reduction);
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