package io.github.alkyaly.somnia.core;

import draylar.omegaconfig.OmegaConfig;
import io.github.alkyaly.somnia.config.SomniaConfig;
import io.github.alkyaly.somnia.handler.EventHandler;
import io.github.alkyaly.somnia.network.NetworkHandler;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Somnia implements ModInitializer {
    public static final String MODID = "somnia";
    public static final Logger LOGGER = LogManager.getLogger();
    public static final SomniaConfig CONFIG = OmegaConfig.register(SomniaConfig.class);

    @Override
    public void onInitialize() {
        SomniaObjects.init();
        EventHandler.init();
        NetworkHandler.init();
        SomniaObjects.setupPotions();
    }

    public static ResourceLocation locate(String path) {
        return new ResourceLocation(MODID, path);
    }
}
