package com.lowdragmc.lowdraglib.gui.texture;

import com.lowdragmc.lowdraglib.editor.configurator.ConfiguratorGroup;
import com.lowdragmc.lowdraglib.editor.data.resource.Resource;
import com.lowdragmc.lowdraglib.registry.annotation.LDLRegisterClient;
import com.lowdragmc.lowdraglib.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.mojang.datafixers.util.Either;
import lombok.NoArgsConstructor;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.io.File;

@LDLRegisterClient(name = "ui_resource_texture", registry = "ldlib:gui_texture")
@NoArgsConstructor
public final class UIResourceTexture implements IGuiTexture, IPersistedSerializable {
    public final static ThreadLocal<Resource<IGuiTexture>> RESOURCE = ThreadLocal.withInitial(() -> null);
    public static final ThreadLocal<Boolean> RESOLVE_PROJECT_TEXTURES = ThreadLocal.withInitial(() -> false);
    private transient boolean projectResolved;
    private Either<String, File> key;
    @Nullable
    @Persisted
    private IGuiTexture fallbackTexture;

    public UIResourceTexture(@Nullable IGuiTexture fallbackTexture, Either<String, File> key) {
        this.fallbackTexture = fallbackTexture;
        this.key = key;
        this.projectResolved = RESOLVE_PROJECT_TEXTURES.get();
        this.fallbackTexture = RESOURCE.get() == null ? getFallbackTexture() : RESOURCE.get().getResourceOrDefault(key, getFallbackTexture());
    }

    public IGuiTexture getTexture() {
        if (projectResolved || key == null) return getFallbackTexture();
        var resource = RESOURCE.get();
        return resource == null ? getFallbackTexture() : resource.getResourceOrDefault(key, getFallbackTexture());
    }

    public IGuiTexture getFallbackTexture() {
        return fallbackTexture == null ? IGuiTexture.MISSING_TEXTURE : fallbackTexture;
    }

    @Override
    public IGuiTexture setColor(int color) {
        return getTexture().setColor(color);
    }

    @Override
    public IGuiTexture rotate(float degree) {
        return getTexture().rotate(degree);
    }

    @Override
    public IGuiTexture scale(float scale) {
        return getTexture().scale(scale);
    }

    @Override
    public IGuiTexture transform(int xOffset, int yOffset) {
        return getTexture().transform(xOffset, yOffset);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void draw(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float x, float y, int width, int height) {
        getTexture().draw(graphics, mouseX, mouseY, x, y, width, height);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void updateTick() {
        getTexture().updateTick();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawSubArea(GuiGraphicsExtractor graphics, float x, float y, float width, float height, float drawnU, float drawnV, float drawnWidth, float drawnHeight) {
        getTexture().drawSubArea(graphics, x, y, width, height, drawnU, drawnV, drawnWidth, drawnHeight);
    }

    @Override
    public void createPreview(ConfiguratorGroup father) {
        getTexture().createPreview(father);
    }

    @Override
    public void buildConfigurator(ConfiguratorGroup father) {
        getTexture().buildConfigurator(father);
    }

    @Override
    public IGuiTexture copy() {
        if (!projectResolved && key != null) return new UIResourceTexture(fallbackTexture, key);
        var result = new UIResourceTexture();
        result.key = key;
        result.fallbackTexture = fallbackTexture;
        result.projectResolved = projectResolved;
        return result;
    }

    @Override
    public Tag serializeAdditionalNBT(HolderLookup.@NotNull Provider provider) {
        return key.map(
                l -> {
                    var key = new CompoundTag();
                    key.putString("key", l);
                    key.putString("type", "builtin");
                    return key;
                }, r-> {
                    var key = new CompoundTag();
                    key.putString("key", r.getPath());
                    key.putString("type", "file");
                    return key;
                }
        );
    }

    @Override
    public void deserializeAdditionalNBT(Tag tag, HolderLookup.@NotNull Provider provider) {
        if (tag instanceof CompoundTag compoundTag) {
            var key = compoundTag.getStringOr("key", "");
            var type = compoundTag.getStringOr("type", "");
            if (type.equals("file")) {
                this.key = Either.right(new File(key));
            } else  {
                this.key = Either.left(key);
            }
            // Keep the resolved project texture after the temporary loading scope closes.
            var resource = RESOURCE.get();
            if (resource != null) fallbackTexture = resource.getResourceOrDefault(this.key, getFallbackTexture());
            projectResolved = RESOLVE_PROJECT_TEXTURES.get();
        }
    }
}
