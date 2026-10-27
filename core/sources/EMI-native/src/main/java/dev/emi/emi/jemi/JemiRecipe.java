package dev.emi.emi.jemi;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.InputConstants.Type;
import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiUtil;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.Widget;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.emi.emi.jemi.impl.JemiIngredientAcceptor;
import dev.emi.emi.jemi.impl.JemiRecipeLayoutBuilder;
import dev.emi.emi.jemi.impl.JemiRecipeSlot;
import dev.emi.emi.jemi.impl.JemiRecipeSlotBuilder;
import dev.emi.emi.jemi.impl.JemiTooltipBuilder;
import dev.emi.emi.jemi.impl.extras.JemiRecipeExtrasBuilder;
import dev.emi.emi.jemi.impl.extras.JemiWidgetBuilder;
import dev.emi.emi.jemi.widget.JemiSlotWidget;
import dev.emi.emi.jemi.widget.JemiTankWidget;
import dev.emi.emi.runtime.EmiDrawContext;
import dev.emi.emi.runtime.EmiLog;
import dev.emi.emi.screen.EmiScreenManager;
import java.util.List;
import java.util.Optional;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.library.focus.FocusGroup;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.Nullable;

public class JemiRecipe<T> implements EmiRecipe {
   public List<EmiIngredient> inputs = Lists.newArrayList();
   public List<EmiIngredient> catalysts = Lists.newArrayList();
   public List<EmiStack> outputs = Lists.newArrayList();
   public EmiRecipeCategory recipeCategory;
   public Identifier originalId;
   public Identifier id;
   public IRecipeCategory<T> category;
   public T recipe;
   public boolean allowTree = true;

   public JemiRecipe(EmiRecipeCategory recipeCategory, IRecipeCategory<T> category, T recipe) {
      this.recipeCategory = recipeCategory;
      this.category = category;
      this.recipe = recipe;
      this.originalId = category.getRegistryName(recipe);
      if (this.originalId != null) {
         this.id = EmiPort.id("jei", "/" + EmiUtil.subId(category.getRecipeType().getUid()) + "/" + EmiUtil.subId(this.originalId));
      }

      JemiRecipeLayoutBuilder builder = new JemiRecipeLayoutBuilder();
      category.setRecipe(builder, recipe, JemiPlugin.runtime.getJeiHelpers().getFocusFactory().getEmptyFocusGroup());

      for (JemiRecipeSlotBuilder jrsb : builder.slots) {
         jrsb.acceptor.coerceStacks(jrsb.richTooltipCallback, jrsb.renderers);
      }

      for (JemiIngredientAcceptor acceptor : builder.ingredients) {
         EmiIngredient stack = acceptor.build();
         if (acceptor.role == RecipeIngredientRole.INPUT) {
            this.inputs.add(stack);
         } else if (acceptor.role == RecipeIngredientRole.CRAFTING_STATION) {
            this.catalysts.add(stack);
         } else if (acceptor.role == RecipeIngredientRole.OUTPUT) {
            if (stack.getEmiStacks().size() > 1) {
               this.allowTree = false;
            }

            this.outputs.addAll(stack.getEmiStacks());
         }
      }
   }

   public EmiRecipeCategory getCategory() {
      return this.recipeCategory;
   }

   @Nullable
   public RecipeHolder<?> getBackingRecipe() {
      return EmiPort.getRecipe(this.originalId);
   }

   @Nullable
   public Identifier getId() {
      return this.id;
   }

   public List<EmiIngredient> getInputs() {
      return this.inputs;
   }

   public List<EmiIngredient> getCatalysts() {
      return this.catalysts;
   }

   public List<EmiStack> getOutputs() {
      return this.outputs;
   }

   public int getDisplayWidth() {
      return this.category.getWidth();
   }

   public int getDisplayHeight() {
      return this.category.getHeight();
   }

   public boolean supportsRecipeTree() {
      return this.allowTree && EmiRecipe.super.supportsRecipeTree();
   }

   public void addWidgets(WidgetHolder widgets) {
      var factory = JemiPlugin.runtime.getJeiHelpers().getFocusFactory();
      var focuses = new java.util.ArrayList<mezz.jei.api.recipe.IFocus<?>>();
      if (net.minecraft.client.Minecraft.getInstance().gui.screen() instanceof dev.emi.emi.screen.RecipeScreen screen) {
         var role = screen.lookupUses ? RecipeIngredientRole.INPUT : RecipeIngredientRole.OUTPUT;
         for (var stack : screen.lookupIngredient.getEmiStacks()) JemiUtil.getTyped(stack).ifPresent(typed -> focuses.add(focus(factory, role, typed)));
      }
      var focusGroup = factory.createFocusGroup(focuses);
      Optional<IRecipeLayoutDrawable<T>> opt = JemiPlugin.runtime.getRecipeManager().createRecipeLayoutDrawable(this.category, this.recipe, focusGroup);
      JemiRecipeLayoutBuilder builder = new JemiRecipeLayoutBuilder();
      this.category.setRecipe(builder, this.recipe, focusGroup);

      for (JemiRecipeSlotBuilder jrsb : builder.slots) {
         jrsb.acceptor.coerceStacks(jrsb.richTooltipCallback, jrsb.renderers);
      }

      if (!builder.applyFocusLinks(focusGroup)) return;
      var slotWidgets=new java.util.ArrayList<Widget>();
      var drawables=new java.util.ArrayList<dev.emi.emi.jemi.impl.JemiRecipeSlotDrawable>();
      if (opt.isPresent()) {
         for (JemiRecipeSlotBuilder sb : builder.slots) {
            JemiRecipeSlot slot=new JemiRecipeSlot(sb, focusGroup);
            if(slot.tankInfo!=null && slot.getIngredients(JemiUtil.getFluidType()).findAny().isPresent())
                slotWidgets.add(new JemiTankWidget(slot,this));
            else slotWidgets.add(new JemiSlotWidget(slot,this));
            drawables.add(new dev.emi.emi.jemi.impl.JemiRecipeSlotDrawable(slot));
         }
         widgets.add(new JemiWidget(0,0,getDisplayWidth(),getDisplayHeight(),opt.get(),drawables,focusGroup));
      }

      boolean slotsAdded = false;
      try {
         JemiRecipeExtrasBuilder extras = new JemiRecipeExtrasBuilder(() -> List.copyOf(drawables));
         this.category.createRecipeExtras(extras, this.recipe, focusGroup);

         for (var drawable : drawables) {
            if (!extras.claimedSlots.contains(drawable)) widgets.add(drawable.widget);
         }
         slotsAdded = true;
         for (JemiWidgetBuilder b : extras.widgets) {
            b.addWidgets(widgets);
         }
      } catch (Throwable t) {
         if (!slotsAdded) slotWidgets.forEach(widgets::add);
         EmiLog.error("Exception adding JEMI extras", t);
      }
   }

