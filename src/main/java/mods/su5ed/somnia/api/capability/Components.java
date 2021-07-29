package mods.su5ed.somnia.api.capability;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy;
import mods.su5ed.somnia.core.Somnia;

public class Components implements EntityComponentInitializer {
    public static final ComponentKey<Fatigue> FATIGUE = ComponentRegistry.getOrCreate(Somnia.locate("fatigue"), Fatigue.class);

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerForPlayers(FATIGUE, p -> new Fatigue(), RespawnCopyStrategy.ALWAYS_COPY);
    }
}
