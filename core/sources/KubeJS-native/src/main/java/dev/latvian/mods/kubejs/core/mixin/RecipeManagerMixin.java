package dev.latvian.mods.kubejs.core.mixin;

import com.google.gson.JsonElement;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import dev.latvian.mods.kubejs.CommonProperties;
import dev.latvian.mods.kubejs.core.RecipeManagerKJS;
import dev.latvian.mods.kubejs.core.ReloadableServerResourcesKJS;
import dev.latvian.mods.kubejs.net.KubeServerData;
import dev.latvian.mods.kubejs.net.SyncServerDataPayload;
import dev.latvian.mods.kubejs.plugin.builtin.event.ServerEvents;
import dev.latvian.mods.kubejs.recipe.RecipesKubeEvent;
import dev.latvian.mods.kubejs.recipe.special.SpecialRecipeSerializerManager;
import dev.latvian.mods.kubejs.script.ConsoleJS;
import dev.latvian.mods.kubejs.script.ScriptType;
import dev.latvian.mods.kubejs.server.ServerScriptManager;
import dev.latvian.mods.kubejs.util.Cast;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.MappedRegistry;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeMap;
import net.neoforged.neoforge.resource.ContextAwareReloadListener;
import org.jspecify.annotations.NullUnmarked;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

@Mixin(value = RecipeManager.class, priority = 1100)
public abstract class RecipeManagerMixin implements RecipeManagerKJS {
	@Unique
	private RecipeManager kjs$self() {
		return (RecipeManager) (Object) this;
	}

	@Shadow
	private RecipeMap recipes;



	@Unique
	private @Nullable ReloadableServerResourcesKJS kjs$resources;

    @Inject(method = "finalizeRecipeLoading", at = @At("TAIL"))
    private void kjs$afterRecipeLoading(net.minecraft.world.flag.FeatureFlagSet flags, CallbackInfo ci) {
        var manager = kjs$getServerScriptManager();
        if (manager != null && !CommonProperties.get().serverOnly)
            manager.serverData = new SyncServerDataPayload(KubeServerData.collect());
    }

	@Override
	@NullUnmarked
	public ServerScriptManager kjs$getServerScriptManager() {
		return kjs$resources != null ? kjs$resources.kjs$getServerScriptManager() : null;
	}

	@Override
	public void kjs$setResources(ReloadableServerResourcesKJS resources) {
		kjs$resources = resources;
	}

	@Override
	public void kjs$replaceRecipes(RecipeMap recipeMap) {
		recipes = recipeMap;
		ConsoleJS.SERVER.info("Loaded " + recipeMap.values().size() + " recipes");
	}

	@Override
	public Collection<RecipeHolder<?>> kjs$getRecipes() {
		return recipes.values();
	}
}
