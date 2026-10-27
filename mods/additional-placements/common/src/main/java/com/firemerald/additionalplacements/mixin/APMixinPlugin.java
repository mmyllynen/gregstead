package com.firemerald.additionalplacements.mixin;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class APMixinPlugin implements IMixinConfigPlugin {
    @Override
    public void onLoad(String mixinPackage) {}

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinClassName.endsWith("MixinStructureTransform") || mixinClassName.endsWith("MixinWrapperBlockStateModel")) {
            try { return org.spongepowered.asm.service.MixinService.getService().getBytecodeProvider().getClassNode(targetClassName) != null; }
            catch (Exception ignored) { return false; }
        }
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}

    @Override
    public List<String> getMixins() {
        return getPlatformMixins();
    }

    
    public static List<String> getPlatformMixins() { return com.firemerald.additionalplacements.mixin.neoforge.APMixinPluginImpl.getPlatformMixins(); }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
}
