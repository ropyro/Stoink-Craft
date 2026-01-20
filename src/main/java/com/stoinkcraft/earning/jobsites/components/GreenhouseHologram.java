package com.stoinkcraft.earning.jobsites.components;

import com.stoinkcraft.StoinkCore;
import com.stoinkcraft.earning.jobsites.components.generators.CropGenerator;
import com.stoinkcraft.earning.jobsites.components.generators.GreenhouseGenerator;
import com.stoinkcraft.earning.jobsites.sites.farmland.FarmlandSite;
import com.stoinkcraft.earning.jobsites.sites.farmland.GreenhouseGui;
import com.stoinkcraft.utils.ChatUtils;
import com.stoinkcraft.utils.TimeUtils;
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
        boolean isNight = !TimeUtils.isDay(farmland.getSpawnPoint().getWorld());

        List<String> lines = new ArrayList<>();
        lines.add(ChatColor.DARK_GREEN + "✦ " + ChatColor.GREEN + "" + ChatColor.BOLD + "Greenhouse"  + ChatColor.DARK_GREEN + " ✦");
        //lines.add("");
        lines.add("#ICON: " + getCropMaterial(cropType));
        lines.add(ChatColor.AQUA + "❖ " + ChatColor.WHITE + "Growth " + ChatColor.GRAY + "Lv." + ChatColor.AQUA + growthLevel);
        //lines.add("");
        if (isNight) {
            lines.add(ChatColor.DARK_PURPLE + "☽ " + ChatColor.LIGHT_PURPLE + "The crops slumber...");
        } else {
            lines.add(ChatColor.GOLD + "☀ " + ChatColor.GREEN + "Crops are growing!");
        }
        //
        // lines.add("");
        lines.add(ChatColor.DARK_GRAY + "» " + ChatColor.GRAY + "Click to manage" + ChatColor.DARK_GRAY + " «");

        setLines(0, lines);
    }

    private String getCropMaterial(CropGenerator.CropGeneratorType type) {
        return switch (type) {
            case CARROT -> "CARROT";
            case POTATO -> "POTATO";
            case BEETROOT -> "BEETROOT";
            default -> "WHEAT";
        };
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
