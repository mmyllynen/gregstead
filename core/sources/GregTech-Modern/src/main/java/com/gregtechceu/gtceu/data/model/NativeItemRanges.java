package com.gregtechceu.gtceu.data.model;
import com.gregtechceu.gtceu.client.NativeItemProperty;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.renderer.item.RangeSelectItemModel;
import net.minecraft.resources.Identifier;
import java.util.*;
public final class NativeItemRanges {
    private NativeItemRanges() {}
    public static RangeSelectItemModel.Unbaked select(Identifier property, Identifier fallback,
                                                     List<Identifier> models, List<Float> thresholds) {
        if (models.isEmpty() || models.size() != thresholds.size()) throw new IllegalArgumentException("Invalid range entries");
        var entries = new ArrayList<RangeSelectItemModel.Entry>();
        for (int i = 0; i < models.size(); i++) entries.add(new RangeSelectItemModel.Entry(thresholds.get(i), ItemModelUtils.plainModel(models.get(i))));
        return new RangeSelectItemModel.Unbaked(Optional.empty(), new NativeItemProperty(property), 1f,
                List.copyOf(entries), Optional.of(ItemModelUtils.plainModel(fallback)));
    }
}
