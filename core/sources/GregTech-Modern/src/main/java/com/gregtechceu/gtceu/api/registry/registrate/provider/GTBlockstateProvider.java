package com.gregtechceu.gtceu.api.registry.registrate.provider;

import com.gregtechceu.gtceu.api.block.property.GTBlockStateProperties;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.client.util.ExtendedBlockModelRotation;
import com.gregtechceu.gtceu.client.util.VariantRotationHelpers;
import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.providers.generators.RegistrateBlockModelGenerator;
import net.minecraft.client.data.models.ItemModelOutput;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.blockstates.*;
import net.minecraft.client.data.models.model.ModelInstance;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.client.renderer.block.dispatch.VariantMutator;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;
import java.util.*;
import java.util.function.*;

public class GTBlockstateProvider extends RegistrateBlockModelGenerator {
    public static final String Z_ROT_PROPERTY_NAME = "gtceu:z";
    private static GTBlockstateProvider CURRENT_PROVIDER;
    private final Map<Block, BlockModelDefinitionGenerator> generated = new LinkedHashMap<>();

    private final Set<Identifier> generatedModelIds;
    private Predicate<Identifier> textureResources;

    public GTBlockstateProvider(AbstractRegistrate<?> parent, Consumer<BlockModelDefinitionGenerator> states,
            ItemModelOutput items, BiConsumer<Identifier, ModelInstance> models) {
        this(parent, states, items, models, new HashSet<>());
    }

    private GTBlockstateProvider(AbstractRegistrate<?> parent, Consumer<BlockModelDefinitionGenerator> states,
            ItemModelOutput items, BiConsumer<Identifier, ModelInstance> models, Set<Identifier> ids) {
        super(parent, states, items, (id, model) -> { models.accept(id, model); ids.add(id); });
        generatedModelIds = ids;
    }

    public void setTextureResources(Predicate<Identifier> textures) {
        textureResources = Objects.requireNonNull(textures);
    }
    public Predicate<Identifier> textureResources() {
        return Objects.requireNonNull(textureResources, "Texture resource lookup must be supplied by datagen/runtime");
    }

    public boolean hasGeneratedModel(Identifier id) { return generatedModelIds.contains(id); }

    public static GTBlockstateProvider getCurrentProvider() { return CURRENT_PROVIDER; }

    @Override public void run() {
        var previous = CURRENT_PROVIDER;
        CURRENT_PROVIDER = this;
        try {
            super.run();
            emitPendingBlockstates();
        } finally { CURRENT_PROVIDER = previous; }
    }

    protected final void emitPendingBlockstates() { generated.values().forEach(blockStateOutput); }
    protected final void clearGeneratedCycle() {
        generated.clear();
        generatedModelIds.clear();
        seenBlockstates.clear();
    }

    public MultiVariantGenerator multiVariantGenerator(Block block, Variant base) {
        return addVanillaGenerator(block, MultiVariantGenerator.dispatch(block, BlockModelGenerators.variant(base)));
    }

    public MultiPartGenerator multiPartGenerator(Block block) {
        return addVanillaGenerator(block, MultiPartGenerator.multiPart(block));
    }

    public <T extends BlockModelDefinitionGenerator> T addVanillaGenerator(Block block, T generator) {
        if (generator.block() != block) throw new IllegalArgumentException("Blockstate generator belongs to another block");
        generated.put(block, generator);
        return generator;
    }

    public static @Nullable PropertyDispatch<VariantMutator> createFacingDispatch(RotationState rotationState,
            boolean extended) {
        if (rotationState == RotationState.NONE) return null;
        if (!extended) return PropertyDispatch.modify(rotationState.property).generate(front ->
                variant -> applyOrientation(variant, ExtendedBlockModelRotation.get(front, Direction.NORTH)));
        return PropertyDispatch.modify(rotationState.property, GTBlockStateProperties.UPWARDS_FACING).generate((front, up) ->
                variant -> applyOrientation(variant, ExtendedBlockModelRotation.get(front, up)));
    }

    public static Variant applyOrientation(Variant variant, ExtendedBlockModelRotation orientation) {
        return applyRotation(variant, orientation.getAngleX(), orientation.getAngleY(), orientation.getAngleZ());
    }

    public static Variant applyRotation(Variant variant, int x, int y, int z) {
        return VariantRotationHelpers.rotateVariant(variant, normalizeAngle(x), normalizeAngle(y), normalizeAngle(z));
    }

    private static int normalizeAngle(int angle) { return angle - (angle / 360) * 360; }

    public Optional<MultiVariantGenerator> getExistingMultiVariantGenerator(Block block) {
        return Optional.ofNullable(generated.get(block)).filter(MultiVariantGenerator.class::isInstance)
                .map(MultiVariantGenerator.class::cast);
    }

    public Optional<MultiPartGenerator> getExistingMultipartGenerator(Block block) {
        return Optional.ofNullable(generated.get(block)).filter(MultiPartGenerator.class::isInstance)
                .map(MultiPartGenerator.class::cast);
    }
}
