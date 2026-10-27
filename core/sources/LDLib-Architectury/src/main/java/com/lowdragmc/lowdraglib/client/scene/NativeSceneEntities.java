package com.lowdragmc.lowdraglib.client.scene;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import java.util.*;
import java.util.function.BiConsumer;

/** Native entity state/pose snapshots preserve scene interpolation and dispatcher effects. */
public final class NativeSceneEntities {
    private record Entry(EntityRenderState state,PoseStack.Pose pose,double x,double y,double z) {}
    private final List<Entry> entries;
    private NativeSceneEntities(List<Entry> entries){this.entries=List.copyOf(entries);}
    public static NativeSceneEntities extract(Iterable<? extends Entity> entities,Camera camera,float ticks,
            BiConsumer<Entity,PoseStack> hook) {
        var dispatcher=Minecraft.getInstance().getEntityRenderDispatcher();
        var oldCamera=dispatcher.camera;var oldPick=dispatcher.crosshairPickEntity;
        var entries=new ArrayList<Entry>();
        dispatcher.prepare(camera,null);
        try {
            for(var entity:entities) {
                if(entity.tickCount==0){entity.xOld=entity.getX();entity.yOld=entity.getY();entity.zOld=entity.getZ();}
                double x=Mth.lerp(ticks,entity.xOld,entity.getX()),y=Mth.lerp(ticks,entity.yOld,entity.getY()),z=Mth.lerp(ticks,entity.zOld,entity.getZ());
                int light=dispatcher.getPackedLightCoords(entity,ticks);
                var pose=new PoseStack();if(hook!=null)hook.accept(entity,pose);
                var state=dispatcher.extractEntity(entity,ticks);state.lightCoords=light;
                entries.add(new Entry(state,pose.last().copy(),x,y,z));
            }
        } finally { dispatcher.prepare(oldCamera,oldPick); }
        return new NativeSceneEntities(entries);
    }
    public void submit(SubmitNodeCollector collector,CameraRenderState camera) {
        var dispatcher=Minecraft.getInstance().getEntityRenderDispatcher();
        for(var entry:entries) {
            var pose=new PoseStack();pose.last().set(entry.pose());
            dispatcher.submit(entry.state(),camera,entry.x(),entry.y(),entry.z(),pose,collector);
        }
    }
}
