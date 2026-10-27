package com.lowdragmc.lowdraglib.transfer.persistence;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

/** Retains legacy typed NBT lookups while using the 26.3 optional/heterogeneous list API. */
public final class NbtCompatibility {
    public static final int TAG_ANY_NUMERIC = 99;
    private NbtCompatibility() {}
    public static boolean contains(CompoundTag tag, String key, int expectedType) {
        Tag value = tag.get(key);
        int actualType = value == null ? Tag.TAG_END : value.getId();
        return actualType == expectedType || expectedType == TAG_ANY_NUMERIC &&
                actualType >= Tag.TAG_BYTE && actualType <= Tag.TAG_DOUBLE;
    }
    /** Legacy getAsString unwraps strings; other tags use their SNBT representation. */
    public static String stringValue(Tag tag) {
        return tag instanceof net.minecraft.nbt.StringTag string ? string.value() : tag.toString();
    }
    public static void putUUID(CompoundTag tag, String key, java.util.UUID value) {
        tag.putIntArray(key, net.minecraft.core.UUIDUtil.uuidToIntArray(value));
    }
    public static java.util.UUID getUUID(CompoundTag tag, String key) {
        int[] values = tag.getIntArray(key).filter(array -> array.length == 4)
                .orElseThrow(() -> new IllegalArgumentException("Expected UUID int array of length 4: " + key));
        return net.minecraft.core.UUIDUtil.uuidFromIntArray(values);
    }
    public static net.minecraft.nbt.IntArrayTag uuidTag(java.util.UUID value) {
        return new net.minecraft.nbt.IntArrayTag(net.minecraft.core.UUIDUtil.uuidToIntArray(value));
    }
    public static java.util.UUID uuidFromTag(net.minecraft.nbt.IntArrayTag tag) {
        int[] values=tag.getAsIntArray();
        if(values.length!=4)throw new IllegalArgumentException("Expected UUID int array of length 4");
        return net.minecraft.core.UUIDUtil.uuidFromIntArray(values);
    }
    public static net.minecraft.nbt.IntArrayTag blockPosTag(net.minecraft.core.BlockPos pos) {
        return new net.minecraft.nbt.IntArrayTag(new int[]{pos.getX(),pos.getY(),pos.getZ()});
    }
    public static java.util.Optional<net.minecraft.core.BlockPos> blockPosFromTag(CompoundTag root,String key) {
        return root.getIntArray(key).filter(values->values.length==3)
                .map(values->new net.minecraft.core.BlockPos(values[0],values[1],values[2]));
    }

    /** Original graph-reader outputs: numeric lists use Float, bytes use Boolean. */
    public static java.util.List<?> graphListValues(ListTag list) {
        if (list.isEmpty()) return java.util.List.of();
        int type = list.getFirst().getId();
        for (Tag entry : list) if (entry.getId() != type) return java.util.List.of();
        return switch (type) {
            case Tag.TAG_STRING -> list.stream().map(net.minecraft.nbt.StringTag.class::cast)
                    .map(net.minecraft.nbt.StringTag::value).toList();
            case Tag.TAG_FLOAT, Tag.TAG_DOUBLE, Tag.TAG_INT, Tag.TAG_LONG, Tag.TAG_SHORT ->
                    list.stream().map(net.minecraft.nbt.NumericTag.class::cast)
                            .map(net.minecraft.nbt.NumericTag::floatValue).toList();
            case Tag.TAG_BYTE -> list.stream().map(net.minecraft.nbt.ByteTag.class::cast)
                    .map(tag -> tag.byteValue() != 0).toList();
            case Tag.TAG_COMPOUND -> list.stream().map(CompoundTag.class::cast).toList();
            case Tag.TAG_LIST -> list.stream().map(ListTag.class::cast).toList();
            default -> java.util.List.of();
        };
    }

    public static ListTag getList(CompoundTag tag, String key, int expectedElementType) {
        ListTag list = tag.getListOrEmpty(key);
        // Older lists were homogeneous. Wrong element types returned an empty list.
        for (Tag entry : list) if (entry.getId() != expectedElementType) return new ListTag();
        return list;
    }
}
