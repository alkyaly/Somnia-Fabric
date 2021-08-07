package mods.su5ed.somnia.api.capability;

import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.entity.PlayerComponent;

public interface IFatigue extends PlayerComponent<IFatigue>, AutoSyncedComponent {
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

    boolean shouldSleepNormally();

    long getWakeTime();

    void setWakeTime(long wakeTime);

    double getExtraFatigueRate();

    void setExtraFatigueRate(double rate);

    double getReplenishedFatigue();

    void setReplenishedFatigue(double replenishedFatigue);
}
