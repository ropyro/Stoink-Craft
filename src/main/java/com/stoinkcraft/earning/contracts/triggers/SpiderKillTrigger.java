package com.stoinkcraft.earning.contracts.triggers;

import com.stoinkcraft.earning.contracts.ContractContext;
import org.bukkit.entity.EntityType;

public class SpiderKillTrigger implements ContractTrigger {

    @Override
    public boolean matches(ContractContext context) {
        EntityType entityType = context.getEventData(EntityType.class);
        return entityType == EntityType.SPIDER || entityType == EntityType.CAVE_SPIDER;
    }

    @Override
    public int getProgressIncrement(ContractContext context) {
        return context.getAmount();
    }
}
