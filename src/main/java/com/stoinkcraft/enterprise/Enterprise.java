package com.stoinkcraft.enterprise;

import com.stoinkcraft.StoinkCore;
import com.stoinkcraft.jobs.jobsites.JobSite;
import com.stoinkcraft.jobs.jobsites.JobSiteType;
import com.stoinkcraft.jobs.jobsites.sites.SkyriseSite;
import com.stoinkcraft.market.boosters.Booster;
import com.stoinkcraft.utils.SCConstants;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.*;

public class Enterprise {
    private String name;
    private final UUID ceo;
    private final Map<UUID, Role> members = new HashMap<UUID, Role>();
    private double bankBalance;
    private double netWorth;
    private Location warp;
    private Booster activeBooster;
    private int outstandingShares;
    private final List<PriceSnapshot> priceHistory = new ArrayList<>();
    private UUID enterpriseID;
    private SkyriseSite skyriseSite;

    private Map<JobSiteType, Location> plots;


    public Enterprise(String name, UUID ceo) {
        this(name, ceo, 0, 0, 0, null, UUID.randomUUID());
    }

    public Enterprise(String name, UUID ceo, double bankBalance, double netWorth, int outstandingShares, Booster activeBooster, UUID enterpriseID){
        this.name = name;
        this.ceo = ceo;
        this.bankBalance = bankBalance;
        this.netWorth = netWorth;
        this.outstandingShares = outstandingShares;
        members.put(ceo, Role.CEO);
        this.activeBooster = activeBooster;
        this.enterpriseID = enterpriseID;
        plots = StoinkCore.getEnterprisePlotManager().assignPlots(enterpriseID, 1);
        skyriseSite = new SkyriseSite(this, plots.get(JobSiteType.SKYRISE), false);
    }


    public SkyriseSite getSkyriseSite(){
        return this.skyriseSite;
    }


    public void setEnterpriseID(UUID id){
        this.enterpriseID = id;
    }

    public double getShareValue() {
        return netWorth / SCConstants.MAX_SHARES;
    }

    public int getAvailableShares() {
        return SCConstants.MAX_SHARES - outstandingShares;
    }

    public int getOutstandingShares(){
        return outstandingShares;
    }

    public void addOutstandingShares(int amount) {
        outstandingShares += amount;
    }

    public void removeOutstandingShares(int amount) {
        outstandingShares -= amount;
    }

    public void deposit(double amount) {
        bankBalance += amount;
    }

    public void withdraw(double amount) {
        bankBalance -= amount;
    }

    public void recordPriceSnapshot() {
        priceHistory.add(new PriceSnapshot(System.currentTimeMillis(), getShareValue()));
        if (priceHistory.size() > 100) priceHistory.remove(0); // keep latest 100
    }

    public List<PriceSnapshot> getPriceHistory() {
        return priceHistory;
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

    public UUID getID(){
        return this.enterpriseID;
    }

    public Map<UUID, Role> getMembers() {
        return members;
    }

    public String getName() {
        return name;
    }

    public UUID getCeo() {
        return ceo;
    }

    public List<UUID> getEmployees(){
        List<UUID> uuids = new ArrayList<UUID>();
        for(UUID uuid : members.keySet()){
            if(members.get(uuid).equals(Role.EMPLOYEE)){
                uuids.add(uuid);
            }
        }
        return uuids;
    }

    public void setName(String name){
        this.name = name;
    }

    public Location getWarp() {
        return warp;
    }
    public void setWarp(Location warp) {
        this.warp = warp;
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
    public void decreaseNetworth(double value){
        this.netWorth -= value;
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
