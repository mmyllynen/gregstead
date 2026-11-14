# Sleepwear

Sleepwear 0.1.0 adds **Pajama Shirt** and **Pajama Pants** for Minecraft 26.3 / NeoForge 26.3.0.77. Install on client and server. GregTech and Curios are not required.

Wear both pieces in the chest and legs slots to sleep automatically in a nearby reachable bed when normal sleeping is allowed. No armour protection or durability. The pieces are also available in the Tools & Utilities creative tab.

## Crafting and colour

Craft the shirt like a chestplate (8 wool) and the pants like leggings (7 wool). All wool must have the same colour; the garment inherits that colour. Mixed wool colours do not craft.

- Garment + one dye, anywhere: solid colour.
- Dye directly above + garment + dye directly below: upper and lower halves use those colours.
- Dye directly left + garment + dye directly right: alternating horizontal stripes, starting at the top with the left dye.

Two-dye patterns require three consecutive cells of a crafting table. Extra ingredients and diagonal arrangements are rejected. Two identical dyes are allowed. Re-dyeing replaces the old design, retains other item components, and never mixes old colour into the new colours.

## Automation

Equipment-change events maintain the set of dressed players; login/respawn initialize it. There is no periodic equipment scan. A tiny hook observes Minecraft's already-computed daylight transition; explicit clock edits also invalidate sleeping-window pauses. Daytime does not perform player/bed searches. Only due nighttime attempts are processed, with a five-second retry after blocked or unavailable beds. Removing either piece disables automation.

Normal reach, visibility, bed obstruction, monsters and multiplayer sleep requirements still apply. Occupied beds and beds that would be destroyed on use or wake-up are ignored. The nearest valid bed is tried first. Automatic sleeping sets the respawn point just like ordinary sleeping. Failed attempts produce no chat spam.

Leaving bed during the night pauses automation for that sleeping window. Remove and re-equip a piece to resume immediately. The pause survives reconnect; death clears it. No AFK-kick bypass, chunk loading, or custom time-skipping is provided.

## Build

From repository root: `python3 release.py build sleepwear` with Java 25 and the repository's verified build cache. Original pixel-art assets and recipes are generated with `python3 mods/sleepwear/tools/generate_assets.py` (Pillow); normal builds use the checked-in resources.

Local-only integration probes are under `tools/sleepwear_regression` and excluded from release JARs. All world tests use the single persistent Gregstead-Regression save.
