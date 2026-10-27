package com.gregtechceu.gtceu.client.model;

import net.minecraft.core.BlockPos;
import java.util.List;
import com.gregtechceu.gtceu.client.model.pipe.NativeCoverSnapshot;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.model.data.ModelProperty;

public class GTModelProperties {

    public static final ModelProperty<Level> LEVEL = new ModelProperty<>();
    public static final ModelProperty<BlockPos> POS = new ModelProperty<>();

    public static final ModelProperty<Integer> PIPE_CONNECTION_MASK = new ModelProperty<>();
    public static final ModelProperty<Integer> PIPE_BLOCKED_MASK = new ModelProperty<>();
    public static final ModelProperty<BlockState> PIPE_FRAME_STATE = new ModelProperty<>();
    public static final ModelProperty<Integer> PIPE_COVER_MASK = new ModelProperty<>();
    public static final ModelProperty<List<NativeCoverSnapshot>> PIPE_COVER_GEOMETRY = new ModelProperty<>();
    public static final ModelProperty<Integer> PIPE_PAINT_COLOR = new ModelProperty<>();
    public static final ModelProperty<Integer> PIPE_FRAME_PRIMARY_COLOR = new ModelProperty<>();
    public static final ModelProperty<Integer> PIPE_FRAME_SECONDARY_COLOR = new ModelProperty<>();
}
