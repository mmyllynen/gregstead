package com.gregtechceu.gtceu.api.block;

import com.gregtechceu.gtceu.api.blockentity.IPaintable;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.pipenet.*;

import net.minecraft.client.color.block.BlockTintSource;
import com.gregtechceu.gtceu.client.util.NativeBlockTints;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import org.jetbrains.annotations.Nullable;

public abstract class MaterialPipeBlock<
        PipeType extends Enum<PipeType> & IPipeType<NodeDataType> & IMaterialPipeType<NodeDataType>, NodeDataType,
        WorldPipeNetType extends LevelPipeNet<NodeDataType, ? extends PipeNet<NodeDataType>>>
                                       extends PipeBlock<PipeType, NodeDataType, WorldPipeNetType> implements com.lowdragmc.lowdraglib.transfer.name.DynamicDescription {

    public final Material material;

    public MaterialPipeBlock(Properties properties, PipeType pipeType, Material material) {
        super(properties, pipeType);
        this.material = material;
    }

    @OnlyIn(Dist.CLIENT)
    public static List<BlockTintSource> tintedColor() {
        return NativeBlockTints.materialPipe((state,level,pos,index)->state.getBlock() instanceof MaterialPipeBlock<?,?,?> block
                ?block.tinted(state,level,pos,index):-1);
    }

    public int tinted(BlockState blockState, @Nullable BlockAndTintGetter level, @Nullable BlockPos pos, int index) {
        return index == 0 || index == 1 ? material.getMaterialARGB() : -1;
    }

    @Override
    public final NodeDataType createRawData(BlockState pState, @Nullable ItemStack pStack) {
        return createMaterialData();
    }

    @Override
    public NodeDataType createProperties(IPipeNode<PipeType, NodeDataType> pipeTile) {
        PipeType pipeType = pipeTile.getPipeType();
        Material material = ((MaterialPipeBlock<PipeType, NodeDataType, WorldPipeNetType>) pipeTile
                .getPipeBlock()).material;
        if (pipeType == null || material.isNull()) {
            return getFallbackType();
        }
        return createProperties(pipeType, material);
    }

    protected abstract NodeDataType createProperties(PipeType pipeType, Material material);

    @Override
    public final NodeDataType getFallbackType() {
        return createMaterialData();
    }

    protected abstract NodeDataType createMaterialData();

    @Override
    public String dynamicDescriptionId() {
        return pipeType.getTagPrefix().getUnlocalizedName(material);
    }

    @Override
    public MutableComponent getName() {
        return pipeType.getTagPrefix().getLocalizedName(material);
    }
}
