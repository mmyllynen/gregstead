package com.gregtechceu.gtceu.client;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.cosmetics.CapeRegistry;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.client.renderer.BlockHighlightRenderer;
import com.gregtechceu.gtceu.client.renderer.MultiblockInWorldPreviewRenderer;
import com.gregtechceu.gtceu.client.util.TooltipHelper;
import com.gregtechceu.gtceu.common.commands.GTClientCommands;
import com.gregtechceu.gtceu.common.data.GTAttributeModifierIds;
import com.gregtechceu.gtceu.common.data.GTMobEffects;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.gregtechceu.gtceu.integration.map.ClientCacheManager;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.event.PlayerHeartTypeEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;


import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@EventBusSubscriber(modid = GTCEu.MOD_ID, value = Dist.CLIENT)
@OnlyIn(Dist.CLIENT)
public class ClientEventListener {

    private static final net.minecraft.util.context.ContextKey<MultiblockInWorldPreviewRenderer.Snapshot> PREVIEW =
            new net.minecraft.util.context.ContextKey<>(GTCEu.id("multiblock_preview"));

    @SubscribeEvent
    public static void extractMultiblockPreview(net.neoforged.neoforge.client.event.ExtractLevelRenderStateEvent event) {
        com.gregtechceu.gtceu.client.renderer.AoEHighlightExtractor.breaking(event);
        event.getRenderState().setRenderData(PREVIEW, MultiblockInWorldPreviewRenderer.extractPreview(
                event.getCamera().position(), event.getRenderState().worldPartialTicks));
    }

    @SubscribeEvent
    public static void submitMultiblockPreview(net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent event) {
        var preview=event.getLevelRenderState().getRenderData(PREVIEW);
        if(preview!=null)preview.submit(event.getPoseStack(),event.getSubmitNodeCollector(),
                event.getLevelRenderState().cameraRenderState);
    }

    @SubscribeEvent
    public static void clearPreviewAfterReload(net.neoforged.neoforge.client.event.ClientResourceLoadFinishedEvent event) {
        MultiblockInWorldPreviewRenderer.cleanPreview();
    }

    @SubscribeEvent
    public static void registerPlayerCapes(net.neoforged.neoforge.client.renderstate.RegisterRenderStateModifiersEvent event) {
        NativePlayerCapes.register(event, CapeRegistry::getPlayerCapeTexture);
        NativeArmorModelContext.register(event);
    }

    @SubscribeEvent
    public static void updateFOV(ComputeFovModifierEvent event) {
        Player player = event.getPlayer();

        AttributeInstance speedAttribute = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttribute == null || !speedAttribute.hasModifier(GTAttributeModifierIds.BLOCK_SPEED_BOOST)) {
            return;
        }

        float multiplier = 1.0f;
        BlockState state = player.level().getBlockState(player.getOnPos());

        // inverse of the math done with the speed attribute in AbstractClientPlayer
        if (state.is(CustomTags.VERY_FAST_WALKABLE_BLOCKS)) {
            // base speed is 0.1, boost is 0.1*0.6 -> boosted speed = 0.16
            // the FOV modifier is `1 + (speed / base speed + 1) / 2`, so `1 + (0.16 / 0.1 + 1) / 2 = 1.3`
            // thus, divide by 1.3 to get back to original FOV before the 'fast block boost' modifier
            multiplier /= 1.3f;
        } else if (state.is(CustomTags.FAST_WALKABLE_BLOCKS)) {
            // same as above but the speed boost is 0.25
            multiplier /= 1.125f;
        }

