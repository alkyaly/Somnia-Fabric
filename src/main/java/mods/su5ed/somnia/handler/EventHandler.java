package mods.su5ed.somnia.handler;

import mods.su5ed.somnia.api.capability.Components;
import mods.su5ed.somnia.api.capability.IFatigue;
import mods.su5ed.somnia.compat.Compat;
import mods.su5ed.somnia.core.Somnia;
import mods.su5ed.somnia.core.SomniaCommand;
import mods.su5ed.somnia.core.SomniaObjects;
import mods.su5ed.somnia.mixin.accessor.ServerPlayerAccessor;
import mods.su5ed.somnia.network.NetworkHandler;
import mods.su5ed.somnia.util.MixinHooks;
import mods.su5ed.somnia.util.SideEffectStage;
import mods.su5ed.somnia.util.SomniaUtil;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.util.TriState;
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
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import top.theillusivec4.somnus.api.PlayerSleepEvents;

import java.util.Iterator;

public final class EventHandler {

    public static void init() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> SomniaCommand.register(dispatcher));
        ServerTickEvents.END_SERVER_TICK.register(EventHandler::onEndTick);
        ServerWorldEvents.LOAD.register(EventHandler::levelLoadHook);
        ServerWorldEvents.UNLOAD.register(EventHandler::levelUnloadHook);
        ServerPlayConnectionEvents.JOIN.register(EventHandler::syncFatigue);
        PlayerSleepEvents.TRY_SLEEP.register(EventHandler::onPlayerSleepInBed);
        PlayerSleepEvents.CAN_SLEEP_NOW.register(EventHandler::onSleepingTimeCheck);
        PlayerSleepEvents.WAKE_UP.register(EventHandler::onWakeUp);
        UseBlockCallback.EVENT.register(EventHandler::onRightClickBlock);
    }

    //Forge: TickEvent.ServerTickEvent on ForgeEventHandler
    private static void onEndTick(MinecraftServer server) {
        ServerTickHandler.HANDLERS.forEach(ServerTickHandler::tickEnd);
    }

    //Forge: PlayerSleepInBedEvent on ForgeEventHandler
    private static Player.BedSleepingProblem onPlayerSleepInBed(ServerPlayer player, BlockPos pos) {
        if (!SomniaUtil.checkFatigue(player)) {
            player.displayClientMessage(new TranslatableComponent("somnia.status.cooldown"), true);
            return Player.BedSleepingProblem.OTHER_PROBLEM;
        } else if (!Somnia.CONFIG.options.sleepWithArmor && !player.isCreative() && SomniaUtil.doesPlayerWearArmor(player)) {
            player.displayClientMessage(new TranslatableComponent("somnia.status.armor"), true);
            return Player.BedSleepingProblem.OTHER_PROBLEM;
        }

        IFatigue props = Components.get(player);
        if (props != null) {
            props.setSleepNormally(player.isShiftKeyDown());
        }

        if (Compat.isSleepingInBag(player)) MixinHooks.updateWakeTime(player);

        return null;
    }

    //Forge: SleepTimeCheckEvent on ForgeEventHandler
    public static TriState onSleepingTimeCheck(Player player, BlockPos pos) {
        //if (ModList.get().isLoaded("darkutils") && DarkUtilsPlugin.hasSleepCharm(player)) return; darkutils is not on fabric
        IFatigue props = Components.get(player);

        if (props != null) {
            if (props.shouldSleepNormally()) {
                return TriState.DEFAULT;
            }
        }

        if (SomniaUtil.isEnterSleepTime()) return TriState.TRUE;

        return TriState.DEFAULT;
    }

    //Forge: PlayerInteractEvent.RightClickBlock on ForgeEventHandler
    public static InteractionResult onRightClickBlock(Player player, Level level, InteractionHand hand, BlockHitResult bhr) {
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
    private static void onWakeUp(Player player, boolean reset, boolean update) {
        IFatigue props = Components.get(player);

        if (props != null) {                //DarkUtils is not on fabric
            if (props.shouldSleepNormally() /*|| (ModList.get().isLoaded("darkutils") && DarkUtilsPlugin.hasSleepCharm(player))*/) {
                props.setFatigue(props.getFatigue() - SomniaUtil.getFatigueToReplenish(player));
            }
            props.maxFatigueCounter();
            props.shouldResetSpawn(true);
            props.setSleepNormally(false);
            props.setWakeTime(-1);
        }
    }

    //Forge: WorldEvent.Load on ForgeEventHandler
    public static void levelLoadHook(MinecraftServer server, ServerLevel level) {
        ServerTickHandler.HANDLERS.add(new ServerTickHandler(level));
        Somnia.LOGGER.info("Registering tick handler for level: {}", level.dimension().location().toString());
    }

    //Forge: WorldEvent.Unload on ForgeEventHandler
    public static void levelUnloadHook(MinecraftServer server, ServerLevel level) {
        Iterator<ServerTickHandler> iter = ServerTickHandler.HANDLERS.iterator();
        ServerTickHandler serverTickHandler;
        while (iter.hasNext()) {
            serverTickHandler = iter.next();
            if (serverTickHandler.levelServer == level) {
                Somnia.LOGGER.info("Removing tick handler for level: {}", level.dimension().location().toString());
                iter.remove();
                break;
            }
        }
    }

    public static void syncFatigue(ServerGamePacketListenerImpl handler, PacketSender sender, MinecraftServer server) {
        Components.FATIGUE.sync(handler.player);
    }

    public static void tickPlayer(Player player) {
        if (player.level.isClientSide || (!player.isAlive() || player.isCreative() || player.isSpectator() && !player.isSleeping())) {
            return;
        }

        IFatigue props = Components.get(player);

        if (props != null) {
            double fatigue = props.getFatigue();
            double extraFatigueRate = props.getExtraFatigueRate();
            double replenishedFatigue = props.getReplenishedFatigue();
            boolean isSleeping = props.sleepOverride() || player.isSleeping();

            if (isSleeping) {
                fatigue -= Somnia.CONFIG.fatigue.fatigueReplenishRate;
                double share = Somnia.CONFIG.fatigue.fatigueReplenishRate / Somnia.CONFIG.fatigue.fatigueRate;
                double replenish = Somnia.CONFIG.fatigue.fatigueReplenishRate * share;
                extraFatigueRate -= Somnia.CONFIG.fatigue.fatigueReplenishRate / share / replenishedFatigue / 10;
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
                ServerPlayNetworking.send((ServerPlayer) player, NetworkHandler.UPDATE_FATIGUE, buf);

                if (Somnia.CONFIG.fatigue.fatigueSideEffects) {
                    int lastSideEffectStage = props.getSideEffectStage();
                    SideEffectStage[] stages = SideEffectStage.getSideEffectStages();
                    SideEffectStage firstStage = stages[0];
                    if (fatigue < firstStage.minFatigue()) {
                        props.setSideEffectStage(-1);
                        for (SideEffectStage stage : stages) {
                            if (lastSideEffectStage < stage.minFatigue()) {
                                player.removeEffect(Registry.MOB_EFFECT.get(new ResourceLocation(stage.potionId())));
                            }
                        }
                    }

                    for (int i = 0; i < Somnia.CONFIG.fatigue.sideEffectStages.size(); i++) {
                        SideEffectStage stage = stages[i];
                        boolean permanent = stage.duration() < 0;
                        if (fatigue >= stage.minFatigue() && fatigue <= stage.maxFatigue() && (permanent || lastSideEffectStage < stage.minFatigue())) {
                            if (!permanent) props.setSideEffectStage(stage.minFatigue());
                            player.addEffect(new MobEffectInstance(Registry.MOB_EFFECT.get(new ResourceLocation(stage.potionId())), permanent ? 150 : stage.duration(), stage.amplifier()));
                        }
                    }
                }
            }
        }
    }
}
