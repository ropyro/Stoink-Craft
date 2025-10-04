package com.stoinkcraft.enterprise;

import com.stoinkcraft.StoinkCore;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.*;


public class EnterpriseManager {

    private static EnterpriseManager enterpriseManager;

    // New: target → list of inviter UUIDs
    private final Map<UUID, List<UUID>> invites = new HashMap<>();

    private StoinkCore plugin;
    private Economy econ;

    private ArrayList<Enterprise> enterpriseList;

    private int maximumEmployees;

    public EnterpriseManager(StoinkCore plugin, Economy econ, int maximumEmployees){
        this.plugin = plugin;
        this.econ = econ;

        enterpriseList = new ArrayList<Enterprise>();
        enterpriseManager = this;
        this.maximumEmployees = maximumEmployees;
    }

    public void disband(Enterprise enterprise) {
        if (!enterpriseList.contains(enterprise)) return;

        // Notify online members
        for (UUID uuid : enterprise.getMembers().keySet()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null && p.isOnline()) {
                p.sendMessage(ChatColor.RED + "Your enterprise '" + enterprise.getName() + "' has been disbanded.");
            }
        }

        // Optional: clear member maps (clean up references)
        enterprise.getMembers().clear();
        enterprise.getShares().clear();

        // Remove from enterprise list
        enterpriseList.remove(enterprise);

        // Optional: log to console
        Bukkit.getLogger().info("[StoinkCore] Disbanded enterprise: " + enterprise.getName());

        // Optional: trigger save
        File enterpriseFile = new File(plugin.getDataFolder(), "enterprises.yml");
        EnterpriseStorage.saveAllEnterprises(enterpriseFile);
    }

    public boolean setEnterpriseWarp(Player player){
        UUID uuid = player.getUniqueId();
        if(isInEnterprise(uuid)){
            Enterprise e = getEnterpriseByMember(uuid);
            if(e.getMemberRole(uuid).equals(Role.CEO)){
                e.setWarp(player.getLocation());
                player.sendMessage(e.getName() + "'s warp has been set!");
                File enterpriseFile = new File(plugin.getDataFolder(), "enterprises.yml");
                EnterpriseStorage.saveAllEnterprises(enterpriseFile);
                return true;
            }
        }
        player.sendMessage("You must be the CEO of an enterprise to set a warp!");
        return false;
    }

    public boolean deleteEnterpriseWarp(Player player){
        UUID uuid = player.getUniqueId();
        if(isInEnterprise(uuid)){
            Enterprise e = getEnterpriseByMember(uuid);
            if(e.getMemberRole(uuid).equals(Role.CEO)){
                e.setWarp(null);
                player.sendMessage(e.getName() + "'s warp has been deleted!");
                File enterpriseFile = new File(plugin.getDataFolder(), "enterprises.yml");
                EnterpriseStorage.saveAllEnterprises(enterpriseFile);
                return true;
            }
        }
        player.sendMessage("You must be the CEO of an enterprise to delete a warp!");
        return false;
    }



    public boolean createEnterprise(Enterprise enterprise){
        if(getEnterpriseByMember(enterprise.getCeo()) == null){
            enterpriseList.add(enterprise);
            return true;
        }
        return false;
    }

    public void createEnterprise(ServerEnterprise enterprise){
        enterpriseList.add(enterprise);
    }


        public void sendInvite(UUID target, UUID inviter) {
        invites.computeIfAbsent(target, k -> new ArrayList<>()).add(inviter);
    }

    public List<UUID> getInviters(UUID target) {
        return invites.getOrDefault(target, new ArrayList<>());
    }

    public Map<UUID, UUID> getAllInvites() {
        Map<UUID, UUID> flattened = new HashMap<>();
        for (Map.Entry<UUID, List<UUID>> entry : invites.entrySet()) {
            for (UUID inviter : entry.getValue()) {
                flattened.put(entry.getKey(), inviter); // Only returns one inviter, can adjust if needed
            }
        }
        return flattened;
    }

    public void clearInvite(UUID target, UUID inviter) {
        List<UUID> list = invites.get(target);
        if (list != null) {
            list.remove(inviter);
            if (list.isEmpty()) invites.remove(target);
        }
    }

    public boolean hasInvite(UUID target) {
        return invites.containsKey(target);
    }



    public Enterprise getEnterpriseByMember(UUID uuid){
        for(Enterprise e : enterpriseList){
            for(UUID u : e.getMembers().keySet()){
                if(u.equals(uuid)){
                    return e;
                }
            }
        }

        return null;
    }

    public void removeEnterprise(Enterprise enterprise) {
        enterpriseList.remove(enterprise);
    }

    public boolean isInEnterprise(UUID uuid) {
        return getEnterpriseByMember(uuid) != null;
    }


    public Enterprise getEnterpriseByName(String name){
        for(Enterprise e : enterpriseList){
            if(e.getName().equalsIgnoreCase(name)) return e;
        }
        return null;
    }


    public ArrayList<Enterprise> getEnterpriseList() {
        return enterpriseList;
    }

    public static EnterpriseManager getEnterpriseManager(){
        return enterpriseManager;
    }

    public int getMaximumEmployees() {
        return maximumEmployees;
    }
}
