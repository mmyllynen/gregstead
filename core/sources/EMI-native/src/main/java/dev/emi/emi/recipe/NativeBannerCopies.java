package dev.emi.emi.recipe;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.crafting.BannerDuplicateRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.ItemStack;
import com.mojang.serialization.JsonOps;

/** Use the native banner ingredient instead of multiplying every color recipe by all colors. */
public final class NativeBannerCopies {
    private NativeBannerCopies() {}
    public static ItemStack[] inputs(BannerDuplicateRecipe recipe, RegistryAccess registries) {
        var ops=RegistryOps.create(JsonOps.INSTANCE,registries);
        var json=BannerDuplicateRecipe.MAP_CODEC.codec().encodeStart(ops,recipe).getOrThrow().getAsJsonObject();
        return NativeRecipeData.items(Ingredient.CODEC.parse(ops,json.get("banner")).getOrThrow());
    }
    public static ItemStack output(BannerDuplicateRecipe recipe, ItemStack source, RegistryAccess registries) {
        var ops=RegistryOps.create(JsonOps.INSTANCE,registries);
        var json=BannerDuplicateRecipe.MAP_CODEC.codec().encodeStart(ops,recipe).getOrThrow().getAsJsonObject();
        var template=net.minecraft.world.item.ItemStackTemplate.CODEC.parse(ops,json.get("result")).getOrThrow();
        return net.minecraft.world.item.crafting.TransmuteRecipe.createWithOriginalComponents(template,source);
    }
    public record Example(ItemStack target, ItemStack source, ItemStack output, java.util.List<ItemStack> remainders) {}
    public static Example craft(BannerDuplicateRecipe recipe, ItemStack target, ItemStack source, net.minecraft.world.level.Level level) {
        var input=net.minecraft.world.item.crafting.CraftingInput.of(2,1,java.util.List.of(target.copyWithCount(1),source.copyWithCount(1)));
        if(!recipe.matches(input,level))throw new IllegalArgumentException("Banner preview does not match native recipe");
        return new Example(input.getItem(0).copy(),input.getItem(1).copy(),recipe.assemble(input).copy(),
            recipe.getRemainingItems(input).stream().map(ItemStack::copy).toList());
    }

}
