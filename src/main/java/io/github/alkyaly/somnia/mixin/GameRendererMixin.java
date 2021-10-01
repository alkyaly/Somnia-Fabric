package io.github.alkyaly.somnia.mixin;

import io.github.alkyaly.somnia.util.MixinHooks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(at = @At("HEAD"), method = "renderLevel", cancellable = true)
    private void somnia$cancelRenderLevel(CallbackInfo ci) {
        if (MixinHooks.skipRenderLevel(this.minecraft)) {
            ci.cancel();
        }
    }
}
