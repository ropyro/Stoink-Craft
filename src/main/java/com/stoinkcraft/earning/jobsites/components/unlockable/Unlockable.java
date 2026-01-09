package com.stoinkcraft.earning.jobsites.components.unlockable;

import com.stoinkcraft.earning.jobsites.JobSite;

public interface Unlockable {

    /**
     * @return The configuration defining unlock requirements
     */
    UnlockableConfig getUnlockConfig();

    /**
     * @return The parent JobSite this unlockable belongs to
     */
    JobSite getJobSite();

    /**
     * @return Current unlock state from persistent data
     */
    default UnlockableState getUnlockState() {
        return getJobSite().getData()
                .getUnlockableProgress(getUnlockableId())
                .getState();
    }

    /**
     * @return true if requirements are met and not already unlocked/building
     */
    default boolean canUnlock() {
        return getUnlockState() == UnlockableState.LOCKED
                && getUnlockConfig().meetsRequirements(getJobSite());
    }

    /**
     * @return true if currently in BUILDING state
     */
    default boolean isBuilding() {
        return getUnlockState() == UnlockableState.BUILDING;
    }

    /**
     * @return true if fully unlocked
     */
    default boolean isUnlocked() {
        return getUnlockState() == UnlockableState.UNLOCKED;
    }

    // ==================== Lifecycle Hooks ====================

    /** Called when player initiates the unlock/purchase */
    default void onUnlockStart() {}

    /** Called every tick while in BUILDING state */
    default void onUnlockTick(long millisRemaining) {}

    /** Called when build timer completes */
    default void onUnlockComplete() {}

    // ==================== Tick Helper ====================

    /**
     * Standard tick logic for unlockables. Call this from your component's tick().
     */
    default void tickUnlockProgress() {
        if (!isBuilding()) return;

        UnlockableProgress progress = getJobSite().getData()
                .getUnlockableProgress(getUnlockableId());

        onUnlockTick(progress.getRemainingMillis());

        if (progress.isFinished()) {
            progress.markUnlocked();
            onUnlockComplete();
        }
    }

    // ==================== Convenience Getters ====================

    default String getUnlockableId() {
        return getUnlockConfig().id();
    }

    default String getDisplayName() {
        return getUnlockConfig().displayName();
    }

    default int getRequiredJobsiteLevel() {
        return getUnlockConfig().requiredJobsiteLevel();
    }

    default long getBuildTimeMillis() {
        return getUnlockConfig().buildTimeMillis();
    }

    default int getCost() {
        return getUnlockConfig().getCost();
    }
}