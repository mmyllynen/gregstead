package io.github.gaming32.opacbluemapintegration;

import com.mojang.logging.LogUtils;
import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.BlueMapWorld;
import de.bluecolored.bluemap.api.markers.ExtrudeMarker;
import de.bluecolored.bluemap.api.markers.Marker;
import de.bluecolored.bluemap.api.markers.MarkerSet;
import de.bluecolored.bluemap.api.markers.ShapeMarker;
import de.bluecolored.bluemap.api.math.Color;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.fml.loading.FMLPaths;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import org.slf4j.Logger;
import xaero.pac.common.claims.player.api.IPlayerClaimPosListAPI;
import xaero.pac.common.server.api.OpenPACServerAPI;
import xaero.pac.common.server.claims.api.IServerClaimsManagerAPI;
import xaero.pac.common.claims.tracker.api.IClaimsManagerTrackerAPI;
import xaero.pac.common.claims.tracker.api.IClaimsManagerListenerAPI;
import xaero.pac.common.claims.player.api.IPlayerChunkClaimAPI;

import java.nio.file.Path;
import java.util.*;


@Mod("opac_bluemap_integration")
public class OpacBluemapIntegration implements IClaimsManagerListenerAPI {
    public static final Logger LOGGER = LogUtils.getLogger();

    private static final String MARKER_SET_KEY = "opac-bluemap-integration";
    private static final Path CONFIG_FILE = FMLPaths.CONFIGDIR.get().resolve("opac-bluemap.json");

    public static final OpacBluemapConfig CONFIG = new OpacBluemapConfig();

    private static MinecraftServer minecraftServer;
    private static IClaimsManagerTrackerAPI claimsTracker;

    private volatile boolean dirty = true;
    private int ticks;

    public OpacBluemapIntegration() {
        loadConfig();
        BlueMapAPI.onEnable(api -> dirty = true);
        NeoForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        minecraftServer = event.getServer();
        claimsTracker = OpenPACServerAPI.get(minecraftServer).getServerClaimsManager().getTracker();
        claimsTracker.register(this);
        dirty = true;
    }

