package io.github.alkyaly.somnia.mixin.accessor;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionBrewing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(PotionBrewing.class)
public interface PotionBrewingAccessor {
    @Invoker("addMix")
    static void somnia$invokeAddMix(Potion potion, Item item, Potion potion2) {
        throw new AssertionError("Invoker did not apply!");
    }
}
