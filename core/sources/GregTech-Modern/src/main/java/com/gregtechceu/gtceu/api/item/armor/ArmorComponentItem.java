package com.gregtechceu.gtceu.api.item.armor;

import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.ArmorMaterial;

import com.gregtechceu.gtceu.api.item.IComponentItem;
import com.gregtechceu.gtceu.api.item.component.*;
import com.gregtechceu.gtceu.common.data.GTItems;

import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import com.lowdragmc.lowdraglib.transfer.interaction.UseResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.ItemAbility;

import com.google.common.base.Preconditions;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

public class ArmorComponentItem extends Item implements IComponentItem, com.lowdragmc.lowdraglib.transfer.name.DynamicItemName,
        com.lowdragmc.lowdraglib.transfer.name.DynamicStackDescription,
        com.lowdragmc.lowdraglib.transfer.lifecycle.LegacyInventoryTick, com.lowdragmc.lowdraglib.transfer.enchantment.LegacyEnchantable, com.lowdragmc.lowdraglib.transfer.repair.LegacyRepair {

    @Getter
    private IArmorLogic armorLogic = new DummyArmorLogic();
    @Getter
    protected List<IItemComponent> components;
    private final ArmorMaterial baseMaterial;
    private final ArmorType baseType;

    public ArmorComponentItem(Holder<ArmorMaterial> material, ArmorType type, Properties properties) {
        // Some trickery to always receive damage events without ever actually breaking the armor
        super(com.lowdragmc.lowdraglib.transfer.equipment.ComponentArmorProperties.configure(properties, material.value(), type));
        baseMaterial = material.value();
        baseType = type;
        components = new ArrayList<>();
    }

    public ArmorComponentItem setArmorLogic(IArmorLogic armorLogic) {
        Preconditions.checkNotNull(armorLogic, "Cannot set ArmorLogic to null");
        this.armorLogic = armorLogic;
        this.armorLogic.addToolComponents(this);
        return this;
    }

    public void attachComponents(IItemComponent... components) {
        this.components.addAll(Arrays.asList(components));
        for (IItemComponent component : components) {
            component.onAttached(this);
        }
    }

    @Override
    public ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack) {
        List<ItemAttributeModifiers.Entry> list = new ArrayList<>();
        IArmorLogic armorLogic = getArmorLogic();
        list.addAll(baseMaterial.createAttributes(getType()).modifiers());
        list.addAll(armorLogic.getDefaultAttributeModifiers(getEquipmentSlot(), stack));
        return new ItemAttributeModifiers(list);
    }

    public ArmorType getType() {
        return baseType;
    }

    public EquipmentSlot getEquipmentSlot() {
        return baseType.getSlot();
    }

    @Override
    public void inventoryTick(ItemStack stack, net.minecraft.server.level.ServerLevel level, Entity entity,
                              @Nullable EquipmentSlot equipmentSlot) {
        com.lowdragmc.lowdraglib.transfer.lifecycle.ItemTickBridge.dispatch(stack, level, entity, equipmentSlot);
    }

    public void legacyInventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (entity instanceof LivingEntity wearer) {
            var equipped = stack.get(net.minecraft.core.component.DataComponents.EQUIPPABLE);
            var chest = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(wearer.getItemBySlot(EquipmentSlot.CHEST).getItem());
            com.lowdragmc.lowdraglib.transfer.equipment.ArmorAppearance.asset(
                    net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem()), chest).ifPresent(asset -> {
                if (equipped != null && !equipped.assetId().filter(asset::equals).isPresent()) {
                    stack.set(net.minecraft.core.component.DataComponents.EQUIPPABLE,
                            com.lowdragmc.lowdraglib.transfer.equipment.ArmorAppearance.withAsset(equipped, asset));
                }
            });
        }
        for (IItemComponent component : components) {
            if (component instanceof IItemLifeCycle lifeCycle) {
                lifeCycle.inventoryTick(stack, level, entity, slotId, isSelected);
            }
        }
        if (slotId >= 36 && slotId <= 39 && entity instanceof Player player) {
            this.armorLogic.onArmorTick(level, player, stack);
        }
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack repairCandidate) {
        return false;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

    @Override
    public int getEnchantmentValue(ItemStack stack) { return getEnchantmentValue(); }

    public int getEnchantmentValue() {
        return 50;
    }

    public int getArmorDisplay(Player player, @NotNull ItemStack armor, EquipmentSlot slot) {
        return armorLogic.getArmorDisplay(player, armor, slot);
    }

    @Override
    public void setDamage(ItemStack stack, int damage) {}

    @Override
    public boolean isDamaged(ItemStack stack) {
        return false;
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return Integer.MAX_VALUE;
    }

    @Override
    public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, @Nullable T entity,
                                                   Consumer<ItemStack> onBroken) {
        return armorLogic.damageArmor(entity, stack, amount, this.getEquipmentSlot());
    }

    public @Nullable Identifier getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot,
                                                      com.lowdragmc.lowdraglib.transfer.equipment.ArmorLayer layer, boolean innerModel) {
        return armorLogic.getArmorTexture(stack, entity, slot, layer);
    }

    ///////////////////////////////////////////
    ///// ALL component item things ///////
    ///////////////////////////////////////////

    public void fillItemCategory(CreativeModeTab category, NonNullList<ItemStack> items) {
        boolean found = false;
        for (IItemComponent component : components) {
            if (component instanceof ISubItemHandler subItemHandler) {
                subItemHandler.fillItemCategory(this, category, items);
                found = true;
            }
        }
        if (found) return;
        items.add(new ItemStack(this));
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context,
                                net.minecraft.world.item.component.TooltipDisplay display,
                                java.util.function.Consumer<Component> output, TooltipFlag flag) {
        com.lowdragmc.lowdraglib.transfer.tooltip.LegacyTooltips.emit(output,
                lines -> appendHoverText(stack, context, lines, flag));
    }

    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents,
                                TooltipFlag isAdvanced) {
        for (IItemComponent component : components) {
            if (component instanceof IAddInformation addInformation) {
                addInformation.appendHoverText(stack, context, tooltipComponents, isAdvanced);
            }
        }
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        for (IItemComponent component : components) {
            if (component instanceof IDurabilityBar durabilityBar) {
                return durabilityBar.isBarVisible(stack);
            }
        }
        return super.isBarVisible(stack);
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        for (IItemComponent component : components) {
            if (component instanceof IDurabilityBar durabilityBar) {
                return durabilityBar.getBarWidth(stack);
            }
        }
        return super.getBarWidth(stack);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        for (IItemComponent component : components) {
            if (component instanceof IDurabilityBar durabilityBar) {
                return durabilityBar.getBarColor(stack);
            }
        }
        return super.getBarColor(stack);
    }

    @Override
    public boolean canPerformAction(net.minecraft.world.item.ItemInstance instance, ItemAbility action) {
        ItemStack stack = com.lowdragmc.lowdraglib.transfer.recipe.CraftingRemainders.stack(instance);
        for (IItemComponent component : components) {
            if (component instanceof IAbilityItem abilityItem && abilityItem.canPerformAction(stack, action)) {
                return true;
            }
        }
        return super.canPerformAction(instance, action);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        for (IItemComponent component : components) {
            if (component instanceof IInteractionItem interactionItem) {
                var result = interactionItem.useOn(context);
                if (result != InteractionResult.PASS) {
                    return result;
                }
            }
        }
        return super.useOn(context);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand usedHand) {
        for (IItemComponent component : components) {
            if (component instanceof IInteractionItem interactionItem) {
                var result = interactionItem.use(player.getItemInHand(usedHand), level, player, usedHand);
                if (result.getResult() != InteractionResult.PASS) {
                    return UseResult.toNative(result, stack -> player.setItemInHand(usedHand, stack));
                }
            }
        }
        return super.use(level, player, usedHand);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        stack = super.finishUsingItem(stack, level, livingEntity);
        for (IItemComponent component : components) {
            if (component instanceof IInteractionItem interactionItem) {
                stack = interactionItem.finishUsingItem(stack, level, livingEntity);
            }
        }
        return stack;
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack itemStack, UseOnContext context) {
        for (IItemComponent component : components) {
            if (component instanceof IInteractionItem interactionItem) {
                var result = interactionItem.onItemUseFirst(itemStack, context);
                if (result != InteractionResult.PASS) {
                    return result;
                }
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity interactionTarget,
                                                  InteractionHand usedHand) {
        for (IItemComponent component : components) {
            if (component instanceof IInteractionItem interactionItem) {
                var result = interactionItem.interactLivingEntity(stack, player, interactionTarget, usedHand);
                if (result != InteractionResult.PASS) {
                    return result;
                }
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public Component dynamicItemName(ItemStack stack) {
        for (IItemComponent component : components) {
            if (component instanceof ICustomDescriptionId customDescriptionId) {
                Component name = customDescriptionId.getItemName(stack);
                if (name != null) {
                    return name;
                }
            }
        }
        return com.lowdragmc.lowdraglib.transfer.name.ItemNames.fallbackName(stack);
    }

    @Override
    public String dynamicStackDescriptionId(ItemStack stack) {
        for (IItemComponent component : components) {
            if (component instanceof ICustomDescriptionId customDescriptionId) {
                String langId = customDescriptionId.getItemDescriptionId(stack);
                if (langId != null) {
                    return langId;
                }
            }
        }
        return super.getDescriptionId();
    }

    @Override
    public @org.jetbrains.annotations.Nullable net.minecraft.world.item.ItemStackTemplate getCraftingRemainder(
            net.minecraft.world.item.ItemInstance instance) {
        return com.lowdragmc.lowdraglib.transfer.recipe.CraftingRemainders.template(getCraftingRemainingItem(
                com.lowdragmc.lowdraglib.transfer.recipe.CraftingRemainders.stack(instance)));
    }

    public ItemStack getCraftingRemainingItem(ItemStack itemStack) {
        for (IItemComponent component : components) {
            if (component instanceof IRecipeRemainder recipeRemainder) {
                return recipeRemainder.getRecipeRemained(itemStack);
            }
        }
        var fallback=super.getCraftingRemainder();
        return fallback==null?ItemStack.EMPTY:fallback.create();
    }

    public boolean hasCraftingRemainingItem(ItemStack stack) {
        for (IItemComponent component : components) {
            if (component instanceof IRecipeRemainder recipeRemainder) {
                return recipeRemainder.getRecipeRemained(stack) != ItemStack.EMPTY;
            }
        }
        return super.getCraftingRemainder()!=null;
    }

    @Override
    public boolean canWalkOnPowderedSnow(ItemStack stack, LivingEntity wearer) {
        return stack.is(GTItems.NANO_BOOTS.asItem()) || stack.is(GTItems.QUANTUM_BOOTS.asItem());
    }
}
