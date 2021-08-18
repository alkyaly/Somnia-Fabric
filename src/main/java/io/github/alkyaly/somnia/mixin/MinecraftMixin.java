package io.github.alkyaly.somnia.mixin;

import io.github.alkyaly.somnia.handler.ClientTickHandler;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/toasts/ToastComponent;render(Lcom/mojang/blaze3d/vertex/PoseStack;)V"), method = "runTick")
    private void somnia$postRenderTick(CallbackInfo ci) {
        ClientTickHandler.INSTANCE.onRenderTick();
    }
}