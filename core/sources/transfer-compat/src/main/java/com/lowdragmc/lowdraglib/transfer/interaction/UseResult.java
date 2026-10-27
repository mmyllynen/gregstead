package com.lowdragmc.lowdraglib.transfer.interaction;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import java.util.Objects;
import java.util.function.Consumer;

/** Internal use result retaining a returned value while the native API carries transformed held items on success. */
public final class UseResult<T> {
    private final InteractionResult result;
    private final T object;
    public UseResult(InteractionResult result, T object) { this.result = Objects.requireNonNull(result); this.object = object; }
    public InteractionResult getResult() { return result; }
    public T getObject() { return object; }
    public static <T> UseResult<T> success(T object) { return new UseResult<>(InteractionResult.SUCCESS, object); }
    public static <T> UseResult<T> consume(T object) { return new UseResult<>(InteractionResult.CONSUME, object); }
    public static <T> UseResult<T> pass(T object) { return new UseResult<>(InteractionResult.PASS, object); }
    public static <T> UseResult<T> fail(T object) { return new UseResult<>(InteractionResult.FAIL, object); }
    public static InteractionResult.Success sidedSuccess(boolean client) {
        return client ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
    }
    public static <T> UseResult<T> sidedSuccess(T object, boolean client) { return new UseResult<>(sidedSuccess(client), object); }
    public static InteractionResult toNative(UseResult<ItemStack> result, Consumer<ItemStack> updateHeldItem) {
        if (result.result instanceof InteractionResult.Success success) {
            return success.heldItemTransformedTo(Objects.requireNonNull(result.object));
        }
        // Native pass/fail results have no transformed-item payload. Preserve the
        // internal result's returned stack explicitly rather than discarding it.
        updateHeldItem.accept(Objects.requireNonNull(result.object));
        return result.result;
    }
    public static InteractionResult blockResult(InteractionResult result) {
        return result == InteractionResult.PASS ? InteractionResult.TRY_WITH_EMPTY_HAND : result;
    }
}
