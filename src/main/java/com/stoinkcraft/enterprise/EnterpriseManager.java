package com.stoinkcraft.enterprise;

import com.stoinkcraft.StoinkCore;
import com.stoinkcraft.earning.jobsites.JobSiteManager;
import com.stoinkcraft.items.booster.Booster;
import com.stoinkcraft.earning.jobsites.protection.ProtectionManager;
import com.stoinkcraft.items.booster.BoosterTier;
import com.stoinkcraft.serialization.EnterpriseStorageJson;
import com.stoinkcraft.enterprise.shares.ShareManager;
import com.stoinkcraft.utils.ChatUtils;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;


public class EnterpriseManager {

    private static EnterpriseManager enterpriseManager;

    private final Map<UUID, List<UUID>> invites = new HashMap<>();

    private StoinkCore plugin;

    private ArrayList<Enterprise> enterpriseList;

    private int maximumEmployees;

    private List<NPC> topCeoNpcs = new ArrayList<>();

    public EnterpriseManager(StoinkCore plugin, int maximumEmployees){
        this.plugin = plugin;
        enterpriseList = new ArrayList<Enterprise>();
        enterpriseManager = this;
        this.maximumEmployees = maximumEmployees;
    }

    public Enterprise getEnterpriseByID(UUID enterpriseID){
        return enterpriseList.stream().filter(e -> e.getID().equals(enterpriseID)).findFirst().orElse(null);
    }

    public void activateBooster(Enterprise enterprise, BoosterTier tier) {
        StoinkCore.getInstance().getBoosterManager().activateBooster(enterprise, tier);
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

        EnterpriseStorageJson.disband(enterprise);
        enterprise.getJobSiteManager().disbandJobSites();

        // Optional: log to console
        Bukkit.getLogger().info("[StoinkCore] Disbanded enterprise: " + enterprise.getName());
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
            ProtectionManager pm = StoinkCore.getInstance().getProtectionManager();
            JobSiteManager jsm = enterprise.initializeJobSiteManager();
            jsm.initializeJobSites();
            enterpriseList.add(enterprise);
            StoinkCore.getInstance().getContractManager().generateContracts(enterprise, false); // daily
            StoinkCore.getInstance().getContractManager().generateContracts(enterprise, true);  // weekly
            return true;
        }
        return false;
    }

    public void loadEnterprise(Enterprise enterprise){
        enterpriseList.add(enterprise);
    }

    public void createEnterprise(ServerEnterprise enterprise){
        enterpriseList.add(enterprise);
        enterprise.initializeJobSiteManager();
        enterprise.getJobSiteManager().initializeJobSites();
        StoinkCore.getInstance().getContractManager().generateContracts(enterprise, false); // daily
        StoinkCore.getInstance().getContractManager().generateContracts(enterprise, true);  // weekly
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

    /**
     * Daily bank balance update.
     * Note: Daily tax has been replaced by the reputation system.
     * NetWorth is now calculated as bankBalance * reputationMultiplier.
     */
    public static void updateBankBalances() {
        // Tax logic removed - reputation system now handles enterprise value incentives.
        // NetWorth is calculated dynamically from bankBalance * reputationMultiplier.
        // Completing contracts increases reputation (and thus networth multiplier).
        // Letting contracts expire decreases reputation.
    }


    public void startJobSiteTicker(JavaPlugin plugin) {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Enterprise enterprise : getEnterpriseList()) {
                    JobSiteManager jsm = enterprise.getJobSiteManager();
                    if (jsm == null || !enterprise.isOnline()) continue;

                    // Tick each job site
                    if (jsm.getSkyriseSite() != null) jsm.getSkyriseSite().tick();
                    if (jsm.getQuarrySite() != null) jsm.getQuarrySite().tick();
                    if (jsm.getFarmlandSite() != null) jsm.getFarmlandSite().tick();
                    if (jsm.getGraveyardSite() != null) jsm.getGraveyardSite().tick();
                }
            }
        }.runTaskTimer(plugin, 20L, 20L); // run every second (20 ticks)
    }


}
