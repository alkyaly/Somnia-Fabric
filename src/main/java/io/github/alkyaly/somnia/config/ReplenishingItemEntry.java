package io.github.alkyaly.somnia.config;

import java.util.Objects;

@SuppressWarnings({"FieldMayBeFinal", "CanBeFinal"})
public class ReplenishingItemEntry {
    private String item;
    private double fatigueToReplenish;
    private double fatigueRateModifier;

    public ReplenishingItemEntry(String item, double fatigueToReplenish, double fatigueRateModifier) {
        this.item = item;
        this.fatigueToReplenish = fatigueToReplenish;
        this.fatigueRateModifier = fatigueRateModifier;
    }

    public ReplenishingItemEntry(String item, double fatigueToReplenish) {
        this(item, fatigueToReplenish, 0.00208);
    }

    public String item() {
        return item;
    }

    public double fatigueToReplenish() {
        return fatigueToReplenish;
    }

    public double fatigueRateModifier() {
        return fatigueRateModifier;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ReplenishingItemEntry) obj;
        return Objects.equals(this.item, that.item) &&
                Double.doubleToLongBits(this.fatigueToReplenish) == Double.doubleToLongBits(that.fatigueToReplenish) &&
                Double.doubleToLongBits(this.fatigueRateModifier) == Double.doubleToLongBits(that.fatigueRateModifier);
    }

    @Override
    public String toString() {
        return "ReplenishingItemEntry[" +
                "item=" + item + ", " +
                "fatigueToReplenish=" + fatigueToReplenish + ", " +
                "fatigueRateModifier=" + fatigueRateModifier + ']';
    }
}
