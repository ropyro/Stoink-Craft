package com.stoinkcraft.shares;

import com.stoinkcraft.enterprise.Enterprise;
import org.bukkit.entity.Player;

import java.util.Date;
import java.util.UUID;

public class Share {

    private UUID owner;
    private UUID enterpriseID;
    private double purchasePrice;
    private Date purchaseDate;

    public Share(UUID owner, UUID enterpriseID, double purchasePrice, Date purchaseDate){
        this.owner = owner;
        this.enterpriseID = enterpriseID;
        this.purchasePrice = purchasePrice;
        this.purchaseDate = purchaseDate;
    }

    public Date getPurchaseDate(){
        return this.purchaseDate;
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
