package com.gregtechceu.gtceu.api.pipenet;

import com.gregtechceu.gtceu.api.block.PipeBlock;
import com.gregtechceu.gtceu.api.blockentity.IGregtechBlockEntity;
import com.gregtechceu.gtceu.api.blockentity.IPaintable;
import com.gregtechceu.gtceu.api.blockentity.ITickSubscription;
import com.gregtechceu.gtceu.api.blockentity.PipeBlockEntity;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.client.model.GTModelProperties;
import com.gregtechceu.gtceu.common.data.GTMaterialBlocks;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;

import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.model.data.ModelData;
import net.neoforged.neoforge.model.data.ModelDataManager;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IPipeNode<PipeType extends Enum<PipeType> & IPipeType<NodeDataType>, NodeDataType>
                          extends ITickSubscription, IPaintable, IGregtechBlockEntity {

    long getOffsetTimer();

    /**
     * Get Cover Container.
     */
    ICoverable getCoverContainer();

    /**
     * If tube is set to block connection from the specific side
     * 
     * @param side face
     */
    default boolean isBlocked(Direction side) {
        return PipeBlockEntity.isFaceBlocked(getBlockedConnections(), side);
    }

    /**
     * Unsafe!!! to set internal connections.
     * In general, you shouldn't call it yourself.
     */
    void setConnections(int connections);

    int getConnections();

    int getNumConnections();

    /**
     * set to block connection from the specific side
     * 
     * @param side      face
     * @param isBlocked is blocked
     */
    void setBlocked(Direction side, boolean isBlocked);

    /**
     * Whether pipe can attach to specific side.
     * e.g. check if there is an energyContainer nearby.
     */
    boolean canAttachTo(Direction side);

    /**
     * get connections for rendering and collision.
     */
    int getVisualConnections();

    /**
     * If node is connected to the specific side
     * 
     * @param side face
     */
    default boolean isConnected(Direction side) {
        return PipeBlockEntity.isConnected(getConnections(), side);
    }

    void setConnection(Direction side, boolean connected, boolean fromNeighbor);

    // if a face is blocked it will still render as connected, but it won't be able to receive stuff from that direction
    default boolean canHaveBlockedFaces() {
        return true;
    }

    int getBlockedConnections();

    default BlockState getState() {
        return self().getBlockState();
    }

    default BlockEntity self() {
        return (BlockEntity) this;
    }

    @SuppressWarnings("unchecked")
    default PipeBlock<PipeType, NodeDataType, ?> getPipeBlock() {
        return (PipeBlock<PipeType, NodeDataType, ?>) self().getBlockState().getBlock();
    }

    @Nullable
    default PipeNet<NodeDataType> getPipeNet() {
        if (getLevel() instanceof ServerLevel serverLevel) {
            return getPipeBlock().getWorldPipeNet(serverLevel).getNetFromPos(getBlockPos());
        }
        return null;
    }

    default PipeType getPipeType() {
        return getPipeBlock().pipeType;
    }

    @Nullable
    default NodeDataType getNodeData() {
        var net = getPipeNet();
        if (net != null) {
            return net.getNodeAt(getBlockPos()).data;
        }
        return null;
    }

    void notifyBlockUpdate();

    default void scheduleRenderUpdate() {
        var pos = getBlockPos();
        var level = getLevel();
        if (level != null) {
            var state = level.getBlockState(pos);
            if (level.isClientSide()) {
                // simplified from requestModelDataUpdate
                ModelDataManager manager = level.getModelDataManager();
                if (manager != null) {
                    manager.requestRefresh(self());
                }

                level.sendBlockUpdated(pos, state, state, Block.UPDATE_IMMEDIATE);
            } else {
                level.blockEvent(pos, state.getBlock(), 1, 0);
            }
        }
    }

    default void serverTick() {}

    @Override
    default int getDefaultPaintingColor() {
        return 0xFFFFFF;
    }

    @NotNull
    Material getFrameMaterial();

    @ApiStatus.Internal
    @Override
    default @NotNull ModelData getModelData() {
        int covered=0;
        for(Direction direction:Direction.values())if(getCoverContainer().getCoverAtSide(direction)!=null)
            covered|=1<<direction.ordinal();
        var builder=ModelData.builder()
                .with(GTModelProperties.LEVEL,self().getLevel()).with(GTModelProperties.POS,self().getBlockPos())
                .with(GTModelProperties.PIPE_CONNECTION_MASK,getVisualConnections())
                .with(GTModelProperties.PIPE_BLOCKED_MASK,getBlockedConnections())
                .with(GTModelProperties.PIPE_COVER_MASK,covered);
        if(isPainted())builder.with(GTModelProperties.PIPE_PAINT_COLOR,getPaintingColor());
        if(!getFrameMaterial().isNull()) {
            builder.with(GTModelProperties.PIPE_FRAME_PRIMARY_COLOR,getFrameMaterial().getMaterialRGB())
                    .with(GTModelProperties.PIPE_FRAME_SECONDARY_COLOR,getFrameMaterial().getMaterialSecondaryRGB());
            var entry=GTMaterialBlocks.MATERIAL_BLOCKS.get(TagPrefix.frameGt,getFrameMaterial());
            if(entry!=null)builder.with(GTModelProperties.PIPE_FRAME_STATE,entry.getDefaultState());
        }
        var data=builder.build();
        var world=self().getLevel();
        if(world!=null && world.isClientSide() && covered!=0) {
            var geometry=com.gregtechceu.gtceu.client.renderer.cover.NativeCoverSnapshots.capture(
                    getCoverContainer(),world,self().getBlockPos(),data);
            data=data.derive().with(GTModelProperties.PIPE_COVER_GEOMETRY,geometry).build();
        }
        return data;
    }
}
