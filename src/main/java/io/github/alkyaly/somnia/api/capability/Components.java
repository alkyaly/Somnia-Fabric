package io.github.alkyaly.somnia.api.capability;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy;
import io.github.alkyaly.somnia.core.Somnia;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public class Components implements EntityComponentInitializer {
    public static final ComponentKey<FatigueImpl> FATIGUE = ComponentRegistry.getOrCreate(Somnia.locate("fatigue"), FatigueImpl.class);

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerForPlayers(FATIGUE, p -> new FatigueImpl(), RespawnCopyStrategy.ALWAYS_COPY);
    }

    @Nullable
    public static Fatigue get(Player player) {
        return FATIGUE.getNullable(player);
    }
}
