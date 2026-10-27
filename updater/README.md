# Gregstead update installer

Download the installer for your operating system and the exact version change
from the main [Gregstead releases](https://github.com/mmyllynen/gregstead/releases).
New installations come from the Gregstead CurseForge modpack.

Close Minecraft, open the installer, select your profile, click **Check update**,
review the target folder and version, then click **Install update**. Windows uses
an `.exe`; on Linux extract the `.tar.gz` and open the executable. Your desktop
may first require enabling its executable permission in the file properties.

The installer requires an existing supported version. A Minecraft or NeoForge
version change requires a new full installation through CurseForge. Personal settings, worlds,
maps and the server list are preserved. Pack gameplay settings follow the selected
release. If a required file has been changed unexpectedly, the installer stops
before replacing it. It never deletes unknown extra mods. Use the main project's
issue tracker for help rather than bypassing a failed check.

An installer only installs its named version. It is not a launcher and does not
poll for updates. It does not change CurseForge's own version metadata; the
installed content is identified by `.gregstead-release.json` instead.

Verified backups are retained in the profile's `.gregstead-backups` folder. If
an interrupted operation needs recovery, close the game and run the installer
with `--profile PROFILE --recover OPERATION`. Do not restore game worlds as part
of an installer recovery; they are never modified by the installer.

## Building

Use Python 3.12 on each target operating system. Install `requirements.txt` into
a virtual environment. Linux additionally needs Tcl/Tk and the normal desktop
libraries. Then run from the source repository root:

```sh
python -m updater.build --bundle /path/to/update.zip --output dist/installers
```

Build Windows executables on Windows and Linux executables on Linux. Keep the
repository `.gitattributes` rules: source and license hashes use LF line endings. The build
runs a synthetic packaged-engine self-test; native GUI smoke tests are a separate
required check before publication. Supported targets are Windows 10/11 x64 and
Ubuntu 22.04 or newer x64. No Python installation is needed on players' computers.

Source license: MIT. Bundled third-party/runtime notices are available with the
**Licenses** button. Sources and support are centralized in the main project.
