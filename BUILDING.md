# Building Gregstead components

Requires Python 3.11+, Bash and JDK 25 (including `javac`). On Fedora:

```sh
sudo dnf install java-25-openjdk-devel python3 bash
export JAVA_HOME=/usr/lib/jvm/java-25-openjdk
```

Use a fresh checkout for a release build. The commands below run from the
repository root. They do not install, upload or launch the game.

```sh
python3 release.py bootstrap all --download-dependencies
python3 release.py build all
```

`bootstrap` explicitly downloads verified build inputs and prepares the build.
`build` uses offline Gradle resolution. Both commands clean the selected
component's Gradle output before compiling. Output is under `dist/COMPONENT/`.
Available components: `gregtech`, `emi`, `calm`, `altar`, `corpse-persistence`,
`additional-placements`, `ui-fixes`, `emi-defaults`, `ae2-fixes` and `toolbelt-slot`.
Replace `all` with one component to build it separately. GregTech builds its
embedded libraries; UI Fixes also builds EMI as a compile-only dependency.

Compile-only dependencies are downloaded from their authors using the URLs and
checksums in `dependencies.json`. UI Fixes builds its EMI API from this source
tree. No private server files or accounts are needed.

Component licenses and original author notices are retained. See
[THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md) and the component license files.

## Client and server modpacks

After building the components, prepare the exact external pack inputs and
assemble both ZIPs:

```sh
python3 build_pack.py bootstrap --download-dependencies
python3 build_pack.py build --version 1.0.26-rebuilt.1
```

`pack/build-inputs.json` pins external mod and shader versions, download URLs,
checksums, client/server placement and known CurseForge file IDs. The separate
`pack/client-runtime.json` pins the client ASM workaround. Downloads are cached
under `.build/pack-inputs/`; `build` verifies them again and never downloads.
If an author download is unavailable, obtain that exact file from the author
and use `bootstrap --artifact-dir /path/to/files`; checksums are still required.
The mod builder supports the same `--artifact-dir` option for compile JARs.

Output is under `dist/pack/VERSION/`, with SHA-256 files. Choose a new explicit
version for changed output; an existing ZIP or component JAR with different
content is not overwritten. The client ZIP uses CurseForge manifest references
where exact IDs are known and overrides for locally built components and other
pinned inputs. The server ZIP contains the server-side JARs; install the listed
NeoForge version and JDK separately and review the Minecraft EULA.

These commands assemble local build outputs. CurseForge publication, final App
import validation and deployment are separate steps. The public pack excludes
Mushroom's private Discord bridge and its existing server-specific BlueMap
deployment. Shared defaults already in this repository are used as-is; private
server configuration, worlds, player files and credentials are not build inputs.

All normal builds use only this checkout, the installed JDK and verified
downloaded inputs, with no USB-backup or old Ubuntu workspace dependency.

## Release artifact names

Component versions and artifact stems are declared in `projects.json`. All
release JARs end in `-mc26.3.jar`. The GregTech port produces
`gregstead-VERSION-mc26.3.jar`; upstream-versioned ports use their component
name followed by `UPSTREAM-gregstead.REVISION-mc26.3.jar`. Other components
use `gregstead-COMPONENT-VERSION-mc26.3.jar`. Pack ZIPs use
`gregstead-client-VERSION-mc26.3.zip` and `gregstead-server-VERSION-mc26.3.zip`.
Old build outputs may coexist; packaging selects the declared current artifact.

## CurseForge submission build

`pack/component-files.json` records the submitted component versions, exact JAR
hashes, CurseForge project/file IDs and last observed moderation statuses.
After verifying the current files are approved in the author dashboard, update
the recorded statuses and run:

```sh
python3 build_pack.py build --curseforge --version 1.0.27
```

This writes the client/server ZIPs under `dist/curseforge/1.0.27/`. It refuses
unapproved component versions, changed JARs and missing client file references.
Client mods are referenced in manifest.json instead of bundled into overrides.
The ordinary `build` command produces a local test pack under `dist/pack/`; that
pack is not the CurseForge submission artifact. Neither command submits files.

## Sign resources

