package dev.emi.emi.jemi.widget;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.widget.TankWidget;
import dev.emi.emi.jemi.impl.JemiRecipeSlot;
import dev.emi.emi.runtime.EmiDrawContext;
import java.util.List;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;

public class JemiTankWidget extends TankWidget implements JemiSlotPresentation {
   private final JemiRecipeSlot slot;
   private final NativeJemiFluidRenderer<?> renderer;

   public JemiTankWidget(JemiRecipeSlot slot, EmiRecipe recipe) {
      super(slot.stack, slot.x - 1, slot.y - 1, slot.tankInfo.width() + 2, slot.tankInfo.height() + 2, slot.tankInfo.capacity());
      this.slot = slot;
      slot.widget = this;
      if (slot.getRole() == RecipeIngredientRole.OUTPUT) {
         this.recipeContext(recipe);
      }

      this.drawBack(false);
      this.renderer = NativeJemiFluidRenderer.create(slot.tankInfo.capacity(), slot.tankInfo.showCapacity(),
          slot.tankInfo.width(), slot.tankInfo.height(), slot.tankInfo.direction());
   }

   public void extractRenderState(GuiGraphicsExtractor raw, int mouseX, int mouseY, float delta) {
      EmiDrawContext context = EmiDrawContext.wrap(raw);
      if (this.slot.background != null) {
         this.slot.background.drawable().draw(context.raw(), this.x + 1 + this.slot.background.xOff(), this.y + 1 + this.slot.background.yOff());
      }

      super.extractRenderState(context.raw(), mouseX, mouseY, delta);
   }

   @Override
   public dev.emi.emi.api.widget.Bounds getHoverBounds() {
      return slot.hoverPadding.expand(contentBounds());
   }

   @Override public dev.emi.emi.api.widget.Bounds contentBounds() {
      return new dev.emi.emi.api.widget.Bounds(x+1,y+1,slot.tankInfo.width(),slot.tankInfo.height());
   }

   @Override
   public void drawStack(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
      slot.getDisplayedIngredient().ifPresent(typed -> renderer.render(graphics, typed, x + 1, y + 1));
   }

   public void drawOverlay(GuiGraphicsExtractor raw, int mouseX, int mouseY, float delta) {
      EmiDrawContext context = EmiDrawContext.wrap(raw);
      if (this.slot.overlay != null) {
         raw.nextStratum();
         this.slot.overlay.drawable().draw(raw, this.x + 1 + this.slot.overlay.xOff(), this.y + 1 + this.slot.overlay.yOff());
      }

      super.drawOverlay(context.raw(), mouseX, mouseY, delta);
   }

   @Override public void buildTooltip(mezz.jei.api.gui.builder.ITooltipBuilder tooltip) {
      slot.getDisplayedIngredient().ifPresent(typed -> {
          tooltip.setIngredient(typed);
          var base=new dev.emi.emi.jemi.impl.JemiTooltipBuilder();
          renderer.tooltip(base,typed);
          for(var line:base.getLines()) line.ifLeft(tooltip::add).ifRight(tooltip::add);
      });
      if(slot.richTooltipCallback!=null) slot.richTooltipCallback.onRichTooltip(slot,tooltip);
   }
   public List<ClientTooltipComponent> getTooltip(int mouseX,int mouseY) {
      var builder=new dev.emi.emi.jemi.impl.JemiTooltipBuilder(); buildTooltip(builder); return builder.components();
   }
}
