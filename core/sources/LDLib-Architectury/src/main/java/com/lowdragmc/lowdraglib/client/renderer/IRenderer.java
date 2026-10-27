package com.lowdragmc.lowdraglib.client.renderer;

import com.lowdragmc.lowdraglib.LDLibRegistries;
import com.lowdragmc.lowdraglib.client.renderer.block.RendererBlock;
import com.lowdragmc.lowdraglib.client.renderer.block.RendererBlockEntity;
import com.lowdragmc.lowdraglib.editor.ColorPattern;
import com.lowdragmc.lowdraglib.editor.configurator.ConfiguratorGroup;
import com.lowdragmc.lowdraglib.editor.configurator.IConfigurable;
import com.lowdragmc.lowdraglib.editor.configurator.WrapperConfigurator;
import com.lowdragmc.lowdraglib.gui.texture.ColorBorderTexture;
import com.lowdragmc.lowdraglib.gui.widget.SceneWidget;
import com.lowdragmc.lowdraglib.registry.ILDLRegisterClient;
import com.lowdragmc.lowdraglib.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib.utils.PersistedParser;
import com.lowdragmc.lowdraglib.utils.data.BlockInfo;
import com.lowdragmc.lowdraglib.utils.virtuallevel.TrackedDummyWorld;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import java.util.Set;
import net.neoforged.neoforge.model.data.ModelData;
import net.minecraft.util.TriState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public interface IRenderer extends NativeBlockRenderer, ILDLRegisterClient<IRenderer, Supplier<IRenderer>>, IConfigurable, IPersistedSerializable {
    IRenderer EMPTY = new IRenderer() {};
    Codec<IRenderer> CODEC = LDLibRegistries.RENDERERS.optionalCodec().dispatch(ILDLRegisterClient::getRegistryHolderOptional,
            optional -> optional.map(holder -> PersistedParser.createCodec(holder.value()).fieldOf("data"))
                    .orElseGet(() -> MapCodec.unit(EMPTY)));

    Set<IRenderer> EVENT_REGISTERS = new HashSet<>();

    // should be called after deserialization and only once.
    default void initRenderer() {
    }

    @Override
    default void afterDeserialize() {
        initRenderer();
    }


    /**
     * Render itemstack.
     */


    /**
     * Render static block model.
     */
    @OnlyIn(Dist.CLIENT)
    default List<BakedQuad> renderModel(@Nullable BlockAndTintGetter level, @Nullable BlockPos pos, @Nullable BlockState state, @Nullable Direction side, RandomSource rand, ModelData data, @Nullable ChunkSectionLayer renderType) {
        return Collections.emptyList();
    }

    /**
     * Gets the set of {@link RenderType render types} to use when drawing this block in the level.
     * Native chunk layers are selected from quad material data.
     * <p>
     * By default, reads layers from native quad materials.
     */
    @OnlyIn(Dist.CLIENT)
    default Set<ChunkSectionLayer> getRenderTypes(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource rand, ModelData modelData) {
        return NativeBlockRenderer.super.getRenderTypes(level, pos, state, rand, modelData);
    }

    /**
     * Register TextureSprite here.
     */
    @OnlyIn(Dist.CLIENT)
    default void onPrepareTextureAtlas(Identifier atlasName, Consumer<Identifier> register) {

    }

    /**
     * Register additional model here.
     */
    @OnlyIn(Dist.CLIENT)
    default void onAdditionalModel(Consumer<Identifier> registry) {

    }

    /**
     * If the renderer requires event registration either {@link #onPrepareTextureAtlas} or {@link #onAdditionalModel}, call this method in the constructor.
     */
    @OnlyIn(Dist.CLIENT)
    default void registerEvent() {
        synchronized (EVENT_REGISTERS) {
            EVENT_REGISTERS.add(this);
            com.lowdragmc.lowdraglib.client.model.NativeAtlasSources.register(this, this::onPrepareTextureAtlas);
        }
    }

    /**
     * Does the block entity have the TESR {@link net.minecraft.client.renderer.blockentity.BlockEntityRenderer}.
     */
    @OnlyIn(Dist.CLIENT)
    default boolean hasTESR(BlockEntity blockEntity) {
        return false;
    }

    /**
     * Is the TESR {@link net.minecraft.client.renderer.blockentity.BlockEntityRenderer} global.
     */
    @OnlyIn(Dist.CLIENT)
    default boolean isGlobalRenderer(BlockEntity blockEntity) {
        return false;
    }

    /**
     * Get the view distance for TESR.
     */
    @OnlyIn(Dist.CLIENT)
    default int getViewDistance() {
        return 64;
    }

    /**
     * Should the TESR {@link net.minecraft.client.renderer.blockentity.BlockEntityRenderer} render.
     */
    @OnlyIn(Dist.CLIENT)
    default boolean shouldRender(BlockEntity blockEntity, Vec3 cameraPos) {
        return Vec3.atCenterOf(blockEntity.getBlockPos()).closerThan(cameraPos, this.getViewDistance());
    }

    /**
     * Render the TESR {@link net.minecraft.client.renderer.blockentity.BlockEntityRenderer}.
     */


    /**
     * Get the particle texture.
     */
    @OnlyIn(Dist.CLIENT)
    @Nonnull
    default TextureAtlasSprite getParticleTexture(@Nullable BlockAndTintGetter level, @Nullable BlockPos pos, ModelData modelData) {
        return Minecraft.getInstance().getAtlasManager().get(new net.minecraft.client.resources.model.sprite.SpriteId(TextureAtlas.LOCATION_BLOCKS, MissingTextureAtlasSprite.getLocation()));
    }

    /**
     * Whether to apply AO for the model.
     */
    @OnlyIn(Dist.CLIENT)
    default TriState useAO() {
        return TriState.FALSE;
    }

    /**
     * Whether to apply AO for the model.
     */
    @OnlyIn(Dist.CLIENT)
    default TriState useAO(BlockState state, ModelData modelData, RenderType renderType) {
        return useAO();
    }

    /**
     * Whether to apply block light during the itemstack rendering.
     */
    @OnlyIn(Dist.CLIENT)
    default boolean useBlockLight(ItemStack stack) {
        return false;
    }

    /**
     * Should we rebake quads for mcmeta data?
     */
    @OnlyIn(Dist.CLIENT)
    default boolean reBakeCustomQuads() {
        return false;
    }

    /**
     * Offset for rebake's quads sides while {@link #reBakeCustomQuads()} return true.
     */
    @OnlyIn(Dist.CLIENT)
    default float reBakeCustomQuadsOffset() {
        return 0;
    }

    /**
     * Whether to apply gui 3d transform during itemstack rendering.
     */
    @OnlyIn(Dist.CLIENT)
    default boolean isGui3d() {
        return true;
    }

    /**
     * Return an {@link AABB} that controls the visible scope of this {@link BlockEntityRenderer}.
     * Defaults to the unit cube at the given position. {@link AABB#INFINITE} can be used to declare the BER
     * should be visible everywhere.
     *
     * @return an appropriately sized {@link AABB} for the {@link BlockEntityRenderer}
     */
    @OnlyIn(Dist.CLIENT)
    default AABB getRenderBoundingBox(BlockEntity blockEntity) {
        return new AABB(blockEntity.getBlockPos());
    }

    /**
     * Preview of the renderer.
     */
    default void createPreview(ConfiguratorGroup father) {
        ClientRendererPreview.create(this, father);
    }

    @Override
    default void buildConfigurator(ConfiguratorGroup father) {
        createPreview(father);
        IConfigurable.super.buildConfigurator(father);
    }

    @Nullable
    default CompoundTag serializeWrapper() {
        return (CompoundTag) CODEC.encodeStart(NbtOps.INSTANCE, this).result().orElse(null);
    }

    static IRenderer deserializeWrapper(Tag tag) {
        return CODEC.parse(NbtOps.INSTANCE, tag).result().orElse(EMPTY);
    }
}
