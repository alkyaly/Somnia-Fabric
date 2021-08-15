package mods.su5ed.somnia.core;

import draylar.omegaconfig.OmegaConfig;
import mods.su5ed.somnia.config.SomniaConfig;
import mods.su5ed.somnia.handler.EventHandler;
import mods.su5ed.somnia.network.NetworkHandler;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static mods.su5ed.somnia.mixin.accessor.PotionBrewingAccessor.somnia$invokeAddMix;

public class Somnia implements ModInitializer {
    public static final String MODID = "somnia";
    public static final Logger LOGGER = LogManager.getLogger();
    public static final SomniaConfig CONFIG = OmegaConfig.register(SomniaConfig.class);

    @Override
    public void onInitialize() {
        SomniaObjects.init();
        EventHandler.init();
        NetworkHandler.init();

        setupPotions();
    }

    private static void setupPotions() {
        somnia$invokeAddMix(Potions.NIGHT_VISION, Items.GLISTERING_MELON_SLICE, SomniaObjects.AWAKENING_POTION);
        somnia$invokeAddMix(Potions.LONG_NIGHT_VISION, Items.GLISTERING_MELON_SLICE, SomniaObjects.LONG_AWAKENING_POTION);
        somnia$invokeAddMix(Potions.NIGHT_VISION, Items.BLAZE_POWDER, SomniaObjects.STRONG_AWAKENING_POTION);
        somnia$invokeAddMix(SomniaObjects.AWAKENING_POTION, Items.FERMENTED_SPIDER_EYE, SomniaObjects.INSOMNIA_POTION);
        somnia$invokeAddMix(SomniaObjects.LONG_AWAKENING_POTION, Items.FERMENTED_SPIDER_EYE, SomniaObjects.LONG_INSOMNIA_POTION);
        somnia$invokeAddMix(SomniaObjects.STRONG_AWAKENING_POTION, Items.FERMENTED_SPIDER_EYE, SomniaObjects.STRONG_INSOMNIA_POTION);
    }

    public static ResourceLocation locate(String path) {
        return new ResourceLocation(MODID, path);
    }
}
