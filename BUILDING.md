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
