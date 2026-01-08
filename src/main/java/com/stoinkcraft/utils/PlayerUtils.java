package com.stoinkcraft.utils;

import com.stoinkcraft.StoinkCore;
import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.enterprise.EnterpriseManager;
import com.stoinkcraft.enterprise.shares.Share;
import com.stoinkcraft.enterprise.shares.ShareManager;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import org.bukkit.Bukkit;
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

    public static void givePermission(Player player, String permission){
        try{
            LuckPerms luckPerms = StoinkCore.getInstance().getServer().getServicesManager().load(LuckPerms.class);
            User user = luckPerms.getPlayerAdapter(Player.class).getUser(player);
            Node node = Node.builder(permission).value(true).build();
            user.data().add(node);
            luckPerms.getUserManager().saveUser(user);
        }catch (Exception e){
            Bukkit.getLogger().info(e.getMessage());
        }
    }

    public static void removePermission(Player player, String permission){
        try{
            LuckPerms luckPerms = StoinkCore.getInstance().getServer().getServicesManager().load(LuckPerms.class);
            User user = luckPerms.getPlayerAdapter(Player.class).getUser(player);
            Node node = Node.builder(permission).value(true).build();
            user.data().remove(node);
            luckPerms.getUserManager().saveUser(user);
        }catch (Exception e){
            Bukkit.getLogger().info(e.getMessage());
        }
    }
}
