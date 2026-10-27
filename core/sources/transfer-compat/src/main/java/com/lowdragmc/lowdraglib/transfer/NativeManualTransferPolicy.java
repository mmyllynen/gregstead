package com.lowdragmc.lowdraglib.transfer;
import java.util.function.BooleanSupplier;
/** Original cover manual-flow rule; the opposite direction may be disabled or unfiltered. */
public final class NativeManualTransferPolicy {
    private NativeManualTransferPolicy() {}
    public static boolean allows(boolean reverse,boolean disabled,boolean unfiltered,BooleanSupplier filter) {
        if(reverse && disabled)return false;
        if(reverse && unfiltered)return true;
        return filter.getAsBoolean();
    }
}
