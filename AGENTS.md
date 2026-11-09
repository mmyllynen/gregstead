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
