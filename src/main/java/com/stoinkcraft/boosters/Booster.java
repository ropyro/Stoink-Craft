package com.stoinkcraft.boosters;

import org.bukkit.scheduler.BukkitRunnable;

public class Booster {

    private double multiplier;
    private long duration;

    public Booster(Double multiplier, long duration){
        this.multiplier = multiplier;
        this.duration = duration;
    }

    public double getMultiplier() {
        return multiplier;
    }

    public long getDuration() {
        return duration;
    }
}
