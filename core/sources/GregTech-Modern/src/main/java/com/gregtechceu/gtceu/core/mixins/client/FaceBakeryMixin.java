package com.gregtechceu.gtceu.core.mixins.client;

import com.gregtechceu.gtceu.core.IGTBakedQuad;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.cuboid.CuboidFace;
import net.minecraft.client.resources.model.cuboid.FaceBakery;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import org.joml.Vector3fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value=FaceBakery.class,priority=1500)
public class FaceBakeryMixin {
    @ModifyReturnValue(method="bakeQuad(Lnet/minecraft/client/resources/model/ModelBaker;Lorg/joml/Vector3fc;Lorg/joml/Vector3fc;Lnet/minecraft/client/resources/model/cuboid/CuboidFace;Lnet/minecraft/client/resources/model/sprite/Material$Baked;Lnet/minecraft/core/Direction;Lnet/minecraft/client/renderer/block/dispatch/ModelState;Lnet/minecraft/client/resources/model/cuboid/CuboidRotation;Lnet/minecraft/core/Direction;I)Lnet/minecraft/client/resources/model/geometry/BakedQuad;",
            at=@At("RETURN"),require=1)
    private static BakedQuad gtceu$addQuadTextureKey(BakedQuad quad,ModelBaker baker,Vector3fc from,Vector3fc to,CuboidFace face) {
        quad=com.gregtechceu.gtceu.client.util.GTQuadTransformers.normalizeLegacyTint(quad);
        ((IGTBakedQuad)(Object)quad).gtceu$setTextureKey(face.texture());return quad;
    }
}
