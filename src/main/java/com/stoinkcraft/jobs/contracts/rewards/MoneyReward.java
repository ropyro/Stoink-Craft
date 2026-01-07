package com.stoinkcraft.jobs.contracts.rewards;

import com.stoinkcraft.StoinkCore;
import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.jobs.contracts.ActiveContract;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MoneyReward implements DescribableReward {

    private final double totalAmount;
    private final double playerShare; // 0.0 - 1.0

    public MoneyReward(double totalAmount, double playerShare) {
        this.totalAmount = totalAmount;
        this.playerShare = playerShare;
    }

    // Add these getters
    public double getTotalAmount() {
        return totalAmount;
    }

    public double getPlayerShare() {
        return playerShare;
    }

    public double getEnterpriseAmount() {
        return totalAmount * (1 - playerShare);
    }

    public double getPlayerPoolAmount() {
        return totalAmount * playerShare;
    }

    @Override
    public void apply(Enterprise enterprise, ActiveContract contract) {
        double playerTotal = totalAmount * playerShare;
        double enterpriseTotal = totalAmount - playerTotal;

        enterprise.increaseNetworth(enterpriseTotal);

        Map<UUID, Double> percentages = contract.getContributionPercentages();

        percentages.forEach((uuid, percent) -> {
            OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
            StoinkCore.getEconomy().depositPlayer(player, playerTotal * percent);
        });
    }

    @Override
    public List<String> getLore() {
        return List.of(
                "§f$" + totalAmount,
                "§7Player Share: " + (int)(playerShare * 100) + "%"
        );
    }
}