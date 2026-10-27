package dev.latvian.mods.kubejs.util;
import com.google.gson.JsonElement;
import net.minecraft.core.Registry;
import net.minecraft.resources.*;
import net.minecraft.server.packs.resources.*;
import java.util.*;
/** Native loading boundary installed by the actual server script manager bootstrap. */
public final class NativeRecipeReload {
    private NativeRecipeReload() {}
    @FunctionalInterface public interface Mutation {
        void change(ResourceManager manager, RegistryOps<JsonElement> ops, List<Registry.PendingTags<?>> tags,
                    Map<Identifier, JsonElement> recipes);
    }
    private static volatile Mutation mutation;
    @FunctionalInterface public interface Binding extends AutoCloseable { void close(); }
    public static synchronized Binding install(Mutation handler) {
        var previous = mutation; mutation = Objects.requireNonNull(handler);
        return () -> { synchronized (NativeRecipeReload.class) { if (mutation == handler) mutation = previous; } };
    }
    public static Map<Identifier, Resource> process(Map<Identifier, Resource> resources, FileToIdConverter lister,
            ResourceManager manager, RegistryOps<JsonElement> ops, List<Registry.PendingTags<?>> tags) {
        var handler = mutation;
        return handler == null ? resources : NativeRecipeResources.mutate(resources, lister,
                recipes -> handler.change(manager, ops, tags, recipes));
    }
}
