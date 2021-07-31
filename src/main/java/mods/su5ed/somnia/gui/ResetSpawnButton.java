package mods.su5ed.somnia.gui;

import mods.su5ed.somnia.api.capability.Components;
import mods.su5ed.somnia.api.capability.IFatigue;
import mods.su5ed.somnia.network.NetworkHandler;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TextComponent;

public class ResetSpawnButton extends Button {
    public boolean resetSpawn = true;

    public ResetSpawnButton(int xIn, int yIn, int widthIn, int heightIn) {
        super(xIn, yIn, widthIn, heightIn, new TextComponent("Reset spawn: Yes"), button -> {
            ResetSpawnButton reset = (ResetSpawnButton) button;
            reset.resetSpawn = !reset.resetSpawn;
            button.setMessage(new TextComponent("Reset spawn: " + (reset.resetSpawn ? "Yes" : "No")));
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return;

            IFatigue props = Components.FATIGUE.getNullable(mc.player);

            if (props != null) {
                props.shouldResetSpawn(((ResetSpawnButton) button).resetSpawn);
                FriendlyByteBuf buf = PacketByteBufs.create();
                buf.writeBoolean(props.resetSpawn());

                ClientPlayNetworking.send(NetworkHandler.RESET_SPAWN, buf);
            }
        });
    }
}
