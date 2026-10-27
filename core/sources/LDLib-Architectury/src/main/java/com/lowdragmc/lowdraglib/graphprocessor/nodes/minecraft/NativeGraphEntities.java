package com.lowdragmc.lowdraglib.graphprocessor.nodes.minecraft;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.phys.Vec3;
import org.slf4j.LoggerFactory;

/** Native graph entity construction and side-appropriate kill dispatch. */
public final class NativeGraphEntities {
    private NativeGraphEntities() {}
    public static Entity create(Level level, EntityType<?> type, CompoundTag tag, Vec3 position) {
        var entity = type.create(level, EntitySpawnReason.COMMAND);
        if (entity == null) return null;
        if (tag != null) {
            try (var reporter = new ProblemReporter.ScopedCollector(entity.problemPath(), LoggerFactory.getLogger(NativeGraphEntities.class))) {
                entity.load(TagValueInput.create(reporter, level.registryAccess(), tag));
            }
        }
        entity.setPos(position);
        return entity;
    }
    public static void kill(Entity entity) {
        if (entity.level() instanceof ServerLevel server) entity.kill(server);
        else if (entity instanceof LivingEntity) entity.hurtOrSimulate(entity.damageSources().genericKill(), Float.MAX_VALUE);
        else {
            entity.remove(Entity.RemovalReason.KILLED);
            entity.gameEvent(GameEvent.ENTITY_DIE);
        }
    }
}
