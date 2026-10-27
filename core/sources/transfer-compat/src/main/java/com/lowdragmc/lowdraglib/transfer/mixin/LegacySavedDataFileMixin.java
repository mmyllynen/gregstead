package com.lowdragmc.lowdraglib.transfer.mixin;

import com.lowdragmc.lowdraglib.transfer.persistence.LegacySavedDataTypes;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.SavedDataStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import java.nio.file.Path;

@Mixin(SavedDataStorage.class)
public abstract class LegacySavedDataFileMixin {
    @WrapOperation(method = "readSavedData", at = @At(value = "INVOKE", target =
            "Lnet/minecraft/world/level/storage/SavedDataStorage;getDataFile(Lnet/minecraft/resources/Identifier;)Ljava/nio/file/Path;"), require = 1)
    private Path gtport$legacyRead(SavedDataStorage storage, Identifier id, Operation<Path> original) {
        return LegacySavedDataTypes.readableFile(id, original.call(storage, id));
    }
}
