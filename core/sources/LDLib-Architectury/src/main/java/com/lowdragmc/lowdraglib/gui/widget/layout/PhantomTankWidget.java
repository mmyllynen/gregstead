package com.lowdragmc.lowdraglib.gui.widget.layout;

import com.google.common.collect.Lists;
import com.lowdragmc.lowdraglib.LDLib;
import com.lowdragmc.lowdraglib.Platform;
import com.lowdragmc.lowdraglib.editor.annotation.ConfigSetter;
import com.lowdragmc.lowdraglib.editor.configurator.IConfigurableWidget;
import com.lowdragmc.lowdraglib.gui.ingredient.IGhostIngredientTarget;
import com.lowdragmc.lowdraglib.gui.ingredient.Target;
import com.lowdragmc.lowdraglib.gui.widget.TankWidget;
import com.lowdragmc.lowdraglib.registry.annotation.LDLRegister;
import dev.architectury.fluid.neoforge.FluidStackImpl;
import dev.emi.emi.api.stack.EmiStack;
import mezz.jei.api.ingredients.ITypedIngredient;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.fluids.FluidStack;
import com.lowdragmc.lowdraglib.transfer.fluid.IFluidHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

@LDLRegister(name = "phantom_tank_slot", group = "widget.container", registry = "ldlib:widget")
public class PhantomTankWidget extends TankWidget implements IGhostIngredientTarget, IConfigurableWidget {

    private Consumer<FluidStack> fluidStackUpdater;

    public PhantomTankWidget() {
        super();
        this.allowClickFilled = false;
        this.allowClickDrained = false;
    }

    public PhantomTankWidget(IFluidHandler fluidTank, int x, int y) {
        super(fluidTank, x, y, false, false);
    }

    public PhantomTankWidget(@Nullable IFluidHandler fluidTank, int x, int y, int width, int height) {
        super(fluidTank, x, y, width, height, false, false);
    }

    public PhantomTankWidget(IFluidHandler fluidTank, int tank, int x, int y) {
        super(fluidTank, tank, x, y, false, false);
    }

    public PhantomTankWidget(@Nullable IFluidHandler fluidTank, int tank, int x, int y, int width, int height) {
        super(fluidTank, tank, x, y, width, height, false, false);
    }

    public PhantomTankWidget setIFluidStackUpdater(Consumer<FluidStack> fluidStackUpdater) {
        this.fluidStackUpdater = fluidStackUpdater;
        return this;
    }

    @ConfigSetter(field = "allowClickFilled")
    public PhantomTankWidget setAllowClickFilled(boolean v) {
        // you cant modify it
        return this;
    }

    @ConfigSetter(field = "allowClickDrained")
    public PhantomTankWidget setAllowClickDrained(boolean v) {
        // you cant modify it
        return this;
    }

