package com.gregtechceu.gtceu.data.model;

import com.tterrag.registrate.util.nullness.NonNullBiConsumer;

/** A generic callback keeps client provider casts out of dedicated-server lambda construction. */
public final class DeferredModelCallback {
    private DeferredModelCallback() {}
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <A, B> NonNullBiConsumer<A, B> of(NonNullBiConsumer<Object, Object> callback) {
        return (NonNullBiConsumer) callback;
    }
}
