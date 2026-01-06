package com.stoinkcraft.jobs.contracts.triggers;

import com.stoinkcraft.jobs.contracts.ContractContext;
import org.bukkit.entity.EntityType;

import java.util.Set;

public class MobKillTrigger implements ContractTrigger {

    private final Set<EntityType> validTypes;

    public MobKillTrigger(EntityType... types) {
        this.validTypes = Set.of(types);
    }

    public MobKillTrigger(Set<EntityType> types) {
        this.validTypes = types;
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

    public Set<EntityType> getValidTypes() {
        return validTypes;
    }
}