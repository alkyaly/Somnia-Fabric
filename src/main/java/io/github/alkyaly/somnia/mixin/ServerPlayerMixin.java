package io.github.alkyaly.somnia.mixin;

import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Either;
import io.github.alkyaly.somnia.api.capability.Components;
import io.github.alkyaly.somnia.api.capability.Fatigue;
import io.github.alkyaly.somnia.util.MixinHooks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player {

    private ServerPlayerMixin(Level level, BlockPos blockPos, float f, GameProfile gameProfile) {
        super(level, blockPos, f, gameProfile);
    }

    //Original JS-Coremod: patchServerPlayerEntity.js
    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;updateSleepingPlayerList()V"), method = "startSleepInBed")
    private void somnia$updateAwakeTime(CallbackInfoReturnable<Either<BedSleepingProblem, Unit>> cir) {
        MixinHooks.updateWakeTime((ServerPlayer) (Object) this);
    }

    @Inject(
            at = @At("HEAD"),
            method = "setRespawnPosition",
            cancellable = true
    ) // Forge: PlayerSetSpawnEvent on ForgeEventHandler
    private void somnia$onPlayerSetSpawn(CallbackInfo ci) {
        Fatigue props = Components.get(this);

        if (props != null) {
            if (!props.resetSpawn()) {
                ci.cancel();
            }
        }
    }
}
