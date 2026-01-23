package com.stoinkcraft.jobsites.contracts.triggers;

import com.stoinkcraft.jobsites.contracts.ContractContext;
import org.bukkit.Material;

import java.util.Set;

public class GeodeMineTrigger implements ContractTrigger {

    private static final Set<Material> GEODE_MATERIALS = Set.of(
            Material.AMETHYST_BLOCK,
            Material.AMETHYST_CLUSTER,
            Material.SMALL_AMETHYST_BUD,
            Material.MEDIUM_AMETHYST_BUD,
            Material.LARGE_AMETHYST_BUD
    );

    @Override
    public boolean matches(ContractContext context) {
        Material material = context.getEventData(Material.class);
        return GEODE_MATERIALS.contains(material);
    }

    @Override
    public int getProgressIncrement(ContractContext context) {
        // Clusters give more progress
        Material material = context.getEventData(Material.class);
        if (material == Material.AMETHYST_CLUSTER) {
            return context.getAmount() * 2;
        }
        return context.getAmount();
    }
}