package mods.su5ed.somnia.mixin;

import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class SomniaMixinPlugin implements IMixinConfigPlugin {
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

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        if ("mods.su5ed.somnia.mixin.ServerPlayerMixin".equals(mixinClassName)) {
            var resolver = FabricLoader.getInstance().getMappingResolver();
            String startSleepInBed = resolver.mapMethodName("named", "net.minecraft.server.level.ServerPlayer", "startSleepInBed", "(Lnet/minecraft/core/BlockPos;)Lcom/mojang/datafixers/util/Either;");

            //POV: You're having fun.
            for (var mtd : targetClass.methods) {
                if (mtd.name.equals(startSleepInBed)) {
                    for (int i = 0; i < mtd.instructions.size(); i++) {
                        AbstractInsnNode node = mtd.instructions.get(i);

                        if (node instanceof MethodInsnNode methodNode && node.getOpcode() == Opcodes.INVOKEINTERFACE
                                && "java/util/List".equals(methodNode.owner) && "isEmpty".equals(methodNode.name)) {
                            JumpInsnNode jump = (JumpInsnNode) mtd.instructions.get(i + 1);
                            InsnList list = new InsnList();
                            LabelNode label = new LabelNode();

                            list.add(label);
                            list.add(new FieldInsnNode(Opcodes.GETSTATIC, "mods/su5ed/somnia/core/Somnia", "CONFIG", "Lmods/su5ed/somnia/config/SomniaConfig;"));
                            list.add(new FieldInsnNode(Opcodes.GETFIELD, "mods/su5ed/somnia/core/SomniaConfig", "options", "Lmods/su5ed/somnia/config/SomniaConfig$Options;"));
                            list.add(new FieldInsnNode(Opcodes.GETFIELD, "mods/su5ed/somnia/config/SomniaConfig$Options", "ignoreMonsters", "Z"));
                            list.add(new JumpInsnNode(Opcodes.IFNE, jump.label));
                            mtd.instructions.insert(jump, list);
                        }
                    }
                    return;
                }
            }
        }
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}
