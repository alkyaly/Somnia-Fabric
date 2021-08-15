package mods.su5ed.somnia.mixin;

import mods.su5ed.somnia.util.MixinHooks;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ServerLevel.class)
public class ServerLevelMixin {

    //todo: This is modifying the wrong variable.
    @ModifyVariable(
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/GameRules;getBoolean(Lnet/minecraft/world/level/GameRules$Key;)Z"
            ), method = "tickChunk") //Original JS-Coremod: patchServerWorld.js
    private boolean somnia$dontSpawnMonsters(boolean original) {
        return original && !MixinHooks.doMobSpawning((ServerLevel) (Object) this);
    }
}
