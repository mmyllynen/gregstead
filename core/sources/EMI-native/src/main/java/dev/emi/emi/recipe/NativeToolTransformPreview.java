package dev.emi.emi.recipe;

import java.lang.reflect.Proxy;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.component.BlockTransformer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

/** Read-only provider preview; retains native transform metadata, including loot and conditions. */
public final class NativeToolTransformPreview {
    public record Result(BlockState output, BlockTransformer.BlockTransformData transform) {}
    private NativeToolTransformPreview() {}
    public static Optional<Result> preview(Holder<BlockTransformer> transformer, LevelAccessor level,
                                           BlockPos pos, BlockState input, BlockState above, Direction face) {
        var view=(LevelAccessor)Proxy.newProxyInstance(LevelAccessor.class.getClassLoader(),new Class<?>[]{LevelAccessor.class},
            (proxy,method,args)-> {
                if(method.getName().equals("getBlockState")) {
                    if(pos.equals(args[0]))return input;
                    if(pos.above().equals(args[0]))return above;
                }
                if(method.getName().equals("getFluidState")) {
                    if(pos.equals(args[0]))return input.getFluidState();
                    if(pos.above().equals(args[0]))return above.getFluidState();
                }
                if(method.getName().equals("getBlockEntity") && (pos.equals(args[0]) || pos.above().equals(args[0])))return null;
                if(method.getName().equals("isEmptyBlock")) {
                    if(pos.equals(args[0]))return input.isAir();
                    if(pos.above().equals(args[0]))return above.isAir();
                }
                if(method.getName().startsWith("set") || method.getName().startsWith("remove") || method.getName().startsWith("destroy"))
                    throw new UnsupportedOperationException("Tool preview cannot mutate world");
                try {return method.invoke(level,args);}
                catch(InvocationTargetException failure) {throw failure.getCause();}
            });
        for(var transform:net.neoforged.neoforge.common.DataMapHooks.appendDatamapTransformers(transformer,transformer.value().transforms())) {
            if(transform.disallowedFaces().contains(face))continue;
            var result=transform.blockStateProvider().value().getOptionalState(view,RandomSource.create(0),pos);
            if(result!=null)return Optional.of(new Result(result,transform));
        }
        return Optional.empty();
    }
}
