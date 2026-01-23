package com.stoinkcraft.jobsites.contracts;

import com.stoinkcraft.jobsites.sites.JobSiteType;
import org.bukkit.entity.Player;

public class ContractContext {

    private final Player player;
    private final JobSiteType jobSiteType;
    private final Object eventData;
    private final int amount;

    public ContractContext(Player player, JobSiteType jobSiteType, Object eventData, int amount) {
        this.player = player;
        this.jobSiteType = jobSiteType;
        this.eventData = eventData;
        this.amount = amount;
    }

    public Player getPlayer() {
        return player;
    }

    public JobSiteType getJobSiteType() {
        return jobSiteType;
    }

    @SuppressWarnings("unchecked")
    public <T> T getEventData(Class<T> type) {
        if (type.isInstance(eventData)) {
            return type.cast(eventData);
        }
        return null;
    }
    public int getAmount() {
        return amount;
    }
}