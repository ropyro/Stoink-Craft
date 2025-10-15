package com.stoinkcraft.utils;

import org.bukkit.entity.Player;

public class ChatUtils {

    public static void sendMessage(Player player, String message){
        player.sendMessage("§b§lStonk §8» §f" + message);
    }

    public static String formatMoney(double value) {
        if (value >= 1_000_000_000D) {
            return String.format("%.2fb", value / 1_000_000_000D);
        } else if (value >= 1_000_000D) {
            return String.format("%.2fm", value / 1_000_000D);
        } else if (value >= 1_000D) {
            return String.format("%.2fk", value / 1_000D);
        } else {
            return String.format("%.2f", value);
        }
    }

    public static String formatMoneyNoCents(double value) {
        if (value >= 1_000_000_000D) {
            return String.format("%.0fb", value / 1_000_000_000D);
        } else if (value >= 1_000_000D) {
            return String.format("%.0fm", value / 1_000_000D);
        } else if (value >= 1_000D) {
            return String.format("%.0fk", value / 1_000D);
        } else {
            return String.format("%.0f", value);
        }
    }

}
