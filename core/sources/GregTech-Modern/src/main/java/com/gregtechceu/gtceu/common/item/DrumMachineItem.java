package com.gregtechceu.gtceu.common.item;

import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.FluidPipeProperties;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.item.MetaMachineItem;
import com.gregtechceu.gtceu.api.misc.forge.ThermalFluidHandlerItemStack;
import com.gregtechceu.gtceu.common.data.item.GTDataComponents;
import com.gregtechceu.gtceu.common.data.machines.GTMachineUtils;

import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.ItemAccessFluidHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import org.jetbrains.annotations.NotNull;

public class DrumMachineItem extends MetaMachineItem {

    @NotNull
    private final Material mat;

    protected DrumMachineItem(MetaMachineBlock block, Properties properties, @NotNull Material mat) {
        super(block, properties);
        this.mat = mat;
    }

    public static DrumMachineItem create(MetaMachineBlock block, Properties properties, @NotNull Material mat) {
        return new DrumMachineItem(block, properties, mat);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        var player = context.getPlayer();
        if (player != null && !player.isShiftKeyDown()) {
            var hit = getPlayerPOVHitResult(context.getLevel(), player,
                    ClipContext.Fluid.SOURCE_ONLY);
            if (hit.getType() == HitResult.Type.BLOCK &&
                    !context.getLevel().getFluidState(hit.getBlockPos()).isEmpty()) {
                // This is a bucket action, not block placement. Match BucketItem's
                // snapshot handling so a later placement rollback cannot restore
                // the source after a filled container has already been delivered.
                boolean capture = context.getLevel().captureBlockSnapshots;
                context.getLevel().captureBlockSnapshots = false;
                try {
                    return use(context.getLevel(), player, context.getHand());
                } finally {
                    context.getLevel().captureBlockSnapshots = capture;
                }
            }
        }
        return super.useOn(context);
    }

    @Override
    public InteractionResult use(Level level,
            Player player, InteractionHand hand) {
        if (player.isShiftKeyDown()) return InteractionResult.PASS;
        var hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
        if (hit.getType() != HitResult.Type.BLOCK)
            return InteractionResult.PASS;
        var pos = hit.getBlockPos();
        var state = level.getBlockState(pos);
        if (state.getFluidState().isEmpty() ||
                !state.getFluidState().isSource()) return InteractionResult.PASS;
        var held = player.getItemInHand(hand);
        if (!level.mayInteract(player, pos) ||
                !player.mayUseItemAt(pos.relative(hit.getDirection()), hit.getDirection(), held) ||
                !(state.getBlock() instanceof BucketPickup pickup))
            return InteractionResult.FAIL;
        var filled = held.copyWithCount(1);
        var handler = ItemAccess.forStack(filled).getCapability(Capabilities.Fluid.ITEM);
        if (handler == null) return InteractionResult.FAIL;
        boolean burns = mat.hasProperty(PropertyKey.WOOD) &&
                state.getFluidState().is(net.minecraft.tags.FluidTags.LAVA);
        if (burns) {
            if (!net.neoforged.neoforge.transfer.fluid.FluidUtil.getFirstStackContained(held).isEmpty())
                return InteractionResult.FAIL;
            if (!level.isClientSide()) {
                held.shrink(1);
                player.setItemInHand(hand, held.copy());
                player.lavaIgnite();
                player.lavaHurt();
                level.playSound(null, pos, net.minecraft.sounds.SoundEvents.GENERIC_BURN,
                        net.minecraft.sounds.SoundSource.PLAYERS, 1, 1);
            }
            return InteractionResult.SUCCESS.heldItemTransformedTo(player.getItemInHand(hand));
        }
        // Check acceptance on a detached item before allowing any world mutation.
        try (var tx = Transaction.openRoot()) {
            if (handler.insert(FluidResource.of(state.getFluidState().getType()), 1000, tx) != 1000)
                return InteractionResult.FAIL;
        }
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        var collected = net.neoforged.neoforge.transfer.fluid.FluidUtil.tryPickupFluid(
                handler, player, level, pos, (net.neoforged.neoforge.transfer.transaction.TransactionContext) null);
        if (collected.isEmpty()) return InteractionResult.FAIL;
        if (held.getCount() == 1) player.setItemInHand(hand, filled);
        else {
            held.shrink(1);
            player.setItemInHand(hand, held.copy());
            if (!player.getInventory().add(filled)) player.drop(filled, false, net.minecraft.util.Prediction.SERVER_ONLY);
        }
        player.awardStat(Stats.ITEM_USED.get(this));
        return InteractionResult.SUCCESS.heldItemTransformedTo(player.getItemInHand(hand));
    }

    public void attachCapabilities(RegisterCapabilitiesEvent event) {
        if (mat.hasProperty(PropertyKey.FLUID_PIPE)) {
            FluidPipeProperties property = mat.getProperty(PropertyKey.FLUID_PIPE);
            event.registerItem(Capabilities.Fluid.ITEM,
                    (stack, access) -> new ThermalFluidHandlerItemStack(access,
                            GTMachineUtils.DRUM_CAPACITY.getInt(getDefinition()),
                            property.getMaxFluidTemperature(), property.isGasProof(), property.isAcidProof(),
                            property.isCryoProof(),
                            property.isPlasmaProof()),
                    this);
        } else {
            event.registerItem(Capabilities.Fluid.ITEM,
                    (stack, access) -> new ItemAccessFluidHandler(access,GTDataComponents.FLUID_CONTENT.get(),
                            GTMachineUtils.DRUM_CAPACITY.getInt(getDefinition())),
                    this);
        }
    }
}
