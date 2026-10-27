package com.lowdragmc.lowdraglib.core.mixins.accessor;

import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import java.util.Map;

@Mixin(ModelBakery.class)
public interface ModelBakeryAccessor {
    @Accessor("unbakedBlockStateModels") Map<net.minecraft.world.level.block.state.BlockState, net.minecraft.client.renderer.block.dispatch.BlockStateModel.UnbakedRoot> getUnbakedBlockStateModels();
    @Accessor("clientInfos") Map<Identifier,net.minecraft.client.renderer.item.ClientItem> getClientInfos();
    @Accessor("resolvedModels") Map<Identifier,ResolvedModel> getResolvedModels();
    @Accessor("missingModel") ResolvedModel getMissingModel();
}
