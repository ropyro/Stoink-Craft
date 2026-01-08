package com.stoinkcraft.earning.contracts.triggers;

import com.stoinkcraft.earning.contracts.ContractContext;
import org.bukkit.Material;

import java.util.Set;

public class BlockMineTrigger implements ContractTrigger {

    private final Set<Material> validMaterials;

    public BlockMineTrigger(Material... materials) {
        this.validMaterials = Set.of(materials);
    }

    public BlockMineTrigger(Set<Material> materials) {
        this.validMaterials = materials;
    }

    @Override
    public boolean matches(ContractContext context) {
        Material material = context.getEventData(Material.class);
        return validMaterials.contains(material);
    }

    @Override
    public int getProgressIncrement(ContractContext context) {
        return context.getAmount();
    }

    public Set<Material> getValidMaterials() {
        return validMaterials;
    }
}