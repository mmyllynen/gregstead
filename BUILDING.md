# Building Gregstead components

Requires Python 3.11+, Bash and JDK 25. Set `JAVA_HOME` to your JDK.

```sh
python3 release.py bootstrap gregtech --download-dependencies
python3 release.py build gregtech
```

`bootstrap` explicitly downloads verified build inputs and prepares the build.
`build` uses offline Gradle resolution. Output is under `dist/COMPONENT/`.
Available components: `gregtech`, `emi`, `calm`, `altar`, `corpse-persistence`,
`additional-placements`, `ui-fixes`, and `emi-defaults`.

Compile-only dependencies are downloaded from their authors using the URLs and
checksums in `dependencies.json`. UI Fixes builds its EMI API from this source
tree. No private server files or accounts are needed.

Component licenses and original author notices are retained. See
[THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md) and the component license files.

## Update installers

The portable updater and native build instructions are in [updater/README.md](updater/README.md). Each installer targets one exact version transition. Windows and Linux installers are built and tested separately.
