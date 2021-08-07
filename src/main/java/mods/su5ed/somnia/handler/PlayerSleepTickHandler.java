package mods.su5ed.somnia.handler;

import mods.su5ed.somnia.api.capability.Components;
import mods.su5ed.somnia.api.capability.IFatigue;
import mods.su5ed.somnia.compat.Compat;
import mods.su5ed.somnia.core.Somnia;
import mods.su5ed.somnia.mixin.accessor.PlayerAccessor;
import net.minecraft.world.entity.player.Player;

public final class PlayerSleepTickHandler {

    public static void onPlayerTick(boolean start, Player player) {
        IFatigue props = Components.get(player);

        if (props != null) {
            if (start) {
                tickStart(props, player);
            } else {
                tickEnd(props, player);
            }
        }
    }

    private static void tickStart(IFatigue props, Player player) {
        if (player.isSleeping()) {            //Dark Utils is not on fabric.
            if (props.shouldSleepNormally() || Compat.isSleepingInHammock(player) /*|| (player.getSleepTimer() > 99 && ModList.get().isLoaded("darkutils") && DarkUtilsPlugin.hasSleepCharm(player)) || Compat.isSleepingInHammock(player)*/) {
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

    private static void tickEnd(IFatigue props, Player player) {
        if (props.sleepOverride()) {
            player.startSleeping(player.getSleepingPos().orElse(player.blockPosition()));
            props.setSleepOverride(false);
        }
    }
}