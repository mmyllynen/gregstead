package com.gregtechceu.gtceu.integration.ae2.utils;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import com.lowdragmc.lowdraglib.transfer.persistence.INBTSerializable;

import appeng.api.networking.IGridNodeListener;
import appeng.me.ManagedGridNode;
import org.jetbrains.annotations.NotNull;

public class SerializableManagedGridNode extends ManagedGridNode implements INBTSerializable<CompoundTag> {

    public <T> SerializableManagedGridNode(T nodeOwner, IGridNodeListener<? super T> listener) {
        super(nodeOwner, listener);
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.@NotNull Provider provider) {
        return NativeAEStorageNbt.write(provider, output -> super.serialize(output));
    }

    @Override
    public void deserializeNBT(HolderLookup.@NotNull Provider provider, @NotNull CompoundTag tag) {
        NativeAEStorageNbt.read(provider, tag, input -> { super.deserialize(input); return null; });
    }
}
