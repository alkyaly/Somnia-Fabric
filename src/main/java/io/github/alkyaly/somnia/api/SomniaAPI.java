package io.github.alkyaly.somnia.api;

import io.github.alkyaly.somnia.api.capability.Components;
import io.github.alkyaly.somnia.api.capability.Fatigue;
import io.github.alkyaly.somnia.config.ReplenishingItemEntry;
import io.github.alkyaly.somnia.core.Somnia;
import io.github.alkyaly.somnia.mixin.LivingEntityMixin;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public final class SomniaAPI {
    private static final List<ReplenishingItemEntry> REPLENISHING_ITEMS = new ArrayList<>();

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
     *                            See: {@link LivingEntityMixin#somnia$onFinishUsing(CallbackInfo, InteractionHand, ItemStack)}
     */
    public static void addReplenishingItem(Item item, double fatigueToReplenish, double fatigueRateModifier) {
        addReplenishingItem(Registry.ITEM.getKey(item), fatigueToReplenish, fatigueRateModifier);
    }

    /**
     * @param item                The location of an item that will replenish some amount of fatigue after eaten.
     * @param fatigueToReplenish  The amount of fatigue to replenish after consumption.
     * @param fatigueRateModifier An additional fatigue increasing rate modifier added after consumption.
     */
    public static void addReplenishingItem(ResourceLocation item, double fatigueToReplenish, double fatigueRateModifier) {
        ReplenishingItemEntry entry = new ReplenishingItemEntry(item.toString(), fatigueToReplenish, fatigueRateModifier);
        REPLENISHING_ITEMS.add(entry);
    }

    /**
     * @return The list of the replenishing items added in the API.
     */
    public static List<ReplenishingItemEntry> getReplenishingItems() {
        return REPLENISHING_ITEMS;
    }

    /**
     * @param player The player to retrieve and modify the fatigue component from.
     * @param entry  A {@link ReplenishingItemEntry replenishing item entry} that will modify the {@link Player player} fatigue component.
     * @see LivingEntityMixin#somnia$onFinishUsing(CallbackInfo, InteractionHand, ItemStack)
     */
    public static void modifyAttributesFromEntry(Player player, ReplenishingItemEntry entry) {
        Fatigue props = Components.get(player);
        if (props != null) {
            double fatigue = props.getFatigue();
            double fatigueToReplenish = Math.min(fatigue, entry.fatigueToReplenish());
            double newFatigue = props.getReplenishedFatigue() + fatigueToReplenish;
            props.setReplenishedFatigue(newFatigue);

            double multiplier = newFatigue * 4 * Somnia.CONFIG.fatigue.fatigueRate;
            props.setExtraFatigueRate(props.getExtraFatigueRate() + entry.fatigueRateModifier() * multiplier);
            props.setFatigue(fatigue - fatigueToReplenish);
            props.maxFatigueCounter();
        }
    }
}
