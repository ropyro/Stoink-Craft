package com.stoinkcraft.shares;

import com.stoinkcraft.StoinkCore;
import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.enterprise.EnterpriseManager;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ShareManager {

    private static ShareManager INSTANCE;

    private List<Share> allShares = new ArrayList<>();

    public ShareManager(){
        INSTANCE = this;
    }

    public static ShareManager getInstance(){
        return INSTANCE;
    }

    public ShareManager getShareManager(){
        return INSTANCE;
    }

    public List<Share> getAllShares() {
        return allShares;
    }

    public List<Share> getPlayersShares(OfflinePlayer player, Enterprise enterprise){
        return allShares.stream()
                .filter(s -> s.getOwner().equals(player.getUniqueId()) && s.getEnterpriseID().equals(enterprise.getID()))
                .collect(Collectors.toList());
    }

    public List<Share> getEnterpriseShares(Enterprise enterprise){
        return allShares.stream()
                .filter(s -> s.getEnterpriseID().equals(enterprise.getID()))
                .collect(Collectors.toList());
    }

        public void buyShare(Player player, Enterprise enterprise, int amount) {
        double shareValue = enterprise.getShareValue();
        double totalCost = shareValue * amount;

        if (enterprise.getAvailableShares() < amount) {
            player.sendMessage("§cNot enough shares available!");
            return;
        }

        if (!StoinkCore.getEconomy().has(player, totalCost)) {
            player.sendMessage("§cYou can’t afford that many shares!");
            return;
        }

        StoinkCore.getEconomy().withdrawPlayer(player, totalCost);
        enterprise.deposit(totalCost);
        enterprise.addOutstandingShares(amount);

        for (int i = 0; i < amount; i++) {
            allShares.add(new Share(player.getUniqueId(), enterprise.getID(), shareValue));
        }

        player.sendMessage("§aPurchased " + amount + " shares of " + enterprise.getName() + " for $" + totalCost);
    }

    // --- SELL ---
    public void sellShares(Player player, Enterprise enterprise, int amount) {
        List<Share> owned = getPlayersShares(player, enterprise);

        if (owned.size() < amount) {
            player.sendMessage("§cYou don’t own that many shares!");
            return;
        }

        double currentPrice = enterprise.getShareValue();
        double totalPayout = currentPrice * amount;

        enterprise.withdraw(totalPayout);
        StoinkCore.getEconomy().depositPlayer(player, totalPayout);
        enterprise.removeOutstandingShares(amount);

        for (int i = 0; i < amount; i++) {
            allShares.remove(owned.get(i));
        }

        player.sendMessage("§aSold " + amount + " shares for $" + totalPayout);
    }

    public void sellSharesOffline(OfflinePlayer player, Enterprise enterprise, int amount) {
        List<Share> owned = getPlayersShares(player, enterprise);

        if (owned.size() < amount) {
            return;
        }

        double currentPrice = enterprise.getShareValue();
        double totalPayout = currentPrice * amount;

        enterprise.withdraw(totalPayout);
        StoinkCore.getEconomy().depositPlayer(player, totalPayout);
        enterprise.removeOutstandingShares(amount);

        for (int i = 0; i < amount; i++) {
            allShares.remove(owned.get(i));
        }
    }

        public double calculateTotalPlayerValue(Player player) {
        double total = 0;
        for (Share share : allShares) {
            Enterprise enterprise = EnterpriseManager.getEnterpriseManager().getEnterpriseByID(share.getEnterpriseID());
            total += enterprise.getShareValue();
        }
        return total;
    }
}
