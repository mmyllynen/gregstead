package com.gregtechceu.gtceu.client.model.item;
import com.gregtechceu.gtceu.common.data.item.GTDataComponents;
import com.gregtechceu.gtceu.common.item.LampBlockItem;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.renderer.item.*;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.*;
import org.joml.Matrix4fc;
/** Native lamp item selection is driven by its persisted lamp component. */
public record NativeLampItemModel(ItemModel off,ItemModel lit,ItemModel bloom) implements ItemModel {
    public void update(ItemStackRenderState output,ItemStack stack,ItemModelResolver resolver,ItemDisplayContext context,ClientLevel level,ItemOwner owner,int seed){
        var data=stack.getOrDefault(GTDataComponents.LAMP_DATA,LampBlockItem.LampData.EMPTY);
        ItemModel chosen=NativeLampItemSelection.select(data.lit(),data.bloom(),off,lit,bloom);
        chosen.update(output,stack,resolver,context,level,owner,seed);
    }
    public record Unbaked(Identifier base) implements ItemModel.Unbaked {
        public static final MapCodec<Unbaked> CODEC=Identifier.CODEC.fieldOf("base").xmap(Unbaked::new,Unbaked::base);
        public MapCodec<? extends ItemModel.Unbaked> type(){return CODEC;}
        public void resolveDependencies(ResolvableModel.Resolver resolver){resolver.markDependency(base);resolver.markDependency(base.withSuffix("_off"));resolver.markDependency(base.withSuffix("_bloom"));}
        public ItemModel bake(ItemModel.BakingContext context,Matrix4fc transform){
            return new NativeLampItemModel(net.minecraft.client.data.models.model.ItemModelUtils.plainModel(base.withSuffix("_off")).bake(context,transform),
                    net.minecraft.client.data.models.model.ItemModelUtils.plainModel(base).bake(context,transform),
                    net.minecraft.client.data.models.model.ItemModelUtils.plainModel(base.withSuffix("_bloom")).bake(context,transform));
        }
    }
}
