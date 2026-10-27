package com.lowdragmc.lowdraglib.gui.texture;

import com.lowdragmc.lowdraglib.LDLibRegistries;
import com.lowdragmc.lowdraglib.editor.ColorPattern;
import com.lowdragmc.lowdraglib.editor.configurator.ConfiguratorGroup;
import com.lowdragmc.lowdraglib.editor.configurator.IConfigurable;
import com.lowdragmc.lowdraglib.editor.configurator.WrapperConfigurator;
import com.lowdragmc.lowdraglib.editor.data.resource.Resource;
import com.lowdragmc.lowdraglib.registry.ILDLRegisterClient;
import com.lowdragmc.lowdraglib.registry.annotation.LDLRegisterClient;
import com.lowdragmc.lowdraglib.utils.PersistedParser;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.NbtOps;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.function.Supplier;


public interface IGuiTexture extends IConfigurable, ILDLRegisterClient<IGuiTexture, Supplier<IGuiTexture>> {
    IGuiTexture MISSING_TEXTURE = new IGuiTexture() {
        @Override
        public IGuiTexture copy() {
            return this;
        }

        @OnlyIn(Dist.CLIENT)
        @Override
        public void draw(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float x, float y, int width, int height) {
            com.lowdragmc.lowdraglib.transfer.client.GuiPrimitives.missingTexture(graphics, x, y, width, height);
        }
    };

    Codec<IGuiTexture> NATIVE_CODEC = LDLibRegistries.GUI_TEXTURES.optionalCodec().dispatch(ILDLRegisterClient::getRegistryHolderOptional,
            optional -> optional.<MapCodec<IGuiTexture>>map(holder -> holder.clazz() == EmptyTexture.class
                    ? MapCodec.unit(IGuiTexture.EMPTY) : PersistedParser.createCodec(holder.value()).fieldOf("data"))
                    .orElseGet(() -> MapCodec.unit(MISSING_TEXTURE)));
    // Original persisted texture arrays wrap their compound payload in a typed NBT envelope.
    Codec<IGuiTexture> CODEC = Codec.withAlternative(NATIVE_CODEC,
            RecordCodecBuilder.<IGuiTexture>create(instance -> instance.group(
                    Codec.INT.validate(type -> type == 11 ? DataResult.success(type)
                            : DataResult.error(() -> "Expected legacy compound texture payload type 11"))
                            .fieldOf("t").forGetter(texture -> 11),
                    NATIVE_CODEC.fieldOf("p").forGetter(texture -> texture))
                    .apply(instance, (type, texture) -> texture)));

    @LDLRegisterClient(name = "empty", registry = "ldlib:gui_texture", manual = true)
    final class EmptyTexture implements IGuiTexture {
        @Override
        public IGuiTexture copy() { return EMPTY; }

        @OnlyIn(Dist.CLIENT)
        @Override
        public void draw(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float x, float y, int width, int height) {}
    }

    EmptyTexture EMPTY = new EmptyTexture();

    default IGuiTexture setColor(int color){
        return this;
    }

    default IGuiTexture rotate(float degree) {
        return this;
    }

    default IGuiTexture scale(float scale) {
        return this;
    }

    default IGuiTexture transform(int xOffset, int yOffset) {
        return this;
    }

    @OnlyIn(Dist.CLIENT)
    void draw(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float x, float y, int width, int height);

    @OnlyIn(Dist.CLIENT)
    default void updateTick() { }

    @OnlyIn(Dist.CLIENT)
    default void drawSubArea(GuiGraphicsExtractor graphics, float x, float y, float width, float height, float drawnU, float drawnV, float drawnWidth, float drawnHeight) {
        draw(graphics, 0, 0, x, y, (int) width, (int) height);
    }

    default IGuiTexture copy() {
        return CODEC.encodeStart(NbtOps.INSTANCE, this).result().map(tag -> CODEC.parse(NbtOps.INSTANCE, tag).result()
                .orElse(IGuiTexture.MISSING_TEXTURE))
                .orElse(IGuiTexture.MISSING_TEXTURE);
    }

    // ***************** EDITOR  ***************** //
    default void createPreview(ConfiguratorGroup father) {
        father.addConfigurators(new WrapperConfigurator("ldlib.gui.editor.group.preview",
                new ImageWidget(0, 0, 100, 100, this)
                        .setBorder(2, ColorPattern.T_WHITE.color)));
    }

    @Override
    default void buildConfigurator(ConfiguratorGroup father) {
        createPreview(father);
        IConfigurable.super.buildConfigurator(father);
    }

    default void setUIResource(Resource<IGuiTexture> texturesResource) {

    }
}
