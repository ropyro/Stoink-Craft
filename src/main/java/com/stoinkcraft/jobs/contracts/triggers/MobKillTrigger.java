package com.stoinkcraft.jobs.contracts.triggers;

import com.stoinkcraft.jobs.contracts.ContractContext;
import org.bukkit.entity.EntityType;

public class MobKillTrigger implements ContractTrigger {

    private final EntityType entityType;

    public MobKillTrigger(EntityType entityType) {
        this.entityType = entityType;
    }

    @Override
    public boolean matches(ContractContext context) {
        EntityType killed = context.getEventData(EntityType.class);
        return killed == entityType;
    }

    @Override
    public int getProgressIncrement(ContractContext context) {
        return 1;
    }
}