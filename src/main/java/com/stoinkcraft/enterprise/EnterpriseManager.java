package com.stoinkcraft.enterprise;

import com.stoinkcraft.StoinkCore;
import com.stoinkcraft.market.boosters.Booster;
import com.stoinkcraft.shares.ShareManager;
import com.stoinkcraft.utils.ChatUtils;
import com.stoinkcraft.utils.SCConstants;
import net.citizensnpcs.api.npc.NPC;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Duration;
import java.time.Instant;
import java.util.*;


public class EnterpriseManager {

    private static EnterpriseManager enterpriseManager;

    // New: target → list of inviter UUIDs
    private final Map<UUID, List<UUID>> invites = new HashMap<>();

    private StoinkCore plugin;
    private Economy econ;

    private ArrayList<Enterprise> enterpriseList;

    private int maximumEmployees;

    private List<NPC> topCeoNpcs = new ArrayList<>();

    public EnterpriseManager(StoinkCore plugin, Economy econ, int maximumEmployees){
        this.plugin = plugin;
        this.econ = econ;

        enterpriseList = new ArrayList<Enterprise>();
        enterpriseManager = this;
        this.maximumEmployees = maximumEmployees;
    }

    public Enterprise getEnterpriseByID(UUID enterpriseID){
        return enterpriseList.stream().filter(e -> e.getID().equals(enterpriseID)).findFirst().orElse(null);
    }

    public void setBooster(Enterprise enterprise, Booster booster){
        enterprise.setActiveBooster(booster);
        new BukkitRunnable() {
            @Override
            public void run() {
                enterprise.setActiveBooster(null);
                enterprise.getMembers().keySet().forEach(uuid -> {
                    Player online = Bukkit.getPlayer(uuid);
                    if (online != null && online.isOnline()) {
                        ChatUtils.sendMessage(online,ChatColor.GOLD + "" + booster.getMultiplier() + "x booster has expired!");
                    }
                });
            }
        }.runTaskLater(plugin, booster.getDuration());
    }

    public void disband(Enterprise enterprise) {
        if (!enterpriseList.contains(enterprise)) return;

        // Notify online members
        for (UUID uuid : enterprise.getMembers().keySet()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null && p.isOnline()) {
                ChatUtils.sendMessage(p,ChatColor.RED + "Your enterprise '" + enterprise.getName() + "' has been disbanded.");
            }
        }

        // Optional: clear member maps (clean up references)
        enterprise.getMembers().clear();


        ShareManager.getInstance().getEnterpriseShares(enterprise).forEach(share -> {
            OfflinePlayer player = Bukkit.getOfflinePlayer(share.getOwner());
            ShareManager.getInstance().sellSharesOffline(player, enterprise, 1);
        });

        // Remove from enterprise list
        enterpriseList.remove(enterprise);

        // Optional: log to console
        Bukkit.getLogger().info("[StoinkCore] Disbanded enterprise: " + enterprise.getName());

        // Optional: trigger save
        EnterpriseStorage.disband(enterprise);
    }

    public boolean setEnterpriseWarp(Player player){
        UUID uuid = player.getUniqueId();
        if(isInEnterprise(uuid)){
            Enterprise e = getEnterpriseByMember(uuid);
            if(e.getMemberRole(uuid).equals(Role.CEO)){
                e.setWarp(player.getLocation());
                ChatUtils.sendMessage(player,e.getName() + "'s warp has been set!");
                return true;
            }
        }
        ChatUtils.sendMessage(player,"You must be the CEO of an enterprise to set a warp!");
        return false;
    }

    public boolean deleteEnterpriseWarp(Player player){
        UUID uuid = player.getUniqueId();
        if(isInEnterprise(uuid)){
            Enterprise e = getEnterpriseByMember(uuid);
            if(e.getMemberRole(uuid).equals(Role.CEO)){
                e.setWarp(null);
                ChatUtils.sendMessage(player,e.getName() + "'s warp has been deleted!");
                return true;
            }
        }
        ChatUtils.sendMessage(player,"You must be the CEO of an enterprise to delete a warp!");
        return false;
    }



    public boolean createEnterprise(Enterprise enterprise){
        if(getEnterpriseByMember(enterprise.getCeo()) == null){
            enterpriseList.add(enterprise);
            return true;
        }
        return false;
    }

    public void loadEnterprise(Enterprise enterprise){
        enterpriseList.add(enterprise);
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

    public void recordPriceSnapshots(){
        try {
            for (Enterprise e : EnterpriseManager.getEnterpriseManager().getEnterpriseList()) {
                e.recordPriceSnapshot();
            }
        } catch (Exception e) {
            StoinkCore.getInstance().getLogger().severe("Error recording enterprise price snapshots: " + e.getMessage());
            e.printStackTrace();
        }
    }


    public int getMaximumEmployees() {
        return maximumEmployees;
    }

    public static void updateBankBalances() {
        List<Enterprise> enterprises = EnterpriseManager.getEnterpriseManager().getEnterpriseList();

        // Move funds from ServerEnterprises' balances into their net worth
        enterprises.stream()
                .filter(e -> e instanceof ServerEnterprise)
                .forEach(e -> {
                    double bankBalance = e.getBankBalance();
                    if (bankBalance > 0) {
                        e.increaseNetworth(bankBalance);
                        e.setBankBalance(0);
                    }
                });

        // Apply tax to private enterprises
        enterprises.stream()
                .filter(e -> !(e instanceof ServerEnterprise))
                .forEach(e -> {
                    double taxRate = SCConstants.ENTERPRISE_DAILY_TAX;
                    double before = e.getBankBalance();
                    e.setBankBalance(before * (1 - taxRate));
                });
    }




    private static Instant lastRotationTime;
    private static final Duration ROTATION_INTERVAL = Duration.ofDays(1);

    public static void startDailyTaxes(JavaPlugin plugin) {
        lastRotationTime = Instant.now();
        new BukkitRunnable() {
            @Override
            public void run() {
                // Run heavy economy and file I/O async
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                    updateBankBalances(); // your method that handles tax logic
                    lastRotationTime = Instant.now();
                });
            }
        }.runTaskTimer(plugin, 20L * 60 * 60 * 24, 20L * 60 * 60 * 24); // 24 hours
    }


    public static String getTimeUntilNextTaxation() {
        if (lastRotationTime == null) {
            return "Unknown";
        }

        Instant nextRotation = lastRotationTime.plus(ROTATION_INTERVAL);
        Duration remaining = Duration.between(Instant.now(), nextRotation);

        if (remaining.isNegative()) {
            return "00h 00m 00s";
        }

        long hours = remaining.toHours();
        long minutes = remaining.toMinutesPart();
        long seconds = remaining.toSecondsPart();

        return String.format("%02dh %02dm %02ds", hours, minutes, seconds);
    }

}
