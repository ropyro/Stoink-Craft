package com.stoinkcraft.enterprise.listeners.chatactions;

import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Set;
import java.util.UUID;

public interface ChatAction {

    void handleChat(AsyncPlayerChatEvent event);

}
