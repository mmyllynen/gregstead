package fi.gregstead.mushroom;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class PortalLinks extends SavedData {
    private record Link(BlockPos overworld, BlockPos other) {
        static final Codec<Link> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BlockPos.CODEC.fieldOf("overworld").forGetter(Link::overworld),
                BlockPos.CODEC.fieldOf("other").forGetter(Link::other)
        ).apply(instance, Link::new));
    }

    private static final Codec<PortalLinks> CODEC = Link.CODEC.listOf().xmap(PortalLinks::new, PortalLinks::links);
    private static final SavedDataType<PortalLinks> TYPE = new SavedDataType<>(
            MushroomDimension.id("portal_links"), () -> new PortalLinks(List.of()), CODEC);

    private final Map<BlockPos, BlockPos> outward = new HashMap<>();
    private final Map<BlockPos, BlockPos> inward = new HashMap<>();

    private PortalLinks(List<Link> links) {
        for (Link link : links) {
            if (!outward.containsKey(link.overworld) && !inward.containsKey(link.other)) {
                outward.put(link.overworld, link.other);
                inward.put(link.other, link.overworld);
            }
        }
    }

    private List<Link> links() {
        var links = new ArrayList<Link>(outward.size());
        outward.forEach((from, to) -> links.add(new Link(from, to)));
        return links;
    }

    static PortalLinks get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    @Nullable BlockPos target(ResourceKey<Level> from, BlockPos bottom) {
        return (from == Level.OVERWORLD ? outward : inward).get(bottom);
    }

    void unlink(ResourceKey<Level> from, BlockPos bottom) {
        var source = from == Level.OVERWORLD ? outward : inward;
        var destination = from == Level.OVERWORLD ? inward : outward;
        BlockPos previous = source.remove(bottom);
        if (previous != null) {
            destination.remove(previous);
            setDirty();
        }
    }

    void link(ResourceKey<Level> from, BlockPos bottom, BlockPos destination) {
        var source = from == Level.OVERWORLD ? outward : inward;
        var target = from == Level.OVERWORLD ? inward : outward;
        if (source.containsKey(bottom) || target.containsKey(destination)) return;
        source.put(bottom, destination);
        target.put(destination, bottom);
        setDirty();
    }
}
