package com.stoinkcraft.shares;

import com.stoinkcraft.enterprise.Enterprise;
import org.bukkit.entity.Player;

import java.util.UUID;

public class Share {

    private UUID owner;
    private UUID enterpriseID;
    private double purchasePrice;

    public Share(UUID owner, UUID enterpriseID, double purchasePrice){
        this.owner = owner;
        this.enterpriseID = enterpriseID;
        this.purchasePrice = purchasePrice;
    }

    public UUID getOwner() {
        return owner;
    }

    public UUID getEnterpriseID(){
        return enterpriseID;
    }

    public double getPurchasePrice() {
        return purchasePrice;
    }
}
