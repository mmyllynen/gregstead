package com.gregtechceu.gtceu.client.renderer;

import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.pattern.MultiblockShapeInfo;

import com.lowdragmc.lowdraglib.utils.data.BlockInfo;
import com.lowdragmc.lowdraglib.utils.virtuallevel.TrackedDummyWorld;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import com.mojang.blaze3d.vertex.*;
import org.jetbrains.annotations.Nullable;

import java.util.*;


@OnlyIn(Dist.CLIENT)
public class MultiblockInWorldPreviewRenderer {

    private static final com.lowdragmc.lowdraglib.client.scene.NativeSceneGeometryCache<BlockPos> GEOMETRY =
            new com.lowdragmc.lowdraglib.client.scene.NativeSceneGeometryCache<>();
    private static TrackedDummyWorld LEVEL;
    private static Set<BlockPos> BLOCK_ENTITIES = Set.of();
    private static int LEFT_TICK = -1;
    private static int DURATION;

    @Nullable
    private static BlockPos LAST_POS = null;
    private static int LAST_LAYER = -1;

    public static void cleanPreview() {
        GEOMETRY.clear();
        LEVEL = null;
        BLOCK_ENTITIES = Set.of();
        LEFT_TICK = -1;
        LAST_POS = null;
        LAST_LAYER = -1;
    }

    public static void removePreview(BlockPos pos) {
        if (LAST_POS != null && LAST_POS.equals(pos)) {
            cleanPreview();
        }
    }

    /**
     * Show the multiblock preview in the world by the given pos, side, and shape info.
     *
     * @param pos        the pos of the controller
     * @param controller the controller
     * @param duration   the duration of the preview. in ticks.
     */
    public static void showPreview(BlockPos pos, MultiblockControllerMachine controller,
                                   int duration) {
        if (!controller.getDefinition().isRenderWorldPreview()) return;
        Direction front = controller.getFrontFacing();
        Direction up = controller.getUpwardsFacing();
        var shapes = controller.getDefinition().getMatchingShapes();
        if (shapes.isEmpty()) { cleanPreview(); return; }
        MultiblockShapeInfo shapeInfo = shapes.getFirst();

        Map<BlockPos, BlockInfo> blockMap = new HashMap<>();
        MultiblockControllerMachine controllerBase = null;
        LEVEL = new TrackedDummyWorld();

        var blocks = shapeInfo.getBlocks();
        BlockPos controllerPatternPos = null;
        var maxY = 0;
        // find the pos of controller
        for (int x = 0; x < blocks.length; x++) {
            BlockInfo[][] aisle = blocks[x];
            maxY = Math.max(maxY, aisle.length);
            for (int y = 0; y < aisle.length; y++) {
                BlockInfo[] column = aisle[y];
                for (int z = 0; z < column.length; z++) {
                    var blockState = column[z].getBlockState();
                    // if its controller record its position offset.
                    if (blockState.getBlock() instanceof MetaMachineBlock machineBlock &&
                            machineBlock.getDefinition() instanceof MultiblockMachineDefinition) {
                        controllerPatternPos = new BlockPos(x, y, z);
                    }
                }
            }
        }

        if (controllerPatternPos == null) { // if there is no controller found
            cleanPreview();
            return;
        }

        if (LAST_POS != null && LAST_POS.equals(pos)) {
            LAST_LAYER++;
            if (LAST_LAYER >= maxY) {
                LAST_LAYER = -1;
            }
        } else {
            LAST_LAYER = -1;
        }
        LAST_POS = pos;

        for (int x = 0; x < blocks.length; x++) {
            BlockInfo[][] aisle = blocks[x];
            for (int y = 0; y < aisle.length; y++) {
                BlockInfo[] column = aisle[y];
                if (LAST_LAYER != -1 && LAST_LAYER != y) {
                    continue;
                }
                for (int z = 0; z < column.length; z++) {
                    var blockState = column[z].getBlockState();
                    var offset = new BlockPos(x, y, z).subtract(controllerPatternPos);

                    // rotation
                    offset = switch (front) {
                        case NORTH, UP, DOWN -> offset.rotate(Rotation.NONE);
                        case SOUTH -> offset.rotate(Rotation.CLOCKWISE_180);
                        case EAST -> offset.rotate(Rotation.COUNTERCLOCKWISE_90);
                        case WEST -> offset.rotate(Rotation.CLOCKWISE_90);
                    };

                    Rotation r = up == Direction.NORTH ? Rotation.NONE : up == Direction.EAST ? Rotation.CLOCKWISE_90 :
                            up == Direction.SOUTH ? Rotation.CLOCKWISE_180 :
                                    up == Direction.WEST ? Rotation.COUNTERCLOCKWISE_90 : Rotation.NONE;

                    offset = rotateByFrontAxis(offset, front, r);

                    if (blockState.getBlock() instanceof MetaMachineBlock machineBlock) {
                        var rotationState = machineBlock.getRotationState();
                        if (rotationState != RotationState.NONE) {
                            var face = blockState.getValue(rotationState.property);
                            if (face.getAxis() != Direction.Axis.Y) {
                                face = switch (front) {
                                    case NORTH, UP, DOWN -> front;
                                    case SOUTH -> face.getOpposite();
                                    case WEST -> face.getCounterClockWise();
                                    case EAST -> face.getClockWise();
                                };
                            }
                            if (rotationState.test(face)) {
                                blockState = blockState.setValue(rotationState.property, face);
                            }
                        }
                    }

                    BlockPos realPos = pos.offset(offset);

                    // spotless:off
                    if (column[z].getBlockEntity(realPos, controller.getLevel().registryAccess()) instanceof MultiblockControllerMachine cont) {
                        cont.setLevel(LEVEL);
                        controllerBase = cont;
                    } else {
                        blockMap.put(realPos, BlockInfo.fromBlockState(blockState));
                    }
                    // spotless:on
                }
            }
        }

        LEVEL.addBlocks(blockMap);
        if (controllerBase != null) {
            LEVEL.setInnerBlockEntity(controllerBase.self());
        }

        prepareBuffers(LEVEL, blockMap.keySet(), duration);
    }

