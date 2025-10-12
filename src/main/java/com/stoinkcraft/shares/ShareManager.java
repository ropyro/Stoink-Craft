package com.stoinkcraft.shares;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ShareManager {

    private static ShareManager INSTANCE;

    private List<Share> ownedShares = new ArrayList<>();

    public ShareManager(){
        INSTANCE = this;
    }

    public ShareManager getShareManager(){
        return INSTANCE;
    }

    public List<Share> getPlayersShares(Player player){
        return ownedShares.stream().filter(share -> share.getOwner().equals(player)).toList();
    }

    public double getShareValue(Share share){
        return share.getEnterprise().getNetWorth()/share.getEnterprise().getShares().keySet().size();
    }
}
