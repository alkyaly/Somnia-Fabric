package mods.su5ed.somnia.handler;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import mods.su5ed.somnia.api.capability.Components;
import mods.su5ed.somnia.api.capability.IFatigue;
import mods.su5ed.somnia.core.Somnia;
import mods.su5ed.somnia.network.NetworkHandler;
import mods.su5ed.somnia.util.SideEffectStage;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.text.DecimalFormat;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class ClientTickHandler {
    public static final ClientTickHandler INSTANCE = new ClientTickHandler();
    private static final DecimalFormat MULTIPLIER_FORMAT = new DecimalFormat("0.0");
    private static final ItemStack CLOCK = new ItemStack(Items.CLOCK);
    private final Minecraft mc = Minecraft.getInstance();
    private final List<Double> speedValues = new ArrayList<>();
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

            IFatigue props = Components.FATIGUE.getNullable(mc.player);

            if (props != null) {
                long wakeTime = props.getWakeTime();

                if (wakeTime > -1 && mc.level.getGameTime() >= wakeTime) {
                    FriendlyByteBuf buf = PacketByteBufs.create();
                    buf.writeLong(-1);

                    ClientPlayNetworking.send(NetworkHandler.UPDATE_WAKE_TIME, buf);
                    props.setWakeTime(-1);
                    mc.player.stopSleeping();

                    FriendlyByteBuf wakeBuf = PacketByteBufs.create();
                    ClientPlayNetworking.send(NetworkHandler.WAKE_UP_PLAYER, wakeBuf);
                }
            }
        }
    }

    public void addSpeedValue(double speed) {
        this.speed = speed;
        speedValues.add(speed);
        if (speedValues.size() > 5) speedValues.remove(0);
    }

    public void onRenderTick() {
        Player player = mc.player;
        Screen screen = mc.screen;
        if (screen != null && !(screen instanceof PauseScreen)) {
            if (player == null || !player.isSleeping()) return;
        }

        IFatigue props = Components.FATIGUE.getNullable(player);
        double fatigue = props != null ? props.getFatigue() : 0;
        PoseStack pose = new PoseStack();
        if (player != null && !player.isCreative() && !player.isSpectator() && !mc.options.hideGui) {
            if (!player.isSleeping() && !Somnia.CONFIG.fatigue.fatigueSideEffects && fatigue > Somnia.CONFIG.fatigue.minimumFatigueToSleep) return;
            String str;
            if (Somnia.CONFIG.fatigue.simpleFatigueDisplay) str = SpeedColor.WHITE.code + SideEffectStage.getSideEffectStageDescription(fatigue);
            else str = String.format(SpeedColor.WHITE.code + "Fatigue: %.2f", fatigue);

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
            if (sleepStart == -1) sleepStart = mc.level.getGameTime();
        } else sleepStart = -1;

        RenderSystem.setShaderColor(1, 1, 1, 1);
        //glDisable(GL_LIGHTING);
        //glDisable(GL_FOG);
        IFatigue props = Components.FATIGUE.getNullable(mc.player);

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

                int offsetX = display.equalsIgnoreCase("center") ? screen.width / 2 - 80 : display.equalsIgnoreCase("right") ? width - 160 : 0;
                renderScaledString(pose, offsetX + 20, String.format("%sx%s", SpeedColor.getColorForSpeed(speed).code, MULTIPLIER_FORMAT.format(speed)));
                double average = speedValues.stream()
                        .filter(Objects::nonNull)
                        .mapToDouble(Double::doubleValue)
                        .summaryStatistics()
                        .getAverage();
                long eta = Math.round((remaining - sleepDuration) / (average * 20));

                renderScaledString(pose, offsetX + 80, getETAString(eta));

                renderClock(pose, width);
            }
        }
    }

    private String getETAString(long totalSeconds) {
        long etaSeconds = totalSeconds % 60,
                etaMinutes = (totalSeconds - etaSeconds) / 60;
        return String.format(SpeedColor.WHITE.code + "(%s:%s)", (etaMinutes < 10 ? "0" : "") + etaMinutes, (etaSeconds < 10 ? "0" : "") + etaSeconds);
    }

    private void renderProgressBar(PoseStack pose, int width, double progress) {
        int x = 20;
        for (int amount = (int) (progress * width); amount > 0; amount -= 180, x += 180) {
            if (mc.screen != null) mc.screen.blit(pose, x, 10, 0, 69, Math.min(amount, 180), 5);
        }
    }

    private void renderScaledString(PoseStack pose, int x, String str) {
        if (mc.screen == null) return;
        pose.pushPose();
        pose.translate(x, 20, 0);
        pose.scale(1.5f, 1.5f, 1);
        mc.font.drawShadow(pose, str, 0, 0, Integer.MIN_VALUE);
        pose.popPose();
    }

    private void renderClock(PoseStack pose, int maxWidth) {
        int x = switch (Somnia.CONFIG.options.somniaGuiClockPosition.toLowerCase(Locale.ROOT)) {
            case "left" -> 40;
            case "center" -> maxWidth / 2;
            case "right" -> maxWidth - 40;
            default -> throw new IllegalArgumentException("Value is not valid: " + Somnia.CONFIG.options.somniaGuiClockPosition);
        };

        pose.pushPose();
        pose.translate(x, 35, 0);
        pose.scale(4, 4, 1);
        mc.getItemRenderer().renderAndDecorateItem(mc.player, CLOCK, 0, 0, 21);
        pose.popPose();
    }

    public enum SpeedColor {
        WHITE(SpeedColor.COLOR+"f", 8),
        DARK_RED(SpeedColor.COLOR+"4", 20),
        RED(SpeedColor.COLOR+"c", 30),
        GOLD(SpeedColor.COLOR+"6", 100);

        public static final Set<SpeedColor> VALUES = Arrays.stream(values())
                .sorted(Comparator.comparing(color -> color.range))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        public static final char COLOR = (char) 167;
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

            return SpeedColor.WHITE;
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

        FatigueDisplayPosition(BiFunction<Integer, Integer, Integer> x, BiFunction<Integer, Integer, Integer> y) {
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
