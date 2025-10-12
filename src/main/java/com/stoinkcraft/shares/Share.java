package com.stoinkcraft.shares;

import com.stoinkcraft.enterprise.Enterprise;
import org.bukkit.entity.Player;

public class Share {

    private Player owner;
    private Enterprise enterprise;
    private double purchasePrice;

    public Share(Player owner, Enterprise enterprise, double purchasePrice){
        this.owner = owner;
        this.enterprise = enterprise;
        this.purchasePrice = purchasePrice;
    }

    public Player getOwner() {
        return owner;
    }

    public Enterprise getEnterprise() {
        return enterprise;
    }

    public double getPurchasePrice() {
        return purchasePrice;
    }
}
