package io.github.alkyaly.somnia.mixin;

import io.github.alkyaly.somnia.util.MixinHooks;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ServerChunkCache.class)
public class ServerChunkCacheMixin {

    @Shadow @Final ServerLevel level;

    @ModifyVariable(
            at = @At(
                    value = "INVOKE_ASSIGN",
                    target = "Lnet/minecraft/world/level/GameRules;getBoolean(Lnet/minecraft/world/level/GameRules$Key;)Z",
                    ordinal = 0
            ), method = "tickChunks", ordinal = 1) //Original JS-Coremod: patchServerWorld.js
    private boolean somnia$dontSpawnMonsters(boolean original) {
        return original && MixinHooks.doMobSpawning(level);
    }
}
