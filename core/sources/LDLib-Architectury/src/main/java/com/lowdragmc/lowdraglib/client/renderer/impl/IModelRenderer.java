package com.lowdragmc.lowdraglib.client.renderer.impl;

import com.lowdragmc.lowdraglib.LDLib;
import com.lowdragmc.lowdraglib.client.model.ModelFactory;
import com.lowdragmc.lowdraglib.client.model.NativeModelCache;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import com.lowdragmc.lowdraglib.client.renderer.IBlockRendererProvider;
import com.lowdragmc.lowdraglib.client.renderer.IRenderer;
import com.lowdragmc.lowdraglib.editor.ColorPattern;
import com.lowdragmc.lowdraglib.editor.annotation.ConfigSetter;
import com.lowdragmc.lowdraglib.editor.annotation.Configurable;
import com.lowdragmc.lowdraglib.registry.annotation.LDLRegisterClient;
import com.lowdragmc.lowdraglib.editor.configurator.ConfiguratorGroup;
import com.lowdragmc.lowdraglib.editor.configurator.StringConfigurator;
import com.lowdragmc.lowdraglib.editor.configurator.WrapperConfigurator;
import com.lowdragmc.lowdraglib.editor.ui.Editor;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.DialogWidget;
import lombok.Getter;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import java.util.Set;
import net.neoforged.neoforge.model.data.ModelData;
import net.minecraft.util.TriState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@LDLRegisterClient(name = "json_model", registry = "ldlib:renderer")
public class IModelRenderer implements IRenderer {
    @Getter
    @Configurable(forceUpdate = false)
    protected Identifier modelLocation;

    @OnlyIn(Dist.CLIENT)
    protected NativeModelCache.Item itemModel;

    @OnlyIn(Dist.CLIENT)
    protected Map<ModelState, BlockStateModelPart> modelCaches;
    protected NativeModelCache nativeCache;
    protected com.lowdragmc.lowdraglib.client.model.NativeCachedItemModel nativeItemModel;

    protected IModelRenderer() {
        modelLocation = Identifier.withDefaultNamespace("block/furnace");
    }

