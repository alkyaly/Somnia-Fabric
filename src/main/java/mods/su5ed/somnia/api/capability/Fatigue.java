package mods.su5ed.somnia.api.capability;

import net.minecraft.nbt.CompoundTag;

public class Fatigue implements IFatigue {
    private double fatigue, extraFatigueRate, replenishedFatigue;
    private int fatigueUpdateCounter = 0, sideEffectStage = -1;
    private boolean resetSpawn = true, sleepOverride = false, sleepNormally = false;
    private long wakeTime = -1;

    @Override
    public double getFatigue()
    {
        return this.fatigue;
    }

    @Override
    public void setFatigue(double fatigue)
    {
        this.fatigue = fatigue;
    }

    @Override
    public int getSideEffectStage()
    {
        return this.sideEffectStage;
    }

    @Override
    public void setSideEffectStage(int stage)
    {
        this.sideEffectStage = stage;
    }

    @Override
    public int updateFatigueCounter()
    {
        return ++fatigueUpdateCounter;
    }

    @Override
    public void resetFatigueCounter()
    {
        this.fatigueUpdateCounter = 0;
    }

    @Override
    public void maxFatigueCounter() {
        this.fatigueUpdateCounter = 100;
    }

    @Override
    public void shouldResetSpawn(boolean resetSpawn) {
        this.resetSpawn = resetSpawn;
    }

    @Override
    public boolean resetSpawn() {
        return this.resetSpawn;
    }

    @Override
    public boolean sleepOverride() {
        return this.sleepOverride;
    }

    @Override
    public void setSleepOverride(boolean override) {
        this.sleepOverride = override;
    }

    @Override
    public void setSleepNormally(boolean sleepNormally) {
        this.sleepNormally = sleepNormally;
    }

    @Override
    public boolean shouldSleepNormally() {
        return this.sleepNormally;
    }

    @Override
    public long getWakeTime() {
        return this.wakeTime;
    }

    @Override
    public void setWakeTime(long wakeTime) {
        this.wakeTime = wakeTime;
    }

    @Override
    public double getExtraFatigueRate() {
        return this.extraFatigueRate;
    }

    @Override
    public void setExtraFatigueRate(double rate) {
        this.extraFatigueRate = rate;
    }

    @Override
    public double getReplenishedFatigue() {
        return this.replenishedFatigue;
    }

    @Override
    public void setReplenishedFatigue(double replenishedFatigue) {
        this.replenishedFatigue = replenishedFatigue;
    }

    @Override
    public void readFromNbt(CompoundTag tag) {
        this.fatigue = tag.getDouble("fatigue");
        this.extraFatigueRate = tag.getDouble("extraFatigueRate");
        this.replenishedFatigue = tag.getDouble("replenishedFatigue");
        this.sideEffectStage = tag.getInt("sideEffectStage");
        this.resetSpawn = tag.getBoolean("resetSpawn");
    }

    @Override
    public void writeToNbt(CompoundTag tag) {
        tag.putDouble("fatigue", this.fatigue);
        tag.putDouble("extraFatigueRate", this.extraFatigueRate);
        tag.putDouble("replenishedFatigue", this.replenishedFatigue);
        tag.putInt("sideEffectStage", this.sideEffectStage);
        tag.putBoolean("resetSpawn", this.resetSpawn);
    }
}
