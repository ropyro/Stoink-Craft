package com.stoinkcraft.jobsites.contracts.triggers;

import com.stoinkcraft.jobsites.contracts.ContractContext;
import org.bukkit.Material;

public class HoneyHarvestTrigger implements ContractTrigger {

    @Override
    public boolean matches(ContractContext context) {
        Material material = context.getEventData(Material.class);
        return material == Material.HONEYCOMB;
    }

    @Override
    public int getProgressIncrement(ContractContext context) {
        return context.getAmount();
    }
}
