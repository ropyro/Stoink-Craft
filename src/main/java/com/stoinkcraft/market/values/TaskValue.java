package com.stoinkcraft.market.values;

import org.bukkit.Material;

public class TaskValue {

    private String key;
    private Double value;

    private Material materialValue;

    public TaskValue(String key, Double value, Material materialValue){
        this.key = key;
        this.value = value;
        this.materialValue = materialValue;
    }

    public String getDisplayName(){
        return materialValue.name();
    }

    public Double getValue() {
        return value;
    }

    public Material getMaterialValue(){
        return materialValue;
    }
}
