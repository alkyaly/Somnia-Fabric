package io.github.alkyaly.somnia.api.capability;

import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.entity.PlayerComponent;

public interface Fatigue extends PlayerComponent<Fatigue>, AutoSyncedComponent {
    double getFatigue();

    void setFatigue(double fatigue);

    int getSideEffectStage();

    void setSideEffectStage(int stage);

    int updateFatigueCounter();

    void resetFatigueCounter();

    void maxFatigueCounter();

    void shouldResetSpawn(boolean resetSpawn);

    boolean resetSpawn();

    boolean sleepOverride();

    void setSleepOverride(boolean override);

    void setSleepNormally(boolean sleepNormally);

    /**
     * The {@link net.minecraft.world.entity.player.Player player} will be sleeping normally if they try to sleep
     * {@link net.minecraft.world.entity.player.Player#isShiftKeyDown() sneaking}.
     * @return Whether the {@link net.minecraft.world.entity.player.Player player} should sleep normally.
     */
    boolean shouldSleepNormally();

    long getWakeTime();

    void setWakeTime(long wakeTime);

    double getExtraFatigueRate();

    void setExtraFatigueRate(double rate);

    double getReplenishedFatigue();

    void setReplenishedFatigue(double replenishedFatigue);
}
