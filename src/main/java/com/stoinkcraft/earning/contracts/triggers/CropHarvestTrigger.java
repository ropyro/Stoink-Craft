package com.stoinkcraft.earning.contracts.triggers;

import com.stoinkcraft.earning.contracts.ContractContext;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public class CropHarvestTrigger implements ContractTrigger {

    private final List<Material> cropTypes = new ArrayList<>();

    public CropHarvestTrigger(Material cropType) {
        this.cropTypes.add(cropType);
    }

    public CropHarvestTrigger(List<Material> cropType) {
        cropType.forEach(ct -> this.cropTypes.add(ct));
    }

    @Override
    public boolean matches(ContractContext context) {
        Material harvested = context.getEventData(Material.class);
        return cropTypes.stream().anyMatch(ct -> ct.equals(harvested));
    }

    @Override
    public int getProgressIncrement(ContractContext context) {
        return context.getAmount();
    }
}

