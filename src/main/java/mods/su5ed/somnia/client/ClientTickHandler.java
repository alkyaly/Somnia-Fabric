package mods.su5ed.somnia.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import mods.su5ed.somnia.api.capability.CapabilityFatigue;
import mods.su5ed.somnia.api.capability.IFatigue;
import mods.su5ed.somnia.common.config.SomniaConfig;
import mods.su5ed.somnia.common.network.NetworkHandler;
import mods.su5ed.somnia.common.network.packet.PacketWakeUpPlayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.IngameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static org.lwjgl.opengl.GL11.*;

public class ClientTickHandler {
	private final Minecraft mc = Minecraft.getInstance();
	private final ItemStack clockItemStack = new ItemStack(Items.CLOCK);
	private final List<Double> speedValues = new ArrayList<>();
	public long sleepStart = -1;
	public double speed;

	private boolean muted;
	private float volume;

	public ClientTickHandler() {
		MinecraftForge.EVENT_BUS.register(this);

		CompoundNBT clockNbt = new CompoundNBT();
		clockNbt.putBoolean("quark:clock_calculated", true);
		this.clockItemStack.setTag(clockNbt); //Disables Quark's clock display override
	}
	
	@SubscribeEvent
	public void onClientTick(TickEvent.ClientTickEvent event) {
		if (event.phase == TickEvent.Phase.END) {
			Minecraft mc = Minecraft.getInstance();
			if (mc.player == null) return;

			if (mc.player.isSleeping() && SomniaConfig.muteSoundWhenSleeping && !muted) {
				muted = true;
				volume = mc.gameSettings.getSoundLevel(SoundCategory.MASTER);
				mc.gameSettings.setSoundLevel(SoundCategory.MASTER, 0);
			} else if (muted) {
				muted = false;
				mc.gameSettings.setSoundLevel(SoundCategory.MASTER, volume);
			}

			if (SomniaClient.autoWakeTime > -1) System.out.println("Remaining sleep time: "+(mc.world.getGameTime() - SomniaClient.autoWakeTime));
			if (SomniaClient.autoWakeTime > -1 && mc.world.getGameTime() >= SomniaClient.autoWakeTime) {
				System.out.println("Wake time: "+SomniaClient.autoWakeTime);
				SomniaClient.autoWakeTime = -1;
				mc.player.wakeUp();
				NetworkHandler.INSTANCE.sendToServer(new PacketWakeUpPlayer());
			}
		}
	}

	public void addSpeedValue(double speed) {
		this.speed = speed;
		speedValues.add(speed);
		if (speedValues.size() > 5) speedValues.remove(0);
	}
	
	@SubscribeEvent
	public void onRenderTick(TickEvent.RenderTickEvent event) {
		if (mc.currentScreen != null && !(mc.currentScreen instanceof IngameMenuScreen)) {
			if (mc.player == null || !mc.player.isSleeping()) return;
		}

		double fatigue = mc.player.getCapability(CapabilityFatigue.FATIGUE_CAPABILITY)
				.resolve()
				.map(IFatigue::getFatigue)
				.orElse(0D);
		FontRenderer fontRenderer = mc.fontRenderer;
		MatrixStack matrixStack = new MatrixStack();
		if (event.phase == TickEvent.Phase.END && !mc.player.isCreative() && !mc.player.isSpectator()) {
			if (!mc.player.isSleeping() && !SomniaConfig.fatigueSideEffects && fatigue > SomniaConfig.minimumFatigueToSleep) return;
			String str = String.format(SpeedColor.WHITE.code + "Fatigue: %.2f", fatigue);
			int stringWidth = fontRenderer.getStringWidth(str);
			int scaledWidth = mc.getMainWindow().getScaledWidth();
			int scaledHeight = mc.getMainWindow().getScaledHeight();
			FatigueDisplayPosition pos = mc.player.isSleeping() ? FatigueDisplayPosition.BOTTOM_RIGHT : FatigueDisplayPosition.valueOf(SomniaConfig.displayFatigue);
			fontRenderer.drawString(matrixStack, str, pos.getX(scaledWidth, stringWidth), pos.getY(scaledHeight, fontRenderer.FONT_HEIGHT), Integer.MIN_VALUE);
		}

		if (mc.player.isSleeping() && SomniaConfig.somniaGui && fatigue != -1) renderSleepGui(matrixStack, mc.currentScreen);
		else if (sleepStart != -1 || speed != 0) {
			this.sleepStart = -1;
			this.speed = 0;
		}
	}

