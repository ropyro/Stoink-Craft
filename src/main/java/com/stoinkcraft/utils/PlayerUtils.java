package com.stoinkcraft.utils;

import com.stoinkcraft.StoinkCore;
import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.enterprise.EnterpriseManager;
import com.stoinkcraft.shares.Share;
import com.stoinkcraft.shares.ShareManager;
import org.bukkit.entity.Player;

import java.util.List;

public class PlayerUtils {

    public static double getPlayerNetworth(Player player){
        double networth = 0.0;
        networth += StoinkCore.getEconomy().getBalance(player);
        for(Enterprise enterprise : EnterpriseManager.getEnterpriseManager().getEnterpriseList()) {
            List<Share> shares = ShareManager.getInstance().getPlayersShares(player, enterprise);
            networth += enterprise.getShareValue() * shares.size();
        }
        return networth;
    }
}
