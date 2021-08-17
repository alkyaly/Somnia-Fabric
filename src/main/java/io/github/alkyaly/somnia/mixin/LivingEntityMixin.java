package io.github.alkyaly.somnia.mixin;

import io.github.alkyaly.somnia.api.SomniaAPI;
import io.github.alkyaly.somnia.config.ReplenishingItemEntry;
import io.github.alkyaly.somnia.core.Somnia;
import net.minecraft.core.Registry;
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
        //We can't check against food properties, most "drinks" don't add one.
        boolean eat = stack.getUseAnimation() == UseAnim.DRINK || stack.getUseAnimation() == UseAnim.EAT;

        //noinspection ConstantConditions, InstanceofThis
        if (eat && (Object) this instanceof Player player) {
            for (ReplenishingItemEntry entry : Somnia.CONFIG.fatigue.replenishingItems) {
                if (Registry.ITEM.getKey(item).toString().equals(entry.item())) {
                    SomniaAPI.modifyAttributesFromEntry(player, entry);
                    return;
                }
            }

            for (ReplenishingItemEntry entry : SomniaAPI.getReplenishingItems()) {
                if (Registry.ITEM.getKey(item).toString().equals(entry.item())) {
                    SomniaAPI.modifyAttributesFromEntry(player, entry);
                    return;
                }
            }
        }
    }
}