   private static <I> mezz.jei.api.recipe.IFocus<I> focus(mezz.jei.api.recipe.IFocusFactory factory,
         RecipeIngredientRole role, mezz.jei.api.ingredients.ITypedIngredient<I> ingredient) {
      return factory.createFocus(role, ingredient.getType(), ingredient.getIngredient());
   }

   public class JemiWidget extends Widget {
      private final IRecipeLayoutDrawable<T> recipeLayoutDrawable;
      private final mezz.jei.api.recipe.IFocusGroup focusGroup;
      private final Bounds bounds;
      private final List<dev.emi.emi.jemi.impl.JemiRecipeSlotDrawable> slots;
      private long lastUpdateSecond=Long.MIN_VALUE;
      private final dev.emi.emi.jemi.impl.JemiRecipeSlotsView slotViews;
      private final int x;
      private final int y;

      public JemiWidget(int x, int y, int w, int h, IRecipeLayoutDrawable<T> recipeLayoutDrawable, List<dev.emi.emi.jemi.impl.JemiRecipeSlotDrawable> slots, mezz.jei.api.recipe.IFocusGroup focusGroup) {
         this.focusGroup = focusGroup;
         this.recipeLayoutDrawable = recipeLayoutDrawable;
         this.slots=List.copyOf(slots);
         this.slotViews=new dev.emi.emi.jemi.impl.JemiRecipeSlotsView(slots.stream().map(dev.emi.emi.jemi.impl.JemiRecipeSlotDrawable::slotView).toList());
         this.bounds = new Bounds(x, y, w, h);
         this.x = x;
         this.y = y;
      }

      public Bounds getBounds() {
         return this.bounds;
      }

      public void extractRenderState(GuiGraphicsExtractor draw,int mouseX,int mouseY,float delta) {
         long second=System.currentTimeMillis()/1000;
         if(second!=lastUpdateSecond) {
            lastUpdateSecond=second;
            slots.forEach(dev.emi.emi.jemi.impl.JemiRecipeSlotDrawable::clearDisplayOverrides);
            JemiRecipe.this.category.onDisplayedIngredientsUpdate(JemiRecipe.this.recipe,
                List.copyOf(slots),focusGroup);
         }
         draw.pose().pushMatrix();
         try {
            draw.pose().translate(x,y);
            JemiRecipe.this.category.draw(JemiRecipe.this.recipe,slotViews,draw,mouseX-x,mouseY-y);
         } finally { draw.pose().popMatrix(); }
      }

      public List<ClientTooltipComponent> getTooltip(int mouseX, int mouseY) {
         JemiTooltipBuilder builder = new JemiTooltipBuilder();
         JemiRecipe.this.category.getTooltip(builder, JemiRecipe.this.recipe, slotViews, mouseX-x, mouseY-y);
         return builder.components();
      }

      public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent event,boolean doubleClick) {
         return mouseInput(event,doubleClick,mezz.jei.common.input.InputPhase.PRESS);
      }
      public boolean mouseReleased(net.minecraft.client.input.MouseButtonEvent event) {
         return mouseInput(event,false,mezz.jei.common.input.InputPhase.RELEASE);
      }
      private boolean mouseInput(net.minecraft.client.input.MouseButtonEvent event,boolean doubleClick,mezz.jei.common.input.InputPhase phase) {
         return mezz.jei.common.input.UserInput.fromVanilla(event,doubleClick,phase)
             .map(input -> recipeLayoutDrawable.getInputHandler().handleInput(event.x()-x,event.y()-y,input)).orElse(false);
      }
      public boolean keyPressed(net.minecraft.client.input.KeyEvent event) {
         int mouseX=EmiScreenManager.lastMouseX-x,mouseY=EmiScreenManager.lastMouseY-y;
         if(net.minecraft.client.Minecraft.getInstance().gui.screen() instanceof dev.emi.emi.screen.RecipeScreen screen) {
             var group=screen.getGroup(this);
             if(group!=null) { mouseX-=group.x(); mouseY-=group.y(); }
         }
         return recipeLayoutDrawable.getInputHandler().handleInput(mouseX,mouseY,
             mezz.jei.common.input.UserInput.fromVanilla(event));
      }
   }
}
