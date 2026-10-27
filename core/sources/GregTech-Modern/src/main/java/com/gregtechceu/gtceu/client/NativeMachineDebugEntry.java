package com.gregtechceu.gtceu.client;
import net.minecraft.client.gui.components.debug.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import java.util.*;
import java.util.function.Supplier;
/** Native debug entry adds machine information to the vanilla targeted-block group. */
public final class NativeMachineDebugEntry implements DebugScreenEntry {
    private final Supplier<List<String>> lines;
    public NativeMachineDebugEntry(Supplier<List<String>> lines){this.lines=Objects.requireNonNull(lines);}
    @Override public void display(DebugScreenDisplayer output,Level level,LevelChunk clientChunk,LevelChunk serverChunk){
        var current=List.copyOf(lines.get());
        if(current.isEmpty())return;
        var group=new ArrayList<String>();group.add("");group.addAll(current);
        output.addToGroup(DebugEntryLookingAt.BLOCK_GROUP,group);
    }
}
