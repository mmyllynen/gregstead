package dev.emi.emi.screen.widget;

import dev.emi.emi.api.widget.Bounds;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** One layout for effect drawing and EMI exclusion geometry. */
public final class NativeEffectLayout {
    private NativeEffectLayout() {}
    public record Layout(List<Bounds> boxes,Bounds bounds,boolean wide) {}
    public static Optional<Layout> layout(String location,int screenWidth,int left,int top,int imageWidth,
            int count,boolean creative,boolean inventoryTabs,boolean forge) {
        int right=left+imageWidth+2;
        if(count==0 || location.equals("HIDDEN") || screenWidth-right<32)return Optional.empty();
        var boxes=new ArrayList<Bounds>();
        if(location.equals("TOP")) {
            boolean wide=count==1;int step=wide?122:count>5?(imageWidth-32)/(count-1):34;
            int width=(count-1)*step+(wide?120:32);
            int x=left+(imageWidth-width)/2,y=top-34;
            if(creative || inventoryTabs)y-=28;
            if(creative && forge)y-=22;
            for(int i=0;i<count;i++)boxes.add(new Bounds(x+i*step,y,wide?120:32,32));
            return Optional.of(new Layout(List.copyOf(boxes),new Bounds(x,y,width,32),wide));
        }
        boolean wide=!location.endsWith("COMPRESSED");int width=wide?120:32;
        int x=location.startsWith("LEFT")?left-2-width:right,step=count>5?132/(count-1):33;
        for(int i=0;i<count;i++)boxes.add(new Bounds(x,top+i*step,width,32));
        return Optional.of(new Layout(List.copyOf(boxes),new Bounds(x,top,width,(count-1)*step+32),wide));
    }
}
