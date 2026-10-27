package com.lowdragmc.lowdraglib.client.model;

import com.google.gson.JsonParseException;
import com.lowdragmc.lowdraglib.core.mixins.accessor.ModelBakeryAccessor;
import com.mojang.datafixers.util.Either;
import com.mojang.math.Transformation;
import lombok.Getter;
import lombok.Setter;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import com.lowdragmc.lowdraglib.transfer.annotation.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import com.mojang.math.Quadrant;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.client.resources.model.cuboid.ItemModelGenerator;
import net.minecraft.client.resources.model.cuboid.ItemTransform;
import net.minecraft.client.resources.model.cuboid.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Function;

/**
 * Author: KilaBash
 * Date: 2022/04/24
 * Description:
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ModelFactory {
    public static final ItemModelGenerator ITEM_MODEL_GENERATOR = new ItemModelGenerator();

    public static ModelBakery getModelBakery() {
        return Minecraft.getInstance().getModelManager().getModelBakery();
    }

    public static UnbakedModel getLDLibModel(UnbakedModel vanilla) {
        return vanilla;
    }

    public static ModelBaker getModelBaker() {
        var anchor=NativeStandaloneModels.get(Identifier.parse("minecraft:block/stone"));
        if(anchor==null)throw new IllegalStateException("LD standalone models have not been baked");
        return anchor.baker();
    }

    public static ResolvedModel getResolvedModel(Identifier location) {
        var standalone=NativeStandaloneModels.get(location);
        if(standalone!=null)return standalone.model();
        var bakery=(ModelBakeryAccessor)getModelBakery();
        return bakery.getResolvedModels().getOrDefault(location,bakery.getMissingModel());
    }

    public static boolean isMissingModel(Identifier location) {
        return getResolvedModel(location)==((ModelBakeryAccessor)getModelBakery()).getMissingModel();
    }

    public static UnbakedModel getUnBakedModel(Identifier location){return getResolvedModel(location).wrapped();}
    public static UnbakedModel getTopLevelModel(Identifier location){return getUnBakedModel(location);}

    public static net.minecraft.client.renderer.block.dispatch.BlockStateModelPart bake(Identifier location,ModelState state) {
        return SimpleModelWrapper.bake(getModelBaker(),getResolvedModel(location),state);
    }

    public static net.minecraft.client.resources.model.geometry.QuadCollection bakeGeometry(Identifier location,ModelState state) {
        var model=getResolvedModel(location);
        return model.bakeTopGeometry(model.getTopTextureSlots(),getModelBaker(),state);
    }

    public static ItemTransforms getTransforms(Identifier location){return getResolvedModel(location).getTopTransforms();}

    public static Quaternionf getQuaternion(Direction facing) {
        return switch (facing) {
            case UP -> new Quaternionf().rotateXYZ(Mth.HALF_PI, 0, 0);
            case DOWN -> new Quaternionf().rotateXYZ(-Mth.HALF_PI, 0, 0);
            case EAST -> new Quaternionf().rotateXYZ(0, -Mth.HALF_PI, 0);
            case WEST -> new Quaternionf().rotateXYZ(0, Mth.HALF_PI, 0);
            case SOUTH -> new Quaternionf().rotateXYZ(0, Mth.PI, 0);
            case NORTH -> new Quaternionf();
        };
    }

    public static BlockModelRotation getRotation(Direction facing) {
        Quadrant x=facing==Direction.DOWN?Quadrant.R90:facing==Direction.UP?Quadrant.R270:Quadrant.R0;
        Quadrant y=switch(facing){case SOUTH->Quadrant.R180;case WEST->Quadrant.R270;case EAST->Quadrant.R90;default->Quadrant.R0;};
        return BlockModelRotation.get(Quadrant.fromXYAngles(x,y));
    }

    public static Direction modelFacing(Direction side, Direction frontFacing) {
        if (side == frontFacing) return Direction.NORTH;
        if (frontFacing == Direction.NORTH) return side;
        if (frontFacing == Direction.SOUTH) {
            if (side.getAxis() == Direction.Axis.Y) return side;
            return side.getOpposite();
        }
        if (frontFacing == Direction.EAST) {
            if (side.getAxis() == Direction.Axis.Y) return side;
            return side.getCounterClockWise();
        }
        if (frontFacing == Direction.WEST) {
            if (side.getAxis() == Direction.Axis.Y) return side;
            return side.getClockWise();
        }
        if (frontFacing == Direction.UP) {
            if (side == Direction.DOWN) return Direction.SOUTH;
            if (side.getAxis() == Direction.Axis.X) return side;
            if (side == Direction.SOUTH) return Direction.UP;
            if (side == Direction.NORTH) return Direction.DOWN;
        }
        if (frontFacing == Direction.DOWN) {
            if (side == Direction.UP) return Direction.SOUTH;
            if (side.getAxis() == Direction.Axis.X) return side;
            if (side == Direction.SOUTH) return Direction.DOWN;
            if (side == Direction.NORTH) return Direction.UP;
        }
        return side;
    }

    public static ModelState getRotation(Direction facing,boolean lockedUV) {
        var rotation=getRotation(facing);return lockedUV?rotation.withUvLock():rotation;
    }

    public static Either<SpriteId, String> parseTextureLocationOrReference(Identifier pLocation, String pName) {
        if (isTextureReference(pName)) {
            return Either.right(pName.substring(1));
        } else {
            Identifier resourcelocation = Identifier.tryParse(pName);
            if (resourcelocation == null) {
                throw new JsonParseException(pName + " is not valid resource location");
            } else {
                return Either.left(new SpriteId(pLocation,resourcelocation));
            }
        }
    }

    public static Either<Material, String> parseBlockTextureLocationOrReference(String pName) {
        if (isTextureReference(pName)) {
            return Either.right(pName.substring(1));
        } else {
            Identifier resourcelocation = Identifier.tryParse(pName);
            if (resourcelocation == null) {
                throw new JsonParseException(pName + " is not valid resource location");
            } else {
                return Either.left(new Material(resourcelocation));
            }
        }
    }

    private static boolean isTextureReference(String pStr) {
        return pStr.charAt(0) == '#';
    }

    public static TextureAtlasSprite getBlockSprite(Identifier location) {
        return Minecraft.getInstance().getAtlasManager().get(new net.minecraft.client.resources.model.sprite.SpriteId(net.minecraft.client.renderer.texture.TextureAtlas.LOCATION_BLOCKS, location));
    }

    public static TextureAtlasSprite getSprite(Identifier atlas, Identifier location) {
        return Minecraft.getInstance().getAtlasManager().get(new net.minecraft.client.resources.model.sprite.SpriteId(atlas, location));
    }

    public static ItemTransform makeTransform(float rotationX, float rotationY, float rotationZ, float translationX, float translationY, float translationZ, float scaleX, float scaleY, float scaleZ) {
        var translation = new Vector3f(translationX, translationY, translationZ);
        translation.mul(0.0625f);
        translation.set(Mth.clamp(translation.x, -5.0F, 5.0F), Mth.clamp(translation.y, -5.0F, 5.0F), Mth.clamp(translation.z, -5.0F, 5.0F));
        return new ItemTransform(new Vector3f(rotationX, rotationY, rotationZ), translation, new Vector3f(scaleX, scaleY, scaleZ));
    }

    public static final ItemTransform TRANSFORM_BLOCK_GUI = makeTransform(30, 225, 0, 0, 0, 0, 0.625f, 0.625f, 0.625f);
    public static final ItemTransform TRANSFORM_BLOCK_GROUND = makeTransform(0, 0, 0, 0, 3, 0, 0.25f, 0.25f, 0.25f);
    public static final ItemTransform TRANSFORM_BLOCK_FIXED = makeTransform(0, 0, 0, 0, 0, 0, 0.5f, 0.5f, 0.5f);
    public static final ItemTransform TRANSFORM_BLOCK_3RD_PERSON_RIGHT = makeTransform(75, 45, 0, 0, 2.5f, 0, 0.375f, 0.375f, 0.375f);
    public static final ItemTransform TRANSFORM_BLOCK_1ST_PERSON_RIGHT = makeTransform(0, 45, 0, 0, 0, 0, 0.4f, 0.4f, 0.4f);
    public static final ItemTransform TRANSFORM_BLOCK_1ST_PERSON_LEFT = makeTransform(0, 225, 0, 0, 0, 0, 0.4f, 0.4f, 0.4f);

    /**
     * Mimics the vanilla model transformation used for most vanilla blocks,
     * and should be suitable for most custom block-like models.
     */
    public static final ItemTransforms MODEL_TRANSFORM_BLOCK = new ItemTransforms(TRANSFORM_BLOCK_3RD_PERSON_RIGHT, TRANSFORM_BLOCK_3RD_PERSON_RIGHT, TRANSFORM_BLOCK_1ST_PERSON_LEFT, TRANSFORM_BLOCK_1ST_PERSON_RIGHT, ItemTransform.NO_TRANSFORM, TRANSFORM_BLOCK_GUI, TRANSFORM_BLOCK_GROUND, TRANSFORM_BLOCK_FIXED, ItemTransform.NO_TRANSFORM);
}
