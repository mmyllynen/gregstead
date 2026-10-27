package com.lowdragmc.lowdraglib.core.mixins;

import com.lowdragmc.lowdraglib.syncdata.blockentity.IAsyncAutoSyncBlockEntity;
import com.lowdragmc.lowdraglib.syncdata.blockentity.IAutoPersistBlockEntity;
import com.lowdragmc.lowdraglib.syncdata.blockentity.IAutoSyncBlockEntity;
import com.lowdragmc.lowdraglib.syncdata.blockentity.IManagedBlockEntity;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author KilaBash
 * @date 2022/11/27
 * @implNote BlockEntityMixin
 */
@Mixin(BlockEntity.class)
public abstract class BlockEntityMixin {

    @org.spongepowered.asm.mixin.Unique
    private static final ThreadLocal<HolderLookup.Provider> ldlib$saveLookup = new ThreadLocal<>();
    @com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod(method = "saveCustomOnly(Lnet/minecraft/core/HolderLookup$Provider;)Lnet/minecraft/nbt/CompoundTag;")
    private CompoundTag ldlib$saveCustomOnly(HolderLookup.Provider provider,
            com.llamalad7.mixinextras.injector.wrapoperation.Operation<CompoundTag> original) {
        var previous = ldlib$saveLookup.get();
        ldlib$saveLookup.set(provider);
        try { return original.call(provider); }
        finally { if(previous==null)ldlib$saveLookup.remove();else ldlib$saveLookup.set(previous); }
    }
    @com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod(method = "saveWithoutMetadata(Lnet/minecraft/core/HolderLookup$Provider;)Lnet/minecraft/nbt/CompoundTag;")
    private CompoundTag ldlib$saveWithoutMetadata(HolderLookup.Provider provider,
            com.llamalad7.mixinextras.injector.wrapoperation.Operation<CompoundTag> original) {
        var previous = ldlib$saveLookup.get();
        ldlib$saveLookup.set(provider);
        try { return original.call(provider); }
        finally { if(previous==null)ldlib$saveLookup.remove();else ldlib$saveLookup.set(previous); }
    }
    @com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod(method = "saveWithFullMetadata(Lnet/minecraft/core/HolderLookup$Provider;)Lnet/minecraft/nbt/CompoundTag;")
    private CompoundTag ldlib$saveWithFullMetadata(HolderLookup.Provider provider,
            com.llamalad7.mixinextras.injector.wrapoperation.Operation<CompoundTag> original) {
        var previous = ldlib$saveLookup.get();
        ldlib$saveLookup.set(provider);
        try { return original.call(provider); }
        finally { if(previous==null)ldlib$saveLookup.remove();else ldlib$saveLookup.set(previous); }
    }

    @Inject(method = "getUpdateTag", at = @At(value = "RETURN"))
    private void injectGetUpdateTag(HolderLookup.Provider provider, CallbackInfoReturnable<CompoundTag> cir) {
        if (this instanceof IAutoSyncBlockEntity autoSyncBlockEntity) {
            var tag = cir.getReturnValue();
            tag.put(autoSyncBlockEntity.getSyncTag(), autoSyncBlockEntity.serializeInitialData());
        }
    }

    @Inject(method = "saveAdditional", at = @At(value = "RETURN"))
    private void injectSaveAdditional(net.minecraft.world.level.storage.ValueOutput output, CallbackInfo ci) {
        if (this instanceof IAutoPersistBlockEntity autoPersistBlockEntity) {
            var tag = new CompoundTag();
            var entity = (BlockEntity)(Object)this;
            var provider = ldlib$saveLookup.get();
            if (provider == null) provider = entity.getLevel() == null ? com.lowdragmc.lowdraglib.Platform.getFrozenRegistry() : entity.getLevel().registryAccess();
            autoPersistBlockEntity.saveManagedPersistentData(tag, provider, false);
            output.store(com.mojang.serialization.MapCodec.assumeMapUnsafe(CompoundTag.CODEC), tag);
        }
    }

    @Inject(method = "loadAdditional", at = @At(value = "RETURN"))
    private void injectLoad(net.minecraft.world.level.storage.ValueInput input, CallbackInfo ci) {
        var pTag = input.read(com.mojang.serialization.MapCodec.assumeMapUnsafe(CompoundTag.CODEC)).orElseGet(CompoundTag::new);
        var provider = input.lookup();
        if (this instanceof IAutoSyncBlockEntity autoSyncBlockEntity && pTag.get(autoSyncBlockEntity.getSyncTag()) instanceof CompoundTag tag) {
            autoSyncBlockEntity.deserializeInitialData(tag);
        } else if (this instanceof IAutoPersistBlockEntity autoPersistBlockEntity) {
            autoPersistBlockEntity.loadManagedPersistentData(pTag, provider);
        }
    }

    @Inject(method = "setRemoved", at = @At(value = "RETURN"))
    private void injectSetRemoved(CallbackInfo ci) {
        if (this instanceof IAsyncAutoSyncBlockEntity autoSyncBlockEntity) {
            autoSyncBlockEntity.onInValid();
        }
    }

    @Inject(method = "clearRemoved", at = @At(value = "RETURN"))
    private void injectClearRemoved(CallbackInfo ci) {
        if (this instanceof IManagedBlockEntity managed) {
            managed.getRootStorage().requireInit();
            if (managed instanceof IAsyncAutoSyncBlockEntity autoSyncBlockEntity) {
                autoSyncBlockEntity.onValid();
            }
        }
    }

}
