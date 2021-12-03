package io.github.alkyaly.somnia.core;

import io.github.alkyaly.somnia.handler.ClientTickHandler;
import io.github.alkyaly.somnia.network.ClientNetworkHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;

@Environment(EnvType.CLIENT)
public class SomniaClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(SomniaClient::tickHandler);
        ClientNetworkHandler.init();
    }

    private static void tickHandler(Minecraft mc) {
        ClientTickHandler.INSTANCE.onClientTick();
    }
}
