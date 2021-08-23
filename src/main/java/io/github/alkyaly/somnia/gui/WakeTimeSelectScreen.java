package io.github.alkyaly.somnia.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.alkyaly.somnia.util.SomniaUtil;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

public class WakeTimeSelectScreen extends Screen {
    
    private static final Component SLEEP_UNTIL = new TranslatableComponent("somnia.screen.sleep_until");
    private static final TranslatableComponent[] TIMES = {
            new TranslatableComponent("somnia.screen.midnight"),
            new TranslatableComponent("somnia.screen.after_midnight"),
            new TranslatableComponent("somnia.screen.before_sunrise"),
            new TranslatableComponent("somnia.screen.mid_sunrise"),
            new TranslatableComponent("somnia.screen.after_sunrise"),
            new TranslatableComponent("somnia.screen.early_morning"),
            new TranslatableComponent("somnia.screen.mid_morning"),
            new TranslatableComponent("somnia.screen.late_morning"),
            new TranslatableComponent("somnia.screen.noon"),
            new TranslatableComponent("somnia.screen.early_afternoon"),
            new TranslatableComponent("somnia.screen.mid_afternoon"),
            new TranslatableComponent("somnia.screen.late_afternoon"),
            new TranslatableComponent("somnia.screen.before_sunset"),
            new TranslatableComponent("somnia.screen.mid_sunset"),
            new TranslatableComponent("somnia.screen.after_sunset"),
            new TranslatableComponent("somnia.screen.before_midnight")
    };

    public WakeTimeSelectScreen() {
        super(new TranslatableComponent("somnia.screen.select"));
    }

    @Override
    public void init() {
        int buttonWidth = 100, buttonHeight = 20;
        int buttonCenterX = this.width / 2 - 50;
        int buttonCenterY = this.height / 2 - 10;

        addRenderableWidget(new ResetSpawnButton(buttonCenterX, buttonCenterY - 22, buttonWidth, buttonHeight));
        addRenderableWidget(new CancelButton(buttonCenterX, buttonCenterY + 22, buttonWidth, buttonHeight));
        addRenderableWidget(new WakeTimeButton(buttonCenterX, buttonCenterY + 88, buttonWidth, buttonHeight, TIMES[0], 18000));
        addRenderableWidget(new WakeTimeButton(buttonCenterX - 80, buttonCenterY + 66, buttonWidth, buttonHeight, TIMES[1], 20000));
        addRenderableWidget(new WakeTimeButton(buttonCenterX - 110, buttonCenterY + 44, buttonWidth, buttonHeight, TIMES[2], 22000));
        addRenderableWidget(new WakeTimeButton(buttonCenterX - 130, buttonCenterY + 22, buttonWidth, buttonHeight, TIMES[3], 23000));
        addRenderableWidget(new WakeTimeButton(buttonCenterX - 140, buttonCenterY, buttonWidth, buttonHeight, TIMES[4], 0));
        addRenderableWidget(new WakeTimeButton(buttonCenterX - 130, buttonCenterY - 22, buttonWidth, buttonHeight, TIMES[5], 1500));
        addRenderableWidget(new WakeTimeButton(buttonCenterX - 110, buttonCenterY - 44, buttonWidth, buttonHeight, TIMES[6], 3000));
        addRenderableWidget(new WakeTimeButton(buttonCenterX - 80, buttonCenterY - 66, buttonWidth, buttonHeight, TIMES[7], 4500));
        addRenderableWidget(new WakeTimeButton(buttonCenterX, buttonCenterY - 88, buttonWidth, buttonHeight, TIMES[8], 6000));
        addRenderableWidget(new WakeTimeButton(buttonCenterX + 80, buttonCenterY - 66, buttonWidth, buttonHeight, TIMES[9], 7500));
        addRenderableWidget(new WakeTimeButton(buttonCenterX + 110, buttonCenterY - 44, buttonWidth, buttonHeight, TIMES[10], 9000));
        addRenderableWidget(new WakeTimeButton(buttonCenterX + 130, buttonCenterY - 22, buttonWidth, buttonHeight, TIMES[11], 10500));
        addRenderableWidget(new WakeTimeButton(buttonCenterX + 140, buttonCenterY, buttonWidth, buttonHeight, TIMES[12], 12000));
        addRenderableWidget(new WakeTimeButton(buttonCenterX + 130, buttonCenterY + 22, buttonWidth, buttonHeight, TIMES[13], 13000));
        addRenderableWidget(new WakeTimeButton(buttonCenterX + 100, buttonCenterY + 44, buttonWidth, buttonHeight, TIMES[14], 14000));
        addRenderableWidget(new WakeTimeButton(buttonCenterX + 88, buttonCenterY + 66, buttonWidth, buttonHeight, TIMES[15], 16000));
    }


    @Override
    public void render(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
        super.render(pose, mouseX, mouseY, partialTicks);
        this.renderBackground(pose);
        super.render(pose, mouseX, mouseY, partialTicks);
        drawCenteredString(pose, this.font, SLEEP_UNTIL, this.width / 2, this.height / 2 - 5, 16777215);
        if (this.minecraft != null && this.minecraft.player != null)
            drawCenteredString(pose, this.font, SomniaUtil.timeStringForWorldTime(this.minecraft.player.level.getDayTime()), this.width / 2, this.height / 2 - 66, 16777215);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}