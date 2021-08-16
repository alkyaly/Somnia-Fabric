package io.github.alkyaly.somnia.mixin;

import io.github.alkyaly.somnia.api.capability.Components;
import io.github.alkyaly.somnia.api.capability.Fatigue;
import io.github.alkyaly.somnia.handler.EventHandler;
import io.github.alkyaly.somnia.handler.PlayerSleepTickHandler;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity {
    @Shadow public abstract boolean isSpectator();

    @Shadow public abstract void stopSleeping();

    private PlayerMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    //fixme: broken. Also broken in upstream
    /*@Inject(
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;removeEntitiesOnShoulder()V"
            ),
            method = "hurt"
    ) // Forge: LivingHurtEvent on ForgeEventHandler
    @SuppressWarnings("ConstantConditions")
    private void somnia$onLivingHurt(DamageSource damageSource, float f, CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof ServerPlayer serverPlayer && isSleeping()) {
            //stopSleeping();
            ServerPlayNetworking.send(serverPlayer, NetworkHandler.WAKE_UP_PLAYER, PacketByteBufs.create());
        }
    }*/

    @Inject(at = @At("HEAD"), method = "tick")
    private void somnia$preSleepingTick(CallbackInfo info) {
        PlayerSleepTickHandler.onPlayerTick(true, (Player) (Object) this);
    }

    @Inject(at = @At("HEAD"), method = "tick")
    private void somnia$tickPlayer(CallbackInfo ci) {
        EventHandler.tickPlayer((Player) (Object) this);
    }

    @Inject(at = @At("TAIL"), method = "tick")
    private void somnia$tailTick(CallbackInfo ci) {
        PlayerSleepTickHandler.onPlayerTick(false, (Player) (Object) this);
    }

    @Inject(at = @At("HEAD"), method = "die") //Forge: LivingDeathEvent on ForgeEventHandler
    private void somnia$onDeath(CallbackInfo ci) {
        Fatigue props = Components.get((Player) (Object) this);

        if (props != null) {
            props.setFatigue(0);
            props.setReplenishedFatigue(0);
            props.setExtraFatigueRate(0);
        }
    }
}
