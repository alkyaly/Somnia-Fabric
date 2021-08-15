package mods.su5ed.somnia.mixin;

import mods.su5ed.somnia.api.SomniaAPI;
import mods.su5ed.somnia.api.capability.Components;
import mods.su5ed.somnia.api.capability.Fatigue;
import mods.su5ed.somnia.config.ReplenishingItemEntry;
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

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

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
        boolean eat = stack.getUseAnimation() == UseAnim.DRINK || stack.getUseAnimation() == UseAnim.EAT;

        //noinspection ConstantConditions, InstanceofThis
        if (eat && (Object) this instanceof Player player) {
            for (ReplenishingItemEntry entry : Somnia.CONFIG.fatigue.replenishingItems) {
                if (Registry.ITEM.get(new ResourceLocation(entry.item())) == item) {
                    somnia$setAttributesFromEntry(player, entry);
                    return;
                }
            }

            for (ReplenishingItemEntry entry : SomniaAPI.getReplenishingItems()) {
                if (Registry.ITEM.get(new ResourceLocation(entry.item())) == item) {
                    somnia$setAttributesFromEntry(player, entry);
                    return;
                }
            }
        }
    }

    private void somnia$setAttributesFromEntry(Player player, ReplenishingItemEntry entry) {
        Fatigue props = Components.get(player);
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
    }
}
