package com.gregtechceu.gtceu.common.entity;

import com.gregtechceu.gtceu.core.mixins.PrimedTntAccessor;

import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ServerExplosion;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.ExplosionParticleInfo;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.random.WeightedList;
import java.util.Optional;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.EventHooks;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class GTExplosiveEntity extends PrimedTnt {

    public GTExplosiveEntity(EntityType<? extends GTExplosiveEntity> type,
                             Level level, double x, double y, double z,
                             @Nullable LivingEntity owner) {
        this(type, level);
        this.setPos(x, y, z);
        double d = level.getRandom().nextDouble() * (float) (Math.PI * 2);
        this.setDeltaMovement(-Math.sin(d) * 0.02, 0.2F, -Math.cos(d) * 0.02);
        this.setFuse(80);
        this.xo = x;
        this.yo = y;
        this.zo = z;
        ((PrimedTntAccessor) this).setOwner(net.minecraft.world.entity.EntityReference.of(owner));
    }

    public GTExplosiveEntity(EntityType<? extends GTExplosiveEntity> type, Level level) {
        super(type, level);
    }

    /**
     * @return The strength of the explosive.
     */
    protected abstract float getStrength();

    /**
     * @return Whether to drop all blocks, or use default logic
     */
    public abstract boolean dropsAllBlocks();

    /**
     * @return The range of the explosive, if {@link #dropsAllBlocks} is true.
     */
    protected int getRange() {
        return 2;
    }

    /**
     * @return The block state of the block this explosion entity is created by.
     */
    public abstract @NotNull BlockState getExplosiveState();

    @Override
    protected void explode() {
        explode(level(), this, this.getX(), this.getY(0.0625), this.getZ(), getStrength(), dropsAllBlocks());
    }

    protected void explode(Level level, @Nullable Entity source, double x, double y, double z,
                           float radius, boolean dropBlocks) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        var center = new Vec3(x, y, z);
        var explosion = new ServerExplosion(serverLevel, source, null, null, center, radius, false,
                dropBlocks ? Explosion.BlockInteraction.DESTROY_WITH_DECAY : Explosion.BlockInteraction.DESTROY);
        if (EventHooks.onExplosionStart(serverLevel, explosion)) return;
        int blockCount = explosion.explode();
        var particle = explosion.isSmall() ? ParticleTypes.EXPLOSION : ParticleTypes.EXPLOSION_EMITTER;
        var blockParticles = WeightedList.<ExplosionParticleInfo>builder()
                .add(new ExplosionParticleInfo(ParticleTypes.POOF, 0.5F, 1.0F))
                .add(new ExplosionParticleInfo(ParticleTypes.SMOKE, 1.0F, 1.0F)).build();
        for (ServerPlayer player : serverLevel.players()) {
            if (player.distanceToSqr(center) < 4096.0) {
                player.connection.send(new ClientboundExplodePacket(center, radius, blockCount,
                        Optional.ofNullable(explosion.getHitPlayers().get(player)), particle,
                        SoundEvents.GENERIC_EXPLODE, blockParticles, source == null || !source.isSilent()));
            }
        }
    }
}
