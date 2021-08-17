package io.github.alkyaly.somnia.core;

import io.github.alkyaly.somnia.network.ClientNetworkHandler;
import io.github.alkyaly.somnia.handler.ClientTickHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;

import java.util.concurrent.ThreadLocalRandom;

@Environment(EnvType.CLIENT)
public class SomniaClient implements ClientModInitializer {

    public static boolean easterEggActive;

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(SomniaClient::tickHandler);
        ClientNetworkHandler.init();
        easterEggActive = Somnia.CONFIG.options.easterEgg && ThreadLocalRandom.current().nextInt(256) == 0;
    }

    private static void tickHandler(Minecraft mc) {
        ClientTickHandler.INSTANCE.onClientTick();
    }
}
