package mods.su5ed.somnia.api.capability;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy;
import mods.su5ed.somnia.core.Somnia;
import net.minecraft.world.entity.player.Player;

public class Components implements EntityComponentInitializer {
    public static final ComponentKey<FatigueImpl> FATIGUE = ComponentRegistry.getOrCreate(Somnia.locate("fatigue"), FatigueImpl.class);

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerForPlayers(FATIGUE, p -> new FatigueImpl(), RespawnCopyStrategy.ALWAYS_COPY);
    }

    public static Fatigue get(Player player) {
        return FATIGUE.getNullable(player);
    }
}
