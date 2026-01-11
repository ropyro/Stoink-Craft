package com.stoinkcraft.earning.contracts.rewards;

import com.stoinkcraft.StoinkCore;
import com.stoinkcraft.config.ConfigLoader;
import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.earning.contracts.ActiveContract;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MoneyReward implements DescribableReward {

    private final double totalAmount;

    public MoneyReward(double totalAmount, double playerShare) {
        this.totalAmount = totalAmount;
    }

    // Add these getters
    public double getTotalAmount() {
        return totalAmount;
    }

    public double getPlayerShare() {
        return ConfigLoader.getEconomy().getPlayerPaySplit();
    }

    public double getEnterpriseAmount() {
        return totalAmount * (1 - getPlayerShare());
    }

    public double getPlayerPoolAmount() {
        return totalAmount * getPlayerShare();
    }

    @Override
    public void apply(Enterprise enterprise, ActiveContract contract) {
        double playerTotal = totalAmount * getPlayerShare();
        double enterpriseTotal = totalAmount - playerTotal;

        // Add to bank balance - networth is now calculated from bankBalance * reputationMultiplier
        enterprise.increaseBankBalance(enterpriseTotal);

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
                "§7Player Share: " + (int)(getPlayerShare() * 100) + "%"
        );
    }
}