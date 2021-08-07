package mods.su5ed.somnia.config;

import java.util.Objects;

@SuppressWarnings({"FieldMayBeFinal", "CanBeFinal"})
public class SideEffectStageEntry {
    private int minFatigue;
    private int maxFatigue;
    private String potionId;
    private int duration;
    private int amplifier;

    public SideEffectStageEntry(int minFatigue, int maxFatigue, String potionId, int duration, int amplifier) {
        this.minFatigue = minFatigue;
        this.maxFatigue = maxFatigue;
        this.potionId = potionId;
        this.duration = duration;
        this.amplifier = amplifier;
    }

    public int minFatigue() {
        return minFatigue;
    }

    public int maxFatigue() {
        return maxFatigue;
    }

    public String potionId() {
        return potionId;
    }

    public int duration() {
        return duration;
    }

    public int amplifier() {
        return amplifier;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (SideEffectStageEntry) obj;
        return this.minFatigue == that.minFatigue &&
                this.maxFatigue == that.maxFatigue &&
                Objects.equals(this.potionId, that.potionId) &&
                this.duration == that.duration &&
                this.amplifier == that.amplifier;
    }

    @Override
    public String toString() {
        return "SideEffectStageEntry[" +
                "minFatigue=" + minFatigue + ", " +
                "maxFatigue=" + maxFatigue + ", " +
                "potionId=" + potionId + ", " +
                "duration=" + duration + ", " +
                "amplifier=" + amplifier + ']';
    }
}
