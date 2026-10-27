package com.gregtechceu.gtceu.client.model.pipe;

import net.minecraft.core.Direction;
import java.util.*;

/** Original connection-mask rendering rules shared by legacy and native model backends. */
public final class PipePartSelection {
    public static final int ITEM_CONNECTIONS=0b001100;
    private PipePartSelection() {}
    public record Part(Direction direction,boolean restrictor) {}
    public static List<Part> select(Integer connections,Integer blocked,boolean item) {
        if(item){connections=ITEM_CONNECTIONS;blocked=0;}
        var parts=new ArrayList<Part>();
        if(connections==null || connections!=0b111111)parts.add(new Part(null,false));
        if(connections!=null)for(var direction:Direction.values()) {
            int bit=1<<direction.ordinal();
            if((connections & bit)==0)continue;
            parts.add(new Part(direction,false));
            if(blocked!=null && (blocked & bit)!=0)parts.add(new Part(direction,true));
        }
        return List.copyOf(parts);
    }
}
