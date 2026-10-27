package dev.emi.emi.recipe;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.item.*;
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.renderer.state.gui.GuiItemRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemDisplayContext;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.standalone.*;
import com.lowdragmc.lowdraglib.transfer.mixin.GuiItemStateAccess;
import org.joml.Matrix4f;
import org.joml.Matrix3x2f;

public final class NativeTagModels {
    private static final ConcurrentHashMap<Identifier, StandaloneModelKey<ItemModel>> KEYS=new ConcurrentHashMap<>();
    private NativeTagModels() {}
    public static StandaloneModelKey<ItemModel> key(Identifier id) {
        return KEYS.computeIfAbsent(id,value->new StandaloneModelKey<>(value::toString));
    }
    public static void register(ModelEvent.RegisterStandalone event, Identifier id) {
        event.register(key(id),new SimpleUnbakedStandaloneModel<>(id,(model,baker,name)->{
            var slots=model.getTopTextureSlots();
            var quads=model.bakeTopGeometry(slots,baker,BlockModelRotation.IDENTITY);
            return new CuboidItemModelWrapper(List.of(),quads,ModelRenderProperties.fromResolvedModel(baker,model,slots),new Matrix4f());
        }));
    }
    public static ItemModel get(Identifier id) {return Minecraft.getInstance().getModelManager().getStandaloneModel(key(id));}
    public static void draw(GuiGraphicsExtractor graphics,ItemModel model,int x,int y) {
        var client=Minecraft.getInstance();
        var state=new TrackingItemStackRenderState();
        model.update(state,ItemStack.EMPTY,client.getItemModelResolver(),ItemDisplayContext.GUI,client.level,null,0);
        if(!state.isEmpty()) ((GuiItemStateAccess)graphics).gttransfercompat$itemState().addItem(
            new GuiItemRenderState(new Matrix3x2f(graphics.pose()),state,x,y,graphics.peekScissorStack()));
    }
}
