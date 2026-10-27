package dev.emi.emi.compost;

import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import net.minecraft.network.chat.Component;

public final class CompostText {
    private CompostText() {}
    public static Component describe(Optional<Map<Integer,Double>> distribution,int fill) {
        if(distribution.isEmpty())return Component.translatableWithFallback("emi.compost.context","Layer amount unavailable");
        var outcomes=new TreeMap<Integer,Double>();
        distribution.get().forEach((layers,chance) -> outcomes.merge(dev.emi.emi.recipe.NativeCompostData.fillAfter(fill,layers)-fill,chance,Double::sum));
        var parts=new java.util.ArrayList<String>();
        outcomes.forEach((layers,chance) -> {
            if(layers==0 && outcomes.size()>1)return;
            var format=new java.text.DecimalFormat("0.##",java.text.DecimalFormatSymbols.getInstance(java.util.Locale.ROOT));
            String prefix=chance==1?"":(chance>0 && chance<0.0001?"<0.01":format.format(chance*100))+"% ";
            parts.add(prefix+(layers>0?"+":"")+layers);
        });
        return Component.translatableWithFallback("emi.compost.layers","%s layers",String.join(", ",parts));
    }
}
