package com.firemerald.additionalplacements.test.client;

import com.firemerald.additionalplacements.AdditionalPlacementsMod;
import com.firemerald.additionalplacements.block.AdditionalPlacementBlock;
import com.firemerald.additionalplacements.generation.Registration;
import java.util.ArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value=AdditionalPlacementsMod.MOD_ID,dist=Dist.CLIENT)
public final class PlacementSmokeClient {
    private int ticks;
    private boolean reloaded;
    private boolean rejoining;
    public PlacementSmokeClient(){if(Boolean.getBoolean("ap.smokeClient"))NeoForge.EVENT_BUS.addListener(this::tick);}
    private void capture(String name){var mc=Minecraft.getInstance();Screenshot.grab(mc.gameDirectory,name,mc.gameRenderer.mainRenderTarget(),1,message->System.out.println("AP_SMOKE_SCREENSHOT "+message.getString()));}
    private void verifyHighlight() {
        var mc=Minecraft.getInstance();var hit=new net.minecraft.world.phys.BlockHitResult(
            new net.minecraft.world.phys.Vec3(5.5,101.5,10),Direction.SOUTH,new BlockPos(5,101,9),false);
        var state=new net.minecraft.client.renderer.state.level.LevelRenderState();
        state.cameraRenderState.pos=mc.player.position();
        // NeoForge posts this event before constructing the vanilla outline state.
        if(state.blockOutlineRenderState!=null)throw new IllegalStateException("Expected null extraction state");
        for(var item:java.util.List.of(net.minecraft.world.item.Items.DARK_OAK_SLAB,net.minecraft.world.item.Items.OAK_SLAB,net.minecraft.world.item.Items.OAK_STAIRS)) {
            mc.player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,new net.minecraft.world.item.ItemStack(item));
            for(boolean highContrast:new boolean[]{false,true}) {
                var event=new net.neoforged.neoforge.client.event.ExtractBlockOutlineRenderStateEvent(
                    mc.levelExtractor,mc.level,hit.getBlockPos(),mc.level.getBlockState(hit.getBlockPos()),hit,
                    net.minecraft.world.phys.shapes.CollisionContext.of(mc.player),false,highContrast,mc.gameRenderer.mainCamera(),state);
                NeoForge.EVENT_BUS.post(event);
                if(event.getCustomRenderers().isEmpty())throw new IllegalStateException("No custom placement renderer");
                var geometry=new com.firemerald.additionalplacements.client.HighlightGeometry();
                var collector=(net.minecraft.client.renderer.SubmitNodeCollector)java.lang.reflect.Proxy.newProxyInstance(
                    getClass().getClassLoader(),new Class<?>[]{net.minecraft.client.renderer.SubmitNodeCollector.class},(proxy,method,args)->{
                        if(method.getName().equals("submitCustomGeometry")) {
                            ((net.minecraft.client.renderer.SubmitNodeCollector.CustomGeometryRenderer)args[2]).render(
                                ((com.mojang.blaze3d.vertex.PoseStack)args[0]).last(),geometry.buffer((net.minecraft.client.renderer.rendertype.RenderType)args[1]));
                            return null;
                        }
                        // GregTech contributes a separate AoE outline to the same event.
                        // This probe counts AP's custom vertices, not other mods' shape outlines.
                        if(method.getName().equals("submitShapeOutline"))return null;
                        throw new UnsupportedOperationException(method.getName());
                    });
                var outline=new net.minecraft.client.renderer.state.level.BlockOutlineRenderState(hit.getBlockPos(),false,highContrast,
                    net.minecraft.world.phys.shapes.Shapes.block(),event.getCustomRenderers());
                for(var renderer:event.getCustomRenderers())renderer.render(outline,collector,new com.mojang.blaze3d.vertex.PoseStack(),state);
                int vertices=geometry.snapshot().values().stream().mapToInt(java.util.List::size).sum();
                if(vertices==0)throw new IllegalStateException("Placement preview did not extract vertices for "+item);
                System.out.println("AP_SMOKE_HIGHLIGHT_PASS item="+item+" highContrast="+highContrast+" vertices="+vertices+" nullOutline=true");
            }
        }
        mc.player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DARK_OAK_SLAB));
    }

    private void verifyModels(){
        var mc=Minecraft.getInstance();var models=mc.getModelManager().getBlockStateModelSet();int states=0,quads=0;
        for(var entry:Registration.created().toList()){
            for(var state:entry.newBlock().getStateDefinition().getPossibleStates()){
                if(models.get(state)==models.missingModel())throw new IllegalStateException("Missing generated model: "+state);
                states++;
            }
            var state=entry.newBlock().defaultBlockState();var parts=new ArrayList<BlockStateModelPart>();
            models.get(state).collectParts(mc.level,new BlockPos(0,101,0),state,RandomSource.create(1),parts);
            if(parts.isEmpty())throw new IllegalStateException("Empty generated model: "+state);
            for(var part:parts){
                for(Direction side:Direction.values())quads+=part.getQuads(side).size();
                quads+=part.getQuads(null).size();
                if(part.particleMaterial().sprite().contents().name().getPath().contains("missingno"))throw new IllegalStateException("Missing generated texture: "+state);
            }
        }
        System.out.println("AP_SMOKE_MODELS_PASS variants="+Registration.created().count()+" states="+states+" sampleQuads="+quads);
    }
    private void tick(ClientTickEvent.Post event){
        var mc=Minecraft.getInstance();
        if(mc.level==null||mc.player==null||!(mc.level.getBlockState(new BlockPos(0,101,0)).getBlock() instanceof AdditionalPlacementBlock<?>))return;
        ticks++;
        if(ticks==60){verifyModels();verifyHighlight();capture("additional-placements-gallery.png");}
        if(ticks==90&&!rejoining)mc.reloadResourcePacks().whenComplete((ignored,error)->mc.execute(()->{
            if(error!=null)throw new IllegalStateException("AP resource reload failed",error);
            verifyModels();verifyHighlight();reloaded=true;capture("additional-placements-reloaded.png");
            System.out.println("AP_SMOKE_CLIENT_PASS: NeoGreg registry sync, generated models and resource reload");
        }));
        if(ticks>150&&reloaded&&!rejoining){
            rejoining=true;ticks=0;
            net.minecraft.client.gui.screens.ConnectScreen.startConnecting(new net.minecraft.client.gui.screens.TitleScreen(),mc,
                net.minecraft.client.multiplayer.resolver.ServerAddress.parseString("127.0.0.1:25587"),
                new net.minecraft.client.multiplayer.ServerData("AP regression","127.0.0.1:25587",net.minecraft.client.multiplayer.ServerData.Type.OTHER),true,null);
        }
        if(rejoining&&ticks>90){
            if(!mc.player.getMainHandItem().is(net.minecraft.world.item.Items.DARK_OAK_SLAB))throw new IllegalStateException("Rejoined without dark oak slab");
            System.out.println("AP_SMOKE_REJOIN_PASS: dark oak slab held, preview and render frames survived");
            mc.stop();
        }
    }
}
