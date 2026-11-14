package com.gregtechceu.gtceu.api.sync_system.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Instructs the sync system to save and load this field to and from world data.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface SaveField {

    /**
     * Specifies the NBT key the data should be stored under, defaulting to the field name.
     */
    String nbtKey() default "";

    /** Explicit item persistence; world persistence is independent of this policy. */
    ItemSave item() default ItemSave.CHILDREN;

    enum ItemSave {
        /** Only explicitly portable descendants of a managed object. */
        CHILDREN,
        /** The complete value is portable (resource or user configuration). */
        VALUE,
        /** A virtual circuit is configuration; a physical circuit drops normally. */
        GHOST_CIRCUIT,
        /** Never carry this field or its descendants on a machine item. */
        NONE
    }
}
