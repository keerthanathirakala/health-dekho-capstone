package com.healthdekho.domain;

public enum PremiumFrequency {
    MONTHLY(1), QUARTERLY(3), HALF_YEARLY(6), YEARLY(12);

    private final int months;
    PremiumFrequency(int months) { this.months = months; }
    public int months() { return months; }
}
