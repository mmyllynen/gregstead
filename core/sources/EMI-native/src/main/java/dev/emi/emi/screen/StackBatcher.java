package dev.emi.emi.screen;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.config.EmiConfig;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;

/** Native item-state cache feeding Minecraft's atlas-backed GUI batch renderer. */
public class StackBatcher {
    public interface Batchable {
        ItemStack batchItem();
        boolean isUnbatchable();
        void setUnbatchable();
    }
    private final NativeItemBatch items=new NativeItemBatch();
    public static boolean isEnabled() { return EmiConfig.useBatchedRenderer; }
    public boolean isPopulated() { return items.populated(); }
    public void repopulate() { items.invalidate(); }
    public void begin() { items.begin(); }
    public void render(Batchable batchable,GuiGraphicsExtractor draw,int x,int y,float delta) {
        dev.emi.emi.runtime.NativeGuiTint.extract(draw, () -> items.icon(draw,batchable.batchItem(),x,y));
    }
    public void render(EmiIngredient stack,GuiGraphicsExtractor draw,int x,int y,float delta) {
        render(stack,draw,x,y,delta,-1^EmiIngredient.RENDER_AMOUNT);
    }
    public void render(EmiIngredient stack,GuiGraphicsExtractor draw,int x,int y,float delta,int flags) {
        dev.emi.emi.runtime.NativeGuiTint.extract(draw, () -> renderTinted(stack,draw,x,y,delta,flags));
    }
    private void renderTinted(EmiIngredient stack,GuiGraphicsExtractor draw,int x,int y,float delta,int flags) {
        if(isEnabled() && stack instanceof Batchable batchable && !batchable.isUnbatchable() && (flags&EmiIngredient.RENDER_ICON)!=0) {
            try {
                items.icon(draw,batchable.batchItem(),x,y);
                stack.render(draw,x,y,delta,flags&~EmiIngredient.RENDER_ICON);
            } catch(RuntimeException failure) {
                batchable.setUnbatchable();
                dev.emi.emi.runtime.EmiLog.error("Native item batch extraction failed",failure);
                stack.render(draw,x,y,delta,flags);
            }
        } else stack.render(draw,x,y,delta,flags);
    }
    public void draw() { items.finish(); }
    public static class ClaimedCollection {
        private final java.util.Set<StackBatcher> claimed=new java.util.HashSet<>();
        private final java.util.ArrayList<StackBatcher> available=new java.util.ArrayList<>();
        public synchronized StackBatcher claim() {
            var batch=available.isEmpty()?new StackBatcher():available.removeLast();
            batch.repopulate();claimed.add(batch);return batch;
        }
        public synchronized void unclaim(StackBatcher batch) {
            if(claimed.remove(batch)) {batch.repopulate();available.add(batch);}
        }
        public synchronized void unclaimAll() {
            claimed.forEach(StackBatcher::repopulate);available.addAll(claimed);claimed.clear();
        }
    }
}
