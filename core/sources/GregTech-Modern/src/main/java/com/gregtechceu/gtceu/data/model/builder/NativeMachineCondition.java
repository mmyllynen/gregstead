package com.gregtechceu.gtceu.data.model.builder;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.world.level.block.state.properties.Property;
import java.util.*;

/** Immutable loader-facing multipart condition shared by JSON output and coverage checks. */
public record NativeMachineCondition(boolean useOr, Map<Property<?>, List<Comparable<?>>> values,
                                     List<NativeMachineCondition> groups) {
    public NativeMachineCondition {
        var copy = new LinkedHashMap<Property<?>, List<Comparable<?>>>();
        values.forEach((property, allowed) -> {
            if (allowed.isEmpty() || !property.getPossibleValues().containsAll(allowed))
                throw new IllegalArgumentException("Invalid values for " + property.getName());
            copy.put(property, List.copyOf(allowed));
        });
        values = Collections.unmodifiableMap(copy);
        groups = List.copyOf(groups);
        if (!values.isEmpty() && !groups.isEmpty())
            throw new IllegalArgumentException("Cannot mix property constraints and nested groups");
    }

    public boolean matches(Map<Property<?>, Comparable<?>> state) {
        if (!values.isEmpty()) {
            var matches = values.entrySet().stream()
                    .map(entry -> entry.getValue().contains(state.get(entry.getKey())));
            return useOr ? matches.anyMatch(Boolean::booleanValue) : matches.allMatch(Boolean::booleanValue);
        }
        if (!groups.isEmpty()) {
            return useOr ? groups.stream().anyMatch(group -> group.matches(state)) :
                    groups.stream().allMatch(group -> group.matches(state));
        }
        return true;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public JsonObject toJson() {
        var json = new JsonObject();
        if (!groups.isEmpty()) {
            var children = new JsonArray();
            groups.forEach(group -> children.add(group.toJson()));
            json.add(useOr ? "OR" : "AND", children);
        } else if (!values.isEmpty()) {
            values.forEach((property, allowed) -> json.addProperty(property.getName(),
                    String.join("|", allowed.stream().map(value -> ((Property) property).getName(value)).toList())));
            if (useOr) {
                var children = new JsonArray();
                json.entrySet().forEach(entry -> {
                    var child = new JsonObject();
                    child.add(entry.getKey(), entry.getValue());
                    children.add(child);
                });
                var result = new JsonObject();
                result.add("OR", children);
                return result;
            }
        }
        return json;
    }
}
