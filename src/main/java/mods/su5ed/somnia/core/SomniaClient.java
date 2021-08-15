package mods.su5ed.somnia.core;

import mods.su5ed.somnia.handler.ClientTickHandler;
import mods.su5ed.somnia.network.ClientNetworkHandler;
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
