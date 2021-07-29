package mods.su5ed.somnia.mixin;

import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Either;
import mods.su5ed.somnia.api.capability.Components;
import mods.su5ed.somnia.api.capability.IFatigue;
import mods.su5ed.somnia.core.Somnia;
import mods.su5ed.somnia.util.ASMHooks;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player {

    private ServerPlayerMixin(Level level, BlockPos blockPos, float f, GameProfile gameProfile) {
        super(level, blockPos, f, gameProfile);
    }

    //Original JS-Coremod: patchServerPlayerEntity.js
    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;updateSleepingPlayerList()V"), method = "startSleepInBed")
    private void somnia$updateAwakeTime(BlockPos blockPos, CallbackInfoReturnable<Either<BedSleepingProblem, Unit>> cir) {
        ASMHooks.updateWakeTime(this);
    }

    //Original JS-Coremod: patchServerPlayerEntity.js
    @ModifyVariable(
            at = @At(
                    value = "INVOKE_ASSIGN",
                    target = "Lnet/minecraft/world/level/Level;getEntitiesOfClass(Ljava/lang/Class;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;)Ljava/util/List;"
            ),
            method = "startSleepInBed"
    ) //ok, this might work, it's definitely one of the worst work-arounds, but I don't want to Redirect the call
    private List<Monster> somnia$ignoreMonsters(List<Monster> original) {
        if (Somnia.CONFIG.options.ignoreMonsters) {
            return ASMHooks.DUMMY_NON_EMPTY_LIST;
        }
        return original;
    }

    @Inject(
            at = @At("HEAD"),
            method = "setRespawnPosition",
            cancellable = true
    ) // Forge: PlayerSetSpawnEvent on ForgeEventHandler
    private void somnia$onPlayerSetSpawn(ResourceKey<Level> resourceKey, BlockPos blockPos, float f, boolean bl, boolean bl2, CallbackInfo ci) {
        IFatigue props = Components.FATIGUE.getNullable(this);

        if (props != null) {
            if (!props.resetSpawn()) {
                ci.cancel();
            }
        }
    }
}
