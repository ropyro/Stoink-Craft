package com.stoinkcraft.shares;

import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.enterprise.EnterpriseManager;
import com.stoinkcraft.enterprise.PriceSnapshot;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class ShareAnalytics {

        /**
         * Get the current share price for this share's enterprise.
         */
        public static double getCurrentPrice(Share share) {
            Enterprise enterprise = EnterpriseManager.getEnterpriseManager().getEnterpriseByID(share.getEnterpriseID());
            if (enterprise == null) return 0;

            List<PriceSnapshot> history = enterprise.getPriceHistory();
            if (history.isEmpty()) return 0;

            return history.get(history.size() - 1).getSharePrice();
        }

        /**
         * Get the percentage increase since purchase.
         * Example: 25.0 means +25% gain, -10.0 means -10% loss.
         */
        public static double getPercentChangeSincePurchase(Share share) {
            double currentPrice = getCurrentPrice(share);
            double purchasePrice = share.getPurchasePrice();

            if (purchasePrice <= 0) return 0;
            return ((currentPrice - purchasePrice) / purchasePrice) * 100.0;
        }

        /**
         * Get the percentage change over a given time window (in milliseconds).
         * e.g. last hour = 3600000L, last day = 86400000L
         */
        public static double getPercentChangeOverTime(Share share, long timeWindowMillis) {
            Enterprise enterprise = EnterpriseManager.getEnterpriseManager().getEnterpriseByID(share.getEnterpriseID());
            if (enterprise == null) return 0;

            List<PriceSnapshot> history = enterprise.getPriceHistory();
            if (history.size() < 2) return 0;

            long now = System.currentTimeMillis();
            long cutoff = now - timeWindowMillis;

            // Find the snapshot closest to the cutoff time (oldest within window)
            Optional<PriceSnapshot> pastSnapshotOpt = history.stream()
                    .filter(snap -> snap.getTimestamp() >= cutoff)
                    .min(Comparator.comparingLong(PriceSnapshot::getTimestamp));

            if (pastSnapshotOpt.isEmpty()) return 0;

            double pastPrice = pastSnapshotOpt.get().getSharePrice();
            double currentPrice = history.get(history.size() - 1).getSharePrice();

            if (pastPrice <= 0) return 0;
            return ((currentPrice - pastPrice) / pastPrice) * 100.0;
        }

        /**
         * Convenience methods for common timeframes.
         */
        public static double getPercentChangeLastHour(Share share) {
            return getPercentChangeOverTime(share, 1000L * 60L * 60L); // 1 hour
        }

        public static double getPercentChangeLastDay(Share share) {
            return getPercentChangeOverTime(share, 1000L * 60L * 60L * 24L); // 24 hours
        }

        public static double getPercentChangeLastWeek(Share share) {
            return getPercentChangeOverTime(share, 1000L * 60L * 60L * 24L * 7L); // 7 days
        }

        /**
         * Get how long ago this share was purchased, in milliseconds.
         */
        public static long getTimeHeld(Share share) {
            return System.currentTimeMillis() - share.getPurchaseDate().getTime();
        }

        /**
         * Get a simple description string for display in GUIs.
         */
        public static String getShareSummary(Share share) {
            double changeSincePurchase = getPercentChangeSincePurchase(share);
            double changeLastDay = getPercentChangeLastDay(share);

            return String.format("§7Held for §f%s§7 | §a%.2f%% today | §b%.2f%% total",
                    formatTimeAgo(share.getPurchaseDate()),
                    changeLastDay,
                    changeSincePurchase);
        }

        /**
         * Helper to format "time ago" (e.g., "3h 15m ago").
         */
        private static String formatTimeAgo(Date date) {
            long diff = System.currentTimeMillis() - date.getTime();

            long days = diff / (1000L * 60L * 60L * 24L);
            long hours = (diff / (1000L * 60L * 60L)) % 24;
            long minutes = (diff / (1000L * 60L)) % 60;

            if (days > 0) return days + "d " + hours + "h ago";
            if (hours > 0) return hours + "h " + minutes + "m ago";
            return minutes + "m ago";
        }
}
