"""Derive isolated Overworld registries from the pinned, unmodified Mojang client."""
import gzip
import hashlib
import json
import math
from pathlib import Path
import re
import struct
import sys
import zipfile
import zlib

NAMESPACE = "gregstead_mushroom"
VANILLA = "minecraft:"
ROOT = "data/minecraft/"
DYNAMIC = "worldgen/"
DENSITY_FIELDS = {
    "coordinate", "input", "left", "right", "shift_x", "shift_z", "when_in_range", "when_out_of_range",
    "chunk_surface_level", "continents", "depth", "erosion", "final_density", "function", "ridges",
    "second", "surface_level", "temperature", "vegetation", "density", "richness", "filler_gap",
}


class OverworldData:
    def __init__(self, archive, output):
        self.archive = archive
        self.output = output / "data" / NAMESPACE
        self.files = set(archive.namelist())
        self.pending = []
        self.used = set()

    def seed(self, folder, path, destination=None):
        key = (folder, path, destination or path)
        if key not in self.used:
            self.used.add(key)
            self.pending.append(key)

    def ref(self, folder, value):
        if value.startswith("#" + VANILLA):
            folder = "tags/" + folder
            prefix = "#"
            path = value[len(VANILLA) + 1:]
        else:
            if not value.startswith(VANILLA):
                return value
            prefix = ""
            path = value[len(VANILLA):]
        if folder == "worldgen/template_pool" and path == "empty":
            return value  # JigsawPlacement uses the vanilla empty pool as a sentinel.
        suffix = "nbt" if folder == "structure" else "json"
        if f"{ROOT}{folder}/{path}.{suffix}" not in self.files:
            if folder == "structure" and path == "ancient_city/walls/intact_horizontal_wall_stairs_5":
                return value  # Vanilla 26.3 has a pool entry but no template for this piece.
            raise ValueError(f"Missing vanilla generation reference {folder}: {value}")
        self.seed(folder, path)
        return f"{prefix}{NAMESPACE}:{path}"

    def dynamic(self, registry, value):
        return self.ref(DYNAMIC + registry, value)

    def transform(self, owner, node, field="", parent=None):
        if isinstance(node, dict):
            return {key: self.transform(owner, value, key, node) for key, value in node.items()}
        if isinstance(node, list):
            return [self.transform(owner, value, field, parent) for value in node]
        if not isinstance(node, str) or not node.startswith((VANILLA, "#" + VANILLA)):
            return node

        if owner.startswith("tags/worldgen/") and field in ("values", "id"):
            return self.ref(owner.removeprefix("tags/"), node)
        if (owner, field) in {("biome", "carvers"), ("biome", "features")}:
            return self.dynamic({"carvers": "carver", "features": "placed_feature"}[field], node)
        if field == "noise" and owner in {"noise_settings", "material_rule", "material_condition", "density_function"}:
            return self.dynamic("noise", node)
        if owner in {"noise_settings", "material_rule", "material_condition", "density_function"} and field in DENSITY_FIELDS:
            return self.dynamic("density_function", node)
        if owner in {"noise_settings", "material_rule"} and field in {"material_rule", "sequence", "then_run"}:
            return self.dynamic("material_rule", node)
        if (owner, field) == ("material_rule", "if_true"):
            return self.dynamic("material_condition", node)
        if (owner, field) in {("material_rule", "biome_is"), ("material_condition", "biome_is"),
                              ("structure", "biomes"), ("structure_set", "preferred_biomes")}:
            return self.dynamic("biome", node)
        if (owner, field) == ("structure_set", "structure"):
            return self.dynamic("structure", node)
        if (owner, field) == ("structure_set", "other_set"):
            return self.dynamic("structure_set", node)
        if owner == "structure" and field in {"start_pool", "target"}:
            return self.dynamic("template_pool", node)
        if owner == "structure" and field == "data" and node.startswith(VANILLA + "trial_chambers/"):
            return self.dynamic("template_pool", node)
        if (owner, field) == ("structure", "alias"):
            return node.replace(VANILLA, NAMESPACE + ":", 1)
        if owner == "template_pool":
            if field == "fallback":
                return self.dynamic("template_pool", node)
            if field == "location":
                return self.ref("structure", node)
            if field == "processors":
                return self.dynamic("processor_list", node)
            if field == "feature":
                return self.dynamic("placed_feature", node)
        if (owner, field) == ("placed_feature", "feature"):
            return self.dynamic("feature", node)
        if owner == "feature":
            if field == "feature":
                registry = "feature" if "placement" in parent else "placed_feature"
                return self.dynamic(registry, node)
            if field == "default":
                return self.dynamic("placed_feature", node)
            if field in {"fossil_processors", "overlay_processors"}:
                return self.dynamic("processor_list", node)
            if field in {"fossil_structures", "overlay_structures"}:
                return self.ref("structure", node)
        if owner == "feature" and (field.endswith("provider") or field == "to_place"):
            path = node.removeprefix(VANILLA)
            if f"{ROOT}worldgen/block_state_provider/{path}.json" in self.files:
                return self.dynamic("block_state_provider", node)
        return node

    def transform_template(self, content):
        def rewrite_pool(match):
            start = match.end()
            size = int.from_bytes(content[start - 2:start], "big")
            pool = content[start:start + size].decode("ascii")
            path = pool.removeprefix(VANILLA)
            if f"{ROOT}worldgen/template_pool/{path}.json" in self.files:
                name = self.dynamic("template_pool", pool)
            elif path.startswith("trial_chambers/spawner/contents/"):
                name = f"{NAMESPACE}:{path}"
            else:
                raise ValueError(f"Unknown vanilla jigsaw pool {pool}")
            encoded = name.encode("ascii")
            return match.group()[:-2] + struct.pack(">H", len(encoded)) + encoded

        # Use the NBT's length, not a delimiter, when matching each pool string.
        pieces = []
        previous = 0
        for match in re.finditer(rb"\x08\x00\x04pool..(?=minecraft:)", content, re.DOTALL):
            pieces.extend((content[previous:match.start()], rewrite_pool(match)))
            previous = match.end() + int.from_bytes(content[match.end() - 2:match.end()], "big")
        pieces.append(content[previous:])
        return b"".join(pieces)

    def write_json(self, path, payload):
        path = self.output / path
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(json.dumps(payload, separators=(",", ":")) + "\n")

    def generate(self):
        self.seed("worldgen/noise_settings", "overworld", "vanilla")
        self.seed("dimension_type", "overworld", "vanilla")
        self.seed("tags/worldgen/biome", "is_overworld")
        for name in sorted(self.files):
            prefix = ROOT + "worldgen/structure_set/"
            if not name.startswith(prefix) or not name.endswith(".json"):
                continue
            path = name.removeprefix(prefix).removesuffix(".json")
            if path not in {"nether_complexes", "nether_fossils", "ruined_portals_nether", "end_cities"}:
                self.seed("worldgen/structure_set", path)

        self.write_json("dimension/vanilla.json", {
            "type": NAMESPACE + ":vanilla",
            "generator": {
                "type": VANILLA + "noise",
                "biome_source": {"type": NAMESPACE + ":vanilla_overworld"},
                "settings": NAMESPACE + ":vanilla",
            },
        })
        while self.pending:
            folder, path, destination = self.pending.pop()
            if folder == "structure":
                template = gzip.decompress(self.archive.read(f"{ROOT}{folder}/{path}.nbt"))
                dest = self.output / folder / (destination + ".nbt")
                dest.parent.mkdir(parents=True, exist_ok=True)
                dest.write_bytes(gzip.compress(self.transform_template(template), mtime=0))
            else:
                payload = json.loads(self.archive.read(f"{ROOT}{folder}/{path}.json"))
                self.write_json(f"{folder}/{destination}.json", self.transform(folder.removeprefix(DYNAMIC), payload))


