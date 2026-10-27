package com.lowdragmc.lowdraglib.client.scene;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;

/**
 * @author KilaBash
 * @date 2022/06/05
 * @implNote CameraEntity,
 */
public class CameraEntity extends Entity {

    public CameraEntity(Level pLevel) {
        super(EntityTypes.PLAYER, pLevel);
    }

    @Override
    public boolean hurtServer(net.minecraft.server.level.ServerLevel level,
                              net.minecraft.world.damagesource.DamageSource source, float amount) {
        if (!isInvulnerableToBase(source)) markHurt();
        return false;
    }

    @Override
    public boolean hurtClient(net.minecraft.world.damagesource.DamageSource source) {
        if (!isInvulnerableToBase(source)) markHurt();
        return false;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder p_326003_) {

    }

    @Override
    protected void readAdditionalSaveData(@Nonnull ValueInput input) {

    }

    @Override
    protected void addAdditionalSaveData(@Nonnull ValueOutput output) {

    }

    @Override
    @Nonnull
    public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity serverEntity) {
        return new ClientboundAddEntityPacket(this, serverEntity);
    }
}
