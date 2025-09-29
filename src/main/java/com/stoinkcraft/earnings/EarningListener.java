package com.stoinkcraft.earnings;

import com.stoinkcraft.StoinkCore;
import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.enterprise.EnterpriseManager;
import com.stoinkcraft.market.MarketManager;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerFishEvent;

public class EarningListener implements Listener {

    private final StoinkCore plugin;

    public EarningListener(StoinkCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        EnterpriseManager em = EnterpriseManager.getEnterpriseManager();
        if(em.getEnterpriseByMember(player.getUniqueId()) == null) return;
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

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        Player killer = entity.getKiller();

        if (killer == null) return;

        EnterpriseManager em = EnterpriseManager.getEnterpriseManager();
        if(em.getEnterpriseByMember(killer.getUniqueId()) == null) return;

            Enterprise e = em.getEnterpriseByMember(killer.getUniqueId());
            String mobType = entity.getType().name();
            double value = MarketManager.getValue(mobType);
            killer.sendMessage("§Killed entity: §e" + mobType + " §7(Value: $" + value + ")");
            if (value > 0) {
                e.increaseBankBalance(value*0.5);
                if (StoinkCore.getEconomy() == null) {
                    killer.sendMessage("§cVault economy not loaded!");
                } else {
                    StoinkCore.getEconomy().depositPlayer(killer, value * 0.5);
                    killer.sendMessage("§aYou earned $" + (value * 0.5));
                }
            }
    }

    @EventHandler
    public void onFish(PlayerFishEvent event) {
        if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH) return;

        Player player = event.getPlayer();
        double value = MarketManager.getValue("FISH");

        EnterpriseManager em = EnterpriseManager.getEnterpriseManager();
        if(em.getEnterpriseByMember(player.getUniqueId()) == null) return;
        Enterprise e = em.getEnterpriseByMember(player.getUniqueId());

        player.sendMessage("§aFish caught! §7(Value: $" + value + ")");
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
