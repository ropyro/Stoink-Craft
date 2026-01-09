package com.stoinkcraft.earning.jobsites.components.unlockable;

import com.google.gson.annotations.Expose;

import java.util.concurrent.TimeUnit;

public class UnlockableProgress {

    @Expose
    private UnlockableState state;
    @Expose
    private long buildStartTime;
    @Expose
    private long buildDurationMillis;

    public UnlockableProgress() {
        this.state = UnlockableState.LOCKED;
        this.buildStartTime = 0;
        this.buildDurationMillis = 0;
    }

    public UnlockableState getState() {
        return state;
    }

    public void startBuilding(long durationMillis) {
        this.state = UnlockableState.BUILDING;
        this.buildStartTime = System.currentTimeMillis();
        this.buildDurationMillis = durationMillis;
    }

    public long getRemainingMillis() {
        if (state != UnlockableState.BUILDING) return 0;
        long elapsed = System.currentTimeMillis() - buildStartTime;
        return Math.max(0, buildDurationMillis - elapsed);
    }
    public long getRemainingSeconds() {
        return TimeUnit.MILLISECONDS.toSeconds(getRemainingMillis());
    }

    public boolean isFinished() {
        return state == UnlockableState.BUILDING && getRemainingMillis() <= 0;
    }

    public void markUnlocked() {
        this.state = UnlockableState.UNLOCKED;
    }
}