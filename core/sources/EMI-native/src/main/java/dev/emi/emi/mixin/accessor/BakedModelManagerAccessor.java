package dev.emi.emi.mixin.accessor;

import net.minecraft.client.resources.model.ModelManager;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ModelManager.class)
public interface BakedModelManagerAccessor {
    @Accessor("bakedStandaloneModels") StandaloneModelLoader.BakedModels getModels();
}
