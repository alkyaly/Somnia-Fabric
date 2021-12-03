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
    protected static final ComponentKey<FatigueImpl> FATIGUE = ComponentRegistry.getOrCreate(Somnia.locate("fatigue"), FatigueImpl.class);

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerForPlayers(FATIGUE, FatigueImpl::new, RespawnCopyStrategy.LOSSLESS_ONLY);
    }

    @Nullable
    public static Fatigue get(Player player) {
        return FATIGUE.getNullable(player);
    }

    public static void sync(Player player) {
        FATIGUE.sync(player);
    }
}