    private static BlockPos rotateByFrontAxis(BlockPos pos, Direction front, Rotation rotation) {
        if (front.getAxis() == Direction.Axis.X) {
            return switch (rotation) {
                default -> new BlockPos(-pos.getX(), pos.getY(), -pos.getZ());
                case CLOCKWISE_90 -> new BlockPos(-pos.getX(), -front.getAxisDirection().getStep() * pos.getZ(),
                        front.getAxisDirection().getStep() * -pos.getY());
                case CLOCKWISE_180 -> new BlockPos(-pos.getX(), -pos.getY(), pos.getZ());
                case COUNTERCLOCKWISE_90 -> new BlockPos(-pos.getX(), front.getAxisDirection().getStep() * pos.getZ(),
                        front.getAxisDirection().getStep() * pos.getY());
            };
        } else if (front.getAxis() == Direction.Axis.Y) {
            return switch (rotation) {
                default -> new BlockPos(-front.getAxisDirection().getStep() * pos.getX(),
                        -front.getAxisDirection().getStep() * pos.getZ(),
                        -pos.getY());
                case CLOCKWISE_90 -> new BlockPos(pos.getY(),
                        -front.getAxisDirection().getStep() * pos.getZ(),
                        -front.getAxisDirection().getStep() * pos.getX());
                case CLOCKWISE_180 -> new BlockPos(front.getAxisDirection().getStep() * pos.getX(),
                        -front.getAxisDirection().getStep() * pos.getZ(),
                        pos.getY());
                case COUNTERCLOCKWISE_90 -> new BlockPos(-pos.getY(),
                        -front.getAxisDirection().getStep() * pos.getZ(),
                        front.getAxisDirection().getStep() * pos.getX());
            };
        } else if (front.getAxis() == Direction.Axis.Z) {
            return switch (rotation) {
                default -> pos;
                case CLOCKWISE_90 -> new BlockPos(front.getAxisDirection().getStep() * pos.getY(),
                        -front.getAxisDirection().getStep() * pos.getX(), pos.getZ());
                case CLOCKWISE_180 -> new BlockPos(-pos.getX(), -pos.getY(), pos.getZ());
                case COUNTERCLOCKWISE_90 -> new BlockPos(front.getAxisDirection().getStep() * -pos.getY(),
                        front.getAxisDirection().getStep() * pos.getX(), pos.getZ());
            };
        }
        return pos;
    }

