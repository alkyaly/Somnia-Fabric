package mods.su5ed.somnia.api;

import com.google.common.collect.ImmutableList;
import mods.su5ed.somnia.config.ReplenishingItemEntry;
import mods.su5ed.somnia.core.Somnia;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public final class SomniaAPI {
    private static final List<ReplenishingItemEntry> REPLENISHING_ITEMS = new ArrayList<>();

    /**
     * @param item An item that will replenish some amount of fatigue after eaten.
     * @param fatigueToReplenish The amount of fatigue to replenish after consumption.
     */
    public static void addReplenishingItem(Item item, double fatigueToReplenish) {
        addReplenishingItem(item, fatigueToReplenish, Somnia.CONFIG.fatigue.fatigueRate);
    }

    /**
     * @param item An item that will replenish some amount of fatigue after eaten.
     * @param fatigueToReplenish The amount of fatigue to replenish after consumption.
     * @param fatigueRateModifier An additional fatigue increasing rate modifier added after consumption. See: {@link mods.su5ed.somnia.mixin.LivingEntityMixin#somnia$onFinishUsing(CallbackInfo, InteractionHand, ItemStack)}
     */
    public static void addReplenishingItem(Item item, double fatigueToReplenish, double fatigueRateModifier) {
        addReplenishingItem(Registry.ITEM.getKey(item), fatigueToReplenish, fatigueRateModifier);
    }

    /**
     * @param item The location of an item that will replenish some amount of fatigue after eaten.
     * @param fatigueToReplenish The amount of fatigue to replenish after consumption.
     * @param fatigueRateModifier An additional fatigue increasing rate modifier added after consumption. See: {@link mods.su5ed.somnia.mixin.LivingEntityMixin#somnia$onFinishUsing(CallbackInfo, InteractionHand, ItemStack)}
     */
    public static void addReplenishingItem(ResourceLocation item, double fatigueToReplenish, double fatigueRateModifier) {
        REPLENISHING_ITEMS.add(new ReplenishingItemEntry(item.toString(), fatigueToReplenish, fatigueRateModifier));
    }

    /**
     * @return An immutable copy of the replenishing items.
     */
    public static List<ReplenishingItemEntry> getReplenishingItems() {
        return ImmutableList.copyOf(REPLENISHING_ITEMS);
    }
}
