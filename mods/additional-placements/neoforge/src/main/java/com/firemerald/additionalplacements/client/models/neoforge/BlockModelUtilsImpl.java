package com.firemerald.additionalplacements.client.models.neoforge;

import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.client.model.quad.BakedColors;
import net.neoforged.neoforge.client.model.quad.BakedNormals;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3fc;

public class BlockModelUtilsImpl {
    public static BakedQuad transformed(BakedQuad originalQuad, Vector3fc[] newPos, long[] newTex, Vector3fc @Nullable [] newNorm, int @Nullable [] newColor, Direction direction, BakedQuad.MaterialInfo sprite) {
        BakedNormals normals = newNorm == null ? BakedNormals.UNSPECIFIED : BakedNormals.of(
                BakedNormals.pack(newNorm[0]),
                BakedNormals.pack(newNorm[1]),
                BakedNormals.pack(newNorm[2]),
                BakedNormals.pack(newNorm[3]));
        BakedColors colors = newColor == null ? BakedColors.DEFAULT : BakedColors.of(
                newColor[0],
                newColor[1],
                newColor[2],
                newColor[3]
        );
        return new BakedQuad(
                newPos[0], newPos[1], newPos[2], newPos[3],
                newTex[0], newTex[1], newTex[2], newTex[3],
                direction,
                sprite,
                normals,
                colors
        );
    }

    @Nullable
    public static Vector3fc[] getNorms(BakedQuad quad) {
        BakedNormals normals = quad.bakedNormals();
        int norm0 = normals.normal(0);
        int norm1 = normals.normal(1);
        int norm2 = normals.normal(2);
        int norm3 = normals.normal(3);
        if (BakedNormals.isUnspecified(norm0) && BakedNormals.isUnspecified(norm1) && BakedNormals.isUnspecified(norm2) && BakedNormals.isUnspecified(norm3)) return null;
        else return new Vector3fc[] {
                BakedNormals.unpack(norm0, null),
                BakedNormals.unpack(norm1, null),
                BakedNormals.unpack(norm2, null),
                BakedNormals.unpack(norm3, null)
        };
    }

    public static int @Nullable [] getColors(BakedQuad quad) {
        BakedColors colors = quad.bakedColors();
        int col0 = colors.color(0);
        int col1 = colors.color(1);
        int col2 = colors.color(2);
        int col3 = colors.color(3);
        if (col0 == 0xFFFFFFFF && col1 == 0xFFFFFFFF && col2 == 0xFFFFFFFF && col3 == 0xFFFFFFFF) return null;
        else return new int[] {
                colors.color(0),
                colors.color(1),
                colors.color(2),
                colors.color(3)
        };
    }
}
