package com.lowdragmc.lowdraglib.transfer.model;

/** Optional model texture-slot metadata retained through native quad transformations. */
public interface QuadTextureKey {
    String textureKey();
    void textureKey(String key);
}
