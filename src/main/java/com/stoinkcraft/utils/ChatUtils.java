package com.stoinkcraft.utils;

public class ChatUtils {

    public static String formatMoney(double value) {
        if (value >= 1_000_000_000D) {
            return String.format("%.2fb", value / 1_000_000_000D);
        } else if (value >= 1_000_000D) {
            return String.format("%.2fm", value / 1_000_000D);
        } else if (value >= 1_000D) {
            return String.format("%.1fk", value / 1_000D);
        } else {
            return String.format("%.2f", value);
        }
    }

}
