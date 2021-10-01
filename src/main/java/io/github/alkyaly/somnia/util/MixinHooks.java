package io.github.alkyaly.somnia.util;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.alkyaly.somnia.api.capability.Components;
import io.github.alkyaly.somnia.api.capability.Fatigue;
import io.github.alkyaly.somnia.core.Somnia;
import io.github.alkyaly.somnia.handler.ServerTickHandler;
import io.github.alkyaly.somnia.network.NetworkHandler;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.lwjgl.opengl.GL11;

public final class MixinHooks {

    public static boolean doMobSpawning(ServerLevel level) {
        if (Somnia.CONFIG.performance.disableCreatureSpawning) {
            for (ServerTickHandler handler : ServerTickHandler.HANDLERS) {
                if (handler.levelServer == level) {
                    return handler.currentState != State.SIMULATING;
                }
            }
            throw new IllegalStateException("Couldn't find tick handler for given level");
        }
        return true;
    }

    public static void updateWakeTime(Player player) {
        Fatigue props = Components.get(player);

        if (props != null && player instanceof ServerPlayer serverPlayer) {
            if (props.getWakeTime() < 0) {
                long totalWorldTime = player.level.getGameTime();
                long wakeTime = SomniaUtil.calculateWakeTime(totalWorldTime, totalWorldTime % 24000 > 12000 ? 0 : 12000);
                props.setWakeTime(wakeTime);

                FriendlyByteBuf buf = PacketByteBufs.create();
                buf.writeLong(wakeTime);
                ServerPlayNetworking.send(serverPlayer, NetworkHandler.UPDATE_WAKE_TIME, buf);
            }
        }
    }

    public static boolean skipRenderLevel(Minecraft mc) {
        if (mc.player.isSleeping() && Somnia.CONFIG.performance.disableRendering) {
            RenderSystem.clear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT, false);
            return true;
        }
        return false;
    }
}
