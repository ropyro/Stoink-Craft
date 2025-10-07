package com.stoinkcraft.market.values;

import org.bukkit.Material;

import java.util.Arrays;
import java.util.stream.Collectors;

public class TaskValue {

    private String key;
    private Double value;

    private Material materialValue;

    public TaskValue(String key, Double value, Material materialValue){
        this.key = key;
        this.value = value;
        this.materialValue = materialValue;
    }

    public String getDisplayName() {
        return Arrays.stream(key.toLowerCase().split("_"))
                .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1))
                .collect(Collectors.joining(" "));
    }

    public Double getValue() {
        return value;
    }

    public Material getMaterialValue(){
        return materialValue;
    }
}
