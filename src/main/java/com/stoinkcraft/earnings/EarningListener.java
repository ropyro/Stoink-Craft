package com.stoinkcraft.earnings;

import com.stoinkcraft.StoinkCore;
import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.enterprise.EnterpriseManager;
import com.stoinkcraft.market.MarketManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class EarningListener implements Listener {

    private final StoinkCore plugin;

    public EarningListener(StoinkCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        EnterpriseManager em = EnterpriseManager.getEnterpriseManager();
        if(em.getEnterpriseByMember(player.getUniqueId()) != null){
            Enterprise e = em.getEnterpriseByMember(player.getUniqueId());
            Material blockType = event.getBlock().getType();
            double value = MarketManager.getValue(blockType.name());
            player.sendMessage("§aBroke block: §e" + blockType.name() + " §7(Value: $" + value + ")");
            if (value > 0) {
                e.increaseBankBalance(value*0.5);
                if (StoinkCore.getEconomy() == null) {
                    player.sendMessage("§cVault economy not loaded!");
                } else {
                    StoinkCore.getEconomy().depositPlayer(player, value * 0.5);
                    player.sendMessage("§aYou earned $" + (value * 0.5));
                }
            }
        }
    }
}