After running game datagen, run `python3 tools/generate_sign_assets.py`
(with Pillow installed). This restores the Minecraft 26.3 sign model layouts
and derives the GUI/block sprites from Gregstead wood palettes. Normal builds
consume the checked-in results.

## Persistent regression client

Use only `.test-client/saves/Gregstead-Regression`. This is persistent state,
never a build/cache directory. See AGENTS.md for the one-world policy.

Prepare this profile with `python3 tools/prepare_test_client.py` after building
components. It verifies pinned inputs, installs the current client mod set,
preserves existing configuration and refuses extra worlds or unmanaged JARs.
Close the client before preparing it again or rebuilding components. Both
commands refuse to proceed while the regression profile holds its session lock. The source candidate is loaded by
NeoForge's development launcher; installed dependency JAR hashes and the release
candidate hash are recorded in `.test-client/test-state.json`.

With Java 25 installed, launch from the repository root:

```sh
JAVA_HOME=/usr/lib/jvm/java-25-openjdk \
GRADLE_USER_HOME="$PWD/.build/gradle-home" \
JAVA_TOOL_OPTIONS=-XX:ActiveProcessorCount=4 \
bash core/sources/GregTech-Modern/gradlew -p core/sources/GregTech-Modern \
  -I "$PWD/.build/regression-client.init.gradle" runRegressionClient --offline --no-daemon
```

The local-only `tools/regression_probe` mod opens the existing world, or creates
it once when neither its save nor the creation marker exists. A warning-only
NeoForge screen may be continued automatically; loading errors are never
bypassed. The probe installs fixtures once near (0,160,0), leaving them intact
on subsequent launches. It is not part of release JARs or modpacks.

For scripted verification, write one command to `.test-client/probe-command.txt`
and wait for `.test-client/probe-result.txt` before the next command. Supported
commands are `capture NAME` (a bounded screenshot filename), `sign X Y Z`,
`text TEXT`, `close`, `use X Y Z`, `emi on`, `emi off`, `server COMMAND` and
`status` (records the actual strict GPU validation flag) and `stop`. Commands
affect this isolated profile only. Use `stop` for a graceful
save and shutdown; retain test text and fixtures for the next release.

The regression launcher sets `-Dneoforge.disableBlaze3DValidation=true`.
NeoForge 26.3.0.77 enables its strict GPU validator only in development clients
(`SharedConstants.IS_RUNNING_IN_IDE`). Its `FrontendRenderPass.validateDraw`
loops over 16 vertex slots and indexes `boundPipeline.vertexFormats()` even
when Iris provides a one-entry list, causing an index-1/length-1 exception in
the shadow pass. The supported flag makes this development-only check match
packaged clients; it does not disable shaders or change release JARs.

The 8.0.10 baseline was verified in this world with shaders disabled:
rubber/treated sign bodies and editors, rubber wall/hanging editors, text
persistence after graceful restart, and a single EMI alpha-0x77 slot overlay
(157,118,88 → 84,63,47). The same checks for sign bodies/editors and a single EMI overlay also passed
with Iris 1.11.7+mc26.3 and Complementary Reimagined r5.9.3 enabled. World and
shadow rendering remained stable.
Local screenshots and detailed results stay in `.test-client`.

## GitHub client distributions

Run `python3 build_pack.py build --github --version 1.0.27`. Outputs are under `dist/github/1.0.27/`: full CurseForge-format client import and own-client-mods-only update. GitHub mode bundles own mods without requiring CurseForge approval; external mods retain manifest references. A matching server ZIP is also produced.

## Optional OPAC BlueMap integration

`opac-bluemap` is an opt-in server add-on and is excluded from normal `all`
builds and modpack distributions. See `mods/opac-bluemap/README.md` for the
pinned public compile inputs and persistent-world probe. Once those inputs
are present, build with `python3 release.py build opac-bluemap` using JDK 25.
Server-specific BlueMap configuration remains outside this repository.

## CurseForge uploads

`tools/curseforge_upload.py` uses the documented author Upload API and requires
Python requests plus kwallet-query. It reads the token from KWallet into memory,
uses version/environment names and records successful uploads without secrets.
Use an explicit receipt path; never retry an uncertain POST before checking the
author dashboard. Package construction still requires approved component IDs.
