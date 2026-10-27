package com.gregtechceu.gtceu.client.renderer.machine.impl;

import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRender;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderType;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.core.mixins.GrowingPlantBlockAccessor;
import com.gregtechceu.gtceu.core.mixins.StemBlockAccessor;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.gregtechceu.gtceu.utils.GTMath;
import com.gregtechceu.gtceu.utils.memoization.GTMemoizer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import org.apache.commons.lang3.function.TriFunction;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class GrowingPlantRender extends DynamicRender<IRecipeLogicMachine, GrowingPlantRender>
        implements com.gregtechceu.gtceu.client.model.machine.NativeMachineRenderExtractor<IRecipeLogicMachine> {

    // spotless:off
    @SuppressWarnings("deprecation")
    public static final MapCodec<GrowingPlantRender> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            com.lowdragmc.lowdraglib.transfer.codec.MutableMathCodecs.VECTOR3F.listOf().fieldOf("offsets").forGetter(GrowingPlantRender::getOffsets),
            BuiltInRegistries.BLOCK.byNameCodec().optionalFieldOf("growing_block").forGetter(GrowingPlantRender::getGrowingBlock),
            GrowthMode.CODEC.optionalFieldOf("growth_mode").forGetter(GrowingPlantRender::getGrowthMode)
    ).apply(instance, GrowingPlantRender::new));
    public static final DynamicRenderType<IRecipeLogicMachine, GrowingPlantRender> TYPE = new DynamicRenderType<>(GrowingPlantRender.CODEC);
    // spotless:on

    private static final float EPSILON = 1e-25f;

    @Getter
    private final List<Vector3f> offsets;
    @Getter
    private final Optional<Block> growingBlock;
    @Getter
    private final Optional<GrowthMode> growthMode;

    public GrowingPlantRender(List<Vector3f> offsets) {
        this(offsets, Optional.empty(), Optional.empty());
    }

    public GrowingPlantRender(List<Vector3f> offsets, Optional<Block> growingBlock, Optional<GrowthMode> growthMode) {
        this.offsets = offsets;
        this.growingBlock = growingBlock;
        this.growthMode = growthMode;
    }

    @Override
    public DynamicRenderType<IRecipeLogicMachine, GrowingPlantRender> getType() {
        return TYPE;
    }

    @Override
    public int getViewDistance() {
        return 32;
    }

    @Override
    public AABB getRenderBoundingBox(IRecipeLogicMachine machine) {
        final BlockPos pos = machine.self().getBlockPos();

        List<BlockPos> positions = new ArrayList<>();
        Collections.addAll(positions, pos.offset(-1, 0, -1), pos.offset(2, 2, 2));
        for (Vector3f offset : this.offsets) {
            positions.add(BlockPos.containing(offset.x(), offset.y(), offset.z()));
        }

        return BoundingBox.encapsulatingPositions(positions).map(AABB::of)
                .orElseGet(() -> super.getRenderBoundingBox(machine));
    }

    @Override
    public com.gregtechceu.gtceu.client.model.machine.NativeMachineRenderSubmission extractSubmission(IRecipeLogicMachine rlm,float partialTick) {
        if (!ConfigHolder.INSTANCE.client.renderer.renderGrowingPlants) return (pose,collector)->{};
        if (!rlm.isActive()) return (pose,collector)->{};
        final RecipeLogic recipeLogic = rlm.getRecipeLogic();

        Optional<Block> currentBlock = this.growingBlock
                .or(() -> Optional.ofNullable(recipeLogic.getLastRecipe()).flatMap(this::findGrowing));
        if (currentBlock.isEmpty()) return (pose,collector)->{};
        Block growing = currentBlock.get();
        BlockState state = growing.defaultBlockState();

        double progress = recipeLogic.getProgressPercent();
        GrowthMode mode = this.growthMode.orElseGet(() -> getGrowthModeForBlock(growing));

        // a couple of special case replacements in case of small mistakes in manual configuration
        if (this.growthMode.isPresent() && !mode.predicate().test(growing)) {
            if (mode == GrowthMode.GROWING_PLANT && GrowthMode.DOUBLE_TRANSLATE.predicate.test(growing)) {
                mode = GrowthMode.DOUBLE_TRANSLATE;
            }
            if (mode == GrowthMode.AGE_4 && GrowthMode.PICKLES.predicate().test(growing)) {
                // special case the pickles property to work if using age_4
                mode = GrowthMode.PICKLES;
            } else if (mode == GrowthMode.AGE_4 && GrowthMode.FLOWER_AMOUNT.predicate().test(growing)) {
                // special case the flower amount property to work if using age_4
                mode = GrowthMode.FLOWER_AMOUNT;
            } else if (mode == GrowthMode.AGE_7 && GrowthMode.STEM.predicate().test(growing)) {
                // special case stem plants to show stems
                mode = GrowthMode.STEM;
            } else {
                // generic incompatibility, use default mode
                mode = GrowthMode.SCALE;
            }
        }

        MetaMachine machine = rlm.self();
        Level level = machine.getLevel();
        assert level != null;
        BlockPos machinePos = machine.getBlockPos();

        var statesToDraw = mode.renderFunction().configureState((BlockAndTintGetter) level, state, progress);

        var captures = new java.util.ArrayList<com.gregtechceu.gtceu.client.model.machine.NativeMachineRenderSubmission>();
        for(Vector3fc offset:getOffsets()) {
            var rotated=NativePlantPlacement.rotate(offset,machine.getFrontFacing());
            BlockPos pos=machinePos.offset(BlockPos.containing(rotated.x(),rotated.y(),rotated.z()));
            for(StateWithOffset draw:statesToDraw) {
                var pose=new PoseStack();pose.translate(rotated.x(),rotated.y()+EPSILON,rotated.z());
                pose.translate(draw.offset().x(),draw.offset().y(),draw.offset().z());
                captures.add(mode.renderFunction().extractGrowingBlock((BlockAndTintGetter)level,pos,rotated,draw.state(),progress,pose));
            }
        }
        var prepared=java.util.List.copyOf(captures);
        return (pose,collector)->{for(var capture:prepared)capture.submit(pose,collector);};
    }

    public void drawBlocks(BlockAndTintGetter level, BlockPos machinePos, Direction frontFacing,
                           double progress, GrowthMode mode, BlockState state,
                           PoseStack poseStack, net.minecraft.client.renderer.SubmitNodeCollector collector) {
        for (final Vector3fc offset : getOffsets()) {
            poseStack.pushPose();

            Vector3f rotated = new Vector3f(offset);
            rotated.rotateX(-Mth.HALF_PI);
            frontFacing.getRotation().transform(rotated);
            poseStack.translate(rotated.x(), rotated.y(), rotated.z());

            poseStack.translate(0, 1 + EPSILON, 0);
            poseStack.translate(0.0, (progress * 2) % (1 + EPSILON) - 1, 0.0);
            if (mode == GrowthMode.GROWING_PLANT && state.getBlock() instanceof GrowingPlantBlock gp) {
                poseStack.last().pose().rotateAround(
                        ((GrowingPlantBlockAccessor) gp).gtceu$getGrowthDirection().getRotation(), 0.5f, 0.5f, 0.5f);
            }
            extractBlock(level,machinePos,state,poseStack).submit(new PoseStack(),collector);

            poseStack.popPose();
        }
    }

    private static com.gregtechceu.gtceu.client.model.machine.NativeMachineRenderSubmission extractBlock(
            BlockAndTintGetter level,BlockPos pos,BlockState state,PoseStack pose) {
        var geometry=com.gregtechceu.gtceu.client.util.NativeBlockDraw.extract(level,pos,state);
        var transform=new org.joml.Matrix4f(pose.last().pose());
        return (target,collector)->{target.pushPose();try{target.mulPose(transform);geometry.submit(target,collector);}finally{target.popPose();}};
    }

    protected Optional<Block> findGrowing(GTRecipe recipe) {
        return RECIPE_BLOCK_CACHE.apply(recipe);
    }

    private static final Function<GTRecipe, Optional<Block>> RECIPE_BLOCK_CACHE = GTMemoizer
            .memoizeFunctionWeakIdent(recipe -> {
                List<Content> allItemContents = new ArrayList<>();
                allItemContents.addAll(recipe.getInputContents(ItemRecipeCapability.CAP));
                allItemContents.addAll(recipe.getTickInputContents(ItemRecipeCapability.CAP));
                allItemContents.addAll(recipe.getOutputContents(ItemRecipeCapability.CAP));
                allItemContents.addAll(recipe.getTickOutputContents(ItemRecipeCapability.CAP));
                return allItemContents.stream()
                        .map(Content::getContent).map(ItemRecipeCapability.CAP::of)
                        .map(com.lowdragmc.lowdraglib.transfer.recipe.IngredientStacks::getItems).flatMap(Arrays::stream)
                        .map(ItemStack::getItem)
                        .filter(BlockItem.class::isInstance)
                        .findFirst()
                        .map(BlockItem.class::cast)
                        .map(BlockItem::getBlock);
            });

    protected GrowthMode getGrowthModeForBlock(Block block) {
        if (block instanceof GrowingPlantBlock) {
            return GrowthMode.GROWING_PLANT;
        }
        if (block instanceof StemBlock) {
            return GrowthMode.STEM;
        }

        BlockState state = block.defaultBlockState();
        IntegerProperty ageProp = findAgeProperty(state.getProperties());
        if (ageProp != null) {
            GrowthMode mode = GrowthMode.MODE_BY_PROPERTY.get(ageProp);
            if (mode != null) return mode;
        }

        if (state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF) ||
                state.hasProperty(BlockStateProperties.HALF) ||
                state.is(CustomTags.TALL_PLANTS)) {
            return GrowthMode.DOUBLE_TRANSLATE;
        } else if (state.is(BlockTags.FLOWERS)) {
            return GrowthMode.TRANSLATE;
        }
        // default to SCALE
        return GrowthMode.SCALE;
    }

    public static @Nullable IntegerProperty findAgeProperty(Collection<Property<?>> properties) {
        for (Property<?> prop : properties) {
            if ((prop.getName().equals("age") || prop.getName().equals("pickles") ||
                    prop.getName().equals("flower_amount")) &&
                    prop instanceof IntegerProperty intProp) {
                return intProp;
            }
        }
        return null;
    }

    public record GrowthMode(String name,
                             Predicate<Block> predicate,
                             RenderFunction renderFunction) {

        public static final Map<String, GrowthMode> VALUES = new HashMap<>();
        public static final Map<IntegerProperty, GrowthMode> MODE_BY_PROPERTY = new HashMap<>();

        public static final GrowthMode NONE = new GrowthMode("none", RenderFunction.NO_OP);
        public static final GrowthMode SCALE = new GrowthMode("scale", RenderFunction.SCALE);
        public static final GrowthMode TRANSLATE = new GrowthMode("translate", RenderFunction.TRANSLATE);

        public static final GrowthMode DOUBLE_TRANSLATE = new GrowthMode("double_translate",
                RenderFunction.DOUBLE_BLOCK);
        public static final GrowthMode GROWING_PLANT = new GrowthMode("growing_plant",
                block -> block instanceof GrowingPlantBlock, RenderFunction.GROWING_PLANT);

        public static final GrowthMode STEM = new GrowthMode("stem", block -> block instanceof StemBlock,
                RenderFunction.STEM);

        // all the different age properties. not going to add extras, though.
        public static final GrowthMode AGE_1 = ofIntegerProperty("age_1", BlockStateProperties.AGE_1);
        public static final GrowthMode AGE_2 = ofIntegerProperty("age_2", BlockStateProperties.AGE_2);
        public static final GrowthMode AGE_3 = ofIntegerProperty("age_3", BlockStateProperties.AGE_3);
        public static final GrowthMode AGE_4 = ofIntegerProperty("age_4", BlockStateProperties.AGE_4);
        public static final GrowthMode AGE_5 = ofIntegerProperty("age_5", BlockStateProperties.AGE_5);
        public static final GrowthMode AGE_7 = ofIntegerProperty("age_7", BlockStateProperties.AGE_7);
        public static final GrowthMode AGE_15 = ofIntegerProperty("age_15", BlockStateProperties.AGE_15);
        public static final GrowthMode AGE_25 = ofIntegerProperty("age_25", BlockStateProperties.AGE_25);

        public static final GrowthMode PICKLES = ofIntegerProperty("pickles", BlockStateProperties.PICKLES, 0, 4);
        public static final GrowthMode FLOWER_AMOUNT = ofIntegerProperty("flower_amount",
                BlockStateProperties.FLOWER_AMOUNT, 0, 4);

        private static final Codec<GrowthMode> CODEC = Codec.STRING.comapFlatMap(name -> {
            GrowthMode mode = VALUES.get(name);
            if (mode != null) {
                return DataResult.success(mode);
            } else {
                // default to SCALE in case of an error
                return DataResult.error(() -> "Could not find growth mode named " + name, SCALE);
            }
        }, GrowthMode::name);

        public GrowthMode {
            VALUES.put(name, this);
        }

        public GrowthMode(String name, RenderFunction renderFunction) {
            this(name, block -> true, renderFunction);
        }

        public static GrowthMode ofIntegerProperty(String name, IntegerProperty property) {
            return ofIntegerProperty(name, property, OptionalInt.empty(), OptionalInt.empty());
        }

        public static GrowthMode ofIntegerProperty(String name, IntegerProperty property, int min, int max) {
            return ofIntegerProperty(name, property, OptionalInt.of(min), OptionalInt.of(max));
        }

        public static GrowthMode ofIntegerProperty(String name, IntegerProperty property, OptionalInt min,
                                                   OptionalInt max) {
            GrowthMode mode = new GrowthMode(name,
                    block -> block.getStateDefinition().getProperties().contains(property),
                    RenderFunction.byIntegerProperty(property, min, max));
            MODE_BY_PROPERTY.put(property, mode);
            return mode;
        }
    }

    @FunctionalInterface
    public interface RenderFunction {

        com.gregtechceu.gtceu.client.model.machine.NativeMachineRenderSubmission extractGrowingBlock(BlockAndTintGetter level, BlockPos pos, Vector3f offset, BlockState state,
                                double progress, PoseStack poseStack);

        default Collection<StateWithOffset> configureState(BlockAndTintGetter level, BlockState state,
                                                           double progress) {
            return Collections.singleton(new StateWithOffset(state));
        }

        RenderFunction NO_OP = (level, pos, offset, state, progress, poseStack) -> (pose,collector)->{};

        RenderFunction SCALE = (level, pos, offset, state, progress, poseStack) -> {
            poseStack.last().pose().scaleAround((float) progress, 0.5f, 0.0f, 0.5f);
            poseStack.last().normal().scale((float) progress);

            return extractBlock(level,pos,state,poseStack);
        };

        RenderFunction.ConfigureOnly TRANSLATE = (level, state, progress) -> {
            Vector3fc translation = new Vector3f(0, (float) (progress - 1), 0);
            return Collections.singleton(new StateWithOffset(state, translation));
        };

        RenderFunction.ConfigureOnly DOUBLE_BLOCK = (level, state, progress) -> {
            Vector3fc translation = new Vector3f(0, (float) (progress * 2 - 1), 0);

            if (progress > 0.5) {
                Vector3fc bottomTranslation = new Vector3f(translation.x(), translation.y() - 1, translation.z());

                BlockState topState = state;
                if (state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)) {
                    topState = topState.trySetValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER);
                } else if (state.hasProperty(BlockStateProperties.HALF)) {
                    topState = topState.trySetValue(BlockStateProperties.HALF, Half.TOP);
                }

                return Arrays.asList(new StateWithOffset(state, bottomTranslation),
                        new StateWithOffset(topState, translation));
            } else {
                if (state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)) {
                    state = state.trySetValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER);
                } else if (state.hasProperty(BlockStateProperties.HALF)) {
                    state = state.trySetValue(BlockStateProperties.HALF, Half.TOP);
                }

                return Collections.singleton(new StateWithOffset(state, translation));
            }
        };

        RenderFunction GROWING_PLANT = new RenderFunction() {

            @Override
            public com.gregtechceu.gtceu.client.model.machine.NativeMachineRenderSubmission extractGrowingBlock(BlockAndTintGetter level, BlockPos pos, Vector3f offset, BlockState state,
                                           double progress, PoseStack poseStack) {
                GrowingPlantBlockAccessor accessor = (GrowingPlantBlockAccessor) state.getBlock();
                poseStack.rotateAround(accessor.gtceu$getGrowthDirection().getRotation(), 0.5f, 0.5f, 0.5f);

                return extractBlock(level,pos,state,poseStack);
            }

            @Override
            public Collection<StateWithOffset> configureState(BlockAndTintGetter level, BlockState state,
                                                              double progress) {
                GrowingPlantBlockAccessor accessor = (GrowingPlantBlockAccessor) state.getBlock();

                Vector3fc translation = new Vector3f(0, (float) (progress * 2 - 1), 0);

                if (progress < 0.5) {
                    BlockState headState = accessor.gtceu$getHeadBlock().defaultBlockState();
                    IntegerProperty ageProp = findAgeProperty(headState.getProperties());
                    if (ageProp != null) {
                        int minValue = ageProp.getPossibleValues().getFirst();
                        int maxValue = ageProp.getPossibleValues().getLast();

                        int stage = GTMath.lerpInt(progress, minValue, maxValue + 1);
                        headState = headState.trySetValue(ageProp, Math.min(stage, maxValue));
                    }

                    if (progress >= 0.25 && headState.hasProperty(BlockStateProperties.BERRIES)) {
                        headState = headState.trySetValue(CaveVines.BERRIES, true);
                    }

                    return Collections.singleton(new StateWithOffset(headState, translation));
                } else {
                    BlockState headState = accessor.gtceu$getHeadBlock().defaultBlockState();
                    IntegerProperty ageProp = findAgeProperty(headState.getProperties());
                    if (ageProp != null) {
                        headState = headState.trySetValue(ageProp, ageProp.getPossibleValues().getLast());
                    }
                    if (headState.hasProperty(BlockStateProperties.BERRIES)) {
                        headState = headState.trySetValue(CaveVines.BERRIES, true);
                    }

                    BlockState bodyState = accessor.gtceu$getBodyBlock().defaultBlockState();
                    if (progress >= 0.75 && bodyState.hasProperty(BlockStateProperties.BERRIES)) {
                        bodyState = bodyState.trySetValue(CaveVines.BERRIES, true);
                    }
                    Vector3fc bodyTranslation = new Vector3f(translation.x(), translation.y() - 1, translation.z());

                    return Arrays.asList(new StateWithOffset(bodyState, bodyTranslation),
                            new StateWithOffset(headState, translation));
                }
            }
        };

        RenderFunction.ConfigureOnly STEM = (level, state, progress) -> {
            final StemBlock block = (StemBlock) state.getBlock();
            final int growthStage = GTMath.lerpInt(progress, 0, StemBlock.MAX_AGE + 2);
            if (growthStage > StemBlock.MAX_AGE)
                return List.of(new StateWithOffset(BuiltInRegistries.BLOCK.getOrThrow(((StemBlockAccessor) block).gtceu$getFruit()).value().defaultBlockState()));
            state = state.trySetValue(StemBlock.AGE, growthStage);
            return List.of(new StateWithOffset(state));
        };

        TriFunction<IntegerProperty, OptionalInt, OptionalInt, ConfigureOnly> PROPERTY_FUNCTION_CACHE = GTMemoizer
                .memoize((property, setMin, setMax) -> {
                    final int presumedMinValue = property.getPossibleValues().getFirst();
                    final int presumedMaxValue = property.getPossibleValues().getLast();
                    return (level, state, progress) -> {
                        final int min = setMin.orElse(presumedMinValue);
                        final int betterMaxValue = state.getBlock() instanceof CropBlock crop ?
                                Math.max(presumedMaxValue, crop.getMaxAge()) : presumedMaxValue;
                        final int max = setMax.orElse(betterMaxValue);
                        int growthStage = GTMath.lerpInt(progress, min, max + 1);
                        if (growthStage < presumedMinValue) {
                            return Collections.emptySet();
                        }
                        if (state.getBlock() instanceof CropBlock crop) {
                            state = crop.getStateForAge(Math.min(growthStage, betterMaxValue));
                        } else {
                            state = state.trySetValue(property, Math.min(growthStage, betterMaxValue));
                        }
                        if (state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)) {
                            final var topState = state.trySetValue(BlockStateProperties.DOUBLE_BLOCK_HALF,
                                    DoubleBlockHalf.UPPER);
                            return List.of(new StateWithOffset(state),
                                    new StateWithOffset(topState, new Vector3f(0, 1, 0)));
                        }
                        if (state.hasProperty(BlockStateProperties.HALF)) {
                            final var topState = state.trySetValue(BlockStateProperties.HALF, Half.TOP);
                            return List.of(new StateWithOffset(state),
                                    new StateWithOffset(topState, new Vector3f(0, 1, 0)));
                        }

                        return List.of(new StateWithOffset(state));
                    };
                });

        static RenderFunction.ConfigureOnly byIntegerProperty(IntegerProperty property, OptionalInt min,
                                                              OptionalInt max) {
            return PROPERTY_FUNCTION_CACHE.apply(property, min, max);
        }

        @FunctionalInterface
        interface ConfigureOnly extends RenderFunction {

            @Override
            default com.gregtechceu.gtceu.client.model.machine.NativeMachineRenderSubmission extractGrowingBlock(BlockAndTintGetter level, BlockPos pos, Vector3f offset, BlockState state,
                                            double progress, PoseStack poseStack) {
                return extractBlock(level,pos,state,poseStack);
            }

            @Override
            Collection<StateWithOffset> configureState(BlockAndTintGetter level, BlockState state, double progress);
        }
    }

    public record StateWithOffset(BlockState state, Vector3fc offset) {

        private static final Vector3fc ZERO_VECTOR = new Vector3f();

        public StateWithOffset(BlockState state) {
            this(state, ZERO_VECTOR);
        }
    }
}
