# Mushroom Dimension

Build a Nether-sized rectangular frame in the Overworld. Replace one obsidian
block along a side, top or bottom with Crying Obsidian. Corners are optional
and do not count. Light the frame as usual. The turquoise portal links the
Overworld and a separate Overworld-style dimension at 1:1 scale.

The destination uses vanilla 26.3 terrain, structures, biomes and ores with
GregTech stone and deepslate ore veins. Minecraft generates chunks on demand
when finding an exit and as players explore. Keep the addon on both sides
while players use the dimension.

## Mushroom update

Mushroom multiplayer clients use `gregstead-mushroom-dimension-client-0.1.1-mc26.3.jar`
and `gregstead-8.0.16-mc26.3.jar`. Close the game, remove the older GregTech
JAR and any previous Mushroom Dimension JAR, then add both files alongside
the other installed mods. Check both SHA-256 digests against the Mushroom
update. Existing settings and third-party mods stay with the instance.

Dedicated servers and singleplayer worlds use
`gregstead-mushroom-dimension-0.1.0-mc26.3.jar` and
`gregstead-8.0.16-mc26.3.jar`. The 0.1.1 client works with Mushroom’s 0.1.0 server addon. The client includes
the registry definitions it can be asked to load during the multiplayer handshake.

The addon is a `pack-addon` excluded from the public pack builder. Build it
with `python3 release.py build mushroom-dimension`. For a clean checkout, run
`python3 release.py bootstrap mushroom-dimension --download-dependencies`
first. The builder verifies the Mojang and OPAC inputs. Gradle generates
the addon worldgen resources from the pinned Minecraft client. Use the
`clientJar` Gradle task with the same pinned inputs to build the multiplayer
client download.

For deployment, take a development backup with Mushroom's shared controller.
Install both JARs together after a graceful stop. Confirm that the server
reports both versions and that a matching client joins before use. Treat any
generated dimension chunks and player locations as persistent server data.
