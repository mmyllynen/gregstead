package com.gregtechceu.gtceu.client.renderer;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.block.MaterialBlock;
import com.gregtechceu.gtceu.api.block.MaterialPipeBlock;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.api.machine.feature.ITieredMachine;
import com.gregtechceu.gtceu.api.machine.steam.SteamMachine;
import com.gregtechceu.gtceu.api.pipenet.IPipeNode;
import com.gregtechceu.gtceu.common.blockentity.CableBlockEntity;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.utils.GTUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.context.UseOnContext;
import net.neoforged.neoforge.client.event.ExtractBlockOutlineRenderStateEvent;
import net.neoforged.neoforge.client.event.ExtractLevelRenderStateEvent;
import java.util.*;

public final class AoEHighlightExtractor {
    private AoEHighlightExtractor() {}
    public static void breaking(ExtractLevelRenderStateEvent event) {
        var mc=Minecraft.getInstance();
        if(mc.player==null || mc.player.isShiftKeyDown() || !(mc.hitResult instanceof net.minecraft.world.phys.BlockHitResult hit))return;
        var tool=mc.player.getMainHandItem();
        if(!ToolHelper.hasBehaviorsComponent(tool) || !tool.isCorrectToolForDrops(event.getLevel().getBlockState(hit.getBlockPos())))return;
        var aoe=ToolHelper.getAoEDefinition(tool);
        if(aoe.isZero())return;
        var progress=event.getLevel().destructionProgress().get(hit.getBlockPos().asLong());
        if(progress==null || progress.isEmpty())return;
        NativeAoEBreaking.append(event.getRenderState(),ToolHelper.getHarvestableBlocks(aoe,
                new UseOnContext(mc.player,InteractionHand.MAIN_HAND,hit)),event.getLevel(),progress.last().getProgress());
    }
    public static net.neoforged.neoforge.client.CustomBlockOutlineRenderer outline(ExtractBlockOutlineRenderStateEvent event) {
        var mc=Minecraft.getInstance();var player=mc.player;
        if(player==null)return new NativeAoEOutlines(List.of(),false,1,false).renderer();
        var level=event.getLevel();var pos=event.getBlockPos();var tool=player.getMainHandItem();
        boolean aoe=!event.getBlockState().isAir() && !player.isShiftKeyDown() && level.isInWorldBounds(pos)
                && tool.isCorrectToolForDrops(event.getBlockState()) && ToolHelper.hasBehaviorsComponent(tool);
        var positions=new ArrayList<BlockPos>();
        if(aoe)positions.addAll(ToolHelper.getHarvestableBlocks(ToolHelper.getAoEDefinition(tool),
                new UseOnContext(player,InteractionHand.MAIN_HAND,event.getHitResult())));
        else positions.add(pos);
        positions.sort(Comparator.comparingInt(p -> level.getBlockState(p).getBlock() instanceof MaterialBlock?1:0));
        var entries=new ArrayList<NativeAoEOutlines.Entry>();boolean suppress=aoe;
        for(var blockPos:positions) {
            var state=level.getBlockState(blockPos);Integer color=color(level,blockPos,state);
            if(aoe || color!=null)entries.add(new NativeAoEOutlines.Entry(blockPos,state.getShape(level,blockPos,event.getCollisionContext()),color));
            if(color!=null)suppress=true;
        }
        return new NativeAoEOutlines(entries,suppress,mc.gameRenderer.gameRenderState().windowRenderState.appropriateLineWidth,
                mc.gameRenderer.useImprovedTransparency()).renderer();
    }
    private static Integer color(net.minecraft.client.multiplayer.ClientLevel level,BlockPos pos,net.minecraft.world.level.block.state.BlockState state) {
        var cfg=ConfigHolder.INSTANCE.client.renderer;
        var material=ChemicalHelper.getMaterialEntry(state.getBlock());Integer rgb=null;
        if(cfg.coloredMaterialBlockOutline && !material.isEmpty())rgb=material.material().getMaterialRGB();
        else if(cfg.coloredTieredMachineOutline) {
            if(level.getBlockEntity(pos) instanceof SteamMachine steam)rgb=steam.isHighPressure()?GTValues.VC_HP_STEAM:GTValues.VC_LP_STEAM;
            else if(level.getBlockEntity(pos) instanceof ITieredMachine tiered)rgb=GTValues.VCM[tiered.getTier()];
        } else if(cfg.coloredWireOutline && level.getBlockEntity(pos) instanceof IPipeNode<?,?> pipe) {
            if(!pipe.getFrameMaterial().isNull())rgb=pipe.getFrameMaterial().getMaterialRGB();
            else if(pipe instanceof CableBlockEntity cable)rgb=GTValues.VCM[GTUtil.getTierByVoltage(cable.getNodeData().getVoltage())];
            else if(state.getBlock() instanceof MaterialPipeBlock<?,?,?> block)rgb=block.material.getMaterialRGB();
        }
        return rgb==null?null:0x66000000 | rgb & 0xFFFFFF;
    }
}
