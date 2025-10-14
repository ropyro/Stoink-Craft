package com.stoinkcraft.market.boosters;

import org.bukkit.scheduler.BukkitRunnable;

import java.time.Instant;
import java.util.Date;

public class Booster {

    private double multiplier;
    private long duration;
    private long startTime;

    public Booster(Double multiplier, long duration){
        this.multiplier = multiplier;
        this.duration = duration;
        this.startTime = System.currentTimeMillis();
    }

    public double getMultiplier() {
        return multiplier;
    }

    public long getDuration() {
        return duration;
    }

    public long getTimeRemaining(){
        return startTime - System.currentTimeMillis();
    }
}
