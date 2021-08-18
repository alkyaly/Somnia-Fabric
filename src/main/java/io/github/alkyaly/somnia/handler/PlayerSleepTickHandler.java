package io.github.alkyaly.somnia.handler;

import io.github.alkyaly.somnia.api.capability.Components;
import io.github.alkyaly.somnia.api.capability.Fatigue;
import io.github.alkyaly.somnia.compat.Compat;
import io.github.alkyaly.somnia.mixin.accessor.PlayerAccessor;
import io.github.alkyaly.somnia.core.Somnia;
import net.minecraft.world.entity.player.Player;

public final class PlayerSleepTickHandler {

    public static void onPlayerTick(boolean start, Player player) {
        Fatigue props = Components.get(player);

        if (props != null) {
            if (start) {
                tickStart(props, player);
            } else {
                tickEnd(props, player);
            }
        }
    }

    private static void tickStart(Fatigue props, Player player) {
        if (player.isSleeping()) {
            if (props.shouldSleepNormally() || Compat.isSleepingInHammock(player)) {
                props.setSleepOverride(false);
                return;
            }

            props.setSleepOverride(true);

            if (Somnia.CONFIG.options.fading) {
                int sleepTimer = player.getSleepTimer() + 1;
                if (sleepTimer >= 99) sleepTimer = 98;
                ((PlayerAccessor) player).setSleepCounter(sleepTimer);
            }
        }
    }

    private static void tickEnd(Fatigue props, Player player) {
        if (props.sleepOverride()) {
            player.startSleeping(player.getSleepingPos().orElse(player.blockPosition()));
            props.setSleepOverride(false);
        }
    }
}