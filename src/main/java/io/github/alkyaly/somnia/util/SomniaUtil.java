package io.github.alkyaly.somnia.util;

import io.github.alkyaly.somnia.api.capability.Components;
import io.github.alkyaly.somnia.api.capability.Fatigue;
import io.github.alkyaly.somnia.core.Somnia;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

public final class SomniaUtil {
    public static boolean isPlayerWearingArmor(Player player) {
        return player.getInventory().armor.stream()
                .anyMatch(stack -> !stack.isEmpty());
    }

    public static long calculateWakeTime(long totalWorldTime, int target) {
        long timeInDay = totalWorldTime % 24000;
        long wakeTime = totalWorldTime - timeInDay + target;
        return timeInDay > target ? wakeTime + 24000 : wakeTime;
    }

    public static boolean checkFatigue(Player player) {
        Fatigue props = Components.get(player);

        return props != null && (player.isCreative() || props.getFatigue() >= Somnia.CONFIG.fatigue.minimumFatigueToSleep);
    }

    public static String timeStringForWorldTime(long time) {
        time += 6000;

        time %= 24000;
        String hours = String.valueOf(time / 1000);
        String minutes = String.valueOf((int) ((time % 1000) / 1000D * 60));

        if (hours.length() == 1) hours = "0" + hours;
        if (minutes.length() == 1) minutes = "0" + minutes;

        return hours + ":" + minutes;
    }

    /**
     * Only used when the {@link Player player} chooses to {@link Fatigue#shouldSleepNormally() sleep normally}
     * @param player The player.
     * @return The amount of fatigue to replenish after sleeping normally.
     */
    public static double getFatigueToReplenish(Player player) {
        long levelTime = player.level.getGameTime();
        long wakeTime = calculateWakeTime(levelTime, player.level.isNight() ? 0 : 12000);
        return Somnia.CONFIG.fatigue.fatigueReplenishRate * (wakeTime - levelTime);
    }

    public static boolean isEnterSleepTime() {
        return 24000 >= Somnia.CONFIG.timings.enterSleepStart && 24000 <= Somnia.CONFIG.timings.enterSleepEnd;
    }

    public static boolean isValidSleepTime(ServerLevel level) {
        long time = level.getGameTime() % 24000;
        return time >= Somnia.CONFIG.timings.validSleepStart && time <= Somnia.CONFIG.timings.validSleepEnd;
    }
}
