package io.github.alkyaly.somnia.mixin;

import io.github.alkyaly.somnia.api.capability.Components;
import io.github.alkyaly.somnia.api.capability.Fatigue;
import io.github.alkyaly.somnia.handler.PlayerSleepTickHandler;
import io.github.alkyaly.somnia.network.NetworkHandler;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity {
    @Shadow public abstract boolean isSpectator();

    @Shadow public abstract void stopSleeping();

    private PlayerMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;removeEntitiesOnShoulder()V"
            ),
            method = "hurt"
    ) // Forge: LivingHurtEvent on ForgeEventHandler
    private void somnia$onLivingHurt(DamageSource damageSource, float f, CallbackInfoReturnable<Boolean> cir) {
        //noinspection InstanceofThis, ConstantConditions
        if ((Object) this instanceof ServerPlayer player && isSleeping()) {
            Fatigue props = Components.get(player);
            if (props != null) {
                props.setSleepOverride(false);
            }
            player.stopSleeping();
            ServerPlayNetworking.send(player, NetworkHandler.WAKE_UP_PLAYER, PacketByteBufs.create());
        }
    }

    @Inject(at = @At("HEAD"), method = "tick()V")
    private void somnia$preSleepingTick(CallbackInfo info) {
        PlayerSleepTickHandler.onPlayerTick(true, (Player) (Object) this);
    }

    @Inject(at = @At("TAIL"), method = "tick()V")
    private void somnia$tailTick(CallbackInfo ci) {
        PlayerSleepTickHandler.onPlayerTick(false, (Player) (Object) this);
    }
}
