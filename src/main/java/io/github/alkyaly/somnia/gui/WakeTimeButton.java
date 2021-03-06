package io.github.alkyaly.somnia.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.alkyaly.somnia.api.capability.Components;
import io.github.alkyaly.somnia.api.capability.Fatigue;
import io.github.alkyaly.somnia.network.NetworkHandler;
import io.github.alkyaly.somnia.util.SomniaUtil;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class WakeTimeButton extends Button {
    private final String hoverText;
    private final Component buttonText;

    public WakeTimeButton(int x, int y, int widthIn, int heightIn, Component buttonText, long wakeTime) {
        super(x, y, widthIn, heightIn, buttonText, button -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null) return;

            long targetWakeTime = SomniaUtil.calculateWakeTime(mc.level.getGameTime(), (int) wakeTime);
            FriendlyByteBuf buf = PacketByteBufs.create();
            buf.writeLong(targetWakeTime);

            ClientPlayNetworking.send(NetworkHandler.UPDATE_WAKE_TIME, buf);

            Fatigue props = Components.get(mc.player);

            if (props != null) {
                props.setWakeTime(targetWakeTime);
            }
            HitResult mouseOver = mc.hitResult;
            if (mouseOver instanceof BlockHitResult bhr) {
                FriendlyByteBuf byteBuf = PacketByteBufs.create();
                byteBuf.writeBlockHitResult(bhr);

                ClientPlayNetworking.send(NetworkHandler.ACTIVATE_BLOCK, byteBuf);
            }

            mc.setScreen(null);
        });
        this.buttonText = buttonText;
        this.hoverText = SomniaUtil.timeStringForWorldTime(wakeTime);
    }

    @Override
    public void renderButton(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
        super.renderButton(pose, mouseX, mouseY, partialTicks);
        this.setMessage(this.isHovered ? new TextComponent(hoverText) : buttonText);
    }
}
