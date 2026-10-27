package com.lowdragmc.lowdraglib.core.mixins;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/**
 * @author KilaBash
 * @date 2023/2/11
 * @implNote LDLibMixinPlugin
 */
public class LDLibMixinPlugin implements IMixinConfigPlugin, MixinPluginShared {
    @Override
    public void onLoad(String mixinPackage) {

    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinClassName.contains("com.lowdragmc.lowdraglib.core.mixins.jei")) {
            return IS_JEI_LOAD;
        } else if (mixinClassName.contains("com.lowdragmc.lowdraglib.core.mixins.rei")) {
            return IS_REI_LOAD;
        } else if (mixinClassName.contains("com.lowdragmc.lowdraglib.core.mixins.emi")) {
            return IS_MEI_LOAD;
        }
        if (mixinClassName.contains("com.lowdragmc.lowdraglib.core.mixins.kjs"))
            return MixinPluginShared.isClassFound("dev.latvian.mods.kubejs.server.ServerScriptManager");
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

    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        if (!(mixinClassName.endsWith("ServerTextureMembersMixin") ||
                mixinClassName.endsWith("ServerTextureInterfaceMembersMixin") ||
                mixinClassName.endsWith("ServerWidgetMembersMixin")) ||
                net.neoforged.fml.loading.FMLEnvironment.getDist() != net.neoforged.api.distmarker.Dist.DEDICATED_SERVER) return;
        var removed = new java.util.HashSet<String>();
        for (var method : targetClass.methods) {
            if (clientOnly(method.visibleAnnotations) || clientOnly(method.invisibleAnnotations))
                removed.add(method.name + method.desc);
        }
        // Lambdas belonging solely to stripped render methods also carry client-only signatures.
        boolean expanded;
        do {
            expanded = false;
            for (var method : targetClass.methods) {
                if (!removed.contains(method.name + method.desc)) continue;
                for (var instruction : method.instructions) {
                    if (!(instruction instanceof org.objectweb.asm.tree.InvokeDynamicInsnNode dynamic)) continue;
                    for (var argument : dynamic.bsmArgs) {
                        if (argument instanceof org.objectweb.asm.Handle handle && handle.getOwner().equals(targetClass.name)
                                && handle.getName().startsWith("lambda$"))
                            expanded |= removed.add(handle.getName() + handle.getDesc());
                    }
                }
            }
        } while (expanded);
        targetClass.methods.removeIf(method -> removed.contains(method.name + method.desc));
        targetClass.fields.removeIf(field -> clientOnly(field.visibleAnnotations) || clientOnly(field.invisibleAnnotations));
    }

    private static boolean clientOnly(java.util.List<org.objectweb.asm.tree.AnnotationNode> annotations) {
        if (annotations == null) return false;
        for (var annotation : annotations) {
            if (!annotation.desc.equals("Lnet/neoforged/api/distmarker/OnlyIn;") || annotation.values == null) continue;
            for (int i=0; i+1<annotation.values.size(); i+=2) {
                var value = annotation.values.get(i+1);
                if (annotation.values.get(i).equals("value") && value instanceof String[] enumValue
                        && enumValue.length==2 && enumValue[1].equals("CLIENT")) return true;
            }
        }
        return false;
    }
}
