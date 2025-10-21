package com.stoinkcraft.utils;

import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ChatUtils {

    public static void sendMessage(Player player, String message){
        player.sendMessage("§b§lStonk §8» §f" + message);
    }

    // In ChatUtils.java
    public static String colorizePercent(double percent) {
        String color = percent > 0 ? "§a" : percent < 0 ? "§c" : "§7";
        return color + (percent > 0 ? "+" : "") + formatPercent(percent);
    }

    public static String formatPercent(double percent) {
        return String.format("%.2f%%", percent);
    }

    public static String formatDate(Date date) {
        return new SimpleDateFormat("MMM dd, yyyy HH:mm").format(date);
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
