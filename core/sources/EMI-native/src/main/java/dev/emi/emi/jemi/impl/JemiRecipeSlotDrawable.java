package dev.emi.emi.jemi.impl;

import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.jemi.widget.JemiSlotPresentation;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import mezz.jei.api.gui.builder.IIngredientAcceptor;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;

public class JemiRecipeSlotDrawable implements IRecipeSlotDrawable {
    public final SlotWidget widget;
    private final JemiRecipeSlot slot;
    private final JemiSlotPresentation presentation;
    public JemiRecipeSlotDrawable(JemiRecipeSlot slot) {
        this.slot=slot;
        widget=java.util.Objects.requireNonNull(slot.widget,"Slot must have a widget before creating its drawable");
        presentation=(JemiSlotPresentation)widget;
    }
    public JemiRecipeSlot slotView() { return slot; }
    public Stream<ITypedIngredient<?>> getAllIngredients() { return slot.getAllIngredients(); }
    public List<ITypedIngredient<?>> getAllIngredientsList() { return slot.getAllIngredientsList(); }
    public Optional<ITypedIngredient<?>> getDisplayedIngredient() { return slot.getDisplayedIngredient(); }
    public Stream<ITypedIngredient<?>> getDisplayedIngredients() { return slot.getDisplayedIngredients(); }
    public Optional<net.minecraft.tags.TagKey<?>> getTagKey() { return slot.getTagKey(); }
    public RecipeIngredientRole getRole() { return slot.getRole(); }
    public Optional<String> getSlotName() { return slot.getSlotName(); }
    public void drawHighlight(GuiGraphicsExtractor graphics,int color) { slot.drawHighlight(graphics,color); }
    public void draw(GuiGraphicsExtractor graphics) { draw(graphics,false); }
    public void draw(GuiGraphicsExtractor graphics,boolean hovered) {
        var bounds=presentation.contentBounds();
        widget.extractRenderState(graphics,hovered?bounds.x():Integer.MIN_VALUE,hovered?bounds.y():Integer.MIN_VALUE,0);
    }
    public void drawHoverOverlays(GuiGraphicsExtractor graphics) {
        var bounds=presentation.contentBounds();
        graphics.blitSprite(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED,
            mezz.jei.common.platform.Services.PLATFORM.getScreenHelper().getSlotHighlightFrontSprite(),
            bounds.x()-4,bounds.y()-4,bounds.width()+8,bounds.height()+8);
    }
    public List<Component> getTooltip() {
        var tooltip=new mezz.jei.common.gui.JeiTooltip();
        getTooltip(tooltip); return tooltip.getLegacyComponents();
    }
    public void getTooltip(ITooltipBuilder tooltip) { presentation.buildTooltip(tooltip); }
    public void drawTooltip(GuiGraphicsExtractor graphics,int x,int y) {
        var tooltip=new mezz.jei.common.gui.JeiTooltip();
        getTooltip(tooltip); tooltip.draw(graphics,x,y);
    }
    public boolean isMouseOver(double x,double y) {
        var bounds=widget.getHoverBounds();
        return x>=bounds.left() && x<bounds.right() && y>=bounds.top() && y<bounds.bottom();
    }
    public void setPosition(int x,int y) {
        var content=presentation.contentBounds(); var drawn=widget.getBounds();
        widget.setPosition(drawn.x()+x-content.x(),drawn.y()+y-content.y());
    }
    public IIngredientAcceptor<?> createDisplayOverrides() { return slot.choices.overrideDisplay(); }
    public void clearDisplayOverrides() { slot.choices.clearOverrides(); }
    public Rect2i getRect() {
        var bounds=presentation.contentBounds();
        return new Rect2i(bounds.x(),bounds.y(),bounds.width(),bounds.height());
    }
    public Rect2i getAreaIncludingBackground() {
        var area=slot.defaultBackground?widget.getBounds():presentation.contentBounds();
        int left=area.left(),top=area.top(),right=area.right(),bottom=area.bottom();
        if(slot.background!=null) {
            var content=presentation.contentBounds();
            int x=content.x()+slot.background.xOff(),y=content.y()+slot.background.yOff();
            left=Math.min(left,x); top=Math.min(top,y);
            right=Math.max(right,x+slot.background.drawable().getWidth());
            bottom=Math.max(bottom,y+slot.background.drawable().getHeight());
        }
        return new Rect2i(left,top,right-left,bottom-top);
    }
}
