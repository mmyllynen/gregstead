package com.gregtechceu.gtceu.client.model.pipe;

import com.google.gson.*;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.model.StandardModelParameters;
import net.neoforged.neoforge.client.model.UnbakedModelLoader;
import java.util.Set;

public class PipeModelLoader implements UnbakedModelLoader<UnbakedPipeModel> {
    public static final PipeModelLoader INSTANCE=new PipeModelLoader();
    public static final Identifier ID=Identifier.parse("gtceu:pipe");
    public static final String PRIMARY_CENTER_KEY="center";
    public static final Set<String> CENTER_KEYS=Set.of(PRIMARY_CENTER_KEY,"core","null","none");
    @Override public UnbakedPipeModel read(JsonObject json,JsonDeserializationContext context) throws JsonParseException {
        return new UnbakedPipeModel(StandardModelParameters.parse(json,context),PipeModelDefinitions.read(json));
    }
}
