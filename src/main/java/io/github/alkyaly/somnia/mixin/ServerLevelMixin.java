package io.github.alkyaly.somnia.mixin;

import io.github.alkyaly.somnia.util.MixinHooks;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ServerLevel.class)
public class ServerLevelMixin {

    @ModifyVariable(
            at = @At(
                    value = "STORE"
            ), method = "tickChunk", ordinal = 1) //Original JS-Coremod: patchServerWorld.js
    private boolean somnia$dontSpawnMonsters(boolean original) {
        return original && MixinHooks.doMobSpawning((ServerLevel) (Object) this);
    }
}