def write_portal_texture(output):
    # Tile in both axes and over time, and generate the alpha channel independently of color.
    width, frames = 16, 32
    rows = bytearray()
    for frame in range(frames):
        t = math.tau * frame / frames
        for y in range(width):
            rows.append(0)  # PNG filter type: none
            for x in range(width):
                u, v = math.tau * x / width, math.tau * y / width
                ribbon = abs(math.sin(2 * u - 2 * v + t + 1.3 * math.sin(u + v - t)))
                detail = 0.5 + 0.5 * math.sin(3 * (u + v) + t + math.sin(2 * u - v))
                w = 0.75 * ribbon ** 2 + 0.25 * detail
                rows.extend((int(10 + 40 * w), int(86 + 155 * w), int(77 + 139 * w), int(87 + 125 * w)))

    def chunk(kind, data):
        return struct.pack(">I", len(data)) + kind + data + struct.pack(">I", zlib.crc32(kind + data))

    path = output / "assets" / NAMESPACE / "textures/block/portal.png"
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_bytes(b"\x89PNG\r\n\x1a\n"
                     + chunk(b"IHDR", struct.pack(">IIBBBBB", width, width * frames, 8, 6, 0, 0, 0))
                     + chunk(b"IDAT", zlib.compress(rows))
                     + chunk(b"IEND", b""))
    path.with_suffix(".png.mcmeta").write_text('{"animation":{"frametime":2,"interpolate":true}}\n')


def main():
    client, metadata, output = map(Path, sys.argv[1:])
    expected = json.loads(metadata.read_text())["downloads"]["client"]["sha1"]
    with client.open("rb") as file:
        if hashlib.file_digest(file, "sha1").hexdigest() != expected:
            raise ValueError("Mojang client JAR differs from pinned version")
    with zipfile.ZipFile(client) as archive:
        OverworldData(archive, output).generate()
    write_portal_texture(output)


if __name__ == "__main__":
    main()
