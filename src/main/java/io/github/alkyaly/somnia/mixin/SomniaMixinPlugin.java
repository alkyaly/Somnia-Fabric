package io.github.alkyaly.somnia.mixin;

import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class SomniaMixinPlugin implements IMixinConfigPlugin {
    private static final Logger LOGGER = LogManager.getLogger("Somnia|ASM");

    @Override public void onLoad(String mixinPackage) {}
    @Override public String getRefMapperConfig() { return null; }
    @Override public boolean shouldApplyMixin(String targetClassName, String mixinClassName) { return true; }
    @Override public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}
    @Override public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
    @Override public List<String> getMixins() { return null; }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        if ("io.github.alkyaly.somnia.mixin.GameRendererMixin".equals(mixinClassName)) {
            patchRenderLevel(targetClass);
        }
    }

    /*
        Initially, this was a mixin at the HEAD of "renderLevel".
        Then, I realized, it's slow and unperformant to do so.
        Since it's going to create a new CallbackInfo every render frame.
    */
    private static void patchRenderLevel(ClassNode targetClass) {
        String renderLevel = FabricLoader.getInstance().isDevelopmentEnvironment() ? "renderLevel" : "method_3188";

        for (var mtd : targetClass.methods) {
            if (mtd.name.equals(renderLevel)) {
                LOGGER.debug("Patching GameRenderer#renderLevel...");

                AbstractInsnNode head = mtd.instructions.get(0);
                InsnList list = new InsnList();

                list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "io/github/alkyaly/somnia/util/MixinHooks", "skipRenderLevel", "()Z", false));
                list.add(new JumpInsnNode(Opcodes.IFEQ, (LabelNode) head));
                list.add(new InsnNode(Opcodes.RETURN));

                mtd.instructions.insertBefore(head, list);
                return;
            }
        }
    }
}
