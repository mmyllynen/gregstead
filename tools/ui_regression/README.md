# Machine UI network regression

Local-only probes; these sources are excluded from the release JAR. Always use
`.test-client/saves/Gregstead-Regression`. The dedicated server and integrated
server must never open this world simultaneously. Do not copy or reset it.

## Confirmed failure and fix

Baseline 8.0.10 reproduced the user's disconnect three times over an actual TCP
connection. Mixer opened with 45 vanilla slots on both sides. Lathe's dedicated
server selected the generated UI (`custom=false`), while the client selected the
asset template (`custom=true`). The client loaded no recipe children.

Legacy `.rtui` files keep group children at `data.children` (and `root.children`).
The persisted codec reads extra widget data only from `_additional`; consequently
`WidgetGroup.deserializeAdditionalNBT` was never called for those legacy child
lists. The server's data resource manager does not expose the client-side asset
and uses the generated layout instead. Mixer has no `.rtui` asset and uses the
same generated layout on both sides. Physical-client side checks and the shared
UI cache mean an integrated-server test alone cannot establish network parity.

Measured Lathe menu layout:

| Index | Server before / both sides after | Client before |
| --- | --- | --- |
| 0–35 | Player inventory | Player inventory |
| 36 | Battery | Battery |
| 37 | `item_in_0`, storage slot 0 | Circuit configurator |
| 38 | `item_out_0`, storage slot 0 | Missing |
| 39 | `item_out_1`, storage slot 1 | Missing |
| 40 | Circuit configurator | Missing |

Server content initialization therefore failed on client slot 38. Fixing only
bounds checks would also leave the circuit slot assigned the input's index.

`NativeUIProjectLoader` now converts legacy child lists in a private copy before
loading. Nested groups are converted recursively. Current-format child lists
win in mixed documents, avoiding duplicate children. The cached project and the
editor's global resource scope are preserved. No packet or world format changes.

## Running the probes

The prepared local server profile is `.build/lathe-server`, bound only to
`127.0.0.1:25575`, with offline test player `GregsteadTest` whitelisted. Its
`level-name` must be the absolute path to the sole persistent regression world.
It uses pinned server-side pack JARs plus world-content mods from the test
profile, excluding SimpleBackups. Disable the config watcher and set
`pause-when-empty-seconds=0` in this local profile. Never use production server
configuration, backups or private Mushroom components here.

With the ordinary regression client closed, run these tasks from the repository
root in separate terminals, using JDK 25 and the normal offline Gradle cache:

```sh
export JAVA_HOME=/usr/lib/jvm/java-25-openjdk
export GRADLE_USER_HOME="$PWD/.build/gradle-home"
export JAVA_TOOL_OPTIONS=-XX:ActiveProcessorCount=4
bash core/sources/GregTech-Modern/gradlew -p core/sources/GregTech-Modern \
  -I "$PWD/tools/ui-regression.init.gradle" runUiRegressionServer --offline --no-daemon
bash core/sources/GregTech-Modern/gradlew -p core/sources/GregTech-Modern \
  -I "$PWD/tools/ui-regression.init.gradle" runUiRegressionClient --offline --no-daemon
```

Write `connect` to `.test-client/ui-command.txt`. Fixture machines are retained
at `(1,160,10)` (LV Lathe) and `(4,160,10)` (LV Mixer). On first placement, ensure
the chunk is loaded and positions are empty. Never replace existing fixtures.

```sh
python3 tools/ui_regression/run_checks.py
python3 tools/ui_regression/check_reload.py
```

The first driver verifies all five legacy recipe templates, an independent
current-format fixture, mixed-format precedence and cached NBT immutability.
It then compares complete client/server slot maps and contents across ten
Lathe opens and Mixer pickup/shift-click/output transfers; tests pickup, shift-click, both Lathe outputs, battery
insertion/removal, and a real iron-ingot recipe. Existing fixture contents are
collected through network clicks; they are never deleted or reset. Three empty
player inventory slots are required. Test items and crafted results remain in
the persistent world/player inventory.

The second driver checks ten further opens, resource reload, and reconnect.
Run rendering checks with the pinned shader enabled and disabled; restore its
original setting after closing the client. Screenshots go to the persistent
profile. Reports go to `.build/lathe-test-reports/` and are not release inputs.

For graceful shutdown write `stop` to each profile's `ui-command.txt`. Wait for
both processes to finish before updating mods, building, or opening singleplayer.

## Validation recorded 2026-11-11

- Unchanged baseline: 3/3 Lathe disconnects, index 38 / length 38.
- Fixed loader: all five legacy templates contain their expected item/fluid
  widgets; current-format and mixed-format fixtures and cache preservation pass.
- Network maps and item contents match; Lathe 41 slots, Mixer 45 slots.
- Ten consecutive opens with shaders on, ten on version 8.0.11 with shaders off;
  reload and reconnect retain parity.
- Pickup, shift-click, both outputs, battery insert/remove: pass.
- Real recipe: one iron ingot becomes one iron rod plus two small iron dusts.
- UI screenshots inspected with Complementary Reimagined r5.9.3 on and off;
  NVIDIA RTX 4080 renderer confirmed.

An exploratory attempt to serialize whole loaded UI trees back through the
editor codec hit an existing unknown `ldlib:gui_texture` registry key. This is
not part of the runtime load fix. Current-format compatibility is tested with
an independent explicit fixture instead of depending on that editor save path.

Test setup initially included the server pack's SimpleBackups and it produced
local test backups automatically. Its link was removed before subsequent runs;
no snapshots were restored or used as test worlds. The first fixture-open
attempt also failed before placement because the chunk had not loaded; the
probe now reports command errors without crashing the server. Neither setup
failure is counted as a Lathe baseline reproduction.
