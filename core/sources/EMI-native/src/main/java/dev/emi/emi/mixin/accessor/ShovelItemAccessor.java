package dev.emi.emi.mixin.accessor;

import net.minecraft.core.Vec3i;
import net.minecraft.world.level.levelgen.blockpredicates.StateTestingPredicate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(StateTestingPredicate.class)
public interface ShovelItemAccessor {
    @Accessor("offset") Vec3i emi$offset();
}
