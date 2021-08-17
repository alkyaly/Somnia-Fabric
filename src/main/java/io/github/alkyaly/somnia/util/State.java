package io.github.alkyaly.somnia.util;

import io.github.alkyaly.somnia.api.capability.Components;
import io.github.alkyaly.somnia.api.capability.Fatigue;
import io.github.alkyaly.somnia.core.SomniaCommand;
import io.github.alkyaly.somnia.handler.ServerTickHandler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public enum State {
    /**
     * If there is no players online.
     */
    INACTIVE,

    /**
     * If the current level is suitable for simulating.
     * <p>
     * (Whether the majority (Inclusive) of players is sleeping {@link Fatigue#shouldSleepNormally() not-normally})
     * </p>
     * <p>
     * Will multiply the ticking of the level.
     * </p>
     */
    SIMULATING,

    /**
     * Waiting for more players to sleep
     */
    WAITING,

    /**
     * If the current {@link ServerLevel#getGameTime() level time} is not valid for simulating.
     * <p>
     * Will make the {@link net.minecraft.world.entity.player.Player player} stop sleeping with a message.
     * </p>
     */
    UNAVAILABLE;

    /**
     * Retrieves the current state for a given level.
     * Called every 1/2 second at the end of the server tick.
     *
     * @param level The level to retrieve from.
     * @return The state of the given level.
     * @see ServerTickHandler#tickEnd()
     */
    public static State forLevel(ServerLevel level) {
        if (!SomniaUtil.isValidSleepTime(level)) return UNAVAILABLE;
        List<ServerPlayer> players = level.players();

        if (!players.isEmpty()) {
            boolean anySleeping = false, allSleeping = true;
            int somniaSleep = 0, normalSleep = 0;

            for (ServerPlayer player : players) {
                boolean sleeping = player.isSleeping() || SomniaCommand.OVERRIDES.contains(player.getUUID());
                anySleeping |= sleeping;
                allSleeping &= sleeping;

                Fatigue props = Components.get(player);
                if (props != null && props.shouldSleepNormally()) {
                    normalSleep++;
                } else {
                    somniaSleep++;
                }
            }

            if (allSleeping) {
                if (somniaSleep >= normalSleep) {
                    return SIMULATING;
                }
            } else if (anySleeping) {
                return WAITING;
            }
        }

        return INACTIVE;
    }
}
