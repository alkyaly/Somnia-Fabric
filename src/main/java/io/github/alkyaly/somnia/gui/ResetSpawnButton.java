package io.github.alkyaly.somnia.gui;

import io.github.alkyaly.somnia.api.capability.Components;
import io.github.alkyaly.somnia.api.capability.Fatigue;
import io.github.alkyaly.somnia.network.NetworkHandler;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

public class ResetSpawnButton extends Button {
    private static final Component YES = new TranslatableComponent("somnia.button.reset_yes");
    private static final Component NO = new TranslatableComponent("somnia.button.reset_no");

    private boolean resetSpawn = true;

    public ResetSpawnButton(int xIn, int yIn, int widthIn, int heightIn) {
        super(xIn, yIn, widthIn, heightIn, YES, button -> {
            ResetSpawnButton reset = (ResetSpawnButton) button;
            reset.resetSpawn = !reset.resetSpawn;
            button.setMessage(reset.resetSpawn ? YES : NO);
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return;

            Fatigue props = Components.get(mc.player);

            if (props != null) {
                props.shouldResetSpawn(((ResetSpawnButton) button).resetSpawn);
                FriendlyByteBuf buf = PacketByteBufs.create();
                buf.writeBoolean(props.resetSpawn());

                ClientPlayNetworking.send(NetworkHandler.RESET_SPAWN, buf);
            }
        });
    }
}
