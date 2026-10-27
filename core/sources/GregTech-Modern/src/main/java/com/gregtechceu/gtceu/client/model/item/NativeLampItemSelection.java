package com.gregtechceu.gtceu.client.model.item;
public final class NativeLampItemSelection {
    private NativeLampItemSelection() {}
    public static <T> T select(boolean lit,boolean bloom,T off,T normal,T emissive){return lit?bloom?emissive:normal:off;}
}
