package io.github.alkyaly.somnia.util;

import io.github.alkyaly.somnia.config.SideEffectStageEntry;
import io.github.alkyaly.somnia.core.Somnia;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.language.I18n;

public record SideEffectStage(int minFatigue, int maxFatigue, String potionId, int duration, int amplifier) {
    private static SideEffectStage[] stages;

    public static SideEffectStage[] getSideEffectStages() {
        if (stages == null) {
            stages = new SideEffectStage[Somnia.CONFIG.fatigue.sideEffectStages.size()];

            for (int i = 0; i < stages.length; i++) {
                stages[i] = parseStage(Somnia.CONFIG.fatigue.sideEffectStages.get(i));
            }
        }

        return stages;
    }

    private static SideEffectStage parseStage(SideEffectStageEntry stage) {
        return new SideEffectStage(stage.minFatigue(), stage.maxFatigue(), stage.potionId(), stage.duration(), stage.amplifier());
    }

    @Environment(EnvType.CLIENT)
    public static String getSideEffectStageDescription(double fatigue) {
        int stage = getForFatigue(fatigue);
        float ratio = Somnia.CONFIG.fatigue.sideEffectStages.size() / 4F;
        int desc = Math.round(stage / ratio);
        return I18n.get("somnia.side_effect." + desc);
    }

    public static int getForFatigue(double fatigue) {
        for (int i = 0; i < Somnia.CONFIG.fatigue.sideEffectStages.size(); i++) {
            SideEffectStage stage = getSideEffectStages()[i];
            if (fatigue >= stage.minFatigue && fatigue <= stage.maxFatigue && (stage.duration >= 0 || i == Somnia.CONFIG.fatigue.sideEffectStages.size() - 1))
                return i + 1;
        }
        return 0;
    }
}
