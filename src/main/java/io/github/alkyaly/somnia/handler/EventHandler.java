package io.github.alkyaly.somnia.handler;

import io.github.alkyaly.somnia.api.capability.Components;
import io.github.alkyaly.somnia.api.capability.Fatigue;
import io.github.alkyaly.somnia.compat.Compat;
import io.github.alkyaly.somnia.core.Somnia;
import io.github.alkyaly.somnia.core.SomniaCommand;
import io.github.alkyaly.somnia.core.SomniaObjects;
import io.github.alkyaly.somnia.mixin.accessor.ServerPlayerAccessor;
import io.github.alkyaly.somnia.network.NetworkHandler;
import io.github.alkyaly.somnia.util.MixinHooks;
import io.github.alkyaly.somnia.util.SideEffectStage;
import io.github.alkyaly.somnia.util.SomniaUtil;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.Iterator;

public final class EventHandler {

    public static void init() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> SomniaCommand.register(dispatcher));
        ServerTickEvents.END_SERVER_TICK.register(EventHandler::onEndTick);
        ServerWorldEvents.LOAD.register(EventHandler::levelLoadHook);
        ServerWorldEvents.UNLOAD.register(EventHandler::levelUnloadHook);
        ServerPlayConnectionEvents.JOIN.register(EventHandler::syncFatigue);
        EntitySleepEvents.ALLOW_SLEEPING.register(EventHandler::onPlayerSleepInBed);
        EntitySleepEvents.ALLOW_SLEEP_TIME.register(EventHandler::onSleepingTimeCheck);
        EntitySleepEvents.STOP_SLEEPING.register(EventHandler::onWakeUp);
        EntitySleepEvents.ALLOW_NEARBY_MONSTERS.register(EventHandler::cancelMonsterCheck);
        UseBlockCallback.EVENT.register(EventHandler::onRightClickBlock);
    }

    //Forge: TickEvent.ServerTickEvent on ForgeEventHandler
    private static void onEndTick(MinecraftServer server) {
        ServerTickHandler.HANDLERS.forEach(ServerTickHandler::tickEnd);
    }

    //Forge: PlayerSleepInBedEvent on ForgeEventHandler
    private static Player.BedSleepingProblem onPlayerSleepInBed(Player player, BlockPos pos) {
        if (!SomniaUtil.checkFatigue(player)) {
            player.displayClientMessage(new TranslatableComponent("somnia.status.cooldown"), true);
            return Player.BedSleepingProblem.OTHER_PROBLEM;
        } else if (!Somnia.CONFIG.options.sleepWithArmor && !player.isCreative() && SomniaUtil.isPlayerWearingArmor(player)) {
            player.displayClientMessage(new TranslatableComponent("somnia.status.armor"), true);
            return Player.BedSleepingProblem.OTHER_PROBLEM;
        }

        Fatigue props = Components.get(player);
        if (props != null) {
            props.setSleepNormally(player.isShiftKeyDown());
        }

        if (Compat.isSleepingInBag(player)) MixinHooks.updateWakeTime(player);

        return null;
    }

    //Forge: SleepTimeCheckEvent on ForgeEventHandler
    private static InteractionResult onSleepingTimeCheck(Player player, BlockPos pos, boolean day) {
        Fatigue props = Components.get(player);

        if (props != null) {
            if (props.shouldSleepNormally()) {
                return InteractionResult.PASS;
            }
        }

        if (SomniaUtil.isEnterSleepTime()) return InteractionResult.SUCCESS;

        return InteractionResult.PASS;
    }

    //Forge: PlayerInteractEvent.RightClickBlock on ForgeEventHandler
    private static InteractionResult onRightClickBlock(Player player, Level level, InteractionHand hand, BlockHitResult bhr) {
        if (!level.isClientSide) {
            BlockPos pos = bhr.getBlockPos();
            BlockState state = level.getBlockState(pos);
            if (!state.hasProperty(HorizontalDirectionalBlock.FACING)) return InteractionResult.PASS;
            Direction direction = state.getValue(HorizontalDirectionalBlock.FACING);

            if (!Compat.isBed(state) || !((ServerPlayerAccessor) player).somnia$invokeBedInRange(pos, direction))
                return InteractionResult.PASS;

            ItemStack stack = player.getInventory().getSelected();
            String regName = Registry.ITEM.getKey(stack.getItem()).toString();
            if (!stack.isEmpty() && regName.equals(Somnia.CONFIG.options.wakeTimeSelectItem)) {
                FriendlyByteBuf buf = PacketByteBufs.create();
                ServerPlayNetworking.send((ServerPlayer) player, NetworkHandler.OPEN_GUI, buf);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    //Forge: PlayerWakeUpEvent on ForgeEventHandler
    private static void onWakeUp(LivingEntity entity, BlockPos pos) {
        if (entity instanceof Player player) {
            Fatigue props = Components.get(player);

            if (props == null) return;

            if (props.shouldSleepNormally()) {
                props.setFatigue(props.getFatigue() - SomniaUtil.getFatigueToReplenish(player));
            }
            props.maxFatigueCounter();
            props.shouldResetSpawn(true);
            props.setSleepNormally(false);
            props.setWakeTime(-1);
        }
    }

    //Forge: WorldEvent.Load on ForgeEventHandler
    private static void levelLoadHook(MinecraftServer server, ServerLevel level) {
        ServerTickHandler.HANDLERS.add(new ServerTickHandler(level));
        Somnia.LOGGER.info("Registering tick handler for dimension {}", level.dimension().location().toString());
    }

    //Forge: WorldEvent.Unload on ForgeEventHandler
    private static void levelUnloadHook(MinecraftServer server, ServerLevel level) {
        Iterator<ServerTickHandler> iter = ServerTickHandler.HANDLERS.iterator();
        ServerTickHandler serverTickHandler;
        while (iter.hasNext()) {
            serverTickHandler = iter.next();
            if (serverTickHandler.levelServer == level) {
                Somnia.LOGGER.info("Removing tick handler for dimension {}", level.dimension().location().toString());
                iter.remove();
                break;
            }
        }
    }

    private static InteractionResult cancelMonsterCheck(Player player, BlockPos pos, boolean result) {
        return Somnia.CONFIG.options.ignoreMonsters ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }

    private static void syncFatigue(ServerGamePacketListenerImpl handler, PacketSender sender, MinecraftServer server) {
        Components.sync(handler.player);
    }

    public static void tickPlayer(Player player, Fatigue props) {
        if (!player.isAlive() || player.isCreative() || player.isSpectator() && !player.isSleeping()) return;

        if (props != null) {
            double fatigue = props.getFatigue();
            double extraFatigueRate = props.getExtraFatigueRate();
            double replenishedFatigue = props.getReplenishedFatigue();
            boolean isSleeping = props.sleepOverride() || player.isSleeping();

            if (isSleeping) {
                fatigue -= Somnia.CONFIG.fatigue.fatigueReplenishRate;
                double share = Somnia.CONFIG.fatigue.fatigueReplenishRate / Somnia.CONFIG.fatigue.fatigueRate;
                double replenish = Somnia.CONFIG.fatigue.fatigueReplenishRate * share;
                extraFatigueRate -= Somnia.CONFIG.fatigue.fatigueReplenishRate
                        / share / replenishedFatigue == 0 ? 1 : replenishedFatigue / 10;
                replenishedFatigue -= replenish;
            } else {
                double rate = Somnia.CONFIG.fatigue.fatigueRate;

                MobEffectInstance wakefulness = player.getEffect(SomniaObjects.AWAKENING_EFFECT);
                if (wakefulness != null) {
                    rate -= wakefulness.getAmplifier() == 0 ? rate / 4 : rate / 3;
                }

                MobEffectInstance insomnia = player.getEffect(SomniaObjects.INSOMNIA_EFFECT);
                if (insomnia != null) {
                    rate += insomnia.getAmplifier() == 0 ? rate / 2 : rate;
                }
                fatigue += rate + props.getExtraFatigueRate();
            }

            if (fatigue > 100) fatigue = 100;
            if (fatigue < 0) fatigue = 0;

            if (replenishedFatigue > 100) replenishedFatigue = 100;
            if (replenishedFatigue < 0) replenishedFatigue = 0;

            if (extraFatigueRate < 0) extraFatigueRate = 0;

            props.setFatigue(fatigue);
            props.setReplenishedFatigue(replenishedFatigue);
            props.setExtraFatigueRate(extraFatigueRate);

            if (props.updateFatigueCounter() >= 100) {
                props.resetFatigueCounter();
                FriendlyByteBuf buf = PacketByteBufs.create();
                buf.writeDouble(fatigue);
                ServerPlayNetworking.send((ServerPlayer) player, NetworkHandler.UPDATE_FATIGUE, buf);

                if (Somnia.CONFIG.fatigue.fatigueSideEffects) {
                    int lastSideEffectStage = props.getSideEffectStage();
                    SideEffectStage[] stages = SideEffectStage.getSideEffectStages();

                    if (fatigue < stages[0].minFatigue()) {
                        props.setSideEffectStage(-1);
                    }

                    for (SideEffectStage stage : stages) {
                        boolean permanent = stage.duration() < 0;

                        if (fatigue >= stage.minFatigue() && fatigue <= stage.maxFatigue()) {
                            props.setSideEffectStage(stage.minFatigue());

                            if (permanent || lastSideEffectStage < stage.minFatigue()) {
                                MobEffect eff = Registry.MOB_EFFECT.get(new ResourceLocation(stage.potionId()));
                                player.addEffect(new MobEffectInstance(eff, permanent ? 150 : stage.duration(), stage.amplifier()));
                            }
                        }
                    }
                }
            }
        }
    }
}
