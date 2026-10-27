package com.gregtechceu.gtceu.client.model.machine.overlays;

import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic.Status;
import com.gregtechceu.gtceu.common.data.models.GTModels;

import com.lowdragmc.lowdraglib.transfer.annotation.MethodsReturnNonnullByDefault;
import net.minecraft.resources.Identifier;
import java.util.function.Predicate;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class HPCAOverlay {

    // spotless:off
    public static HPCAOverlay get(Identifier normalSprite, Identifier damagedSprite,
                                  Predicate<Identifier> texturesExist) {
        // normal
        if (!texturesExist.test(normalSprite)) {
            return HPCAOverlay.EMPTY;
        }
        Identifier activeSprite = normalSprite.withSuffix("_active");
        if (!texturesExist.test(activeSprite)) activeSprite = normalSprite;

        Identifier damagedActiveSprite = damagedSprite.withSuffix("_active");
        if (!texturesExist.test(damagedActiveSprite)) damagedActiveSprite = damagedSprite;

        // emissive
        Identifier normalSpriteEmissive = normalSprite.withSuffix("_emissive");
        if (!texturesExist.test(normalSpriteEmissive)) normalSpriteEmissive = null;

        Identifier activeSpriteEmissive = activeSprite.withSuffix("_emissive");
        if (!texturesExist.test(activeSpriteEmissive)) activeSpriteEmissive = null;

        Identifier damagedSpriteEmissive = damagedSprite.withSuffix("_emissive");
        if (!texturesExist.test(damagedSpriteEmissive)) damagedSpriteEmissive = null;

        Identifier damagedActiveSpriteEmissive = damagedActiveSprite.withSuffix("_emissive");
        if (!texturesExist.test(damagedActiveSpriteEmissive)) damagedActiveSpriteEmissive = null;

        return new HPCAOverlay(normalSprite, activeSprite, damagedSprite, damagedActiveSprite,
                normalSpriteEmissive, activeSpriteEmissive, damagedSpriteEmissive, damagedActiveSpriteEmissive);
    }
    // spotless:on

    public static final HPCAOverlay EMPTY = new HPCAOverlay();

    private final Map<Status, Identifier> textures = new EnumMap<>(Status.class);
    private final Map<Status, Identifier> emissiveTextures = new EnumMap<>(Status.class);

    public HPCAOverlay(@Nullable Identifier normalSprite,
                       @Nullable Identifier activeSprite,
                       @Nullable Identifier damagedSprite,
                       @Nullable Identifier damagedActiveSprite,
                       @Nullable Identifier normalSpriteEmissive,
                       @Nullable Identifier activeSpriteEmissive,
                       @Nullable Identifier damagedSpriteEmissive,
                       @Nullable Identifier damagedActiveSpriteEmissive) {
        textures.put(Status.IDLE, normalSprite);
        emissiveTextures.put(Status.IDLE, normalSpriteEmissive);

        textures.put(Status.WORKING, activeSprite);
        emissiveTextures.put(Status.WORKING, activeSpriteEmissive);

        textures.put(Status.WAITING, damagedActiveSprite);
        emissiveTextures.put(Status.WAITING, damagedActiveSpriteEmissive);
        textures.put(Status.SUSPEND, damagedSprite);
        emissiveTextures.put(Status.SUSPEND, damagedSpriteEmissive);
    }

    private HPCAOverlay() {}

    private static Status getStatus(boolean active, boolean damaged) {
        if (damaged && active) return Status.WAITING;
        else if (damaged) return Status.SUSPEND;
        else if (active) return Status.WORKING;
        else return Status.IDLE;
    }

    public @NotNull Identifier getTexture(boolean active, boolean damaged) {
        Identifier value = textures.get(getStatus(active, damaged));
        return value != null ? value : GTModels.BLANK_TEXTURE;
    }

    public @NotNull Identifier getEmissiveTexture(boolean active, boolean damaged) {
        Identifier value = emissiveTextures.get(getStatus(active, damaged));
        return value != null ? value : GTModels.BLANK_TEXTURE;
    }
}
