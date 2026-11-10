# OPAC BlueMap integration (server only)

NeoForge 26.3 port of https://github.com/alexiokay/opac-bluemap-1.21.1-neoforge
at commit 91e5727ff2d4d15da2fd364fe687cde6d9039858. MIT; upstream copyright retained in LICENSE.

Requires OPAC 0.33.0 and BlueMap 5.29. Not included in public modpacks.
Updates are coalesced onto the server thread once per second; full resync every
five minutes also captures claim metadata changes. No chunk ownership cache.
The layer is toggleable and uses upstream polygon/hole geometry and styling.

Build with JDK 25, GRADLE_USER_HOME=$PWD/.build/gradle-home and the wrapper:
`bash mods/opac-bluemap/gradlew -p mods/opac-bluemap build --offline --no-daemon`.
Compile dependencies are the pinned OPAC in .build/pack-inputs and BlueMap 5.29
plus its embedded flow-math-1.0.3.jar in .build/opac-bluemap-inputs.

Input hashes are checked against `compile-inputs.json` before compilation.
Obtain the exact public author JARs; extract flow-math from BlueMap's
`META-INF/jarjar/flow-math-1.0.3.jar`. No Mushroom configuration is a build input.
The `pack-addon` entry in projects.json keeps this opt-in integration out of
normal client/server modpack builds and the nine-client-mod distribution.

Regression testing uses the existing persistent world only. With the candidate
and BlueMap installed in .test-client, add
`-I "$PWD/tools/opac-bluemap-regression.init.gradle"` after the normal regression
init script when launching the client. Write `opac` to probe-command.txt and read
opac-bluemap-test.txt. The local probe creates persistent metadata fixtures for
two synthetic party members and one offline solo owner around the fixture area;
it does not load terrain for those claims. It checks the budget boundary, party
budget, same-coordinate cross-dimension claims, unclaim and initial resync.
The probe is never packaged. Use `stop` for graceful save and shutdown.
