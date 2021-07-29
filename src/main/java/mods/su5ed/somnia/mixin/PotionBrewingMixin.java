package mods.su5ed.somnia.mixin;

import mods.su5ed.somnia.core.SomniaObjects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.Potions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PotionBrewing.class)
public abstract class PotionBrewingMixin {

    @Shadow
    private static void addMix(Potion potion, Item item, Potion potion2) {
        throw new AssertionError("Shadowing Failed");
    }

    @Inject(at = @At("TAIL"), method = "bootStrap")
    private static void somnia$addPotions(CallbackInfo ci) {
        addMix(Potions.NIGHT_VISION, Items.GLISTERING_MELON_SLICE, SomniaObjects.AWAKENING_POTION);
        addMix(Potions.LONG_NIGHT_VISION, Items.GLISTERING_MELON_SLICE, SomniaObjects.LONG_AWAKENING_POTION);
        addMix(Potions.NIGHT_VISION, Items.BLAZE_POWDER, SomniaObjects.STRONG_AWAKENING_POTION);
        addMix(SomniaObjects.AWAKENING_POTION, Items.FERMENTED_SPIDER_EYE, SomniaObjects.INSOMNIA_POTION);
        addMix(SomniaObjects.LONG_AWAKENING_POTION, Items.FERMENTED_SPIDER_EYE, SomniaObjects.LONG_INSOMNIA_POTION);
        addMix(SomniaObjects.STRONG_AWAKENING_POTION, Items.FERMENTED_SPIDER_EYE, SomniaObjects.STRONG_INSOMNIA_POTION);
    }
}
