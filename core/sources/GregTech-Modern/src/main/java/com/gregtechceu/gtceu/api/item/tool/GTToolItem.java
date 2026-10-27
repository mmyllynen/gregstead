package com.gregtechceu.gtceu.api.item.tool;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags;
import com.gregtechceu.gtceu.api.item.IGTTool;
import com.gregtechceu.gtceu.api.sound.SoundEntry;
import com.gregtechceu.gtceu.client.renderer.item.ToolItemRenderer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import com.lowdragmc.lowdraglib.transfer.interaction.UseResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.ItemAbility;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class GTToolItem extends Item implements IGTTool, com.gregtechceu.gtceu.api.item.NativeShieldTool, com.lowdragmc.lowdraglib.transfer.name.DynamicDescription, com.lowdragmc.lowdraglib.transfer.name.DynamicItemName, com.lowdragmc.lowdraglib.transfer.combat.LegacyItemAttack, com.lowdragmc.lowdraglib.transfer.enchantment.LegacyEnchantable, com.lowdragmc.lowdraglib.transfer.repair.LegacyRepair {

    private final MaterialToolTier tier;

    @Getter
    protected final GTToolType toolType;
    @Getter
    protected final int electricTier;
    @Getter
    protected final Material material;
    @Getter
    private final IGTToolDefinition toolStats;

    public GTToolItem(GTToolType toolType, MaterialToolTier tier, Material material, IGTToolDefinition definition,
                      Properties properties) {
        super(com.lowdragmc.lowdraglib.transfer.tool.ToolTierProperties.configure(
                material.hasFlag(MaterialFlags.FIRE_RESISTANT) ? properties.fireResistant() : properties, tier));
        this.tier = tier;
        this.toolType = toolType;
        this.material = material;
        this.electricTier = toolType.electricTier;
        this.toolStats = definition;
        if (GTCEu.isClientSide()) {
            ToolItemRenderer.create(this, toolType);
        }
        definition$init();
    }

    @Override
    public MaterialToolTier getTier() { return tier; }

    @Override
    public ItemStack getDefaultInstance() {
        return get();
    }

    @Override
    public boolean canPerformAction(net.minecraft.world.item.ItemInstance stack, ItemAbility action) {
        return definition$canPerformAction(com.lowdragmc.lowdraglib.transfer.recipe.CraftingRemainders.stack(stack), action);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        return definition$onItemUseFirst(stack, context);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        return definition$onItemUse(context);
    }

    @Override
    public String dynamicDescriptionId() {
        return toolType.getUnlocalizedName();
    }

    public Component getDescription() {
        return Component.translatable(toolType.getUnlocalizedName(), material.getLocalizedName());
    }

    @Override
    public Component dynamicItemName(ItemStack stack) {
        return this.getDescription();
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity miningEntity) {
        definition$mineBlock(stack, level, state, pos, miningEntity);
        return true;
    }

    @Override
    public boolean isElectric() {
        return electricTier > -1;
    }

    @Nullable
    @Override
    public SoundEntry getSound() {
        return toolType.soundEntry;
    }

    @Override
    public boolean playSoundOnBlockDestroy() {
        return toolType.playSoundOnBlockDestroy;
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        legacyHurtEnemy(stack,target,attacker);
    }

    @Override
    public boolean legacyHurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        return definition$hurtEnemy(stack, target, attacker);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context,
                                net.minecraft.world.item.component.TooltipDisplay display,
                                java.util.function.Consumer<Component> output, TooltipFlag flag) {
        com.lowdragmc.lowdraglib.transfer.tooltip.LegacyTooltips.emit(output,
                lines -> appendHoverText(stack, context, lines, flag));
    }

    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents,
                                TooltipFlag isAdvanced) {
        definition$appendHoverText(stack, context, tooltipComponents, isAdvanced);
    }

    @Override
    public boolean isEnchantable(ItemStack stack) { return com.lowdragmc.lowdraglib.transfer.enchantment.Enchantability.defaultPredicate(stack); }

    @Override
    public int getEnchantmentValue(ItemStack stack) {
        return getTotalEnchantability(stack);
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack repairCandidate) {
        return definition$isValidRepairItem(stack, repairCandidate);
    }

    @Override
    public ItemEnchantments getAllEnchantments(ItemStack stack, HolderLookup.RegistryLookup<Enchantment> lookup) {
        return definition$getAllEnchantments(stack, lookup);
    }

    @Override
    public int getEnchantmentLevel(net.minecraft.world.item.ItemInstance stack, Holder<Enchantment> enchantment) {
        return definition$getEnchantmentLevel(com.lowdragmc.lowdraglib.transfer.recipe.CraftingRemainders.stack(stack), enchantment);
    }

    @Override
    public boolean canDisableBlocking(ItemStack weapon, ItemStack shield, LivingEntity defender, LivingEntity attacker) {
        return definition$canDisableShield(weapon, shield, defender, attacker);
    }

    @Override
    public boolean doesSneakBypassUse(ItemStack stack, LevelReader level, BlockPos pos, Player player) {
        return definition$doesSneakBypassUse(stack, level, pos, player);
    }

    @Override
    public boolean shouldCauseBlockBreakReset(ItemStack oldStack, ItemStack newStack) {
        return definition$shouldCauseBlockBreakReset(oldStack, newStack);
    }

    public boolean hasCraftingRemainingItem(ItemStack stack) {
        return definition$hasCraftingRemainingItem(stack);
    }

    @Override
    public @org.jetbrains.annotations.Nullable net.minecraft.world.item.ItemStackTemplate getCraftingRemainder(
            net.minecraft.world.item.ItemInstance instance) {
        return com.lowdragmc.lowdraglib.transfer.recipe.CraftingRemainders.template(getCraftingRemainingItem(
                com.lowdragmc.lowdraglib.transfer.recipe.CraftingRemainders.stack(instance)));
    }

    public ItemStack getCraftingRemainingItem(ItemStack itemStack) {
        return definition$getCraftingRemainingItem(itemStack);
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return definition$shouldCauseReequipAnimation(oldStack, newStack, slotChanged);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand usedHand) {
        return UseResult.toNative(definition$use(level, player, usedHand), stack -> player.setItemInHand(usedHand, stack));
    }
}
