package io.github.alkyaly.somnia.handler;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.alkyaly.somnia.api.capability.Components;
import io.github.alkyaly.somnia.api.capability.Fatigue;
import io.github.alkyaly.somnia.core.Somnia;
import io.github.alkyaly.somnia.network.NetworkHandler;
import io.github.alkyaly.somnia.util.SideEffectStage;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

@Environment(EnvType.CLIENT)
public final class ClientTickHandler {
    public static final ClientTickHandler INSTANCE = new ClientTickHandler();
    private static final DecimalFormat MULTIPLIER_FORMAT = new DecimalFormat("0.0");

    private static final ItemStack CLOCK = new ItemStack(Items.CLOCK);

    private final Minecraft mc = Minecraft.getInstance();
    private final DoubleList speedValues = new DoubleArrayList();
    private long sleepStart = -1;
    private double speed;
    private boolean muted;
    private float volume;

    private ClientTickHandler() {
        //singleton
    }

    public void onClientTick() {
        if (mc.player != null && mc.level != null) {
            if (mc.player.isSleeping() && Somnia.CONFIG.options.muteSoundWhenSleeping && !muted) {
                muted = true;
                volume = mc.options.getSoundSourceVolume(SoundSource.MASTER);
                mc.options.setSoundCategoryVolume(SoundSource.MASTER, 0);
            } else if (muted) {
                muted = false;
                mc.options.setSoundCategoryVolume(SoundSource.MASTER, volume);
            }

            Fatigue props = Components.get(mc.player);

            if (props != null) {
                long wakeTime = props.getWakeTime();

                if (wakeTime > -1 && mc.level.getGameTime() >= wakeTime) {
                    FriendlyByteBuf buf = PacketByteBufs.create();
                    buf.writeLong(-1);

                    ClientPlayNetworking.send(NetworkHandler.UPDATE_WAKE_TIME, buf);
                    props.setWakeTime(-1);
                    mc.player.stopSleeping();

                    ClientPlayNetworking.send(NetworkHandler.WAKE_UP_PLAYER, PacketByteBufs.create());
                }
            }
        }
    }

    public void addSpeedValue(double speed) {
        this.speed = speed;
        speedValues.add(speed);
        if (speedValues.size() > 5) speedValues.removeDouble(0);
    }

    //I'd love to use HudRenderCallback, but getting a component every FRAME might be really bad.
    public void onRenderTick() {
        Player player = mc.player;
        Screen screen = mc.screen;
        if (screen != null && !(screen instanceof PauseScreen)) {
            if (player == null || !player.isSleeping()) return;
        }

        Fatigue props = Components.get(player);
        double fatigue = props != null ? props.getFatigue() : 0;
        PoseStack pose = new PoseStack();

        if (player != null && !player.isCreative() && !player.isSpectator() && !mc.options.hideGui) {
            if (!player.isSleeping() && !Somnia.CONFIG.fatigue.fatigueSideEffects && fatigue > Somnia.CONFIG.fatigue.minimumFatigueToSleep)
                return;

            String str;
            if (Somnia.CONFIG.fatigue.simpleFatigueDisplay) {
                str = SpeedColor.WHITE.code + SideEffectStage.getSideEffectStageDescription(fatigue);
            } else {
                str = String.format(SpeedColor.WHITE.code + "Fatigue: %.2f", fatigue);
            }

            int width = mc.font.width(str),
                    scaledWidth = mc.getWindow().getGuiScaledWidth(),
                    scaledHeight = mc.getWindow().getGuiScaledHeight();
            FatigueDisplayPosition pos = player.isSleeping() ? FatigueDisplayPosition.BOTTOM_RIGHT : FatigueDisplayPosition.valueOf(Somnia.CONFIG.fatigue.displayFatigue);
            mc.font.draw(pose, str, pos.getX(scaledWidth, width), pos.getY(scaledHeight, mc.font.lineHeight), Integer.MIN_VALUE);
        }

        if (player != null && player.isSleeping() && Somnia.CONFIG.options.somniaGui && fatigue != -1) {
            renderSleepGui(pose, screen);
        } else if (sleepStart != -1 || speed != 0) {
            this.sleepStart = -1;
            this.speed = 0;
        }
    }

    private void renderSleepGui(PoseStack pose, Screen screen) {
        if (screen == null) return;

        if (speed != 0) {
            if (sleepStart == -1) {
                sleepStart = mc.level.getGameTime();
            }
        } else {
            sleepStart = -1;
        }

        RenderSystem.setShaderColor(1, 1, 1, 1);
        Fatigue props = Components.get(mc.player);

        if (props != null && mc.level != null) {
            long wakeTime = props.getWakeTime();
            if (sleepStart != -1 && wakeTime > -1) {
                RenderSystem.setShader(GameRenderer::getPositionTexShader);
                RenderSystem.setShaderColor(1, 1, 1, 1);
                RenderSystem.setShaderTexture(0, GuiComponent.GUI_ICONS_LOCATION);

                double sleepDuration = mc.level.getGameTime() - sleepStart,
                        remaining = wakeTime - sleepStart,
                        progress = sleepDuration / remaining;
                int width = screen.width - 40;

                RenderSystem.enableBlend();
                RenderSystem.setShaderColor(1, 1, 1, .2f);
                renderProgressBar(pose, width, 1);

                RenderSystem.disableBlend();
                RenderSystem.setShaderColor(1, 1, 1, 1);
                renderProgressBar(pose, width, progress);

                String display = Somnia.CONFIG.fatigue.displayETASleep;

                int offsetX = "center".equalsIgnoreCase(display) ? screen.width / 2 - 80 : "right".equalsIgnoreCase(display) ? width - 160 : 0;
                renderScaledString(pose, offsetX + 20, String.format("%sx%s", SpeedColor.getColorForSpeed(speed).code, MULTIPLIER_FORMAT.format(speed)));

                double sum = 0;
                //avoids creating a DoubleSummaryStatistics and a Stream every tick
                for (double value : speedValues) {
                    sum += value;
                }
                double average = speedValues.isEmpty() ? 0 : sum / speedValues.size();

                long eta = Math.round((remaining - sleepDuration) / (average * 20));

                if (Somnia.CONFIG.options.coolETASleepText) {
                    renderScaledRainbowString(pose, offsetX + 80, getETAString(eta));
                } else {
                    renderScaledString(pose, offsetX + 80, SpeedColor.WHITE.code + getETAString(eta));
                }
                renderClock(width);
            }
        }
    }

