package dev.emi.emi.mixin;

import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.mixin.accessor.HandledScreenAccessor;
import dev.emi.emi.platform.EmiAgnos;
import dev.emi.emi.screen.widget.NativeEffectLayout;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.EffectsInInventory;
import net.minecraft.world.effect.MobEffectInstance;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EffectsInInventory.class)
public abstract class AbstractInventoryScreenMixin {
    @Shadow @Final private AbstractContainerScreen<?> screen;
    @Invoker("extractEffects")
    protected abstract void emi$extractEffects(GuiGraphicsExtractor graphics,java.util.Collection<MobEffectInstance> effects,
        int x,int yStep,int mouseX,int mouseY,int maxWidth);
    @Inject(method="canSeeEffects",at=@At("HEAD"),cancellable=true)
    private void emi$visibility(org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<Boolean> info) {
        if(EmiConfig.effectLocation==dev.emi.emi.config.EffectLocation.HIDDEN)info.setReturnValue(false);
    }
    @Inject(method="extractRenderState",at=@At("HEAD"),cancellable=true)
    private void emi$extract(GuiGraphicsExtractor graphics,int mouseX,int mouseY,CallbackInfo info) {
        info.cancel();
        var client=Minecraft.getInstance();
        var effects=client.player.getActiveEffects().stream().filter(net.neoforged.neoforge.client.ClientHooks::shouldRenderEffect).sorted().toList();
        var access=(HandledScreenAccessor)screen;
        var layout=NativeEffectLayout.layout(EmiConfig.effectLocation.name(),screen.width,access.getX(),access.getY(),access.getBackgroundWidth(),
            effects.size(),screen instanceof CreativeModeInventoryScreen,EmiAgnos.isModLoaded("inventorytabs"),EmiAgnos.isForge());
        if(layout.isEmpty())return;
        var first=layout.get().boxes().getFirst();
        var event=net.neoforged.neoforge.client.ClientHooks.onScreenPotionSize(screen,screen.width-access.getX()-access.getBackgroundWidth()-2,
            !layout.get().wide(),first.x());
        if(event.isCanceled())return;
        int restore=access.getY();
        try {
            if(EmiConfig.effectLocation==dev.emi.emi.config.EffectLocation.TOP) {
                for(int i=0;i<effects.size();i++) {
                    var box=layout.get().boxes().get(i);access.emi$setTopPos(box.y());
                    emi$extractEffects(graphics,java.util.List.of(effects.get(i)),box.x()+event.getHorizontalOffset()-first.x(),32,mouseX,mouseY,event.isCompact()?32:box.width());
                }
            } else {
                int step=effects.size()>5?132/(effects.size()-1):33;
                emi$extractEffects(graphics,effects,event.getHorizontalOffset(),step,mouseX,mouseY,event.isCompact()?32:120);
            }
        } finally { access.emi$setTopPos(restore); }
    }
}