    public IModelRenderer(Identifier modelLocation) {
        this.modelLocation = modelLocation;
        initRenderer();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    @Nonnull
    public TextureAtlasSprite getParticleTexture(@Nullable BlockAndTintGetter level, @Nullable BlockPos pos, ModelData modelData) {
        var model = getItemBakedModel();
        if (model == null) {
            return IRenderer.super.getParticleTexture(level, pos, modelData);
        }
        return model.particle().sprite();
    }

    public net.minecraft.client.renderer.item.ItemModel getNativeItemModel(ItemStack stack) {
        return ClientIModelRendererSupport.getNativeItemModel(this, stack);
    }

    @OnlyIn(Dist.CLIENT)
    protected UnbakedModel getModel() {
        return ModelFactory.getUnBakedModel(modelLocation);
    }



    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean useBlockLight(ItemStack stack) {
        var model = getItemBakedModel(stack);
        if (model != null) {
            return model.guiLight()==UnbakedModel.GuiLight.SIDE;
        }
        return false;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public TriState useAO() {
        var model = getItemBakedModel();
        if (model != null) {
            return model.ambientOcclusion() ? TriState.DEFAULT : TriState.FALSE;
        }
        return TriState.FALSE;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<BakedQuad> renderModel(@Nullable BlockAndTintGetter level, @Nullable BlockPos pos, @Nullable BlockState state, @Nullable Direction side, RandomSource rand, ModelData data, @Nullable ChunkSectionLayer renderType) {
        var ibakedmodel = getBlockBakedModel(level, pos, state);
        if (ibakedmodel == null) return Collections.emptyList();
        return com.lowdragmc.lowdraglib.client.model.NativeQuadLayers.select(ibakedmodel.getQuads(side), renderType);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public Set<ChunkSectionLayer> getRenderTypes(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource rand, ModelData modelData) {
        var ibakedmodel = getBlockBakedModel(level, pos, state);
        if (ibakedmodel != null) return com.lowdragmc.lowdraglib.client.model.NativeQuadLayers.layers(ibakedmodel);
        return IRenderer.super.getRenderTypes(level, pos, state, rand, modelData);
    }

    @OnlyIn(Dist.CLIENT)
    @Nullable
    protected NativeModelCache.Item getItemBakedModel() {
        if(nativeCache==null)nativeCache=new NativeModelCache(modelLocation);
        return itemModel=nativeCache.item();
    }

    @OnlyIn(Dist.CLIENT)
    @Nullable
    protected NativeModelCache.Item getItemBakedModel(ItemStack stack){return getItemBakedModel();}

    @OnlyIn(Dist.CLIENT)
    @Nullable
    protected BlockStateModelPart getBlockBakedModel(@Nullable BlockAndTintGetter level,@Nullable BlockPos pos,@Nullable BlockState state) {
        return ClientIModelRendererSupport.getBlockBakedModel(this, level, pos, state);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onPrepareTextureAtlas(Identifier atlasName, Consumer<Identifier> register) {
        if (atlasName.equals(TextureAtlas.LOCATION_BLOCKS)) {
            itemModel = null;
            if(nativeCache!=null)nativeCache.clear();
            modelCaches.clear();
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onAdditionalModel(Consumer<Identifier> registry) {
        registry.accept(modelLocation);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean isGui3d() {
        var model = getItemBakedModel();
        if (model == null) {
            return IRenderer.super.isGui3d();
        }
        return model.gui3d();
    }

    // ISerializableRenderer
    public void initRenderer() {
        if (this.modelLocation != null) {
            if (LDLib.isClient()) {
                modelCaches = new ConcurrentHashMap<>();
                registerEvent();
            }
        }
    }

    @ConfigSetter(field = "modelLocation")
    @SuppressWarnings("unused")
    public void updateModelWithoutReloadingResource(Identifier modelLocation) {
        this.modelLocation = modelLocation;
        nativeCache=null;nativeItemModel=null;
        if (LDLib.isClient()) {
            itemModel = null;
            if(nativeCache!=null)nativeCache.clear();
            if (modelCaches != null) modelCaches.clear();
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void updateModelWithReloadingResource(Identifier modelLocation) {
        updateModelWithoutReloadingResource(modelLocation);
        if (ModelFactory.isMissingModel(modelLocation)) {
            Minecraft.getInstance().reloadResourcePacks();
        }
    }

    @Override
    public void buildConfigurator(ConfiguratorGroup father) {
        IRenderer.super.buildConfigurator(father);
        var locationConfigurator = father.getConfigurators().stream()
                .filter(configurator -> configurator instanceof StringConfigurator stringConfigurator && stringConfigurator.getName().equals("modelLocation"))
                .map(configurator -> (StringConfigurator) configurator)
                .findFirst();
        father.addConfigurators(new WrapperConfigurator(wrapper -> new ButtonWidget(0, 0, 90, 10,
                new GuiTextureGroup(ColorPattern.T_GRAY.rectTexture().setRadius(5), new TextTexture("ldlib.gui.editor.tips.select_model")), cd -> {
            if (Editor.INSTANCE == null) return;
            File path = new File(Editor.INSTANCE.getWorkSpace(), "models");
            DialogWidget.showFileDialog(Editor.INSTANCE, "ldlib.gui.editor.tips.select_model", path, true,
                    DialogWidget.suffixFilter(".json"), r -> {
                        if (r != null && r.isFile()) {
                            var newModel = getModelFromFile(path, r);
                            if (newModel.equals(modelLocation)) return;
                            locationConfigurator.ifPresent(stringConfigurator -> stringConfigurator.setValue(newModel.toString()));
                            updateModelWithReloadingResource(newModel);
                            wrapper.notifyChanges();
                        }
                    });
        })).setRemoveTitleBar(true));
    }

    private static Identifier getModelFromFile(File path, File r){
        var id = path.getPath().replace('\\', '/').split("assets/")[1].split("/")[0];
        return Identifier.fromNamespaceAndPath(id, r.getPath().replace(path.getPath(), "").replace(".json", "").replace('\\', '/').substring(1));
    }
}
