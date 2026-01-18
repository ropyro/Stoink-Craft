package com.stoinkcraft.earning.jobsites.components;

import com.stoinkcraft.StoinkCore;
import com.stoinkcraft.earning.jobsites.components.generators.CropGenerator;
import com.stoinkcraft.earning.jobsites.components.generators.GreenhouseGenerator;
import com.stoinkcraft.earning.jobsites.sites.farmland.FarmlandSite;
import com.stoinkcraft.earning.jobsites.sites.farmland.GreenhouseGui;
import com.stoinkcraft.utils.ChatUtils;
import eu.decentsoftware.holograms.event.HologramClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

/**
 * A hologram for a greenhouse that displays status information and can be clicked
 * to open a GUI for managing the greenhouse's crop type and upgrades.
 */
public class GreenhouseHologram extends JobSiteHologram {

    private final GreenhouseGenerator greenhouse;

    public GreenhouseHologram(FarmlandSite parent, GreenhouseGenerator greenhouse, Vector offset) {
        super(parent, "greenhouse_" + greenhouse.getGreenhouseIndex(), offset, buildLines(greenhouse));
        this.greenhouse = greenhouse;
    }

    private static List<String> buildLines(GreenhouseGenerator greenhouse) {
        List<String> lines = new ArrayList<>();
        lines.add(ChatColor.GREEN + "" + ChatColor.BOLD + "Greenhouse " + greenhouse.getGreenhouseIndex());
        lines.add(ChatColor.GRAY + "Click to manage");
        return lines;
    }

    @Override
    public void tick() {
        super.tick();
        updateHologram();
    }

    @Override
    public void build() {
        if (!greenhouse.isUnlocked()) {
            return; // Don't show hologram if greenhouse is locked
        }
        super.build();
    }

    /**
     * Updates the hologram with current status.
     */
    public void updateHologram() {
        if (!greenhouse.isUnlocked()) {
            delete(); // Remove hologram if greenhouse is locked
            return;
        }

        FarmlandSite farmland = (FarmlandSite) getParent();
        int growthLevel = farmland.getData().getLevel(greenhouse.getGrowthSpeedUpgradeKey());
        CropGenerator.CropGeneratorType cropType = farmland.getData().getGreenhouseCropType(greenhouse.getGreenhouseIndex());

        List<String> lines = new ArrayList<>();
        lines.add(ChatColor.GREEN + "" + ChatColor.BOLD + "Greenhouse " + greenhouse.getGreenhouseIndex());
        lines.add(ChatColor.GRAY + "Crop: " + ChatColor.YELLOW + formatCropName(cropType));
        lines.add(ChatColor.GRAY + "Growth Lvl: " + ChatColor.AQUA + growthLevel);
        lines.add(ChatColor.DARK_GRAY + "[Click to manage]");

        setLines(0, lines);
    }

    private String formatCropName(CropGenerator.CropGeneratorType type) {
        String name = type.name().toLowerCase();
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    @Override
    public void onHologramInteract(HologramClickEvent event) {
        Player player = event.getPlayer();

        if (!greenhouse.isUnlocked()) {
            ChatUtils.sendMessage(player, ChatColor.RED + "This greenhouse is not unlocked yet!");
            return;
        }

        FarmlandSite farmland = (FarmlandSite) getParent();
        // Must run synchronously - HologramClickEvent is fired from async Netty handler
        Bukkit.getScheduler().runTask(StoinkCore.getInstance(), () -> {
            new GreenhouseGui(farmland, greenhouse, player).openWindow();
            ChatUtils.sendMessage(player, ChatColor.GREEN + "Opening Greenhouse " + greenhouse.getGreenhouseIndex() + "...");
        });
    }

    /**
     * Called when the greenhouse is unlocked. Shows the hologram.
     */
    public void onGreenhouseUnlock() {
        build();
        updateHologram();
    }

    public GreenhouseGenerator getGreenhouse() {
        return greenhouse;
    }
}
