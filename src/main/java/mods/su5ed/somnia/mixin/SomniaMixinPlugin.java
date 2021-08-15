package mods.su5ed.somnia.mixin;

import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class SomniaMixinPlugin implements IMixinConfigPlugin {
    private static final Logger LOGGER = LogManager.getLogger("Somnia|ASM");

    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    //todo: Soon this will not be needed: https://github.com/FabricMC/fabric/pull/1633
    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        if ("mods.su5ed.somnia.mixin.ServerPlayerMixin".equals(mixinClassName)) {
            // I couldn't get MappingResolver to work.
            String startSleepInBed = FabricLoader.getInstance().isDevelopmentEnvironment() ? "startSleepInBed" : "method_7269";

            //POV: You're having fun.
            for (var mtd : targetClass.methods) {
                if (mtd.name.equals(startSleepInBed)) {
                    for (int i = 0; i < mtd.instructions.size(); i++) {
                        AbstractInsnNode node = mtd.instructions.get(i);

                        if (node instanceof MethodInsnNode methodNode && node.getOpcode() == Opcodes.INVOKEINTERFACE
                                && "java/util/List".equals(methodNode.owner) && "isEmpty".equals(methodNode.name)) {
                            if (mtd.instructions.get(i + 1) instanceof JumpInsnNode jump) {
                                LOGGER.debug("Modifying check for monsters in ServerPlayer#startSleepInBed...");
                                InsnList list = new InsnList();

                                list.add(new LabelNode());
                                list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "mods/su5ed/somnia/util/MixinHooks", "ignoreMonsters", "()Z", false));
                                list.add(new JumpInsnNode(Opcodes.IFNE, jump.label));

                                mtd.instructions.insert(jump, list);
                            }
                            return;
                        }
                    }
                }
            }
        }
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}
