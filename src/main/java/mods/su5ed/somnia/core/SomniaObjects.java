package mods.su5ed.somnia.core;

import mods.su5ed.somnia.object.AwakeningEffect;
import mods.su5ed.somnia.object.InsomniaEffect;
import net.minecraft.core.Registry;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;

public class SomniaObjects {
    public static final MobEffect AWAKENING_EFFECT = new AwakeningEffect();
    public static final MobEffect INSOMNIA_EFFECT = new InsomniaEffect();
    public static final Potion AWAKENING_POTION = new Potion("awakening", new MobEffectInstance(AWAKENING_EFFECT, 2400));
    public static final Potion LONG_AWAKENING_POTION = new Potion("long_awakening", new MobEffectInstance(AWAKENING_EFFECT, 3600));
    public static final Potion STRONG_AWAKENING_POTION = new Potion("strong_awakening", new MobEffectInstance(AWAKENING_EFFECT, 2400, 1));

    public static final Potion INSOMNIA_POTION = new Potion("insomnia", new MobEffectInstance(INSOMNIA_EFFECT, 1800));
    public static final Potion LONG_INSOMNIA_POTION = new Potion("long_insomnia", new MobEffectInstance(INSOMNIA_EFFECT, 3000));
    public static final Potion STRONG_INSOMNIA_POTION = new Potion("strong_insomnia", new MobEffectInstance(INSOMNIA_EFFECT, 1800, 1));

    public static void init() {
        registerEffect("awakening", AWAKENING_EFFECT);
        registerEffect("insomnia", INSOMNIA_EFFECT);
        registerPotion(AWAKENING_POTION);
        registerPotion(LONG_AWAKENING_POTION);
        registerPotion(STRONG_AWAKENING_POTION);
        registerPotion(INSOMNIA_POTION);
        registerPotion(LONG_INSOMNIA_POTION);
        registerPotion(STRONG_INSOMNIA_POTION);
    }

    private static void registerPotion(Potion potion) {
        Registry.register(Registry.POTION, Somnia.locate(potion.getName("")), potion);
    }

    private static void registerEffect(String id, MobEffect effect) {
        Registry.register(Registry.MOB_EFFECT, Somnia.locate(id), effect);
    }
}
