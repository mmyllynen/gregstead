package dev.emi.emi.screen;

import com.lowdragmc.lowdraglib.transfer.mixin.GuiItemStateAccess;
import java.util.ArrayList;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.item.TrackingItemStackRenderState;
import net.minecraft.client.renderer.state.gui.GuiItemRenderState;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3x2f;

/** Cached native model states; native GuiRenderer owns atlas batching, lighting and sprite lifetime. */
public final class NativeItemBatch {
    private record Entry(ItemStack stack,Optional<TrackingItemStackRenderState> model,boolean cacheable) {}
    private static final java.util.concurrent.atomic.AtomicLong generation=new java.util.concurrent.atomic.AtomicLong();
    private long seenGeneration=-1;
    private final ArrayList<Entry> entries=new ArrayList<>();
    private int cursor;
    private GuiGraphicsExtractor frame;
    private boolean populated;
    private int captures;
    public static void invalidateAll() { generation.incrementAndGet(); }
    public int captures() { return captures; }
    public boolean populated() { return populated; }
    public void invalidate() { entries.clear();populated=false; }
    public void begin() {
        if(seenGeneration!=generation.get()) {invalidate();seenGeneration=generation.get();}
        cursor=0;frame=null;
    }
    public void icon(GuiGraphicsExtractor graphics,ItemStack stack,int x,int y) {
        Entry entry=cursor<entries.size()?entries.get(cursor):null;
        if(entry==null || !entry.cacheable() || !ItemStack.matches(entry.stack(),stack) || entry.model().map(TrackingItemStackRenderState::isAnimated).orElse(false)) {
            var state=new GuiRenderState();var extraction=new GuiGraphicsExtractor(Minecraft.getInstance(),state,0,0);
            extraction.fakeItem(stack,0,0);
            var items=new ArrayList<GuiItemRenderState>();state.forEachItem(items::add);
            var modelId=stack.get(net.minecraft.core.component.DataComponents.ITEM_MODEL);
            var root=modelId==null?null:Minecraft.getInstance().getModelManager().getItemModel(modelId);
            boolean cacheable=root!=null && root.getClass()==net.minecraft.client.renderer.item.CuboidItemModelWrapper.class
                && ((com.lowdragmc.lowdraglib.transfer.mixin.ItemModelTintAccess)root).gttransfercompat$tints().isEmpty();
            entry=new Entry(stack.copy(),items.isEmpty()?Optional.empty():Optional.of(items.getFirst().itemStackRenderState()),cacheable);
            captures++;
            if(cursor<entries.size())entries.set(cursor,entry);else entries.add(entry);
        }
        cursor++;frame=graphics;
        Entry current=entry;
        current.model().ifPresent(model -> ((GuiItemStateAccess)graphics).gttransfercompat$itemState().addItem(
            new GuiItemRenderState(new Matrix3x2f(graphics.pose()),model,x,y,graphics.peekScissorStack())));
        graphics.itemDecorations(Minecraft.getInstance().font,stack,x,y,"");
    }
    public void finish() {
        if(cursor<entries.size())entries.subList(cursor,entries.size()).clear();
        if(frame!=null)frame.nextStratum();
        populated=true;frame=null;
    }
}
