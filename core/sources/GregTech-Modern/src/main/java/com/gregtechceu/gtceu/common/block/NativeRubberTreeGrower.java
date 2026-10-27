package com.gregtechceu.gtceu.common.block;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.levelgen.feature.Feature;
/** Original rubber sapling selects one normal tree; no mega-tree or flower variant. */
public final class NativeRubberTreeGrower {
    private NativeRubberTreeGrower() {}
    public static TreeGrower create(Identifier name,ResourceKey<Feature> tree) {
        return new TreeGrower(name.toString(),WeightedList.of(tree),WeightedList.of(),WeightedList.of(),null);
    }
}