	private void renderSleepGui(MatrixStack matrixStack, Screen screen) {
		if (screen == null) return;

		if (speed != 0) {
			if (sleepStart == -1) sleepStart = this.mc.world.getGameTime();
		} else sleepStart = -1;

		glColor4f(1, 1, 1, 1);
		glDisable(GL_LIGHTING);
		glDisable(GL_FOG);

		if (sleepStart != -1 && SomniaClient.autoWakeTime > -1) {
			mc.getTextureManager().bindTexture(AbstractGui.GUI_ICONS_LOCATION);

			double sleepDuration = mc.world.getGameTime() - sleepStart,
				   remaining = SomniaClient.autoWakeTime - sleepStart,
                   progress = sleepDuration / remaining;

			int maxWidth = screen.width - 40;

			glEnable(GL_BLEND);
			glColor4f(1, 1, 1, 0.2F);
			renderProgressBar(matrixStack, maxWidth, 1);

			glDisable(GL_BLEND);
			glColor4f(1, 1, 1, 1);
			renderProgressBar(matrixStack, maxWidth, progress);

			int offsetX = SomniaConfig.displayETASleep.equals("center") ? screen.width/2 - 80 : SomniaConfig.displayETASleep.equals("right") ? maxWidth - 160 : 0;
			renderScaledString(matrixStack, offsetX + 20, String.format("%sx%s", SpeedColor.getColorForSpeed(speed).code, speed));

			double average = speedValues.stream()
					.filter(Objects::nonNull)
					.mapToDouble(Double::doubleValue)
					.summaryStatistics()
					.getAverage();
			long etaTotalSeconds = Math.round((remaining - sleepDuration) / (average * 20));

			renderScaledString(matrixStack, offsetX + 80, getETAString(etaTotalSeconds));

			renderClock(maxWidth - 40);
		}
	}

	private String getETAString(long totalSeconds) {
		long etaSeconds = totalSeconds % 60, etaMinutes = (totalSeconds - etaSeconds) / 60;
		return String.format(SpeedColor.WHITE.code + "(%s:%s)", (etaMinutes<10?"0":"") + etaMinutes, (etaSeconds<10?"0":"") + etaSeconds);
	}

	private void renderProgressBar(MatrixStack matrixStack, int maxWidth, double progress) {
		int x = 20;
		int amount = (int) (progress * maxWidth);
		while (amount > 0) {
			if (mc.currentScreen != null) this.mc.currentScreen.blit(matrixStack, x, 10, 0, 69, Math.min(amount, 180), 5);
			amount -= 180;
			x += 180;
		}
	}

	private void renderScaledString(MatrixStack matrixStack, int x, String str) {
		if (mc.currentScreen == null) return;
		glPushMatrix();
		glTranslatef(x, 20, 0);
		glScalef(1.5F, 1.5F, 1);
		mc.fontRenderer.drawStringWithShadow(matrixStack, str, 0, 0, Integer.MIN_VALUE);
		glPopMatrix();
	}

	private void renderClock(int x) {
		glPushMatrix();
		glTranslatef(x, 30, 0);
		glScalef(4, 4, 1);
		mc.getItemRenderer().renderItemAndEffectIntoGUI(mc.player, clockItemStack, 0, 0);
		glPopMatrix();
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