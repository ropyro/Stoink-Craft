package com.stoinkcraft.enterprise;

import com.stoinkcraft.StoinkCore;
import net.milkbowl.vault.economy.Economy;

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

    public boolean createEnterprise(Enterprise enterprise){
        if(getEnterpriseByMember(enterprise.getCeo()) == null){
            enterpriseList.add(enterprise);
            return true;
        }
        return false;
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
