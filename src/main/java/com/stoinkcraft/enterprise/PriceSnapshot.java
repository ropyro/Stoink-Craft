package com.stoinkcraft.enterprise;

public class PriceSnapshot {
    private final long timestamp;
    private final double sharePrice;

    public PriceSnapshot(long timestamp, double sharePrice) {
        this.timestamp = timestamp;
        this.sharePrice = sharePrice;
    }

    public long getTimestamp() { return timestamp; }
    public double getSharePrice() { return sharePrice; }
}
