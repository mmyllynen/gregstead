package dev.emi.emi.compost;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Exact layer probabilities at fill levels0..6, or unknown when additional context is needed. */
public record CompostProfile(List<Optional<Map<Integer,Double>>> levels) {
    public CompostProfile {
        if(levels.size()!=7)throw new IllegalArgumentException("Compost profile requires seven fill levels");
        levels=levels.stream().map(level -> level.map(values -> {
            if(values.isEmpty() || values.entrySet().stream().anyMatch(e -> e.getKey()<0 || !Double.isFinite(e.getValue()) || e.getValue()<0 || e.getValue()>1)
                    || Math.abs(values.values().stream().mapToDouble(Double::doubleValue).sum()-1)>1e-8)
                throw new IllegalArgumentException("Invalid layer probabilities");
            return Map.copyOf(values);
        })).toList();
    }
}
