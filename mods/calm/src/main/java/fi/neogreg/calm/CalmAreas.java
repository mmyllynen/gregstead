package fi.neogreg.calm;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import java.util.*;

/** Only loaded crystal block entities are indexed; lookups never load chunks. */
public final class CalmAreas {
    private static final Map<ServerLevel, Map<Long, Set<BlockPos>>> LEVELS = new IdentityHashMap<>();
    static { NeoForge.EVENT_BUS.addListener((ServerStoppedEvent e) -> LEVELS.clear()); }
    static void add(ServerLevel level, BlockPos pos) {
        LEVELS.computeIfAbsent(level, l -> new HashMap<>())
                .computeIfAbsent(ChunkPos.pack(pos.getX() >> 4, pos.getZ() >> 4), k -> new HashSet<>()).add(pos.immutable());
    }
    static void remove(ServerLevel level, BlockPos pos) {
        var chunks = LEVELS.get(level); if (chunks == null) return;
        long key = ChunkPos.pack(pos.getX() >> 4, pos.getZ() >> 4);
        var points = chunks.get(key);
        if (points != null && points.remove(pos) && points.isEmpty()) chunks.remove(key);
        if (chunks.isEmpty()) LEVELS.remove(level);
    }
    public static boolean contains(LivingEntity entity) {
        if (!(entity.level() instanceof ServerLevel level)) return false;
        int radius = NeoGregCalm.settings.radius;
        for (var player : level.players()) {
            if (player.isAlive() && !player.isSpectator() && player.distanceToSqr(entity) <= (double)radius * radius
                && top.theillusivec4.curios.api.CuriosApi.getCuriosInventory(player)
                    .map(inv -> inv.getStacksHandler("necklace").map(slot -> {
                        for (int i=0;i<slot.getSlots();i++)
                            if (inv.isSlotActive("necklace",i) && slot.getStacks().getStackInSlot(i).is(CalmBlocks.AMULET.get())) return true;
                        return false;
                    }).orElse(false)).orElse(false)) return true;
        }
        var chunks = LEVELS.get(level); if (chunks == null) return false;
        int r = NeoGregCalm.settings.radius;
        int x0 = ((int)Math.floor(entity.getX() - r)) >> 4, x1 = ((int)Math.floor(entity.getX() + r)) >> 4;
        int z0 = ((int)Math.floor(entity.getZ() - r)) >> 4, z1 = ((int)Math.floor(entity.getZ() + r)) >> 4;
        for (int x=x0;x<=x1;x++) for (int z=z0;z<=z1;z++) {
            var points = chunks.get(ChunkPos.pack(x,z)); if (points == null) continue;
            for (var p : points) {
                double dx=entity.getX()-p.getX()-0.5, dy=entity.getY()-p.getY()-0.5, dz=entity.getZ()-p.getZ()-0.5;
                if (dx*dx+dy*dy+dz*dz <= (double)r*r) return true;
            }
        }
        return false;
    }
}
