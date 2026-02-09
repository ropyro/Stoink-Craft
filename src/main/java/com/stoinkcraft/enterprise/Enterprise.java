package com.stoinkcraft.enterprise;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.stoinkcraft.jobsites.sites.JobSiteManager;
import com.stoinkcraft.items.booster.Booster;
import com.stoinkcraft.enterprise.reputation.ReputationCalculator;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.function.Function;

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

    private double netWorth;

    @Expose
    private double reputation = 0.0;

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


    private transient JobSiteManager jobSiteManager;


    public Enterprise(String name, UUID ceo, double bankBalance, double reputation,
                      int outstandingShares, Booster activeBooster, UUID enterpriseID) {
        this.name = name;
        this.ceo = ceo;
        this.bankBalance = bankBalance;
        this.reputation = reputation;
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

    public JobSiteManager initializeJobSiteManager() {
        if (jobSiteManager == null) {
            jobSiteManager = new JobSiteManager(this, plotIndex);
        }
        return jobSiteManager;
    }

    public JobSiteManager getJobSiteManager() {
        return jobSiteManager;
    }


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
        return getNetWorth() / com.stoinkcraft.utils.SCConstants.MAX_SHARES;
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

    public Booster getActiveBooster() {
        return activeBooster;
    }

    public void setActiveBooster(Booster booster) {
        this.activeBooster = booster;
    }

    public boolean hasActiveBooster() {
        return activeBooster != null && !activeBooster.isExpired();
    }

    public double getBoosterMultiplier() {
        if (!hasActiveBooster()) {
            return 1.0;
        }
        return activeBooster.getMultiplier();
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
            if (!members.get(uuid).equals(Role.CEO)) {
                uuids.add(uuid);
            }
        }
        return uuids;
    }

    public List<UUID> getExecutives() {
        List<UUID> uuids = new ArrayList<>();
        for (UUID uuid : members.keySet()) {
            if (members.get(uuid).equals(Role.EXECUTIVE)) {
                uuids.add(uuid);
            }
        }
        return uuids;
    }

    public boolean hasManagementPermission(UUID member) {
        Role role = getMemberRole(member);
        return role != null && (role.equals(Role.CEO) || role.equals(Role.EXECUTIVE));
    }

    public boolean setMemberRole(UUID member, Role role) {
        if (!isMember(member)) return false;
        if (member.equals(ceo) && !role.equals(Role.CEO)) return false;
        members.put(member, role);
        return true;
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

    @Deprecated
    public void increaseNetworth(double value) {
        this.bankBalance += value;
    }

    @Deprecated
    public void decreaseNetworth(double value) {
        this.bankBalance -= value;
    }

    public void setBankBalance(double bankBalance) {
        this.bankBalance = bankBalance;
    }

    public double getNetWorth() {
        return ReputationCalculator.calculateNetWorth(this);
    }

    @Deprecated
    public void setNetWorth(double netWorth) {}

    public double getReputation() {
        return reputation;
    }

    public void setReputation(double reputation) {
        this.reputation = clampReputation(reputation);
    }

    public void addReputation(double amount) {
        this.reputation = clampReputation(this.reputation + amount);
    }

    public void removeReputation(double amount) {
        this.reputation = clampReputation(this.reputation - amount);
    }

    private double clampReputation(double value) {
        double min = com.stoinkcraft.config.ConfigLoader.getEconomy().getReputationMinValue();
        double max = com.stoinkcraft.config.ConfigLoader.getEconomy().getReputationMaxValue();
        return Math.max(min, Math.min(max, value));
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

    public void sendEnterpriseMessage(String... lines) {
        getOnlineMembers().forEach(player -> {
            for (String line : lines) {
                player.sendMessage(line);
            }
        });
    }

    public void sendPersonalizedMessage(Function<Player, String[]> messageBuilder) {
        getOnlineMembers().forEach(player -> {
            String[] lines = messageBuilder.apply(player);
            for (String line : lines) {
                player.sendMessage(line);
            }
        });
    }
}