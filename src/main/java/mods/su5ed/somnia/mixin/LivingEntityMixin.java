package mods.su5ed.somnia.mixin;

import mods.su5ed.somnia.api.SomniaAPI;
import mods.su5ed.somnia.api.capability.Components;
import mods.su5ed.somnia.api.capability.IFatigue;
import mods.su5ed.somnia.core.Somnia;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Collection;
import java.util.stream.Stream;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    //No similar event in fabric api
    @Inject(
            at = @At(
                    value = "INVOKE_ASSIGN",
                    target = "Lnet/minecraft/world/item/ItemStack;finishUsingItem(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/world/item/ItemStack;"
            ),
            method = "completeUsingItem",
            locals = LocalCapture.CAPTURE_FAILHARD
    ) // Forge: LivingEntityUseItemEvent.Finish on ForgeEventHandler
    private void somnia$onFinishUsing(CallbackInfo ci, InteractionHand result, ItemStack stack) {
        Item item = stack.getItem();
        //todo: What if someone wants to add a non-drink item, like a coffee pie? Change the first check.
        //noinspection ConstantConditions
        if (stack.getUseAnimation() == UseAnim.DRINK && (Object) this instanceof Player player) {
            Stream.of(Somnia.CONFIG.fatigue.replenishingItems, SomniaAPI.getReplenishingItems())
                    .flatMap(Collection::stream)
                    .filter(entry -> Registry.ITEM.get(new ResourceLocation(entry.item())) == item)
                    .findFirst()
                    .ifPresent(entry -> {
                        IFatigue props = Components.get(player);

                        if (props != null) {
                            double fatigue = props.getFatigue();
                            double replenishedFatigue = props.getReplenishedFatigue();
                            double coffeeFatigueReplenish = entry.fatigueToReplenish();
                            double fatigueToReplenish = Math.min(fatigue, coffeeFatigueReplenish);
                            double newFatigue = replenishedFatigue + fatigueToReplenish;
                            props.setReplenishedFatigue(newFatigue);

                            double baseMultiplier = entry.fatigueRateModifier();
                            double multiplier = newFatigue * 4 * Somnia.CONFIG.fatigue.fatigueRate;
                            props.setExtraFatigueRate(props.getExtraFatigueRate() + baseMultiplier * multiplier);
                            props.setFatigue(fatigue - fatigueToReplenish);
                            props.maxFatigueCounter();
                        }
                    });
        }
    }
}
