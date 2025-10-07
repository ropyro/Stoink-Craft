package com.stoinkcraft.market.values;

import org.bukkit.Material;

public class ItemValue extends TaskValue{

    private Material material;

    public ItemValue(Material material, Double value){
        super(material.name(), value, material);
        this.material = material;
    }

    public Material getMaterial() {
        return material;
    }
}
