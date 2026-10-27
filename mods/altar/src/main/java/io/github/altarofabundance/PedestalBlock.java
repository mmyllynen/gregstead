package io.github.altarofabundance;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Prediction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class PedestalBlock extends Block implements EntityBlock {
    public enum Role { MATERIAL, OFFERING, OUTPUT }

    private static final VoxelShape SHAPE = Shapes.or(
        Block.box(1, 0, 1, 15, 3, 15), Block.box(4, 3, 4, 12, 9, 12), Block.box(1, 9, 1, 15, 12, 15));
    private final Role role;

    public PedestalBlock(Role role, Properties properties) {
        super(properties);
        this.role = role;
    }

    public Role role() { return role; }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PedestalBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide() || role != Role.OFFERING || type != AltarOfAbundance.PEDESTAL.get()) return null;
        return (world, pos, currentState, entity) -> ((PedestalBlockEntity) entity).tick((ServerLevel) world);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                          Player player, InteractionHand hand, BlockHitResult hit) {
        if (stack.isEmpty()) return InteractionResult.TRY_WITH_EMPTY_HAND;
        if (level instanceof ServerLevel && level.getBlockEntity(pos) instanceof PedestalBlockEntity pedestal) {
            String error = pedestal.insert(stack, player.hasInfiniteMaterials());
            player.sendOverlayMessage(Component.translatable("message.altarofabundance." + error));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level instanceof ServerLevel && level.getBlockEntity(pos) instanceof PedestalBlockEntity pedestal) {
            ItemStack taken = pedestal.take();
            if (!taken.isEmpty()) {
                if (!player.getInventory().add(taken)) player.drop(taken, false, Prediction.SERVER_ONLY);
            } else {
                player.sendOverlayMessage(Component.translatable("message.altarofabundance." + pedestal.status()));
            }
        }
        return InteractionResult.SUCCESS;
    }
}
