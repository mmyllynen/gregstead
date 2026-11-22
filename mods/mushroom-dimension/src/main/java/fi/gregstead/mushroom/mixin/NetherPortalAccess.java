package fi.gregstead.mushroom.mixin;

import net.minecraft.util.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.portal.TeleportTransition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(NetherPortalBlock.class)
public interface NetherPortalAccess {
    @Invoker("getDimensionTransitionFromExit")
    static TeleportTransition mushroom$transition(Entity entity, BlockPos entry, BlockUtil.FoundRectangle exit,
                                                  ServerLevel destination, TeleportTransition.PostTeleportTransition post) {
        throw new AssertionError();
    }
}
