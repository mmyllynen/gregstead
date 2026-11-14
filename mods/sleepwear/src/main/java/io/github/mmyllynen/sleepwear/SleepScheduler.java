package io.github.mmyllynen.sleepwear;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractBedBlock;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.*;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

public final class SleepScheduler {
  private static final String PAUSE = "sleepwear:pause", DAY = "sleepwear:pause_day";
  private static final Map<UUID, ServerPlayer> active = new HashMap<>();

  private record Task(long at, long sequence, UUID player) implements Comparable<Task> {
    public int compareTo(Task t) {
      int c = Long.compare(at, t.at);
      return c != 0 ? c : Long.compare(sequence, t.sequence);
    }
  }

  private static final PriorityQueue<Task> tasks = new PriorityQueue<>();
  private static final Map<UUID, Task> pending = new HashMap<>();
  private static long tick, sequence;
  public static long attempts, bedScans, equipmentChecks;

  public static void register() {
    NeoForge.EVENT_BUS.addListener((LivingEquipmentChangeEvent e) -> {
      if (e.getEntity() instanceof ServerPlayer p
          && (e.getSlot() == EquipmentSlot.CHEST || e.getSlot() == EquipmentSlot.LEGS))
        refresh(p, true);
    });
    NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedInEvent e) -> {
      if (e.getEntity() instanceof ServerPlayer p) refresh(p, false);
    });
    NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerRespawnEvent e) -> {
      if (e.getEntity() instanceof ServerPlayer p) refresh(p, false);
    });
    NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerChangedDimensionEvent e) -> {
      if (e.getEntity() instanceof ServerPlayer p && active.containsKey(p.getUUID())) schedule(p);
    });
    NeoForge.EVENT_BUS.addListener(
        (PlayerEvent.PlayerLoggedOutEvent e) -> remove(e.getEntity().getUUID()));
    NeoForge.EVENT_BUS.addListener((LivingDeathEvent e) -> {
      if (e.getEntity() instanceof ServerPlayer p) {
        remove(p.getUUID());
        p.getPersistentData().remove(PAUSE);
        p.getPersistentData().remove(DAY);
      }
    });
    NeoForge.EVENT_BUS.addListener((PlayerWakeUpEvent e) -> {
      if (e.getEntity() instanceof ServerPlayer p && active.containsKey(p.getUUID())) {
        if (p.level().isDarkOutside()) {
          p.getPersistentData().putBoolean(PAUSE, true);
          p.getPersistentData().putLong(DAY, Math.floorDiv(p.level().getDefaultClockTime(), 24000));
        }
        schedule(p);
      }
    });
    NeoForge.EVENT_BUS.addListener((ServerTickEvent.Post e) -> {
      tick++;
      while (!tasks.isEmpty() && tasks.peek().at <= tick) {
        Task t = tasks.poll();
        if (pending.get(t.player) != t) continue;
        pending.remove(t.player);
        ServerPlayer p = active.get(t.player);
        if (p != null) attempt(p);
      }
    });
    NeoForge.EVENT_BUS.addListener((ServerStoppedEvent e) -> {
      active.clear();
      tasks.clear();
      pending.clear();
      tick = sequence = 0;
    });
  }

  private static void remove(UUID id) {
    active.remove(id);
    Task old = pending.remove(id);
    if (old != null) tasks.remove(old);
  }

  public static void refresh(ServerPlayer p, boolean equipmentChange) {
    equipmentChecks++;
    boolean dressed =
        p.getItemBySlot(EquipmentSlot.CHEST).is(Sleepwear.SHIRT.get())
            && p.getItemBySlot(EquipmentSlot.LEGS).is(Sleepwear.PANTS.get());
    boolean was = active.containsKey(p.getUUID());
    if (!dressed) {
      remove(p.getUUID());
      if (equipmentChange) {
        p.getPersistentData().remove(PAUSE);
        p.getPersistentData().remove(DAY);
      }
      return;
    }
    active.put(p.getUUID(), p);
    if (!was) schedule(p);
  }

  private static boolean paused(ServerPlayer p) {
    var data = p.getPersistentData();
    if (!data.getBooleanOr(PAUSE, false)) return false;
    if (!p.level().isDarkOutside()
        || data.getLongOr(DAY, -1) != Math.floorDiv(p.level().getDefaultClockTime(), 24000)) {
      data.remove(PAUSE);
      data.remove(DAY);
      return false;
    }
    return true;
  }

  public static void clocksChanged() {
    for (ServerPlayer p : List.copyOf(active.values())) schedule(p);
  }

  public static void darknessChanged(Level level) {
    if (!(level instanceof ServerLevel)) return;
    for (ServerPlayer p : List.copyOf(active.values()))
      if (p.level() == level) {
        paused(p);
        schedule(p);
      }
  }

  private static void schedule(ServerPlayer p) {
    Task old = pending.remove(p.getUUID());
    if (old != null) tasks.remove(old);
    if (!p.level().isDarkOutside() || paused(p) || p.isSleeping()) return;
    Task t = new Task(tick + 1, sequence++, p.getUUID());
    pending.put(t.player, t);
    tasks.add(t);
  }

  private static void retry(ServerPlayer p) {
    Task t = new Task(tick + 100, sequence++, p.getUUID());
    pending.put(t.player, t);
    tasks.add(t);
  }

  private static void attempt(ServerPlayer p) {
    attempts++;
    if (!p.level().isDarkOutside() || paused(p) || p.isSleeping() || !p.isAlive()) return;
    if (!p.isSpectator() && !p.isPassenger()) {
      double range = p.blockInteractionRange();
      int r = (int) Math.ceil(range);
      List<BlockPos> beds = new ArrayList<>();
      bedScans++;
      for (BlockPos pos :
          BlockPos.betweenClosed(
              p.blockPosition().offset(-r, -r, -r), p.blockPosition().offset(r, r, r))) {
        if (!p.level().hasChunkAt(pos)) continue;
        var state = p.level().getBlockState(pos);
        if (!(state.getBlock() instanceof AbstractBedBlock bed)
            || state.getValue(AbstractBedBlock.PART) != BedPart.HEAD
            || state.getValue(AbstractBedBlock.OCCUPIED)) continue;
        var foot =
            p.level()
                .getBlockState(pos.relative(state.getValue(AbstractBedBlock.FACING).getOpposite()));
        if (!foot.is(state.getBlock())
            || foot.getValue(AbstractBedBlock.PART) != BedPart.FOOT
            || foot.getValue(AbstractBedBlock.OCCUPIED)) continue;
        var rule = bed.getBedRule(p.level(), pos);
        if (rule.destroyOnUse() || rule.destroyOnLeave() || !rule.canSleep(p.level())) continue;
        if (reachable(p, pos, range)
            || reachable(
                p, pos.relative(state.getValue(AbstractBedBlock.FACING).getOpposite()), range))
          beds.add(pos.immutable());
      }
      beds.sort(
          Comparator.<BlockPos>comparingDouble(pos -> pos.distToCenterSqr(p.position()))
              .thenComparingInt(BlockPos::getX)
              .thenComparingInt(BlockPos::getY)
              .thenComparingInt(BlockPos::getZ));
      for (BlockPos pos : beds) {
        var state = p.level().getBlockState(pos);
        var bed = (AbstractBedBlock) state.getBlock();
        if (p.startSleepInBed(bed, state, bed.getBedRule(p.level(), pos), pos).right().isPresent())
          return;
      }
    }
    retry(p);
  }

  private static boolean reachable(ServerPlayer p, BlockPos pos, double range) {
    Vec3 eye = p.getEyePosition();
    Vec3 center = Vec3.atCenterOf(pos).add(0, -0.22, 0);
    if (eye.distanceToSqr(center) > range * range) return false;
    var hit =
        p.level()
            .clip(
                new ClipContext(
                    eye, center, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, p));
    return hit.getType() == HitResult.Type.MISS || hit.getBlockPos().equals(pos);
  }
}
