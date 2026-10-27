package com.lowdragmc.lowdraglib.client.scene;

import com.lowdragmc.lowdraglib.client.renderer.NativeDelegatedBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import java.util.*;
import java.util.function.BiConsumer;

/** Scene BER capture preserves original full brightness and freezes the hook's complete pose. */
public final class NativeSceneBlockEntities {
    private record Entry(PoseStack.Pose pose,NativeDelegatedBlockEntity.Submission submission) {}
    private final List<Entry> entries;
    private NativeSceneBlockEntities(List<Entry> entries){this.entries=List.copyOf(entries);}
    public static NativeSceneBlockEntities extract(Iterable<BlockEntity> entities,float ticks,Vec3 camera,
            BiConsumer<BlockEntity,PoseStack> hook) {
        var entries=new ArrayList<Entry>();
        for(var entity:entities) {
            if(entity==null || !entity.hasLevel() || !entity.getType().isValid(entity.getBlockState()))continue;
            var pose=new PoseStack();var pos=entity.getBlockPos();pose.translate(pos.getX(),pos.getY(),pos.getZ());
            if(hook!=null)hook.accept(entity,pose);
            var submission=NativeDelegatedBlockEntity.extract(entity,ticks,camera,null,LightCoordsUtil.FULL_BRIGHT);
            if(submission!=null)entries.add(new Entry(pose.last().copy(),submission));
        }
        return new NativeSceneBlockEntities(entries);
    }
    public void submit(SubmitNodeCollector collector,CameraRenderState camera) {
        for(var entry:entries) {
            var pose=new PoseStack();pose.last().set(entry.pose());
            entry.submission().submit(pose,collector,camera);
        }
    }
}
