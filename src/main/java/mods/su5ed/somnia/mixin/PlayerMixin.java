package mods.su5ed.somnia.mixin;

import mods.su5ed.somnia.api.capability.Components;
import mods.su5ed.somnia.api.capability.IFatigue;
import mods.su5ed.somnia.handler.EventHandler;
import mods.su5ed.somnia.handler.PlayerSleepTickHandler;
import mods.su5ed.somnia.network.NetworkHandler;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
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

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity {
    @Shadow public abstract boolean isSpectator();

    private PlayerMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

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

    @Inject(
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;getDamageAfterArmorAbsorb(Lnet/minecraft/world/damagesource/DamageSource;F)F"
            ),
            method = "actuallyHurt"
    ) // Forge: LivingHurtEvent on ForgeEventHandler
    @SuppressWarnings("ConstantConditions")
    private void somnia$onPlayerDamage(DamageSource damageSource, float f, CallbackInfo ci) {
        if ((Object) this instanceof ServerPlayer serverPlayer && isSleeping()) {
            FriendlyByteBuf buf = PacketByteBufs.create();

            ServerPlayNetworking.send(serverPlayer, NetworkHandler.WAKE_UP_PLAYER, buf);
        }
    }

    @Inject(at = @At("HEAD"), method = "die") //Forge: LivingDeathEvent on ForgeEventHandler
    private void somnia$onDeath(DamageSource damageSource, CallbackInfo ci) {
        IFatigue props = Components.get((Player) (Object) this);

        if (props != null) {
            props.setFatigue(0);
            props.setReplenishedFatigue(0);
            props.setExtraFatigueRate(0);
        }
    }
}
