package fi.neogreg.calm;

import com.google.gson.GsonBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Enderman;
import net.minecraft.world.entity.monster.cubemob.AbstractCubeMob;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import java.nio.file.Files;
import java.util.List;

/** Server-authoritative, player-provoked combat without changing spawning or loot. */
@Mod("neogreg_calm")
public final class NeoGregCalm {
    private static final String TIMER = "neogreg_calm:remaining_ticks";
    static Settings settings;

    public NeoGregCalm(net.neoforged.bus.api.IEventBus bus) {
        CuriosReloadCompatibility.register();
        CalmBlocks.register(bus);
        CalmVisualState.register(bus);
        KitDistribution.register();
        FirstGift.register();
        settings = readSettings();
        NeoForge.EVENT_BUS.addListener(NeoGregCalm::onTarget);
        NeoForge.EVENT_BUS.addListener(NeoGregCalm::onDamage);
        NeoForge.EVENT_BUS.addListener(NeoGregCalm::onIncomingDamage);
        NeoForge.EVENT_BUS.addListener(NeoGregCalm::onTick);
    }

    public static final class Settings {
        public boolean enabled = true;
        public int radius = 32;
        public int provokeDuration = 400;
        public boolean allowSlimeContactDamage = true;
        public List<String> excludedMobs = List.of("minecraft:ender_dragon", "minecraft:wither", "minecraft:warden");
    }

    private static Settings readSettings() {
        var gson = new GsonBuilder().setPrettyPrinting().create();
        var path = FMLPaths.CONFIGDIR.get().resolve("neogreg-calm.json");
        try {
            Settings result;
            if (Files.exists(path)) result = gson.fromJson(Files.readString(path), Settings.class);
            else {
                result = new Settings();
                Files.createDirectories(path.getParent());
                Files.writeString(path, gson.toJson(result) + "\n");
            }
            if (result == null || result.provokeDuration < 1 || result.provokeDuration > 72000 || result.excludedMobs == null || result.radius < 1 || result.radius > 256)
                throw new IllegalArgumentException("Invalid NeoGreg Calm settings: duration must be 1..72000 ticks and exclusions a list");
            Files.writeString(path, gson.toJson(result) + "\n");
            return result;
        } catch (Exception e) { throw new IllegalStateException("Cannot load " + path, e); }
    }

    static boolean managed(LivingEntity entity) {
        return settings.enabled && !entity.level().isClientSide() && entity instanceof Mob
            && (entity instanceof Enemy || entity instanceof NeutralMob)
            && !settings.excludedMobs.contains(BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString());
    }
    static boolean provoked(LivingEntity entity) {
        return entity.getPersistentData().getInt(TIMER).orElse(0) > 0;
    }

    /** Reject protected players before a combat goal can displace another target. */
    public static boolean blocksPlayerTarget(LivingEntity attacker, LivingEntity target) {
        return target instanceof Player && managed(attacker) && !provoked(attacker)
            && CalmAreas.contains(attacker);
    }

    private static void onTarget(LivingChangeTargetEvent event) {
        if (blocksPlayerTarget(event.getEntity(), event.getNewAboutToBeSetTarget()))
            event.setCanceled(true);
    }
    private static void onDamage(LivingDamageEvent.Post event) {
        if (managed(event.getEntity()) && event.getInflictedDamage() > 0 && event.getSource().getEntity() instanceof Player) {
            event.getEntity().getPersistentData().putInt(TIMER, settings.provokeDuration);
            CalmVisualState.set(event.getEntity(),false);
        }
    }
    private static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof Player) || !(event.getSource().getEntity() instanceof Mob mob)) return;
        if (!managed(mob) || !CalmAreas.contains(mob) || provoked(mob)) return;
        // Accepted exception: slime and magma cube collision damage is still vanilla.
        if (settings.allowSlimeContactDamage && mob instanceof AbstractCubeMob) return;
        event.setCanceled(true);
    }
    private static void onTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof Mob mob) || mob.level().isClientSide()) return;
        if (!managed(mob)) { CalmVisualState.set(mob,false); return; }
        int remaining = mob.getPersistentData().getInt(TIMER).orElse(0);
        if (remaining > 0) {
            remaining--;
            mob.getPersistentData().putInt(TIMER, remaining);
        }
        CalmVisualState.refresh(mob);
        if (remaining > 0 || !CalmAreas.contains(mob)) return;
        // canAttack now hides protected targets from getTarget(); clear the stored reference too.
        boolean playerAggression = mob.getTargetUnchecked() instanceof Player;
        if (playerAggression) mob.setTarget(null);
        var brain = mob.getBrain();
        if (brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET) && brain.getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null) instanceof Player) {
            playerAggression = true;
            brain.eraseMemory(MemoryModuleType.ATTACK_TARGET);
        }
        if (brain.hasMemoryValue(MemoryModuleType.WALK_TARGET)) brain.getMemory(MemoryModuleType.WALK_TARGET).ifPresent(target -> {
            if (target.getTarget() instanceof EntityTracker tracker && tracker.getEntity() instanceof Player)
                brain.eraseMemory(MemoryModuleType.WALK_TARGET);
        });
        if (brain.hasMemoryValue(MemoryModuleType.ANGRY_AT)) {
            var angryAt = brain.getMemory(MemoryModuleType.ANGRY_AT).orElse(null);
            if (playerAggression || (angryAt != null && mob.level().getEntity(angryAt) instanceof Player))
                brain.eraseMemory(MemoryModuleType.ANGRY_AT);
        }
        if (mob instanceof NeutralMob neutral) {
            var target = neutral.getPersistentAngerTarget();
            if (playerAggression || neutral.isAngryAtAllPlayers((net.minecraft.server.level.ServerLevel)mob.level())
                    || (target != null && target.getEntity(mob.level(), LivingEntity.class) instanceof Player))
                neutral.stopBeingAngry();
        }
        if (mob instanceof Creeper creeper && !creeper.isIgnited() && creeper.getSwellDir() > 0 && mob.getTarget() == null) creeper.setSwellDir(-1);
    }
}
