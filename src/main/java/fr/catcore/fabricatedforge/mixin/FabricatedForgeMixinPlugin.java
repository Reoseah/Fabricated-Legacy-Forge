package fr.catcore.fabricatedforge.mixin;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Objects;
import java.util.Set;

public class FabricatedForgeMixinPlugin implements IMixinConfigPlugin {
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
        switch (targetClassName) {
            case "net.minecraft.class_582":
            case "net.minecraft.class_583":
            case "net.minecraft.class_585":
            case "net.minecraft.class_586":
            case "net.minecraft.class_587":
            case "net.minecraft.class_588":
            case "net.minecraft.class_589":
            case "net.minecraft.class_590":
                targetClass.superName = "cpw/mods/fml/client/FMLTextureFX";
                break;
            default:
                break;
        }
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        switch (targetClassName) {
            case "net.minecraft.class_582":
            case "net.minecraft.class_583":
            case "net.minecraft.class_585":
            case "net.minecraft.class_586":
            case "net.minecraft.class_587":
            case "net.minecraft.class_588":
            case "net.minecraft.class_589":
            case "net.minecraft.class_590":
                for (MethodNode node : targetClass.methods) {
                    if (Objects.equals(node.name, "<init>")) {
                        int invSpe = Opcodes.INVOKESPECIAL;
                        for(AbstractInsnNode insNode : node.instructions) {
                            if (insNode instanceof MethodInsnNode && insNode.getOpcode() == invSpe) {
                                MethodInsnNode mTheNode = (MethodInsnNode) insNode;
                                if (Objects.equals(mTheNode.owner, "net/minecraft/class_584")) mTheNode.owner = "cpw/mods/fml/client/FMLTextureFX";
                            }
                        }
                    }
                }
                break;
            default:
                break;
        }
    }
}