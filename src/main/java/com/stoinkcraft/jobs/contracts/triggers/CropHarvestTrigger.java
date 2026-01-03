package com.stoinkcraft.jobs.contracts.triggers;

import com.stoinkcraft.jobs.contracts.ContractContext;
import org.bukkit.Material;

public class CropHarvestTrigger implements ContractTrigger {

    private final Material cropType;

    public CropHarvestTrigger(Material cropType) {
        this.cropType = cropType;
    }

    @Override
    public boolean matches(ContractContext context) {
        Material harvested = context.getEventData(Material.class);
        return harvested == cropType;
    }

    @Override
    public int getProgressIncrement(ContractContext context) {
        return context.getAmount();
    }
}

