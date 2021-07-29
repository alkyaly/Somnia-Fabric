package mods.su5ed.somnia.util;

import mods.su5ed.somnia.api.capability.Components;
import mods.su5ed.somnia.api.capability.IFatigue;
import mods.su5ed.somnia.core.Somnia;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

public class SomniaUtil {
    public static boolean doesPlayerWearArmor(Player player) {
        return player.getInventory().armor.stream()
                .anyMatch(stack -> !stack.isEmpty());
    }

    public static long calculateWakeTime(long totalWorldTime, int target) {
        long timeInDay = totalWorldTime % 24000;
        long wakeTime = totalWorldTime - timeInDay + target;
        return timeInDay > target ? wakeTime + 24000 : wakeTime;
    }

    public static boolean checkFatigue(Player player) {
        IFatigue props = Components.FATIGUE.getNullable(player);

        return props != null && (player.isCreative() || props.getFatigue() >= Somnia.CONFIG.fatigue.minimumFatigueToSleep);
    }

    public static String timeStringForWorldTime(long time) {
        time += 6000;

        time = time % 24000;
        String hours = String.valueOf(time / 1000);
        String minutes = String.valueOf((int) ((time % 1000) / 1000D * 60));

        if (hours.length() == 1) hours = "0" + hours;
        if (minutes.length() == 1) minutes = "0" + minutes;

        return hours + ":" + minutes;
    }

    public static double getFatigueToReplenish(Player player) {
        long levelTime = player.level.getGameTime();
        long wakeTime = SomniaUtil.calculateWakeTime(levelTime, player.level.isNight() ? 0 : 12000);
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