    @SubscribeEvent
    public void onServerStopped(ServerStoppedEvent event) {
        minecraftServer = null;
        claimsTracker = null;
        ticks = 0;
        dirty = true;
    }

    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post event) {
        if (minecraftServer == null || ++ticks % 20 != 0) return;
        if (ticks >= Math.max(20, CONFIG.getUpdateInterval())) { dirty = true; ticks = 0; }
        if (!dirty) return;
        BlueMapAPI.getInstance().ifPresent(api -> {
            dirty = false;
            try { updateClaims(api); }
            catch (RuntimeException e) { dirty = true; LOGGER.error("Claim marker refresh failed", e); }
        });
    }

    public static void loadConfig() {
        try {
            CONFIG.loadFromFile(CONFIG_FILE);
        } catch (Exception e) {
            LOGGER.warn("Failed to read {}.", CONFIG_FILE, e);
        }
        saveConfig();
    }

    public static void saveConfig() {
        try {
            CONFIG.saveToFile(CONFIG_FILE);
            LOGGER.info("Saved OpenPaC BlueMap config");
        } catch (Exception e) {
            LOGGER.error("Failed to write {}.", CONFIG_FILE, e);
        }
    }

    public static void updateClaims(BlueMapAPI blueMap) {
        if (minecraftServer == null) return;
        var claims = OpenPACServerAPI.get(minecraftServer).getServerClaimsManager();
        // Build a complete snapshot before replacing any live marker sets.
        Map<de.bluecolored.bluemap.api.BlueMapMap, MarkerSet> snapshot = new HashMap<>();
        for (var map : blueMap.getMaps()) snapshot.put(map, MarkerSet.builder()
            .toggleable(true).label("Open Parties and Claims").build());
        claims.getPlayerInfoStream().forEach(player -> player.getStream().forEach(entry -> {
            var dimension = entry.getKey();
            var world = blueMap.getWorld(ResourceKey.create(Registries.DIMENSION, dimension)).orElse(null);
            if (world == null) return;
            Map<Integer, Set<ChunkPos>> groups = new HashMap<>();
            entry.getValue().getStream().flatMap(IPlayerClaimPosListAPI::getStream).forEach(pos -> {
                var claim = claims.get(dimension, pos);
                if (claim != null) groups.computeIfAbsent(claim.getSubConfigIndex(), k -> new HashSet<>()).add(pos);
            });
            groups.forEach((sub, chunks) -> {
                var sample = claims.get(dimension, chunks.iterator().next());
                String name = claims.getFullName(sample, dimension, true).getString();
                int color = claims.getColor(sample, dimension);
                var shapes = createShapes(chunks);
                for (var map : world.getMaps()) {
                    var set = snapshot.get(map);
                    if (set == null) continue;
                    for (int i = 0; i < shapes.size(); i++) {
                        var shape = shapes.get(i);
                        float minY = CONFIG.getMarkerMinY(), maxY = CONFIG.getMarkerMaxY();
                        Marker marker = Mth.equal(minY, maxY)
                            ? ShapeMarker.builder().label(name)
                                .fillColor(new Color(color, 102)).lineColor(new Color(color, 255))
                                .shape(shape.baseShape(), minY).holes(shape.holes())
                                .depthTestEnabled(CONFIG.isDepthTest()).build()
                            : ExtrudeMarker.builder().label(name)
                                .fillColor(new Color(color, 102)).lineColor(new Color(color, 255))
                                .shape(shape.baseShape(), minY, maxY).holes(shape.holes())
                                .depthTestEnabled(CONFIG.isDepthTest()).build();
                        set.getMarkers().put(player.getPlayerId() + "---" + sub + "---" + i, marker);
                    }
                }
            });
        }));
        snapshot.forEach((map, set) -> map.getMarkerSets().put(MARKER_SET_KEY, set));
        LOGGER.info("Refreshed OPAC claim markers on {} BlueMap maps", snapshot.size());
    }

    public static List<ShapeHolder> createShapes(Set<ChunkPos> chunks) {
        return createChunkGroups(chunks)
            .stream()
            .map(ShapeHolder::create)
            .toList();
    }

    public static List<Set<ChunkPos>> createChunkGroups(Set<ChunkPos> chunks) {
        final List<Set<ChunkPos>> result = new ArrayList<>();
        final Set<ChunkPos> visited = new HashSet<>();
        for (final ChunkPos chunk : chunks) {
            if (visited.contains(chunk)) continue;
            final Set<ChunkPos> neighbors = findNeighbors(chunk, chunks);
            result.add(neighbors);
            visited.addAll(neighbors);
        }
        return result;
    }

    public static Set<ChunkPos> findNeighbors(ChunkPos chunk, Set<ChunkPos> chunks) {
        if (!chunks.contains(chunk)) {
            throw new IllegalArgumentException("chunks must contain chunk to find neighbors!");
        }
        final Set<ChunkPos> visited = new HashSet<>();
        final Queue<ChunkPos> toVisit = new ArrayDeque<>();
        visited.add(chunk);
        toVisit.add(chunk);
        while (!toVisit.isEmpty()) {
            final ChunkPos visiting = toVisit.remove();
            for (final ChunkPosDirection dir : ChunkPosDirection.values()) {
                final ChunkPos offsetPos = dir.add(visiting);
                if (!chunks.contains(offsetPos) || !visited.add(offsetPos)) continue;
                toVisit.add(offsetPos);
            }
        }
        return visited;
    }

    @Override
    public void onWholeRegionChange(net.minecraft.resources.Identifier dimension, int x, int z) { dirty = true; }
    @Override
    public void onChunkChange(net.minecraft.resources.Identifier dimension, int x, int z, IPlayerChunkClaimAPI claim) { dirty = true; }
    @Override
    public void onDimensionChange(net.minecraft.resources.Identifier dimension) { dirty = true; }
}
