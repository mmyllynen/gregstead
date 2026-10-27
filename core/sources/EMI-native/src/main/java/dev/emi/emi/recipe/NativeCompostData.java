package dev.emi.emi.recipe;

import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.*;
import net.minecraft.world.level.storage.loot.predicates.MatchBlock;
import net.minecraft.world.level.storage.loot.providers.number.ints.*;
import net.minecraft.world.phys.Vec3;

/** Native compost layers and exact block-only distributions; other contexts remain explicitly unresolved. */
public final class NativeCompostData {
    private NativeCompostData() {}
    public static LootContext context(ServerLevel level,BlockPos pos,int fill) {
        var state=Blocks.COMPOSTER.defaultBlockState().setValue(ComposterBlock.LEVEL,fill);
        return new LootContext.Builder(new LootParams.Builder(level).withParameter(LootContextParams.BLOCK_STATE,state)
            .withParameter(LootContextParams.ORIGIN,Vec3.atCenterOf(pos)).create(LootContextParamSets.BLOCK_INTERACT)).create(Optional.empty());
    }
    public static int layers(ItemStack stack,LootContext context) {
        var compostable=stack.get(DataComponents.COMPOSTABLE);
        return compostable==null?0:compostable.layers().get(context,0);
    }
    public static int fillAfter(int fill,int layers) {
        return layers>0?net.minecraft.util.Mth.clamp(fill+layers,0,7):fill;
    }
    public static Optional<Map<Integer,Double>> distribution(ResolvableInt layers,HolderGetter.Provider registries,int fill) {
        if(layers instanceof ResolvableInt.Constant fixed) return Optional.of(Map.of(Math.max(0,fixed.value()),1.0));
        var key=((ResolvableInt.Reference)layers).key();
        return registries.lookup(Registries.CONTEXT_INT_PROVIDER).flatMap(registry -> registry.get(key))
            .flatMap(holder -> distribution(holder.value(),fill,0));
    }
    private static Optional<Map<Integer,Double>> distribution(ContextIntProvider provider,int fill,int depth) {
        if(depth>64) return Optional.empty();
        if(provider instanceof ConstantValue fixed) return Optional.of(Map.of(Math.max(0,fixed.value()),1.0));
        if(provider instanceof WeightedListValue weighted) {
            long total=weighted.distribution().unwrap().stream().mapToLong(entry -> entry.weight()).sum();
            if(total==0)return Optional.empty();
            var result=new TreeMap<Integer,Double>();
            for(var entry:weighted.distribution().unwrap()) {
                if(entry.weight()==0)continue;
                var child=distribution(entry.value().value(),fill,depth+1);
                if(child.isEmpty())return Optional.empty();
                child.get().forEach((value,chance)->result.merge(value,chance*entry.weight()/total,Double::sum));
            }
            return Optional.of(Map.copyOf(result));
        }
        if(provider instanceof NumberDispatcher dispatch) {
            for(var branch:dispatch.cases()) {
                var condition=branch.condition().value();
                if(!(condition instanceof MatchBlock match) || match.predicate().willMatchBlockEntity())return Optional.empty();
                if(match.predicate().matchesState(Blocks.COMPOSTER.defaultBlockState().setValue(ComposterBlock.LEVEL,fill)))
                    return distribution(branch.value().value(),fill,depth+1);
            }
            return distribution(dispatch.defaultValue().value(),fill,depth+1);
        }
        return Optional.empty();
    }
}
