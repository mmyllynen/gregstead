package dev.emi.emi.jemi.widget;

import com.google.common.collect.Lists;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.jemi.JemiStack;
import dev.emi.emi.jemi.JemiUtil;
import dev.emi.emi.jemi.impl.JemiIngredientAcceptor;
import dev.emi.emi.jemi.impl.JemiRecipeSlot;
import dev.emi.emi.jemi.impl.JemiTooltipBuilder;
import dev.emi.emi.runtime.EmiDrawContext;
import dev.emi.emi.runtime.EmiLog;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;

public class JemiSlotWidget extends SlotWidget implements JemiSlotPresentation {
   public final JemiRecipeSlot slot;

   public JemiSlotWidget(JemiRecipeSlot slot, EmiRecipe recipe) {
      super(slot.stack, slot.x - (slot.large ? 6 : 1), slot.y - (slot.large ? 6 : 1));
      this.slot = slot;
      slot.widget = this;
      if (slot.getRole() == RecipeIngredientRole.OUTPUT) {
         this.recipeContext(recipe);
      }

      this.drawBack(slot.defaultBackground);
      IIngredientRenderer<?> renderer = this.getRenderer();
      if (renderer != null) {
         this.customBackground(null, 0, 0, renderer.getWidth() + 2, renderer.getHeight() + 2);
      } else if (slot.large) {
         this.large(true);
      }
   }

   @Override public Bounds contentBounds() {
      Bounds drawn=getBounds();
      var renderer=getRenderer();
      int width=renderer==null?16:renderer.getWidth(), height=renderer==null?16:renderer.getHeight();
      return new Bounds(drawn.x()+(drawn.width()-width)/2,drawn.y()+(drawn.height()-height)/2,width,height);
   }
   @Override public Bounds getHoverBounds() { return slot.hoverPadding.expand(contentBounds()); }

   private ITypedIngredient<?> getIngredient() { return slot.getDisplayedIngredient().orElse(null); }

   private IIngredientRenderer<?> getRenderer() {
      ITypedIngredient<?> typed = this.getIngredient();
      return typed != null && this.slot.renderers != null && this.slot.renderers.containsKey(typed.getType()) ? this.slot.renderers.get(typed.getType()).renderer() : null;
   }

   public void extractRenderState(GuiGraphicsExtractor raw, int mouseX, int mouseY, float delta) {
      if (this.slot.background != null) {
         this.slot.background.drawable().draw(raw, contentBounds().x() + this.slot.background.xOff(), contentBounds().y() + this.slot.background.yOff());
      }

      super.extractRenderState(raw, mouseX, mouseY, delta);
   }

   public void drawStack(GuiGraphicsExtractor raw, int mouseX, int mouseY, float delta) {
      EmiDrawContext context = EmiDrawContext.wrap(raw);
      IIngredientRenderer renderer = this.getRenderer();
      if (renderer != null) {
         ITypedIngredient<?> typed = this.getIngredient();
         Bounds bounds = this.getBounds();
         int xOff = bounds.x() + (bounds.width() - 16) / 2 + (16 - renderer.getWidth()) / 2;
         int yOff = bounds.y() + (bounds.height() - 16) / 2 + (16 - renderer.getHeight()) / 2;
         renderCustom(this.slot.renderers.get(typed.getType()), typed, raw, xOff, yOff);
      } else {
         slot.getDisplayedIngredient().ifPresent(typed -> {
             var selected = JemiUtil.getStack(typed).copy().setAmount(getStack().getAmount()).setChance(getStack().getChance());
             var bounds = getBounds();
             selected.render(raw,bounds.x()+(bounds.width()-16)/2,bounds.y()+(bounds.height()-16)/2,delta);
         });
      }
   }

   public void drawOverlay(GuiGraphicsExtractor raw, int mouseX, int mouseY, float delta) {
      EmiDrawContext context = EmiDrawContext.wrap(raw);
      if (this.slot.overlay != null) {
         raw.nextStratum();
         this.slot.overlay.drawable().draw(raw, contentBounds().x() + this.slot.overlay.xOff(), contentBounds().y() + this.slot.overlay.yOff());
      }

      super.drawOverlay(context.raw(), mouseX, mouseY, delta);
   }

   private static <T> void renderCustom(JemiRecipeSlot.IngredientRenderer<T> renderer,
           ITypedIngredient<?> typed, GuiGraphicsExtractor graphics, int x, int y) {
      typed.getIngredient(renderer.type()).ifPresent(value -> renderer.renderer().render(graphics,value,x,y));
   }

   @SuppressWarnings("unchecked")
   private static <T> JemiStack<T> withRenderer(JemiStack<T> stack, IIngredientRenderer<?> renderer) {
      // The renderer came from this slot's map keyed by the displayed ingredient's exact JEI type.
      JemiStack<T> copy = stack.copy();
      copy.renderer = (IIngredientRenderer<T>)renderer;
      return copy;
   }

   public static void addTooltip(List<ClientTooltipComponent> list, JemiRecipeSlot slot, EmiIngredient stack, IIngredientRenderer<?> renderer) {
      if (renderer != null && stack.getEmiStacks().size() == 1 && stack.getEmiStacks().get(0) instanceof JemiStack js) {
         stack = withRenderer(js, renderer);
      }

      list.addAll(stack.getTooltip());
      if (slot.richTooltipCallback != null) {
         try {
            JemiTooltipBuilder tooltipBuilder = new JemiTooltipBuilder();
            slot.richTooltipCallback.onRichTooltip(slot, tooltipBuilder);
            list.addAll(tooltipBuilder.components());
         } catch (Exception e) {
            EmiLog.error("Error initializing JEI TooltipBuilder", e);
         }
      }
   }

   @Override public void buildTooltip(mezz.jei.api.gui.builder.ITooltipBuilder tooltip) {
      slot.getDisplayedIngredient().ifPresent(typed -> addNativeTooltip(tooltip,typed));
      if(slot.richTooltipCallback!=null) slot.richTooltipCallback.onRichTooltip(slot,tooltip);
   }
   private <T> void addNativeTooltip(mezz.jei.api.gui.builder.ITooltipBuilder tooltip, ITypedIngredient<T> typed) {
      var renderer=dev.emi.emi.jemi.JemiPlugin.runtime.getIngredientManager().getIngredientRenderer(typed.getType());
      if(slot.renderers!=null && slot.renderers.containsKey(typed.getType())) {
          @SuppressWarnings("unchecked")
          IIngredientRenderer<T> custom=(IIngredientRenderer<T>)slot.renderers.get(typed.getType()).renderer();
          renderer=custom;
      }
      NativeSlotTooltip.add(tooltip,typed,renderer);
   }
   public List<ClientTooltipComponent> getTooltip(int mouseX,int mouseY) {
      var builder=new JemiTooltipBuilder(); buildTooltip(builder);
      var result=builder.components(); addSlotTooltip(result); return result;
   }
}
