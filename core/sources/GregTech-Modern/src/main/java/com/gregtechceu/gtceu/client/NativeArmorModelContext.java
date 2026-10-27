package com.gregtechceu.gtceu.client;

import com.google.common.reflect.TypeToken;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.renderstate.RegisterRenderStateModifiersEvent;
import java.util.function.Supplier;

/** Preserves the legacy armor model callback's wearer/slot during native model selection. */
public final class NativeArmorModelContext {
    private static final ContextKey<LivingEntity> WEARER = new ContextKey<>(Identifier.parse("gtceu:armor_wearer"));
    private static final ThreadLocal<Context> CURRENT = new ThreadLocal<>();
    private record Context(LivingEntity wearer, ItemStack stack, EquipmentSlot slot) {}
    private NativeArmorModelContext() {}

    public static void register(RegisterRenderStateModifiersEvent event) {
        event.registerEntityModifier(new TypeToken<EntityRenderer<Entity, EntityRenderState>>() {},
                (entity, state) -> extract(entity, state));
    }
    public static void extract(Entity entity, EntityRenderState state) {
        state.setRenderData(WEARER, entity instanceof LivingEntity living ? living : null);
    }
    public static <T> T select(Object state, ItemStack stack, Supplier<T> action) {
        Context previous = CURRENT.get();
        var equippable = stack.get(DataComponents.EQUIPPABLE);
        var wearer = state instanceof EntityRenderState entityState ? entityState.getRenderData(WEARER) : null;
        try {
            if (wearer != null && equippable != null) CURRENT.set(new Context(wearer, stack, equippable.slot()));
            else CURRENT.remove();
            return action.get();
        } finally {
            if (previous == null) CURRENT.remove(); else CURRENT.set(previous);
        }
    }
    public static Model apply(ItemStack stack, Model original, Callback callback) {
        var context = CURRENT.get();
        if (context != null && context.stack == stack && original instanceof HumanoidModel<?> humanoid)
            return callback.apply(context.wearer, stack, context.slot, humanoid);
        return original;
    }
    @FunctionalInterface
    public interface Callback {
        HumanoidModel<?> apply(LivingEntity wearer, ItemStack stack, EquipmentSlot slot, HumanoidModel<?> original);
    }
}
