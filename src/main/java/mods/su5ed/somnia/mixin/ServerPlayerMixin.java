package mods.su5ed.somnia.mixin;

import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Either;
import mods.su5ed.somnia.api.capability.Components;
import mods.su5ed.somnia.api.capability.IFatigue;
import mods.su5ed.somnia.util.MixinHooks;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
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
    private void somnia$updateAwakeTime(BlockPos blockPos, CallbackInfoReturnable<Either<BedSleepingProblem, Unit>> cir) {
        MixinHooks.updateWakeTime(this);
    }

    @Inject(
            at = @At("HEAD"),
            method = "setRespawnPosition",
            cancellable = true
    ) // Forge: PlayerSetSpawnEvent on ForgeEventHandler
    private void somnia$onPlayerSetSpawn(ResourceKey<Level> resourceKey, BlockPos blockPos, float f, boolean bl, boolean bl2, CallbackInfo ci) {
        IFatigue props = Components.get(this);

        if (props != null) {
            if (!props.resetSpawn()) {
                ci.cancel();
            }
        }
    }
}
