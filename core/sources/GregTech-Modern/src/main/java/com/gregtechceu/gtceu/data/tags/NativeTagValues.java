package com.gregtechceu.gtceu.data.tags;

import net.minecraft.core.Registry;
import net.minecraft.data.tags.TagAppender;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

/** Native datagen converts registered values to keys without changing required/optional tag entries. */
public final class NativeTagValues<T> {
    private final Registry<T> registry;
    private final TagAppender<T> appender;
    public NativeTagValues(Registry<T> registry, TagAppender<T> appender) {
        this.registry=registry;this.appender=appender;
    }
    @SafeVarargs public final NativeTagValues<T> add(T... values) {
        for(var value:values)appender.add(registry.getResourceKey(value).orElseThrow(() -> new IllegalArgumentException("Unregistered tag value: "+value)));
        return this;
    }
    public NativeTagValues<T> addOptional(Identifier id) {
        appender.addOptional(ResourceKey.create(registry.key(),id));return this;
    }
    public NativeTagValues<T> addTag(TagKey<T> tag) {appender.addTag(tag);return this;}
    @SafeVarargs public final NativeTagValues<T> addTags(TagKey<T>... tags) {for(var tag:tags)addTag(tag);return this;}
    public NativeTagValues<T> addOptionalTag(TagKey<T> tag) {appender.addOptionalTag(tag);return this;}
    public NativeTagValues<T> addOptionalTag(Identifier id) {
        return addOptionalTag(TagKey.create(registry.key(), id));
    }
    @SafeVarargs public final NativeTagValues<T> remove(T... values) {
        for (var value : values) appender.remove(registry.getResourceKey(value).orElseThrow(
                () -> new IllegalArgumentException("Unregistered removed tag value: " + value)));
        return this;
    }
    public NativeTagValues<T> replace(boolean replace) {appender.replace(replace);return this;}
}