    private static String getETAString(long totalSeconds) {
        long etaSeconds = totalSeconds % 60,
                etaMinutes = (totalSeconds - etaSeconds) / 60;
        return String.format("(%s:%s)", (etaMinutes < 10 ? "0" : "") + etaMinutes, (etaSeconds < 10 ? "0" : "") + etaSeconds);
    }

    private void renderProgressBar(PoseStack pose, int width, double progress) {
        int x = 20;
        for (int amount = (int) (progress * width); amount > 0; amount -= 180, x += 180) {
            if (mc.screen != null) {
                mc.screen.blit(pose, x, 10, 0, 69, Math.min(amount, 180), 5);
            }
        }
    }

    private void renderScaledRainbowString(PoseStack pose, int x, String str) {
        if (mc.screen == null) return;
        pose.pushPose();
        pose.translate(x, 20, 0);
        pose.scale(1.5f, 1.5f, 1);

        MutableComponent cmp = new TextComponent("");

        for (char xar : str.toCharArray()) {
            cmp.append(String.valueOf(xar));
        }

        for (Component sibling : cmp.getSiblings()) {
            ((MutableComponent) sibling).withStyle(s ->
                    s.withColor(Mth.hsvToRgb((System.currentTimeMillis() % 4000) / 4000f, 0.7f, 0.9f))
            );
        }
        mc.font.drawShadow(pose, cmp, 0, 0, Integer.MIN_VALUE);
    }

    private void renderScaledString(PoseStack pose, int x, String str) {
        if (mc.screen == null) return;
        pose.pushPose();
        pose.translate(x, 20, 0);
        pose.scale(1.5f, 1.5f, 1);
        mc.font.drawShadow(pose, str, 0, 0, Integer.MIN_VALUE);
        pose.popPose();
    }

    private void renderClock(int maxWidth) {
        int x = switch (Somnia.CONFIG.options.somniaGuiClockPosition.toLowerCase(Locale.ROOT)) {
            case "left" -> 40;
            case "center" -> maxWidth / 2;
            case "right" -> maxWidth - 40;
            default -> throw new IllegalArgumentException("Invalid Value: " + Somnia.CONFIG.options.somniaGuiClockPosition);
        };

        PoseStack pose = RenderSystem.getModelViewStack();

        pose.pushPose();
        pose.translate(x - 10, 45, 0);
        pose.scale(4, 4, 1);
        mc.getItemRenderer().renderAndDecorateItem(CLOCK, 0, 0);
        pose.popPose();
    }

    public enum SpeedColor {
        WHITE(ChatFormatting.PREFIX_CODE + "f", 8),
        DARK_RED(ChatFormatting.PREFIX_CODE + "4", 20),
        RED(ChatFormatting.PREFIX_CODE + "c", 30),
        GOLD(ChatFormatting.PREFIX_CODE + "6", 101);

        public static final Set<SpeedColor> VALUES = Arrays.stream(values())
                .sorted(Comparator.comparing(color -> color.range))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        public final String code;
        public final double range;

        SpeedColor(String code, double range) {
            this.code = code;
            this.range = range;
        }

        public static SpeedColor getColorForSpeed(double speed) {
            for (SpeedColor color : VALUES) {
                if (speed < color.range) return color;
            }

            return WHITE;
        }
    }

    @SuppressWarnings("unused")
    public enum FatigueDisplayPosition {
        TOP_CENTER((scaledWidth, stringWidth) -> scaledWidth / 2 - stringWidth / 2, (scaledHeight, fontHeight) -> fontHeight),
        TOP_LEFT((scaledWidth, stringWidth) -> 10, (scaledHeight, fontHeight) -> fontHeight),
        TOP_RIGHT((scaledWidth, stringWidth) -> scaledWidth - stringWidth - 10, (scaledHeight, fontHeight) -> fontHeight),
        BOTTOM_CENTER((scaledWidth, stringWidth) -> scaledWidth / 2 - stringWidth / 2, (scaledHeight, fontHeight) -> scaledHeight - fontHeight - 45),
        BOTTOM_LEFT((scaledWidth, stringWidth) -> 10, (scaledHeight, fontHeight) -> scaledHeight - fontHeight - 10),
        BOTTOM_RIGHT((scaledWidth, stringWidth) -> scaledWidth - stringWidth - 10, (scaledHeight, fontHeight) -> scaledHeight - fontHeight - 10);

        private final BiFunction<Integer, Integer, Integer> x;
        private final BiFunction<Integer, Integer, Integer> y;

        FatigueDisplayPosition(BiFunction<Integer, Integer, Integer> y, BiFunction<Integer, Integer, Integer> x) {
            this.x = x;
            this.y = y;
        }

        public int getX(int scaledWidth, int stringWidth) {
            return this.x.apply(scaledWidth, stringWidth);
        }

        public int getY(int scaledHeight, int fontHeight) {
            return this.y.apply(scaledHeight, fontHeight);
        }
    }
}
