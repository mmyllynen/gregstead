package com.gregtechceu.gtceu.client.model.machine;
import com.mojang.serialization.MapCodec;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.*;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.resources.model.*;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.renderer.block.dispatch.*;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.*;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import org.joml.*;
import java.util.*;
/** Native item geometry and captured machine display submissions share the resolved item transforms. */
public final class NativeMachineItemModel implements ItemModel {
    private final ItemModel base;
    private final ModelRenderProperties properties;
    private final Matrix4fc transform;
    private final UnbakedMachineModel machine;
    public NativeMachineItemModel(ItemModel base,ModelRenderProperties properties,Matrix4fc transform,UnbakedMachineModel machine){
        this.base=base;this.properties=properties;this.transform=new Matrix4f(transform);this.machine=machine;
    }
    @Override public void update(ItemStackRenderState output,ItemStack stack,ItemModelResolver resolver,ItemDisplayContext context,ClientLevel level,ItemOwner owner,int seed){
        base.update(output,stack,resolver,context,level,owner,seed);
        var captures=new ArrayList<NativeMachineRenderSubmission>();
        for(var render:machine.getDynamicRenders())render.extractItem(stack,context).ifPresent(captures::add);
        NativeItemDisplayLayer.append(output,stack,context,properties,transform,captures,this);
    }
    public record Unbaked(Identifier base) implements ItemModel.Unbaked {
        public static final MapCodec<Unbaked> CODEC=Identifier.CODEC.fieldOf("base").xmap(Unbaked::new,Unbaked::base);
        public MapCodec<? extends ItemModel.Unbaked> type(){return CODEC;}
        public void resolveDependencies(ResolvableModel.Resolver resolver){resolver.markDependency(base);}
        public ItemModel bake(ItemModel.BakingContext context,Matrix4fc transformation){
            var baker=context.blockModelBaker();var resolved=baker.getModel(base);
            var machine=UnbakedMachineModel.find(resolved);
            if(machine==null)return net.minecraft.client.data.models.model.ItemModelUtils.plainModel(base).bake(context,transformation);
            var geometry=machine.bakeDynamic(baker,BlockModelRotation.IDENTITY,resolved);
            var parts=new ArrayList<BlockStateModelPart>();geometry.collectParts(RandomSource.create(42),parts);
            var quads=new QuadCollection.Builder();
            for(var part:parts){part.getQuads(null).forEach(quads::addUnculledFace);for(var side:Direction.values())part.getQuads(side).forEach(quads::addUnculledFace);}
            var properties=new ModelRenderProperties(resolved.getTopGuiLight().lightLikeBlock(), geometry.particleMaterial(), resolved.getTopTransforms());
            var item=new CuboidItemModelWrapper(List.of(),quads.build(),properties,transformation);
            return new NativeMachineItemModel(item,properties,transformation,machine);
        }
    }
}
