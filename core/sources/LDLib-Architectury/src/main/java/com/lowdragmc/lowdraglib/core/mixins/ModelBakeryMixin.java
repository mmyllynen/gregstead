package com.lowdragmc.lowdraglib.core.mixins;
import com.lowdragmc.lowdraglib.client.renderer.NativeItemModelProvider;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import net.minecraft.client.resources.model.ModelDiscovery;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Missing resource models are expected for items whose renderer supplies native geometry. */
@Mixin(ModelDiscovery.class)
public abstract class ModelBakeryMixin {
    @WrapOperation(method="lambda$new$0",at=@At(value="INVOKE",target="Lorg/slf4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;)V",remap=false),require=1)
    private void ldlib$missingRendererModel(Logger logger,String message,Object argument,Operation<Void> original) {
        if(argument instanceof Identifier id) {
            if(id.getPath().startsWith("block/")) id=id.withPath(id.getPath().substring(6));
            else if(id.getPath().startsWith("item/")) id=id.withPath(id.getPath().substring(5));
            if(BuiltInRegistries.ITEM.getValue(id) instanceof NativeItemModelProvider)return;
        }
        original.call(logger,message,argument);
    }
}
