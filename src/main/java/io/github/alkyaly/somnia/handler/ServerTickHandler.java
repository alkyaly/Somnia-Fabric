package io.github.alkyaly.somnia.handler;

import io.github.alkyaly.somnia.core.Somnia;
import io.github.alkyaly.somnia.mixin.accessor.MinecraftServerAccessor;
import io.github.alkyaly.somnia.network.NetworkHandler;
import io.github.alkyaly.somnia.util.State;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.GameRules;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static io.github.alkyaly.somnia.util.State.SIMULATING;

public class ServerTickHandler {
    public static final List<ServerTickHandler> HANDLERS = new ArrayList<>();
    private static int tickHandlers;
    public final ServerLevel levelServer;
    public State currentState;
    private int timer;
    private double overflow,
            multiplier = Somnia.CONFIG.logic.baseMultiplier;

    public ServerTickHandler(ServerLevel levelServer) {
        this.levelServer = levelServer;
    }

    public void tickEnd() {
        if (++timer == 10) {
            timer = 0;
            State state = State.forLevel(levelServer);

            if (state != currentState) {
                if (currentState == SIMULATING) {
                    tickHandlers--;
                    if (state == State.UNAVAILABLE) closeGuiWithMessage(currentState.toString());
                } else if (state == SIMULATING) tickHandlers++;
            }

            if (state == SIMULATING || state == State.WAITING) {
                FriendlyByteBuf buf = PacketByteBufs.create();
                buf.writeDouble(state == SIMULATING ? multiplier + overflow : 0);

                for (ServerPlayer player : levelServer.players()) {
                    if (player.level == levelServer) {
                        ServerPlayNetworking.send(player, NetworkHandler.UPDATE_SPEED, buf);
                    }
                }
            }

            this.currentState = state;
        }

        if (currentState == SIMULATING) {
            multiplyTick();
        }
    }

    private void closeGuiWithMessage(@Nullable String key) {
        levelServer.players().stream()
                .filter(LivingEntity::isSleeping)
                .forEach(p -> {
                    ServerPlayNetworking.send(p, NetworkHandler.WAKE_UP_PLAYER, PacketByteBufs.create());

                    if (key != null) {
                        p.sendMessage(new TranslatableComponent("somnia.status." + key), UUID.randomUUID());
                    }
                });
    }

    private void multiplyTick() {
        double target = multiplier + overflow;
        int flooredTarget = (int) target;
        overflow = target - flooredTarget;

        long timeMillis = System.currentTimeMillis();

        for (int i = 0; i < flooredTarget; i++) {
            multiplyServerTick();
        }

        multiplier += (System.currentTimeMillis() - timeMillis <= Somnia.CONFIG.logic.delta / tickHandlers) ? 0.1 : -0.1;

        if (multiplier > Somnia.CONFIG.logic.multiplierCap) multiplier = Somnia.CONFIG.logic.multiplierCap;
        if (multiplier < Somnia.CONFIG.logic.baseMultiplier) multiplier = Somnia.CONFIG.logic.baseMultiplier;
    }

    private void multiplyServerTick() {
        levelServer.tick(() -> ((MinecraftServerAccessor) levelServer.getServer()).somnia$invokeHaveTime());
        levelServer.getServer().getPlayerList().broadcastAll(new ClientboundSetTimePacket(levelServer.getGameTime(), levelServer.getDayTime(), levelServer.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)), levelServer.dimension());
    }
}