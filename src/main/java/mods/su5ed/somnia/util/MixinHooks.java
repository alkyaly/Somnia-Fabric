package mods.su5ed.somnia.util;

import com.mojang.blaze3d.systems.RenderSystem;
import mods.su5ed.somnia.api.capability.Components;
import mods.su5ed.somnia.api.capability.Fatigue;
import mods.su5ed.somnia.core.Somnia;
import mods.su5ed.somnia.handler.ServerTickHandler;
import mods.su5ed.somnia.network.NetworkHandler;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import org.lwjgl.opengl.GL11;

public final class MixinHooks {

    public static boolean doMobSpawning(ServerLevel level) {
        boolean spawnMobs = level.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING);
        if (!Somnia.CONFIG.performance.disableCreatureSpawning || !spawnMobs) return spawnMobs;

        return ServerTickHandler.HANDLERS.stream()
                .filter(handler -> handler.levelServer == level)
                .map(handler -> handler.currentState != State.SIMULATING)
                .findAny()
                .orElseThrow(() -> new IllegalStateException("Couldn't find tick handler for given level"));
    }

    public static void updateWakeTime(Player player) {
        Fatigue props = Components.get(player);

        if (props != null) {
            if (props.getWakeTime() < 0) {
                long totalWorldTime = player.level.getGameTime();
                long wakeTime = SomniaUtil.calculateWakeTime(totalWorldTime, totalWorldTime % 24000 > 12000 ? 0 : 12000);
                props.setWakeTime(wakeTime);

                FriendlyByteBuf buf = PacketByteBufs.create();
                buf.writeLong(wakeTime);
                ServerPlayNetworking.send((ServerPlayer) player, NetworkHandler.UPDATE_WAKE_TIME, buf);
            }
        }
    }

    public static boolean skipRenderWorld(Minecraft mc) {
        if (mc.player.isSleeping() && Somnia.CONFIG.performance.disableRendering) {
            RenderSystem.clear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT, false);
            return true;
        }
        return false;
    }

    public static boolean ignoreMonsters() {
        return Somnia.CONFIG.options.ignoreMonsters;
    }
}
