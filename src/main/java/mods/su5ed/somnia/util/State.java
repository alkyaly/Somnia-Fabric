package mods.su5ed.somnia.util;

import mods.su5ed.somnia.api.capability.Components;
import mods.su5ed.somnia.api.capability.IFatigue;
import mods.su5ed.somnia.core.SomniaCommand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public enum State {
	INACTIVE,
	SIMULATING,
	WAITING,
	UNAVAILABLE;

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

				IFatigue props = Components.FATIGUE.getNullable(player);
				if (props != null && props.shouldSleepNormally()) normalSleep++;
				else somniaSleep++;
			}

			if (allSleeping) {
				if (somniaSleep >= normalSleep) return SIMULATING;
			} else if (anySleeping) return WAITING;
		}

		return INACTIVE;
	}
}
