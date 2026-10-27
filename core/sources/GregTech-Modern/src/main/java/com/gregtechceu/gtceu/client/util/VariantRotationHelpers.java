package com.gregtechceu.gtceu.client.util;

import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.util.Mth;
import com.mojang.math.Quadrant;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import java.util.Optional;

import com.google.common.base.Preconditions;
import com.mojang.math.Transformation;
import org.joml.Quaternionf;

/**
 * This code is from
 * <a href=
 * "https://github.com/AppliedEnergistics/Applied-Energistics-2/blob/forge/1.20.1/src/main/java/appeng/hooks/BlockstateDefinitionHook.java">Applied
 * Energistics 2</a>,
 * licensed as LGPL 3.0.
 */
public class VariantRotationHelpers {

    private static final Transformation[] TRANSFORMS = createTransformations();

    private static Transformation[] createTransformations() {
        var result = new Transformation[4 * 4 * 4];

        for (var xRot = 0; xRot < 360; xRot += 90) {
            for (var yRot = 0; yRot < 360; yRot += 90) {
                // Reuse existing transform from Vanilla
                result[indexFromAngles(xRot, yRot, 0)] = BlockModelRotation.get(Quadrant.fromXYAngles(quadrant(xRot), quadrant(yRot))).transformation();

                for (var zRot = 90; zRot < 360; zRot += 90) {
                    var idx = indexFromAngles(xRot, yRot, zRot);

                    // NOTE: Mojang's block model rotation rotates in the opposite direction
                    var quaternion = new Quaternionf().rotateYXZ(
                            -yRot * Mth.DEG_TO_RAD,
                            -xRot * Mth.DEG_TO_RAD,
                            -zRot * Mth.DEG_TO_RAD);

                    result[idx] = new Transformation(null, quaternion, null, null);
                }
            }
        }

        return result;
    }

    private VariantRotationHelpers() {}

    public static Variant rotateVariant(Variant variant, int xRot, int yRot, int zRot) {
        var nativeState = NATIVE_STATES[indexFromAngles(xRot, yRot, zRot)];
        return variant.withState(nativeState.withUvLock(variant.modelState().uvLock()));
    }

    private static final Variant.SimpleModelState[] NATIVE_STATES = createNativeStates();

    private static Variant.SimpleModelState[] createNativeStates() {
        var result = new Variant.SimpleModelState[64];
        for (int x=0;x<360;x+=90) for(int y=0;y<360;y+=90) for(int z=0;z<360;z+=90) {
            var expected = getRotationTransform(x,y,z).getMatrix();
            for (Quadrant nx : Quadrant.values()) for (Quadrant ny : Quadrant.values()) for (Quadrant nz : Quadrant.values()) {
                var state = new Variant.SimpleModelState(nx,ny,nz,false);
                if (result[indexFromAngles(x,y,z)] == null
                        && state.asModelState().transformation().getMatrix().equals(expected,0.00001f))
                    result[indexFromAngles(x,y,z)] = state;
            }
            Preconditions.checkState(result[indexFromAngles(x,y,z)] != null, "No native model orientation");
        }
        return result;
    }

    /** Reads old resource packs while writing native z rotations. A native z field takes precedence. */
    public static MapCodec<Variant.SimpleModelState> legacyCompatibleStateCodec() {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
                Quadrant.CODEC.optionalFieldOf("x", Quadrant.R0).forGetter(Variant.SimpleModelState::x),
                Quadrant.CODEC.optionalFieldOf("y", Quadrant.R0).forGetter(Variant.SimpleModelState::y),
                Quadrant.CODEC.optionalFieldOf("z").forGetter(state -> Optional.of(state.z())),
                Quadrant.CODEC.optionalFieldOf("gtceu:z").forGetter(state -> Optional.<Quadrant>empty()),
                Codec.BOOL.optionalFieldOf("uvlock", false).forGetter(Variant.SimpleModelState::uvLock)
        ).apply(instance, (x,y,z,legacy,uvlock) -> {
            if (z.isEmpty() && legacy.isPresent()) {
                return rotateVariant(new Variant(Identifier.withDefaultNamespace("missing")).withUvLock(uvlock),
                        x.shift * 90, y.shift * 90, legacy.get().shift * 90).modelState();
            }
            return new Variant.SimpleModelState(x,y,z.orElse(Quadrant.R0),uvlock);
        }));
    }

    public static Transformation getRotationTransform(int xRot, int yRot, int zRot) {
        return TRANSFORMS[indexFromAngles(xRot, yRot, zRot)];
    }

    private static Quadrant quadrant(int angle) {
        return switch (angle) { case 0 -> Quadrant.R0; case 90 -> Quadrant.R90;
            case 180 -> Quadrant.R180; case 270 -> Quadrant.R270;
            default -> throw new IllegalArgumentException("Invalid rotation " + angle); };
    }

    private static int indexFromAngles(int xRot, int yRot, int zRot) {
        Preconditions.checkArgument(xRot >= 0 && xRot < 360 && xRot % 90 == 0);
        Preconditions.checkArgument(yRot >= 0 && yRot < 360 && yRot % 90 == 0);
        Preconditions.checkArgument(zRot >= 0 && zRot < 360 && zRot % 90 == 0);
        return xRot / 90 * 16 + yRot / 90 * 4 + zRot / 90;
    }
}
