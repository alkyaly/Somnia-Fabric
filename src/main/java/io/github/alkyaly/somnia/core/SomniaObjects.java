package io.github.alkyaly.somnia.core;

import io.github.alkyaly.somnia.object.ExtendedMobEffect;
import net.minecraft.core.Registry;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;

import static io.github.alkyaly.somnia.mixin.accessor.PotionBrewingAccessor.somnia$invokeAddMix;

public final class SomniaObjects {
    public static final MobEffect AWAKENING_EFFECT = new ExtendedMobEffect(MobEffectCategory.BENEFICIAL, 0x00ffee);
    public static final MobEffect INSOMNIA_EFFECT = new ExtendedMobEffect(MobEffectCategory.HARMFUL, 0x23009a);
    public static final Potion AWAKENING_POTION = new Potion(new MobEffectInstance(AWAKENING_EFFECT, 2400));
    public static final Potion LONG_AWAKENING_POTION = new Potion("awakening", new MobEffectInstance(AWAKENING_EFFECT, 3600));
    public static final Potion STRONG_AWAKENING_POTION = new Potion("awakening", new MobEffectInstance(AWAKENING_EFFECT, 2400, 1));

    public static final Potion INSOMNIA_POTION = new Potion(new MobEffectInstance(INSOMNIA_EFFECT, 1800));
    public static final Potion LONG_INSOMNIA_POTION = new Potion("insomnia", new MobEffectInstance(INSOMNIA_EFFECT, 3000));
    public static final Potion STRONG_INSOMNIA_POTION = new Potion("insomnia", new MobEffectInstance(INSOMNIA_EFFECT, 1800, 1));

    public static void init() {
        registerEffect("awakening", AWAKENING_EFFECT);
        registerEffect("insomnia", INSOMNIA_EFFECT);

        registerPotion("awakening", AWAKENING_POTION);
        registerPotion("long_awakening", LONG_AWAKENING_POTION);
        registerPotion("strong_awakening", STRONG_AWAKENING_POTION);
        registerPotion("insomnia", INSOMNIA_POTION);
        registerPotion("long_insomnia", LONG_INSOMNIA_POTION);
        registerPotion("strong_insomnia", STRONG_INSOMNIA_POTION);
    }

    private static void registerPotion(String id, Potion potion) {
        Registry.register(Registry.POTION, Somnia.locate(id), potion);
    }

    private static void registerEffect(String id, MobEffect effect) {
        Registry.register(Registry.MOB_EFFECT, Somnia.locate(id), effect);
    }

    static void setupPotions() {
        somnia$invokeAddMix(Potions.NIGHT_VISION, Items.GLISTERING_MELON_SLICE, AWAKENING_POTION);
        somnia$invokeAddMix(Potions.LONG_NIGHT_VISION, Items.GLISTERING_MELON_SLICE, LONG_AWAKENING_POTION);
        somnia$invokeAddMix(Potions.NIGHT_VISION, Items.BLAZE_POWDER, STRONG_AWAKENING_POTION);
        somnia$invokeAddMix(AWAKENING_POTION, Items.FERMENTED_SPIDER_EYE, INSOMNIA_POTION);
        somnia$invokeAddMix(LONG_AWAKENING_POTION, Items.FERMENTED_SPIDER_EYE, LONG_INSOMNIA_POTION);
        somnia$invokeAddMix(STRONG_AWAKENING_POTION, Items.FERMENTED_SPIDER_EYE, STRONG_INSOMNIA_POTION);
    }
}
