package fi.gregstead.mushroom;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.fml.ModList;
import xaero.pac.common.server.api.OpenPACServerAPI;

final class PortalClaims {
    static boolean allowsAutomaticPlacement(ServerLevel level, BlockPos pos) {
        return !ModList.get().isLoaded("openpartiesandclaims") || OpenPac.isUnclaimed(level, pos);
    }

    private static final class OpenPac {
        static boolean isUnclaimed(ServerLevel level, BlockPos pos) {
            var api = OpenPACServerAPI.get(level.getServer());
            return api != null && api.getServerClaimsManager().get(level.dimension().identifier(), pos) == null;
        }
    }
}