    public static void onClientTick() {
        if (LEFT_TICK > 0 && --LEFT_TICK <= 0) cleanPreview();
    }

    public record EntityPart(BlockPos pos, com.lowdragmc.lowdraglib.client.renderer.NativeDelegatedBlockEntity.Submission submission) {}
    public record Snapshot(java.util.List<com.lowdragmc.lowdraglib.client.scene.NativeSceneBlockGeometry> geometry,
                           java.util.List<EntityPart> entities) {
        public Snapshot { geometry=List.copyOf(geometry);entities=List.copyOf(entities); }
        public void submit(PoseStack pose, net.minecraft.client.renderer.SubmitNodeCollector collector,
                           net.minecraft.client.renderer.state.level.CameraRenderState camera) {
            pose.pushPose();
            try {
                pose.translate(-camera.pos.x,-camera.pos.y,-camera.pos.z);
                for(var block:geometry)block.submit(pose,collector);
                for(var entity:entities) {
                    pose.pushPose();
                    try {
                        pose.translate(entity.pos().getX(),entity.pos().getY(),entity.pos().getZ());
                        entity.submission().submit(pose,collector,camera);
                    } finally { pose.popPose(); }
                }
            } finally { pose.popPose(); }
        }
    }

    public static Snapshot extractPreview(Vec3 camera,float partialTicks) {
        if(LEVEL==null)return new Snapshot(List.of(),List.of());
        boolean compiling=GEOMETRY.compiling();
        GEOMETRY.advance(64);
        if(compiling && !GEOMETRY.compiling())LEFT_TICK=DURATION;
        if(GEOMETRY.compiling())return new Snapshot(List.of(),List.of());
        var entities=new ArrayList<EntityPart>();
        for(var pos:BLOCK_ENTITIES) {
            var entity=LEVEL.getBlockEntity(pos);
            if(entity==null || !entity.hasLevel() || !entity.getType().isValid(entity.getBlockState()))continue;
            var submission=com.lowdragmc.lowdraglib.client.renderer.NativeDelegatedBlockEntity.extract(
                    entity,partialTicks,camera,null,net.minecraft.util.LightCoordsUtil.FULL_BRIGHT);
            if(submission!=null)entities.add(new EntityPart(pos,submission));
        }
        return new Snapshot(GEOMETRY.snapshot(),entities);
    }

    private static void prepareBuffers(TrackedDummyWorld level, Collection<BlockPos> renderedBlocks, int duration) {
        GEOMETRY.clear();LEFT_TICK=-1;DURATION=duration;
        var entities=new HashSet<BlockPos>();
        for(var pos:renderedBlocks)if(level.getBlockEntity(pos)!=null)entities.add(pos.immutable());
        BLOCK_ENTITIES=Set.copyOf(entities);
        GEOMETRY.rebuild(renderedBlocks.stream().map(BlockPos::immutable).toList(),pos -> {
            var state=level.getBlockState(pos);
            level.setRenderFilter(candidate -> candidate.equals(pos));
            try {
                return com.lowdragmc.lowdraglib.client.scene.NativeSceneBlockGeometry.extract(level,pos,state,
                        Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(state),true,true,null,
                        output -> new NativePreviewVertices(output,pos));
            } finally { level.setRenderFilter(candidate -> true); }
        });
    }
}
