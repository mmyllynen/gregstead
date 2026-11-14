# Private Mushroom component

The Discord bridge is private to the Mushroom server. Do not import its source,
configuration, credentials or artifacts into this repository, and do not include
it in public GitHub or CurseForge releases or distributable modpacks.

# Release versioning

- Normal component versions use x.y.z. GregTech retains its existing independent version line.
- Upstream-based ports use <upstream-version>-gregstead.<revision>. Preserve upstream version components, including four-part versions.
- Keep the Minecraft version separate from the mod version, in filenames and release metadata.
- Public modpack versions use x.y.z; rebuilt suffixes are for local test packages only.
- New CurseForge display names use <project name> <version> (MC 26.3), substituting the actual target Minecraft version.
- projects.json is the component version source of truth. Gradle configuration, JAR metadata and release metadata must agree with it. Do not maintain a duplicate version inventory here.
- Never replace published content with different content under the same version. Do not change this policy without an explicit user decision.

# Artifact filenames

- Every mod JAR ends in -mc<Minecraft-version>.jar.
- GregTech port: gregstead-<version>-mc<Minecraft-version>.jar.
- EMI and Additional Placements: <component>-<upstream-version>-gregstead.<revision>-mc<Minecraft-version>.jar. Do not add a second Gregstead prefix.
- Other components: gregstead-<component>-<version>-mc<Minecraft-version>.jar.
- Modpacks: gregstead-client-<version>-mc<Minecraft-version>.zip and gregstead-server-<version>-mc<Minecraft-version>.zip.
- Repackaging an already published component uses a new patch version (or port revision), with an explicit packaging-only changelog.

# Persistent regression world

- Use only .test-client/saves/Gregstead-Regression for every world-based test. Create it once in creative mode; never create parallel, disposable, copied or replacement worlds.
- Preserve the world, player progress and test fixtures across releases. Never reset or roll it back after a test. If it disappears or fails to open after initial creation, investigate instead of recreating it.
- .test-client is persistent local state, not build output. Build cleanup must never delete it. Do not publish it. Disable automatic world backups and snapshots in this profile.
- Close the game before updating mods. Track the release baseline, exact mod versions/hashes, candidate builds and test results in .test-client/test-state.json. Replace outdated JARs for the same mod; do not load duplicate versions.
- Keep tests within the persistent fixture area; avoid unnecessary exploration/chunk generation. Do not modify Mushroom, production worlds or the user's ordinary client profile.
- Verify rendering fixes with the pack's pinned shader pack enabled as well as disabled before release. Shader-disabled success alone does not complete rendering verification. See BUILDING.md for development-client GPU validation settings.

# GitHub pack distribution

Publish full client import and own-client-mods-only ZIPs together with checksums and matching tagged sources. Mods-only contains ten own client mods, never settings or server-only mods. Document removal of previous versions including legacy names; preserve third-party mods and player configuration. Keep CurseForge approval checks separate and intact.

- Sleepwear is independent: its JAR uses sleepwear-<version>-mc<Minecraft-version>.jar without the Gregstead prefix.
