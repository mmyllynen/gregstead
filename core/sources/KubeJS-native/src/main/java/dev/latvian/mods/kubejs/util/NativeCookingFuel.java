package dev.latvian.mods.kubejs.util;

import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.component.CookingFuel;
import net.minecraft.world.level.storage.loot.predicates.LootPredicates;
import net.minecraft.world.level.storage.loot.providers.number.floats.ContextFloatProviders;
import net.minecraft.world.level.storage.loot.providers.number.ints.*;

/** Fuel resources can be generated before the reloadable provider registries exist. */
public final class NativeCookingFuel {
    private NativeCookingFuel() {}

    public static final net.neoforged.neoforge.registries.DeferredRegister<net.minecraft.core.component.DataComponentType<?>> COMPONENTS =
            net.neoforged.neoforge.registries.DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, "kubejs");
    public static final java.util.function.Supplier<net.minecraft.core.component.DataComponentType<Integer>> BURN_TICKS =
            COMPONENTS.register("burn_ticks", () -> net.minecraft.core.component.DataComponentType.<Integer>builder()
                    .persistent(com.mojang.serialization.Codec.INT)
                    .networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.INT).build());
    public static final net.neoforged.neoforge.registries.DeferredRegister<com.mojang.serialization.MapCodec<? extends ContextIntProvider>> PROVIDERS =
            net.neoforged.neoforge.registries.DeferredRegister.create(Registries.CONTEXT_INT_PROVIDER_TYPE, "kubejs");
    public static final java.util.function.Supplier<com.mojang.serialization.MapCodec<StackBurnTicks>> STACK_TICKS =
            PROVIDERS.register("stack_burn_ticks", () -> StackBurnTicks.CODEC);
    public static final ResourceKey<ContextIntProvider> DEFERRED_FUEL = ResourceKey.create(Registries.CONTEXT_INT_PROVIDER,
            Identifier.fromNamespaceAndPath("kubejs", "cooking/stack_burn_ticks"));

    /** Reads the final synchronized component, rather than a previous reload's callback map. */
    public record StackBurnTicks() implements ContextIntProvider, net.minecraft.world.level.storage.loot.LootContextUser {
        public static final com.mojang.serialization.MapCodec<StackBurnTicks> CODEC = com.mojang.serialization.MapCodec.unit(new StackBurnTicks());
        @Override public int getIntUnsafe(net.minecraft.world.level.storage.loot.LootContext context) {
            var stack = context.getOptional(net.neoforged.neoforge.common.loot.NeoForgeLootContextParams.QUERIED_STACK);
            return stack == null ? 0 : stack.getOrDefault(BURN_TICKS.get(), 0);
        }
        @Override public com.mojang.serialization.MapCodec<StackBurnTicks> codec() { return CODEC; }
        @Override public void validate(net.minecraft.world.level.storage.loot.ValidationContext context) { context.validateContextUsage(this); }
        @Override public java.util.Set<net.minecraft.util.context.ContextKey<?>> getReferencedContextParams() {
            return java.util.Set.of(net.neoforged.neoforge.common.loot.NeoForgeLootContextParams.QUERIED_STACK);
        }
    }

    public static void setDeferred(net.minecraft.core.component.DataComponentMap.Builder components, int ticks) {
        components.set(BURN_TICKS.get(), ticks);
        components.set(net.minecraft.core.component.DataComponents.COOKING_FUEL,
                new CookingFuel(DEFERRED_FUEL, ContextFloatProviders.COOKING_DEFAULT_SPEED_MULTIPLIER));
    }

    public static Identifier deferredResource() {
        return DEFERRED_FUEL.identifier().withPath(path -> Registries.CONTEXT_INT_PROVIDER.identifier().getPath() + "/" + path + ".json");
    }

    public static JsonObject deferredProviderJson() {
        var result = providerJson(0);
        var input = new JsonObject();
        input.addProperty("type", BuiltInRegistries.CONTEXT_INT_PROVIDER_TYPE.getKey(StackBurnTicks.CODEC).toString());
        result.add("left", input);
        return result;
    }

    public static ResourceKey<ContextIntProvider> key(Identifier item) {
        return ResourceKey.create(Registries.CONTEXT_INT_PROVIDER,
                Identifier.fromNamespaceAndPath("kubejs", "cooking/" + item.getNamespace() + "/" + item.getPath()));
    }

    public static CookingFuel component(Identifier item) {
        return new CookingFuel(key(item), ContextFloatProviders.COOKING_DEFAULT_SPEED_MULTIPLIER);
    }

    public static Identifier resource(Identifier item) {
        return key(item).identifier().withPath(path -> Registries.CONTEXT_INT_PROVIDER.identifier().getPath() + "/" + path + ".json");
    }

    public static JsonObject providerJson(int ticks) {
        var divisor = new JsonObject();
        divisor.addProperty("type", BuiltInRegistries.CONTEXT_INT_PROVIDER_TYPE.getKey(ConditionalValue.MAP_CODEC).toString());
        divisor.addProperty("condition", LootPredicates.FAST_FURNACE.identifier().toString());
        divisor.addProperty("on_true", ContextIntProviders.COOKING_FAST_BURN_TIME_REDUCTION_FACTOR.identifier().toString());
        divisor.addProperty("on_false", ContextIntProviders.COOKING_NORMAL_BURN_TIME_REDUCTION_FACTOR.identifier().toString());
        var result = new JsonObject();
        result.addProperty("type", BuiltInRegistries.CONTEXT_INT_PROVIDER_TYPE.getKey(Quotient.MAP_CODEC).toString());
        result.addProperty("left", ticks);
        result.add("right", divisor);
        return result;
    }
}
