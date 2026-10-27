package com.gregtechceu.gtceu.client.model.item;
import com.gregtechceu.gtceu.client.renderer.cover.FacadeCoverRenderer;
import com.gregtechceu.gtceu.client.util.FacadeBlockAndTintGetter;
import com.gregtechceu.gtceu.common.item.behavior.FacadeItemBehaviour;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.item.*;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.model.*;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.*;
import net.minecraft.util.RandomSource;
import net.neoforged.neoforge.model.data.ModelData;
import org.joml.*;
import java.util.*;
/** Facade item geometry derives from the component blockstate using current native models. */
public final class NativeFacadeItemModel implements ItemModel {
    private final ItemModel fallback;private final ModelRenderProperties properties;private final Matrix4fc transform;
    private NativeFacadeItemModel(ItemModel fallback,ModelRenderProperties properties,Matrix4fc transform){this.fallback=fallback;this.properties=properties;this.transform=new Matrix4f(transform);}
    public void update(ItemStackRenderState output,ItemStack stack,ItemModelResolver resolver,ItemDisplayContext context,ClientLevel level,ItemOwner owner,int seed){
        var facade=FacadeItemBehaviour.getFacadeStateNullable(stack);
        if(facade==null || level==null){fallback.update(output,stack,resolver,context,level,owner,seed);return;}
        var world=new FacadeBlockAndTintGetter(level,BlockPos.ZERO,facade,null);
        var quads=new QuadCollection.Builder();
        FacadeCoverRenderer.INSTANCE.getFacadeQuads(facade,world,RandomSource.create(seed),ModelData.EMPTY,null).forEach(quads::addUnculledFace);
        var blockModel=Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(facade);
        var facadeProperties=new ModelRenderProperties(properties.usesBlockLight(),blockModel.particleMaterial(world,BlockPos.ZERO,facade),properties.transforms());
        new CuboidItemModelWrapper(List.of(),quads.build(),facadeProperties,transform).update(output,stack,resolver,context,level,owner,seed);
    }
    public record Unbaked(Identifier base) implements ItemModel.Unbaked {
        public static final MapCodec<Unbaked> CODEC=Identifier.CODEC.fieldOf("base").xmap(Unbaked::new,Unbaked::base);
        public MapCodec<? extends ItemModel.Unbaked> type(){return CODEC;}
        public void resolveDependencies(ResolvableModel.Resolver resolver){resolver.markDependency(base);}
        public ItemModel bake(ItemModel.BakingContext context,Matrix4fc transform){
            var resolved=context.blockModelBaker().getModel(base);
            return new NativeFacadeItemModel(net.minecraft.client.data.models.model.ItemModelUtils.plainModel(base).bake(context,transform),
                    ModelRenderProperties.fromResolvedModel(context.blockModelBaker(),resolved,resolved.getTopTextureSlots()),transform);
        }
    }
}
