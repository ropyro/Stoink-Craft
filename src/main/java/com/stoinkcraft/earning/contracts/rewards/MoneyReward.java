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

    public MoneyReward(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public double getPlayerShare() {
        return ConfigLoader.getEconomy().getPlayerPaySplit();
    }

    /**
     * Gets the total amount after applying the enterprise's booster multiplier.
     */
    public double getBoostedTotal(Enterprise enterprise) {
        return totalAmount * enterprise.getBoosterMultiplier();
    }

    public double getEnterpriseAmount(Enterprise enterprise) {
        return getBoostedTotal(enterprise) * (1 - getPlayerShare());
    }

    public double getPlayerPoolAmount(Enterprise enterprise) {
        return getBoostedTotal(enterprise) * getPlayerShare();
    }

    @Override
    public void apply(Enterprise enterprise, ActiveContract contract) {
        double boostedTotal = getBoostedTotal(enterprise);
        double playerTotal = boostedTotal * getPlayerShare();
        double enterpriseTotal = boostedTotal - playerTotal;

        // Add to bank balance
        enterprise.increaseBankBalance(enterpriseTotal);

        // Distribute player share based on contributions
        Map<UUID, Double> percentages = contract.getContributionPercentages();

        percentages.forEach((uuid, percent) -> {
            OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
            StoinkCore.getEconomy().depositPlayer(player, playerTotal * percent);
        });
    }

    @Override
    public List<String> getLore() {
        return List.of(
                "§f$" + String.format("%.0f", totalAmount),
                "§7Player Share: " + (int) (getPlayerShare() * 100) + "%"
        );
    }

    /**
     * Gets lore showing boosted amounts (for UI display).
     */
    public List<String> getBoostedLore(Enterprise enterprise) {
        double multiplier = enterprise.getBoosterMultiplier();

        if (multiplier == 1.0) {
            return getLore();
        }

        double boosted = getBoostedTotal(enterprise);

        return List.of(
                "§f$" + String.format("%.0f", boosted) +
                        " §7(§a" + multiplier + "x boosted§7)",
                "§7Player Share: " + (int) (getPlayerShare() * 100) + "%"
        );
    }
}