package dev.emi.emi.loot;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

/** Exact fixed outcomes only. Unsupported conditions/functions are never sampled or ignored. */
public final class NativeToolLoot {
    private NativeToolLoot() {}
    public static Optional<List<ItemStack>> fixedOutputs(JsonElement value) {
        try {
            JsonObject table=value.getAsJsonObject();
            if(!only(table,"type","random_sequence","pools") || table.has("modifier"))return Optional.empty();
            var outputs=new ArrayList<ItemStack>();
            if(!table.has("pools"))return Optional.of(List.of());
            for(var element:table.getAsJsonArray("pools")) {
                var pool=element.getAsJsonObject();
                if(!only(pool,"name","rolls","bonus_rolls","entries") || !pool.has("rolls"))return Optional.empty();
                int rolls=constant(pool.get("rolls"));
                if(rolls<0 || rolls>64 || pool.has("bonus_rolls") && constant(pool.get("bonus_rolls"))!=0)return Optional.empty();
                var entries=pool.getAsJsonArray("entries");
                if(entries==null || entries.size()!=1)return Optional.empty();
                var entry=entries.get(0).getAsJsonObject();
                if(!only(entry,"type","name","weight","quality","modifier")
                    || !entry.get("type").getAsString().equals("minecraft:item")
                    || entry.has("weight") && constant(entry.get("weight"))<=0
                    || entry.has("quality") && constant(entry.get("quality"))!=0)return Optional.empty();
                var id=Identifier.parse(entry.get("name").getAsString());
                if(!BuiltInRegistries.ITEM.containsKey(id))return Optional.empty();
                var item=BuiltInRegistries.ITEM.getValue(id);
                if(item==net.minecraft.world.item.Items.AIR)return Optional.empty();
                int count=1;
                if(entry.has("modifier")) {
                    var modifier=entry.getAsJsonObject("modifier");
                    if(!only(modifier,"type","count","add") || !modifier.get("type").getAsString().equals("minecraft:set_count"))return Optional.empty();
                    int amount=constant(modifier.get("count"));
                    if(amount<0 || amount>64)return Optional.empty();
                    count=amount+(modifier.has("add") && modifier.get("add").getAsBoolean()?1:0);
                }
                if(rolls>0 && count>0)outputs.add(new ItemStack(item,Math.multiplyExact(rolls,count)));
            }
            return Optional.of(List.copyOf(outputs));
        } catch(RuntimeException invalid) { return Optional.empty(); }
    }
    private static boolean only(JsonObject object,String... names) {
        var allowed=java.util.Set.of(names);
        return object.keySet().stream().allMatch(allowed::contains);
    }
    private static int constant(JsonElement value) {
        if(value==null)return -1;
        if(value.isJsonPrimitive() && value.getAsJsonPrimitive().isNumber()) {
            double number=value.getAsDouble();
            return Double.isFinite(number) && number==Math.rint(number) && number>=0 && number<=Integer.MAX_VALUE ? (int)number : -1;
        }
        var object=value.getAsJsonObject();
        return only(object,"type","value") && object.get("type").getAsString().equals("minecraft:constant") ? constant(object.get("value")) : -1;
    }
}
