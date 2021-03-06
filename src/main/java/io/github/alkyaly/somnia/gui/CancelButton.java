package io.github.alkyaly.somnia.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.TranslatableComponent;

public class CancelButton extends Button {

    public CancelButton(int xIn, int yIn, int widthIn, int heightIn) {
        super(xIn, yIn, widthIn, heightIn, new TranslatableComponent("somnia.button.cancel"), button -> Minecraft.getInstance().setScreen(null));
    }
}
