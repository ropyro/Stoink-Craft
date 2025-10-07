package com.stoinkcraft.market.values;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;

public class EntityValue extends TaskValue{

    private EntityType entityType;

    public EntityValue(EntityType entityType, Double value){
        super(entityType.name(), value, Material.AIR);
        this.entityType = entityType;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    @Override
    public String getDisplayName(){
        return entityType.name();
    }
}
