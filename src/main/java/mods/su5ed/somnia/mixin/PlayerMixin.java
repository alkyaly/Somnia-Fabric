package mods.su5ed.somnia.mixin;

import mods.su5ed.somnia.api.capability.Components;
import mods.su5ed.somnia.api.capability.IFatigue;
import mods.su5ed.somnia.core.Somnia;
import mods.su5ed.somnia.core.SomniaObjects;
import mods.su5ed.somnia.handler.PlayerSleepTickHandler;
import mods.su5ed.somnia.network.NetworkHandler;
import mods.su5ed.somnia.util.SideEffectStage;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
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
    @Shadow public abstract boolean isCreative();

    @Shadow public abstract boolean isSpectator();

    private PlayerMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(at = @At("HEAD"), method = "tick") // Forge: TickEvent.PlayerTickEvent on ForgeEventHandler
    private void somnia$preTick(CallbackInfo ci) {
        PlayerSleepTickHandler.onPlayerTick(true, (Player) (Object) this);
        if (!level.isClientSide && isAlive() && !isCreative() && !isSpectator() && isSleeping()) {
            IFatigue props = Components.FATIGUE.getNullable(this);

            if (props != null) {
                double fatigue = props.getFatigue();
                double extraFatigueRate = props.getExtraFatigueRate();
                double replenishedFatigue = props.getReplenishedFatigue();
                boolean isSleeping = props.sleepOverride() || isSleeping();

                if (isSleeping) {
                    fatigue -= Somnia.CONFIG.fatigue.fatigueReplenishRate;
                    double share = Somnia.CONFIG.fatigue.fatigueReplenishRate / Somnia.CONFIG.fatigue.fatigueRate;
                    double replenish = Somnia.CONFIG.fatigue.fatigueReplenishRate * share;
                    extraFatigueRate -= Somnia.CONFIG.fatigue.fatigueReplenishRate / share / replenishedFatigue / 10;
                    replenishedFatigue -= replenish;
                } else {
                    double rate = Somnia.CONFIG.fatigue.fatigueRate;

                    MobEffectInstance wakefulness = getEffect(SomniaObjects.AWAKENING_EFFECT);
                    if (wakefulness != null) {
                        rate -= wakefulness.getAmplifier() == 0 ? rate / 4 : rate / 3;
                    }

                    MobEffectInstance insomnia = getEffect(SomniaObjects.INSOMNIA_EFFECT);
                    if (insomnia != null) {
                        rate += insomnia.getAmplifier() == 0 ? rate / 2 : rate;
                    }
                    fatigue += rate + props.getExtraFatigueRate();
                }

                if (fatigue > 100) fatigue = 100;
                else if (fatigue < 0) fatigue = 0;

                if (replenishedFatigue > 100) replenishedFatigue = 100;
                else if (replenishedFatigue < 0) replenishedFatigue = 0;

                if (extraFatigueRate < 0) extraFatigueRate = 0;

                props.setFatigue(fatigue);
                props.setReplenishedFatigue(replenishedFatigue);
                props.setExtraFatigueRate(extraFatigueRate);

                if (props.updateFatigueCounter() >= 100) {
                    props.resetFatigueCounter();
                    FriendlyByteBuf buf = PacketByteBufs.create();
                    buf.writeDouble(fatigue);
                    ServerPlayNetworking.send((ServerPlayer) (Object) this, NetworkHandler.UPDATE_FATIGUE, buf);

                    if (Somnia.CONFIG.fatigue.fatigueSideEffects) {
                        int lastSideEffectStage = props.getSideEffectStage();
                        SideEffectStage[] stages = SideEffectStage.getSideEffectStages();
                        SideEffectStage firstStage = stages[0];
                        if (fatigue < firstStage.minFatigue()) {
                            props.setSideEffectStage(-1);
                            for (SideEffectStage stage : stages) {
                                if (lastSideEffectStage < stage.minFatigue()) {
                                    removeEffect(Registry.MOB_EFFECT.get(new ResourceLocation(stage.potionId())));
                                }
                            }
                        }

                        for (int i = 0; i < Somnia.CONFIG.fatigue.sideEffectStages.size(); i++) {
                            SideEffectStage stage = stages[i];
                            boolean permanent = stage.duration() < 0;
                            if (fatigue >= stage.minFatigue() && fatigue <= stage.maxFatigue() && (permanent || lastSideEffectStage < stage.minFatigue())) {
                                if (!permanent) props.setSideEffectStage(stage.minFatigue());
                                addEffect(new MobEffectInstance(Registry.MOB_EFFECT.get(new ResourceLocation(stage.potionId())), permanent ? 150 : stage.duration(), stage.amplifier()));
                            }
                        }
                    }
                }
            }
        }
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
        IFatigue props = Components.FATIGUE.getNullable(this);

        if (props != null) {
            props.setFatigue(0);
            props.setReplenishedFatigue(0);
            props.setExtraFatigueRate(0);
        }
    }
}
