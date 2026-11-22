package fi.gregstead.mushroom.mixin;

import fi.gregstead.mushroom.MushroomDimension;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.Noises;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(Noises.class)
abstract class NoiseSeedMixin {
    @ModifyArg(method = "instantiate", index = 0,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/PositionalRandomFactory;fromHashOf(Lnet/minecraft/resources/Identifier;)Lnet/minecraft/util/RandomSource;"))
    private static Identifier mushroom$vanillaSeed(Identifier id) {
        return id.getNamespace().equals(MushroomDimension.ID) ? Identifier.withDefaultNamespace(id.getPath()) : id;
    }
}
