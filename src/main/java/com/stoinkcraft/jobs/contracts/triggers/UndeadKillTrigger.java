package com.stoinkcraft.jobs.contracts.triggers;

import com.stoinkcraft.jobs.contracts.ContractContext;
import org.bukkit.entity.EntityType;

import java.util.Set;

public class UndeadKillTrigger implements ContractTrigger {

    private final Set<EntityType> validTypes;

    public UndeadKillTrigger(EntityType... types) {
        this.validTypes = Set.of(types);
    }

    @Override
    public boolean matches(ContractContext context) {
        EntityType entityType = context.getEventData(EntityType.class);
        return entityType != null && validTypes.contains(entityType);
    }

    @Override
    public int getProgressIncrement(ContractContext context) {
        return context.getAmount();
    }
}