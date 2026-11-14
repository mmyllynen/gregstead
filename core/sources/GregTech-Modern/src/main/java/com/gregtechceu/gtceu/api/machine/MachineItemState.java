package com.gregtechceu.gtceu.api.machine;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/** Item-only projection. World saves never pass through this filter. */
public final class MachineItemState {
    private MachineItemState() {}
    public record Placement(Level level, BlockPos pos) {}
    private static final ThreadLocal<Placement> PLACEMENT = new ThreadLocal<>();
    private static final Set<String> RENDER_OPTIONS = Set.of("steam_vent", "fe_to_eu", "transform_up",
            "amp_mode", "random_tick_mode", "working_enabled", "hpca_part_damaged");

    public static Placement beginPlacement(Level level, BlockPos pos) {
        Placement previous = PLACEMENT.get();
        PLACEMENT.set(new Placement(level, pos.immutable()));
        return previous;
    }
    public static void endPlacement(Placement previous) {
        if (previous == null) PLACEMENT.remove();
        else PLACEMENT.set(previous);
    }
    public static boolean isItemPlacement(MetaMachine machine) {
        var current = PLACEMENT.get();
        return current != null && machine.getLevel() == current.level() && machine.getBlockPos().equals(current.pos());
    }

    public static CompoundTag collect(MetaMachine machine, HolderLookup.Provider lookup) {
        CompoundTag data = machine.getSyncDataHolder().serializeItemNBT(lookup);
        addRenderOptions(data, machine);
        // Omit constructor defaults, so a plain empty machine stacks with a fresh craft.
        var prototype = machine.getType().create(machine.getBlockPos(), machine.getBlockState());
        if (prototype instanceof MetaMachine fresh) {
            CompoundTag defaults = fresh.getSyncDataHolder().serializeItemNBT(lookup);
            addRenderOptions(defaults, fresh);
            removeDefaults(data, defaults);
        }
        return data;
    }
    public static void restore(MetaMachine machine, CompoundTag data, HolderLookup.Provider lookup) {
        machine.getSyncDataHolder().deserializeItemNBT(lookup, data);
        CompoundTag options = data.getCompoundOrEmpty("renderOptions");
        var state = machine.getRenderState();
        for (Property<?> property : state.propertyValues().keySet()) {
            if (RENDER_OPTIONS.contains(property.getName()) && options.contains(property.getName()))
                state = setOption(state, property, options.getStringOr(property.getName(), ""));
        }
        var painted = com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties.IS_PAINTED;
        if (state.hasProperty(painted)) state = state.setValue(painted, machine.isPainted());
        machine.setRenderState(state);
    }
    /** Old items used the world render-state schema; accept only known user options. */
    public static void restoreLegacy(MetaMachine machine, CompoundTag tag, HolderLookup.Provider lookup) {
        CompoundTag data = tag.copy();
        CompoundTag options = data.getCompoundOrEmpty("renderState").getCompoundOrEmpty("Properties").copy();
        for (String key : new HashSet<>(options.keySet())) if (!RENDER_OPTIONS.contains(key)) options.remove(key);
        data.put("renderOptions", options);
        restore(machine, data, lookup);
    }
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static com.gregtechceu.gtceu.client.model.machine.MachineRenderState setOption(
            com.gregtechceu.gtceu.client.model.machine.MachineRenderState state, Property property, String value) {
        var parsed = property.getValue(value);
        return parsed.isPresent() ? state.setValue(property, (Comparable) parsed.get()) : state;
    }
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void addRenderOptions(CompoundTag data, MetaMachine machine) {
        CompoundTag options = new CompoundTag();
        machine.getRenderState().propertyValues().forEach((property, value) -> {
            if (RENDER_OPTIONS.contains(property.getName()))
                options.putString(property.getName(), ((Property) property).getName(value));
        });
        if (!options.isEmpty()) data.put("renderOptions", options);
    }
    private static void removeDefaults(CompoundTag data, CompoundTag defaults) {
        for (String key : new HashSet<>(data.keySet())) {
            var value = data.get(key);
            var defaultValue = defaults.get(key);
            if (Objects.equals(value, defaultValue)) data.remove(key);
            else if (value instanceof CompoundTag child && defaultValue instanceof CompoundTag defaultChild) {
                removeDefaults(child, defaultChild);
                if (child.isEmpty()) data.remove(key);
            }
        }
    }
}
