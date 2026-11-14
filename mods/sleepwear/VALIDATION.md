# Sleepwear 0.1.0 validation

Validated 2026-11-14 on Minecraft 26.3 / NeoForge 26.3.0.77 / Java 25.

Final JAR SHA-256: `79885db0330dbd0c78ec7c4f82e04ce55217c8fa691a854112a8da647ef58163`.

- Clean build and JAR metadata/integrity checks passed; local probes excluded.
- All 32 wool/garment recipes and mixed-wool rejection passed.
- All 512 garment/two-colour pairs, single-dye replacement, invalid layouts and preservation of custom item components passed.
- Integrated and dedicated-server player sleeping passed in the sole persistent Gregstead-Regression save.
- Idle counters confirmed no periodic equipment polling and no daytime bed searches.
- Incomplete outfits, manual wake, re-equip, next night, distance, line of sight, obstruction, monsters, occupied beds and nearest valid bed selection passed.
- Paused-clock/time-command and daytime thunderstorm activation passed.
- Final packaged JAR tested on both dedicated server and client. A full client-process exit and reconnect retained the wake pause and both garment designs.
- Equipment textures inspected with Complementary Reimagined enabled and disabled.
- No changes to multiplayer sleep-percentage rules: sleeping uses the normal server API.

Full local reports, screenshots and exact installed dependency hashes remain in `.test-client`; no world or player state is distributed.
