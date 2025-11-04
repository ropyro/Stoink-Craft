package com.stoinkcraft.jobs;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface JobActionHandler {
    void onAction(Player player, JobActionType type, Location loc);
    void onTick();
    void onUpgrade(int level);
}