    public static FluidStack drainFrom(Object ingredient) {
        if (ingredient instanceof Ingredient ing) {
            var items = com.lowdragmc.lowdraglib.transfer.recipe.IngredientStacks.getItems(ing);
            if (items.length > 0) {
                ingredient = items[0];
            }
        }
        if (ingredient instanceof ItemStack itemStack) {
            var fluid = com.lowdragmc.lowdraglib.transfer.fluid.FluidItemProbe.extractable(itemStack,Integer.MAX_VALUE);
            return fluid==null?FluidStack.EMPTY:fluid;
        }
        return FluidStack.EMPTY;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Target> getPhantomTargets(Object ingredient) {
        if (LDLib.isReiLoaded() && ingredient instanceof dev.architectury.fluid.FluidStack fluidStack) {
            ingredient = FluidStackImpl.toValue.apply(fluidStack);
        }
        if (LDLib.isEmiLoaded() && ingredient instanceof EmiStack fluidEmiStack) {
            Fluid fluid = fluidEmiStack.getKeyOfType(Fluid.class);
            if (fluid == null) {
                Item item = fluidEmiStack.getKeyOfType(Item.class);
                ingredient = item == null ? null : new ItemStack(item, (int)fluidEmiStack.getAmount());
                if (ingredient instanceof ItemStack itemStack) {
                    itemStack.applyComponents(fluidEmiStack.getComponentChanges());
                }
            } else {
                ingredient = new FluidStack(fluid, fluidEmiStack.getAmount() == 0L ? 1000 : (int)fluidEmiStack.getAmount());
                ((FluidStack) ingredient).applyComponents(fluidEmiStack.getComponentChanges());
            }
        }
        if (LDLib.isJeiLoaded() && ingredient instanceof ITypedIngredient<?> typedIngredient && typedIngredient.getIngredient() instanceof FluidStack fluidStack) {
            ingredient = fluidStack;
        }
        if (!(ingredient instanceof FluidStack) && drainFrom(ingredient).isEmpty()) {
            return Collections.emptyList();
        }

        Rect2i rectangle = toRectangleBox();
        return Lists.newArrayList(new Target() {
            @Nonnull
            @Override
            public Rect2i getArea() {
                return rectangle;
            }

            @Override
            public void accept(@Nonnull Object ingredient) {
                FluidStack ingredientStack;
                if (LDLib.isReiLoaded() && ingredient instanceof dev.architectury.fluid.FluidStack fluidStack) {
                    ingredient = FluidStackImpl.toValue.apply(fluidStack);
                }
                if (LDLib.isEmiLoaded() && ingredient instanceof EmiStack fluidEmiStack) {
                    var fluid = fluidEmiStack.getKeyOfType(Fluid.class);
                    if (fluid == null) {
                        Item item = fluidEmiStack.getKeyOfType(Item.class);
                        ingredient = item == null ? null : new ItemStack(item, (int)fluidEmiStack.getAmount());
                        if (ingredient instanceof ItemStack itemStack) {
                            itemStack.applyComponents(fluidEmiStack.getComponentChanges());
                        }
                    } else {
                        ingredient = new FluidStack(fluid, fluidEmiStack.getAmount() == 0L ? 1000 : (int) fluidEmiStack.getAmount());
                        ((FluidStack) ingredient).applyComponents(fluidEmiStack.getComponentChanges());
                    }
                }
                if (LDLib.isJeiLoaded() && ingredient instanceof ITypedIngredient<?> typedIngredient && typedIngredient.getIngredient() instanceof FluidStack fluidStack) {
                    ingredient = fluidStack;
                }
                if (ingredient instanceof FluidStack fluidStack)
                    ingredientStack = fluidStack;
                else
                    ingredientStack = drainFrom(ingredient);

                if (ingredientStack != null && !ingredientStack.isEmpty()) {
                    writeClientAction(2, buffer -> FluidStack.OPTIONAL_STREAM_CODEC.encode(buffer, ingredientStack));
                }

                if (isClientSideWidget && fluidTank != null) {
                    NativePhantomFluidSelection.replace(fluidTank, tank, ingredientStack);
                    if (fluidStackUpdater != null) {
                        fluidStackUpdater.accept(ingredientStack);
                    }
                }
            }
        });
    }

    @Override
    public void handleClientAction(int id, RegistryFriendlyByteBuf buffer) {
        if (id == 1) {
            handlePhantomClick();
        } else if (id == 2) {
            FluidStack fluidStack = FluidStack.OPTIONAL_STREAM_CODEC.decode(buffer);
            if (fluidTank == null) return;
            NativePhantomFluidSelection.replace(fluidTank, tank, fluidStack);
            if (fluidStackUpdater != null) {
                fluidStackUpdater.accept(fluidStack);
            }
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isMouseOverElement(mouseX, mouseY)) {
            if (isClientSideWidget) {
                handlePhantomClick();
            } else {
                writeClientAction(1, buffer -> { });
            }
            return true;
        }
        return false;
    }

    private void handlePhantomClick() {
        if (fluidTank == null) return;
        ItemStack itemStack = gui.getModularUIContainer().getCarried().copy();
        if (!itemStack.isEmpty()) {
            itemStack.setCount(1);
            var resultFluid = com.lowdragmc.lowdraglib.transfer.fluid.FluidItemProbe.extractable(itemStack,Integer.MAX_VALUE);
            if (resultFluid != null) {
                fluidTank.drain(fluidTank.getTankCapacity(tank), IFluidHandler.FluidAction.EXECUTE);
                fluidTank.fill(resultFluid.copy(), IFluidHandler.FluidAction.EXECUTE);
                if (fluidStackUpdater != null) {
                    fluidStackUpdater.accept(resultFluid);
                }
            }
        } else {
            fluidTank.drain(fluidTank.getTankCapacity(tank), IFluidHandler.FluidAction.EXECUTE);
            if (fluidStackUpdater != null) {
                fluidStackUpdater.accept(null);
            }
        }
    }

}
