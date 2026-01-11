package com.stoinkcraft.enterprise.listeners;

import com.stoinkcraft.StoinkCore;
import com.stoinkcraft.enterprise.listeners.chatactions.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.*;

public class PlayerChatListener implements Listener {

    private StoinkCore plugin;
    private List<ChatAction> chatActionList = new ArrayList<>();

    public PlayerChatListener(StoinkCore plugin){
        this.plugin = plugin;
        chatActionList.add(new ChatDepositAction(plugin));
        chatActionList.add(new ChatWithdrawAction(plugin));
        chatActionList.add(new EnterpriseChatAction(plugin));
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        chatActionList.stream().forEach(a -> a.handleChat(event));
    }
}
