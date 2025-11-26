package com.stoinkcraft.enterprise.listeners;

import com.stoinkcraft.StoinkCore;
import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.enterprise.EnterpriseManager;
import com.stoinkcraft.enterprise.guis.UnemployedGUI;
import com.stoinkcraft.misc.daily.DailyManager;
import com.stoinkcraft.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerJoinListener implements Listener {

    private final StoinkCore plugin;

    public PlayerJoinListener(StoinkCore plugin){
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();
        if(!EnterpriseManager.getEnterpriseManager().isInEnterprise(player.getUniqueId())) {
            BukkitRunnable task = new BukkitRunnable() {
                @Override
                public void run() {
                    new UnemployedGUI(player).openWindow();
                }
            };

            int delayTicks = 30 * 20;
            task.runTaskLater(plugin, delayTicks);
        }
        Bukkit.getScheduler().runTaskLater(StoinkCore.getInstance(), new Runnable() {
            @Override
            public void run() {
                sendMOTD(event.getPlayer());
            }
        }, 20*3);
    }

    private void sendMOTD(Player player){
        Enterprise enterprise = EnterpriseManager.getEnterpriseManager().getEnterpriseByMember(player.getUniqueId());

        player.sendMessage("§7§m                                                                         §r");
        player.sendMessage("           §b§lWelcome to Stonk Craft!");
        player.sendMessage(" ");
        if(DailyManager.INSTANCE.canClaimDaily(player))
            player.sendMessage(" §a• §fYour daily reward is available §b/daily");
        if(enterprise == null)
            player.sendMessage(" §a• §fYou are unemployed! Do §b/enterprise §fto get a job!");
        else
            player.sendMessage(" §a• §b"+ enterprise.getName() + " §fhas increased in value \n   from §a$" +
                    ChatUtils.formatMoney(enterprise.getPriceHistory().getFirst().getSharePrice())
                    + "§f to §a$" + ChatUtils.formatMoney(enterprise.getShareValue()));
        player.sendMessage("§7§m                                                                         §r");
    }
}
