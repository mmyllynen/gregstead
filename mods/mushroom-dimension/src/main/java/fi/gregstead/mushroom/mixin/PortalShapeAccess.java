package fi.gregstead.mushroom.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.portal.PortalShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PortalShape.class)
public interface PortalShapeAccess {
    @Accessor("bottomLeft") BlockPos mushroom$bottomLeft();
    @Accessor("rightDir") Direction mushroom$right();
    @Accessor("width") int mushroom$width();
    @Accessor("height") int mushroom$height();
}
