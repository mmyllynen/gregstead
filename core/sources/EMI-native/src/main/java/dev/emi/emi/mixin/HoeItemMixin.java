package dev.emi.emi.mixin;

import java.util.List;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.blockpredicates.CombiningPredicate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CombiningPredicate.class)
public interface HoeItemMixin {
    @Accessor("predicates") List<BlockPredicate> emi$predicates();
}
