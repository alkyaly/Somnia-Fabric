package io.github.alkyaly.somnia.api;

import io.github.alkyaly.somnia.config.ReplenishingItemEntry;
import io.github.alkyaly.somnia.core.Somnia;
import io.github.alkyaly.somnia.mixin.LivingEntityMixin;
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
    private static List<ReplenishingItemEntry> replenishingItems;

    /**
     * @param item               An item that will replenish some amount of fatigue after eaten.
     * @param fatigueToReplenish The amount of fatigue to replenish after consumption.
     */
    public static void addReplenishingItem(Item item, double fatigueToReplenish) {
        addReplenishingItem(item, fatigueToReplenish, Somnia.CONFIG.fatigue.fatigueRate);
    }

    /**
     * @param item                An item that will replenish some amount of fatigue after eaten.
     * @param fatigueToReplenish  The amount of fatigue to replenish after consumption.
     * @param fatigueRateModifier An additional fatigue increasing rate modifier added after consumption.
     * See: {@link LivingEntityMixin#somnia$onFinishUsing(CallbackInfo, InteractionHand, ItemStack)}
     */
    public static void addReplenishingItem(Item item, double fatigueToReplenish, double fatigueRateModifier) {
        addReplenishingItem(Registry.ITEM.getKey(item), fatigueToReplenish, fatigueRateModifier);
    }

    /**
     * @param item                The location of an item that will replenish some amount of fatigue after eaten.
     * @param fatigueToReplenish  The amount of fatigue to replenish after consumption.
     * @param fatigueRateModifier An additional fatigue increasing rate modifier added after consumption.
     * See: {@link LivingEntityMixin#somnia$onFinishUsing(CallbackInfo, InteractionHand, ItemStack)}
     */
    public static void addReplenishingItem(ResourceLocation item, double fatigueToReplenish, double fatigueRateModifier) {
        ReplenishingItemEntry entry = new ReplenishingItemEntry(item.toString(), fatigueToReplenish, fatigueRateModifier);
        update(entry);
        REPLENISHING_ITEMS.add(entry);
    }

    /**
     * @return The public facing list of the replenishing items added by in API.
     */
    public static List<ReplenishingItemEntry> getReplenishingItems() {
        create();
        return replenishingItems;
    }

    //better than creating a list every time someone tries to retrieve the list.
    private static void update(ReplenishingItemEntry entry) {
        create();
        if (entry != null) {
            replenishingItems.add(entry);
        }
    }

    private static void create() {
        if (replenishingItems == null) {
            replenishingItems = new ArrayList<>(REPLENISHING_ITEMS);
        }
    }
}
