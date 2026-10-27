package com.gregtechceu.gtceu.api.recipe;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.*;

/** Exact original grid size and coordinates, including deliberate empty cells. */
public final class StrictSizeShapedRecipe extends ShapedRecipe {
    private final ShapedRecipePattern.Data fullData;
    private final ItemStackTemplate resultTemplate;
    private final boolean allowLargerGrid;
    private static ShapedRecipePattern fullPattern(ShapedRecipePattern.Data data) {
        int width=data.pattern().getFirst().length();var ingredients=new ArrayList<Optional<Ingredient>>();
        for(var row:data.pattern()) {
            if(row.length()!=width)throw new IllegalArgumentException("Unequal strict recipe row widths");
            for(char symbol:row.toCharArray()) {
                if(symbol==' ')ingredients.add(Optional.empty());
                else ingredients.add(Optional.of(Objects.requireNonNull(data.key().get(symbol),"Undefined strict recipe symbol")));
            }
        }
        return new ShapedRecipePattern(width,data.pattern().size(),ingredients,Optional.of(data));
    }
    public StrictSizeShapedRecipe(Recipe.CommonInfo commonInfo,CraftingRecipe.CraftingBookInfo bookInfo,
                                 ShapedRecipePattern.Data data,ItemStackTemplate result) {
        this(commonInfo,bookInfo,data,result,false);
    }
    public StrictSizeShapedRecipe(Recipe.CommonInfo commonInfo,CraftingRecipe.CraftingBookInfo bookInfo,
                                 ShapedRecipePattern.Data data,ItemStackTemplate result,boolean allowLargerGrid) {
        super(commonInfo,bookInfo,fullPattern(data),result);fullData=data;resultTemplate=result;this.allowLargerGrid=allowLargerGrid;
    }
    @Override public boolean matches(CraftingInput input,Level level) {
        if(!(input instanceof FullCraftingGrid grid))return false;
        if(allowLargerGrid){if(grid.gtceu$width()<getWidth()||grid.gtceu$height()<getHeight())return false;}
        else if(grid.gtceu$width()!=getWidth()||grid.gtceu$height()!=getHeight())return false;
        for(int y=0;y<grid.gtceu$height();y++)for(int x=0;x<grid.gtceu$width();x++) {
            int tx=x-grid.gtceu$left(),ty=y-grid.gtceu$top();
            var actual=tx>=0&&ty>=0&&tx<input.width()&&ty<input.height()?input.getItem(tx,ty):net.minecraft.world.item.ItemStack.EMPTY;
            var ingredient=x<getWidth()&&y<getHeight()?pattern.ingredients().get(x+y*getWidth()):Optional.<Ingredient>empty();
            if(!Ingredient.testOptionalIngredient(ingredient,actual))return false;
        }
        return true;
    }
    @Override @SuppressWarnings({"rawtypes","unchecked"}) public RecipeSerializer getSerializer(){return Serializer.INSTANCE;}
    private static ShapedRecipePattern.Data networkData(ShapedRecipePattern pattern) {
        var key=new LinkedHashMap<Character,Ingredient>();var rows=new ArrayList<String>();char symbol='A';
        for(int y=0;y<pattern.height();y++){var row=new StringBuilder();for(int x=0;x<pattern.width();x++){var ingredient=pattern.ingredients().get(x+y*pattern.width());if(ingredient.isEmpty())row.append(' ');else{key.put(symbol,ingredient.get());row.append(symbol++);}}rows.add(row.toString());}
        return new ShapedRecipePattern.Data(key,rows);
    }
    public static final class Serializer {
        public static final MapCodec<StrictSizeShapedRecipe> CODEC=RecordCodecBuilder.mapCodec(instance->instance.group(
            Recipe.CommonInfo.MAP_CODEC.forGetter(recipe->recipe.commonInfo),
            CraftingRecipe.CraftingBookInfo.MAP_CODEC.forGetter(recipe->recipe.bookInfo),
            ShapedRecipePattern.Data.MAP_CODEC.forGetter(recipe->recipe.fullData),
            ItemStackTemplate.CODEC.fieldOf("result").forGetter(recipe->recipe.resultTemplate),
            com.mojang.serialization.Codec.BOOL.optionalFieldOf("allow_larger_grid",false).forGetter(recipe->recipe.allowLargerGrid)
        ).apply(instance,StrictSizeShapedRecipe::new));
        public static final StreamCodec<RegistryFriendlyByteBuf,StrictSizeShapedRecipe> STREAM_CODEC=StreamCodec.composite(
            Recipe.CommonInfo.STREAM_CODEC,recipe->recipe.commonInfo,
            CraftingRecipe.CraftingBookInfo.STREAM_CODEC,recipe->recipe.bookInfo,
            ShapedRecipePattern.STREAM_CODEC,recipe->recipe.pattern,
            ItemStackTemplate.STREAM_CODEC,recipe->recipe.resultTemplate,
            net.minecraft.network.codec.ByteBufCodecs.BOOL,recipe->recipe.allowLargerGrid,
            (common,book,pattern,result,allow)->new StrictSizeShapedRecipe(common,book,networkData(pattern),result,allow));
        public static final RecipeSerializer<StrictSizeShapedRecipe> INSTANCE=new RecipeSerializer<>(CODEC,STREAM_CODEC);
    }
}
