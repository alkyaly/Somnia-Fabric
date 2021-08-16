package io.github.alkyaly.somnia.network;

import com.google.common.base.MoreObjects;
import io.github.alkyaly.somnia.api.capability.Components;
import io.github.alkyaly.somnia.api.capability.Fatigue;
import io.github.alkyaly.somnia.core.Somnia;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public final class NetworkHandler {
    public static final ResourceLocation UPDATE_FATIGUE = Somnia.locate("update_fatigue");
    public static final ResourceLocation RESET_SPAWN = Somnia.locate("reset_spawn");
    public static final ResourceLocation UPDATE_WAKE_TIME = Somnia.locate("update_wake_time");
    public static final ResourceLocation ACTIVATE_BLOCK = Somnia.locate("activate_block");
    public static final ResourceLocation UPDATE_SPEED = Somnia.locate("update_speed");
    public static final ResourceLocation WAKE_UP_PLAYER = Somnia.locate("wake_up_player");
    public static final ResourceLocation OPEN_GUI = Somnia.locate("open_gui");

    public static void init() {
        registerReceiver(RESET_SPAWN, (server, player, handler, buf, responseSender) -> {
            boolean resetSpawn = buf.readBoolean();

            server.execute(() -> {
                if (player != null) {
                    Fatigue props = Components.get(player);
                    if (props != null) {
                        props.shouldResetSpawn(resetSpawn);
                    }
                }
            });
        });
        registerReceiver(UPDATE_WAKE_TIME, (server, player, handler, buf, responseSender) -> {
            long wakeTime = buf.readLong();

            server.execute(() -> {
                if (player != null) {
                    Fatigue props = Components.get(player);
                    if (props != null) {
                        props.setWakeTime(wakeTime);
                    }
                }
            });
        });
        registerReceiver(ACTIVATE_BLOCK, (server, player, handler, buf, responseSender) -> {
            BlockHitResult bhr = buf.readBlockHitResult();

            server.execute(() -> {
                if (player != null) {
                    BlockState state = player.level.getBlockState(bhr.getBlockPos());

                    state.use(player.level, player, MoreObjects.firstNonNull(player.swingingArm, InteractionHand.MAIN_HAND), bhr);
                }
            });
        });
        registerReceiver(WAKE_UP_PLAYER, (server, player, handler, buf, responseSender) -> server.execute(() -> {
            if (player != null) {
                player.stopSleeping();
            }
        }));
    }

    private static void registerReceiver(ResourceLocation resourceLocation, ServerPlayNetworking.PlayChannelHandler handler) {
        ServerPlayNetworking.registerGlobalReceiver(resourceLocation, handler);
    }
}
