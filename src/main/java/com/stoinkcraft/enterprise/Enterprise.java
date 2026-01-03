package com.stoinkcraft.enterprise;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.stoinkcraft.jobs.jobsites.JobSiteManager;
import com.stoinkcraft.market.boosters.Booster;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class Enterprise {

    @Expose
    @SerializedName("enterpriseType")
    private String enterpriseType = "PLAYER";

    @Expose
    private String name;

    @Expose
    private final UUID ceo;

    @Expose
    @SerializedName("members")
    private final Map<UUID, Role> members = new HashMap<>();

    @Expose
    private double bankBalance;

    @Expose
    private double netWorth;

    @Expose
    private Location warp;

    @Expose
    private Booster activeBooster;

    @Expose
    private int outstandingShares;

    @Expose
    private final List<PriceSnapshot> priceHistory = new ArrayList<>();

    @Expose
    private UUID enterpriseID;

    @Expose
    private int plotIndex = -1;

    private List<UUID> activeEnterpriseChat = new ArrayList<>();

    // NOT serialized directly - loaded/saved separately
    private transient JobSiteManager jobSiteManager;

    // ... constructors ...

    public Enterprise(String name, UUID ceo, double bankBalance, double netWorth,
                      int outstandingShares, Booster activeBooster, UUID enterpriseID) {
        this.name = name;
        this.ceo = ceo;
        this.bankBalance = bankBalance;
        this.netWorth = netWorth;
        this.outstandingShares = outstandingShares;
        this.activeBooster = activeBooster;
        this.enterpriseID = enterpriseID;
        this.enterpriseType = "PLAYER";
        members.put(ceo, Role.CEO);
    }

    public Enterprise(String name, UUID ceo) {
        this(name, ceo, 0, 0, 0, null, UUID.randomUUID());
    }

    protected Enterprise(String name, UUID ceo, String type) {
        this(name, ceo, 0, 0, 0, null, UUID.randomUUID());
        this.enterpriseType = type;
    }

    // Initialize JobSiteManager after loading
    public void initializeJobSiteManager() {
        if (jobSiteManager == null) {
            jobSiteManager = new JobSiteManager(this, plotIndex);
        }
    }

    public JobSiteManager getJobSiteManager() {
        return jobSiteManager;
    }

    // ... rest of your methods ...

    public String getEnterpriseType() {
        return enterpriseType;
    }

    public boolean isOnline() {
        return members.keySet().stream()
                .anyMatch(uuid -> {
                    Player player = Bukkit.getPlayer(uuid);
                    return player != null && player.isOnline();
                });
    }

    public int getPlotIndex() {
        return plotIndex;
    }

    public void setPlotIndex(int plotIndex) {
        this.plotIndex = plotIndex;
    }

    public void setEnterpriseID(UUID id) {
        this.enterpriseID = id;
    }

    public double getShareValue() {
        return netWorth / com.stoinkcraft.utils.SCConstants.MAX_SHARES;
    }

    public int getAvailableShares() {
        return com.stoinkcraft.utils.SCConstants.MAX_SHARES - outstandingShares;
    }

    public int getOutstandingShares() {
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
        if (priceHistory.size() > 100) priceHistory.remove(0);
    }

    public List<PriceSnapshot> getPriceHistory() {
        return priceHistory;
    }

    public boolean isBoosted() {
        return activeBooster != null;
    }

    public Booster getActiveBooster() {
        return this.activeBooster;
    }

    public void setActiveBooster(Booster booster) {
        this.activeBooster = booster;
    }

    public void hireEmployee(UUID employee) {
        if (getEmployees().size() < EnterpriseManager.getEnterpriseManager().getMaximumEmployees() + 1)
            members.put(employee, Role.EMPLOYEE);
        else
            Bukkit.getOfflinePlayer(employee).getPlayer().sendMessage("This enterprise is full!");
    }

    public boolean isMember(UUID member) {
        return members.containsKey(member);
    }

    public boolean fireMember(UUID member) {
        if (isMember(member)) {
            members.remove(member);
            return true;
        }
        return false;
    }

    public boolean resignMember(UUID member) {
        if (isMember(member)) {
            members.remove(member);
            return true;
        }
        return false;
    }

    public Role getMemberRole(UUID member) {
        return members.getOrDefault(member, null);
    }

    public UUID getID() {
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

    public List<UUID> getEmployees() {
        List<UUID> uuids = new ArrayList<>();
        for (UUID uuid : members.keySet()) {
            if (members.get(uuid).equals(Role.EMPLOYEE)) {
                uuids.add(uuid);
            }
        }
        return uuids;
    }

    public void setName(String name) {
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

    public void increaseBankBalance(double value) {
        this.bankBalance += value;
    }

    public void decreaseBankBalance(double value) {
        this.bankBalance -= value;
    }

    public void increaseNetworth(double value) {
        this.netWorth += value;
    }

    public void decreaseNetworth(double value) {
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

    public List<UUID> getActiveEnterpriseChat() {
        return activeEnterpriseChat;
    }

    public List<Player> getOnlineMembers() {
        return members.keySet().stream()
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .toList();
    }

    public void addEnterpriseChatter(UUID member) {
        if(!activeEnterpriseChat.contains(member))
            this.activeEnterpriseChat.add(member);
    }

    public void removeEnterpriseChatter(UUID member) {
        if(activeEnterpriseChat.contains(member))
            this.activeEnterpriseChat.remove(member);
    }
}