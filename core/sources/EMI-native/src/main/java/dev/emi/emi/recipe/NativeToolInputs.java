package dev.emi.emi.recipe;

import java.util.Set;
import java.util.HashSet;
import java.util.Optional;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.blockpredicates.*;
import dev.emi.emi.mixin.accessor.*;

/** Exact origin-block restrictions from native rules; unsupported predicates remain explicit. */
public final class NativeToolInputs {
    private NativeToolInputs() {}
    public static Optional<Set<Block>> blocks(BlockPredicate predicate) {
        if(predicate instanceof StateTestingPredicate && !((ShovelItemAccessor)predicate).emi$offset().equals(net.minecraft.core.Vec3i.ZERO))
            return Optional.empty();
        if(predicate instanceof MatchingBlocksPredicate)
            return Optional.of(((AxeItemAccessor)predicate).emi$blocks().stream().map(net.minecraft.core.Holder::value).collect(java.util.stream.Collectors.toSet()));
        if(predicate instanceof MatchingBlockTagPredicate) {
            var tag=((HoeItemAccessor)predicate).emi$tag();
            return Optional.of(net.minecraft.core.registries.BuiltInRegistries.BLOCK.getOrThrow(tag).stream()
                .map(net.minecraft.core.Holder::value).collect(java.util.stream.Collectors.toSet()));
        }
        if(predicate instanceof AllOfPredicate) {
            Set<Block> result=null;
            for(var child:((dev.emi.emi.mixin.HoeItemMixin)predicate).emi$predicates()) {
                var candidates=blocks(child);
                if(candidates.isPresent()) {
                    if(result==null)result=new HashSet<>(candidates.get());else result.retainAll(candidates.get());
                }
            }
            return Optional.ofNullable(result);
        }
        return Optional.empty();
    }
}