        multiplier = (float) Mth.lerp(Minecraft.getInstance().options.fovEffectScale().get(), 1.0, multiplier);
        event.setNewFovModifier(event.getNewFovModifier() * multiplier);
    }

    private static double getValueWithoutWalkingBoost(AttributeInstance attribute) {
        double base = attribute.getBaseValue();
        Map<AttributeModifier.Operation, List<AttributeModifier>> modifiers = attribute.getModifiers().stream()
                .collect(Collectors.groupingBy(AttributeModifier::operation));

        for (AttributeModifier mod : modifiers.get(AttributeModifier.Operation.ADD_VALUE)) {
            base += mod.amount();
        }

        double applied = base;
        for (AttributeModifier mod : modifiers.get(AttributeModifier.Operation.ADD_MULTIPLIED_BASE)) {
            if (mod.id() == GTAttributeModifierIds.BLOCK_SPEED_BOOST) continue;
            applied += base * mod.amount();
        }

        for (AttributeModifier mod : modifiers.get(AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)) {
            applied *= 1 + mod.amount();
        }

        return attribute.getAttribute().value().sanitizeValue(applied);
    }

    @SubscribeEvent
    public static void onBlockHighlightEvent(ExtractBlockOutlineRenderStateEvent event) {
        event.addCustomRenderer(com.gregtechceu.gtceu.client.renderer.AoEHighlightExtractor.outline(event));
        event.addCustomRenderer(BlockHighlightRenderer.extract(event.getCamera(),event.getHitResult(),
                Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false)));
    }

    @SubscribeEvent
    public static void onRenderPlayerHearts(PlayerHeartTypeEvent event) {
        NativeWeakPoisonHearts.apply(event,event.getEntity().hasEffect(GTMobEffects.WEAK_POISON));
    }

    @SubscribeEvent
    public static void onClientTickEvent(ClientTickEvent.Post event) {
        TooltipHelper.onClientTick();
        MultiblockInWorldPreviewRenderer.onClientTick();
        EnvironmentalHazardClientHandler.INSTANCE.onClientTick();
        GTValues.CLIENT_TIME++;
    }

    @SubscribeEvent
    public static void onLevelUnloadEvent(LevelEvent.Unload event) {
    }

    public static java.util.List<String> machineDebugLines() {
        Minecraft mc=Minecraft.getInstance();
        if(mc.showOnlyReducedInfo())return java.util.List.of();
        Entity camera=mc.getCameraEntity();
        if(camera==null || mc.level==null)return java.util.List.of();
        BlockHitResult hit=ToolHelper.entityPickBlock(camera,20.0,0,false);
        if(hit.getType()==HitResult.Type.MISS)return java.util.List.of();
        if(!(mc.level.getBlockEntity(hit.getBlockPos()) instanceof MetaMachine machine))return java.util.List.of();
        var lines=new java.util.ArrayList<String>();machine.addDebugOverlayText(lines::add);
        return java.util.List.copyOf(lines);
    }

    @SubscribeEvent
    public static void onClientDisconnect(ClientPlayerNetworkEvent.LoggingOut event) {
        MultiblockInWorldPreviewRenderer.cleanPreview();
        ClientCacheManager.allowReinit();
    }

    @SubscribeEvent
    public static void registerClientCommand(RegisterClientCommandsEvent event) {
        GTClientCommands.register(event.getDispatcher(), event.getBuildContext());
    }

    @SubscribeEvent
    public static void serverStopped(ServerStoppedEvent event) {
        ClientCacheManager.clearCaches();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void recipesSynced(RecipesReceivedEvent event) {
        var received=new java.util.HashMap<Identifier,net.minecraft.world.item.crafting.RecipeHolder<?>>();
        for(var holder:event.getRecipeMap().values()) {
            received.put(holder.id().identifier(),holder);
            if(holder.value() instanceof GTRecipe recipe)recipe.setId(holder.id().identifier());
        }
        for(var category:GTRegistries.RECIPE_CATEGORIES) {
            GTRecipeType type=category.getRecipeType();
            if(!event.getRecipeTypes().contains(type))continue;
            for(GTRecipe recipe:type.getRecipesInCategory(category)) {
                var holder=received.get(recipe.id);
                if(holder!=null)recipe.setId(holder.id().identifier());
            }
        }
    }
}
