package com.stoinkcraft.enterprise;

import com.stoinkcraft.boosters.Booster;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class Enterprise {
    private final String name;
    private final UUID ceo;
//    private UUID cfo;
//    private UUID coo;
    private final Map<UUID, Role> members = new HashMap<UUID, Role>();
    private final Map<UUID, Double> shares = new HashMap<>();
    private double bankBalance;
    private double netWorth;

    private Location warp;

    private Booster activeBooster;

    public Enterprise(String name, UUID ceo) {
        this.name = name;
        this.ceo = ceo;
        this.bankBalance = 0;
        this.netWorth = 0;
        members.put(ceo, Role.CEO);
        shares.put(ceo, 40.0);
        activeBooster = null;
    }

    public boolean isBoosted(){
        return activeBooster != null;
    }

    public Booster getActiveBooster(){
        return this.activeBooster;
    }

    public void setActiveBooster(Booster booster){
        this.activeBooster = booster;
    }


    public void hireEmployee(UUID employee){
        if(getEmployees().size() < EnterpriseManager.getEnterpriseManager().getMaximumEmployees() + 1)
            members.put(employee, Role.EMPLOYEE);
        else
            Bukkit.getOfflinePlayer(employee).getPlayer().sendMessage("This enterprise is full!");
    }

    public boolean isMember(UUID member){
        return members.containsKey(member);
    }

//    /**
//     * A true return means sucessful promotion; false means no promotion available
//     * @param member
//     * @return
//     */
//    public boolean promoteMember(UUID member){
//        if(isMember(member)){
//            Role memberRole = getMemberRole(member);
//            if(memberRole.equals(Role.EMPLOYEE)){
//                if(coo == null){
//                    members.replace(member, memberRole, Role.COO);
//                    this.coo = member;
//                    return true;
//                }
//                return false;
//            }else if(memberRole.equals(Role.COO)){
//                if(cfo == null){
//                    members.replace(member, memberRole, Role.CFO);
//                    this.cfo = member;
//                    this.coo = null;
//                    return true;
//                }
//                return false;
//            }
//        }
//        return false;
//    }
//
//    /**
//     * a true return means sucessful demotion; a false return means player is already lowest level
//     * @param member
//     * @return
//     */
//    public boolean demoteMember(UUID member){
//        if(isMember(member)) {
//            Role memberRole = getMemberRole(member);
//            if(memberRole.equals(Role.CFO)){
//                if(coo == null){
//                    members.replace(member, memberRole, Role.COO);
//                    return true;
//                }
//                return false;
//            }else if(memberRole.equals(Role.COO)){
//                members.replace(member, memberRole, Role.EMPLOYEE);
//                return true;
//            }
//            return false;
//        }
//        return false;
//    }

    public boolean fireMember(UUID member){
        if(isMember(member)){
            members.remove(member);
            return true;
        }
        return false;
    }

    public boolean resignMember(UUID member){
        if(isMember(member)){
            members.remove(member);
            return true;
        }
        return false;
    }

    public Role getMemberRole(UUID member) {
        return members.getOrDefault(member, null);
    }

    /*
    Getters & Setters
     */
    public Map<UUID, Role> getMembers() {
        return members;
    }

    public String getName() {
        return name;
    }

    public UUID getCeo() {
        return ceo;
    }

//    public UUID getCfo() {
//        return cfo;
//    }
//
//    public void setCfo(UUID cfo) {
//        this.cfo = cfo;
//    }
//
//    public UUID getCOO() {
//        return coo;
//    }
//
//    public void setCOO(UUID manager) {
//        this.coo = manager;
//    }

    public List<UUID> getEmployees(){
        List<UUID> uuids = new ArrayList<UUID>();
        for(UUID uuid : members.keySet()){
            if(members.get(uuid).equals(Role.EMPLOYEE)){
                uuids.add(uuid);
            }
        }
        return uuids;
    }

    public Location getWarp() {
        return warp;
    }

    public void setWarp(Location warp) {
        this.warp = warp;
    }

    public Map<UUID, Double> getShares() {
        return shares;
    }

    public double getBankBalance() {
        return bankBalance;
    }

    public void increaseBankBalance(double value){
        this.bankBalance += value;
    }
    public void decreaseBankBalance(double value) {
        this.bankBalance -= value;
    }
    public void increaseNetworth(double value){
        this.netWorth += value;
    }
    public void setBankBalance(double bankBalance) {
        this.bankBalance = bankBalance;
    }
    public double getNetWorth() {
        return netWorth;
    }
    public void setNetWorth(double netWorth) {
        this.netWorth = netWorth;
    }
}
