package dev.emi.emi.recipe;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.ImbueRecipe;
import net.minecraft.world.item.crafting.display.ShapedCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import net.minecraft.world.level.Level;

public final class NativeImbueCrafting {
    private NativeImbueCrafting() {}
    public record Sample(List<ItemStack> inputs,ItemStack output) {}
    public static Sample sample(ImbueRecipe recipe,ItemStack source,ItemStack material,Level level) {
        var input=new ArrayList<ItemStack>();
        for(int slot=0;slot<9;slot++) input.add((slot==4?source:material).copyWithCount(1));
        var crafting=CraftingInput.of(3,3,input);
        if(!recipe.matches(crafting,level)) throw new IllegalArgumentException("Imbue example is not accepted by native recipe");
        return new Sample(List.copyOf(input),recipe.assemble(crafting));
    }
    public static List<Sample> examples(ImbueRecipe recipe,Level level) {
        var result=new ArrayList<Sample>();
        for(var display:recipe.display()) if(display instanceof ShapedCraftingRecipeDisplay shaped) {
            var sources=shaped.ingredients().get(4).resolveForStacks(SlotDisplayContext.fromLevel(level));
            var materials=NativeRecipeData.items(recipe.placementInfo().ingredients().getFirst());
            for(var source:sources) for(var material:materials) result.add(sample(recipe,source,material,level));
        }
        return List.copyOf(result);
    }
    public static String variant(Sample sample,Level level) {
        var ops=net.minecraft.resources.RegistryOps.create(com.mojang.serialization.JsonOps.INSTANCE,level.registryAccess());
        var value=new com.google.gson.JsonArray();
        for(var stack:sample.inputs()) value.add(ItemStack.CODEC.encodeStart(ops,stack).getOrThrow());
        value.add(ItemStack.CODEC.encodeStart(ops,sample.output()).getOrThrow());
        try {
            return java.util.HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256")
                .digest(canonical(value).getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        } catch(java.security.NoSuchAlgorithmException impossible) { throw new IllegalStateException(impossible); }
    }
    private static String canonical(com.google.gson.JsonElement value) {
        if(value.isJsonObject()) {
            var sorted=new java.util.TreeMap<String,com.google.gson.JsonElement>();
            value.getAsJsonObject().entrySet().forEach(entry -> sorted.put(entry.getKey(),entry.getValue()));
            return sorted.entrySet().stream().map(entry -> new com.google.gson.JsonPrimitive(entry.getKey())+":"+canonical(entry.getValue()))
                .collect(java.util.stream.Collectors.joining(",","{","}"));
        }
        if(value.isJsonArray()) {
            var parts=new ArrayList<String>();value.getAsJsonArray().forEach(entry -> parts.add(canonical(entry)));
            return String.join(",",parts).transform(text -> "["+text+"]");
        }
        return value.toString();
    }
}
