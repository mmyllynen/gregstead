package fi.neogreg.calm.client;

import fi.neogreg.calm.CalmVisualState;
import com.google.common.reflect.TypeToken;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import net.neoforged.neoforge.client.renderstate.RegisterRenderStateModifiersEvent;

/** Small depth-tested, unlit ring; no glowing status, teams, particles or potion effects. */
@EventBusSubscriber(modid="neogreg_calm",value=Dist.CLIENT)
public final class CalmHalo {
    private static final ContextKey<Boolean> ACTIVE=new ContextKey<>(Identifier.fromNamespaceAndPath("neogreg_calm","halo"));
    private static final int SEGMENTS=32;
    @SubscribeEvent public static void modifiers(RegisterRenderStateModifiersEvent event){
        event.registerEntityModifier(new TypeToken<LivingEntityRenderer<LivingEntity,LivingEntityRenderState,?>>() {},(entity,state)->{
            var viewer=Minecraft.getInstance().player;
            state.setRenderData(ACTIVE,viewer!=null&&entity.isAlive()&&!entity.isInvisibleTo(viewer)&&CalmVisualState.active(entity));
        });
    }
    public static boolean shouldRender(LivingEntityRenderState state){return Boolean.TRUE.equals(state.getRenderData(ACTIVE))&&!state.isInvisible&&!state.isInvisibleToPlayer&&state.deathTime==0&&state.distanceToCameraSq<=48*48;}
    @SubscribeEvent public static void render(RenderLivingEvent.Post<?,?,?> event){
        var state=event.getRenderState();if(!shouldRender(state))return;
        float radius=Math.max(0.23f,Math.min(0.6f,state.boundingBoxWidth*0.5f));
        float height=state.boundingBoxHeight+0.18f;
        float pulse=0.56f+0.08f*(float)Math.sin(state.ageInTicks*Math.PI/40.0);
        // POSITION_COLOR quads with normal depth testing, no texture/lightmap sampling.
        event.getSubmitNodeCollector().submitCustomGeometry(event.getPoseStack(),RenderTypes.debugQuads(),(pose,vertices)->{
            ring(pose,vertices,radius,height,0.045f,pulse);
            ring(pose,vertices,radius,height,0.075f,pulse*0.18f);
        });
    }
    private static void ring(PoseStack.Pose pose,VertexConsumer v,float radius,float height,float width,float alpha){
        float inner=radius-width/2,outer=radius+width/2;
        for(int i=0;i<SEGMENTS;i++){
            double a=2*Math.PI*i/SEGMENTS,b=2*Math.PI*(i+1)/SEGMENTS;
            vertex(pose,v,inner,a,height,alpha);vertex(pose,v,outer,a,height,alpha);
            vertex(pose,v,outer,b,height,alpha);vertex(pose,v,inner,b,height,alpha);
        }
    }
    private static void vertex(PoseStack.Pose pose,VertexConsumer v,float r,double a,float y,float alpha){v.addVertex(pose,(float)Math.cos(a)*r,y,(float)Math.sin(a)*r).setColor(0.31f,0.91f,0.84f,alpha);}
}
