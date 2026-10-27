package com.lowdragmc.lowdraglib.transfer.tool;

import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

/** Original tool capability tokens, distinct from native mining components. */
public final class LegacyToolAbilities {
    private LegacyToolAbilities() {}
    public static final ItemAbility AXE_DIG = ItemAbility.get("axe_dig");
    public static final ItemAbility PICKAXE_DIG = ItemAbility.get("pickaxe_dig");
    public static final ItemAbility SHOVEL_DIG = ItemAbility.get("shovel_dig");
    public static final ItemAbility HOE_DIG = ItemAbility.get("hoe_dig");
    public static final ItemAbility SWORD_DIG = ItemAbility.get("sword_dig");
    public static final Set<ItemAbility> DEFAULT_AXE_ACTIONS = identitySet(AXE_DIG,
            BlockToolTransforms.AXE_STRIP, BlockToolTransforms.AXE_SCRAPE, BlockToolTransforms.AXE_WAX_OFF);
    public static final Set<ItemAbility> DEFAULT_HOE_ACTIONS = identitySet(HOE_DIG, BlockToolTransforms.HOE_TILL);
    public static final Set<ItemAbility> DEFAULT_SHOVEL_ACTIONS = identitySet(SHOVEL_DIG,
            BlockToolTransforms.SHOVEL_FLATTEN, ItemAbilities.SHOVEL_DOUSE);
    public static final Set<ItemAbility> DEFAULT_PICKAXE_ACTIONS = identitySet(PICKAXE_DIG);
    public static final Set<ItemAbility> DEFAULT_SWORD_ACTIONS = identitySet(SWORD_DIG, ItemAbilities.SWORD_SWEEP);
    private static Set<ItemAbility> identitySet(ItemAbility... abilities) {
        Set<ItemAbility> set=Collections.newSetFromMap(new IdentityHashMap<>());
        Collections.addAll(set,abilities);
        return Collections.unmodifiableSet(set);
    }
}
