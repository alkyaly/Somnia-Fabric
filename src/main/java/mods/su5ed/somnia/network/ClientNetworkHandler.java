package mods.su5ed.somnia.network;

import mods.su5ed.somnia.api.capability.Components;
import mods.su5ed.somnia.api.capability.Fatigue;
import mods.su5ed.somnia.gui.WakeTimeSelectScreen;
import mods.su5ed.somnia.handler.ClientTickHandler;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.resources.ResourceLocation;

public final class ClientNetworkHandler {

    public static void init() {
        registerReceiver(NetworkHandler.UPDATE_FATIGUE, (client, handler, buf, responseSender) -> {
            double fatigue = buf.readDouble();
            client.execute(() -> {
                Fatigue props = Components.get(client.player);

                if (props != null) {
                    props.setFatigue(fatigue);
                }
            });
        });
        registerReceiver(NetworkHandler.UPDATE_WAKE_TIME, (client, handler, buf, responseSender) -> {
            long wakeTime = buf.readLong();
            client.execute(() -> {
                Fatigue props = Components.get(client.player);

                if (props != null) {
                    props.setWakeTime(wakeTime);
                }
            });
        });
        registerReceiver(NetworkHandler.UPDATE_SPEED, (client, handler, buf, responseSender) -> {
            double sp = buf.readDouble();

            client.execute(() -> ClientTickHandler.INSTANCE.addSpeedValue(sp));
        });
        registerReceiver(NetworkHandler.OPEN_GUI, (client, handler, buf, responseSender) -> client.execute(() -> {
            if (!(client.screen instanceof WakeTimeSelectScreen)) {
                client.setScreen(new WakeTimeSelectScreen());
            }
        }));
        registerReceiver(NetworkHandler.WAKE_UP_PLAYER, (client, handler, buf, responseSender) -> client.execute(() -> {
            if (client.player != null) {
                client.player.stopSleeping();
            }
        }));
    }

    private static void registerReceiver(ResourceLocation resourceLocation, ClientPlayNetworking.PlayChannelHandler handler) {
        ClientPlayNetworking.registerGlobalReceiver(resourceLocation, handler);
    }
}
