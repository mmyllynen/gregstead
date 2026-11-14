# Sleepwear regression

Local-only probe; never bundled in release mods. Run the client using `tools/sleepwear-regression.init.gradle` and the Sleepwear Gradle project. It uses the sole persistent `.test-client/saves/Gregstead-Regression` world. Dedicated mode uses `.build/lathe-server` with that exact absolute world path; never run both servers at once.

Control files in `.test-client`:

- `sleepwear-command.txt`: `open`, `connect`, `capture NAME`, `thirdperson`, `reload`, `stop`.
- `sleepwear-server-command.txt`: `test`, `display`, `command VANILLA_COMMAND`, `shutdown`.
- Results: `sleepwear-tests-integrated.txt`, `sleepwear-tests-dedicated.txt`; failures are in `sleepwear-tests-error.txt`.

The recipe probe checks every wool colour and garment and all 512 two-colour garment pairs. The world probe uses a persistent bed at (16,160,8) and checks real server-player sleeping, daytime idle counters, incomplete equipment, manual wake, re-equip, next night, obstruction, distance and line of sight. It also checks monsters, occupied beds, paused-clock commands, nearest-bed selection and daytime thunderstorms. Reconnect and design persistence use the prepareReconnect/wake/verifyReconnect and verifyDesign controls. Preserve fixtures and record exact candidate hashes and results in `.test-client/test-state.json`.

For exact-artifact verification, copy the final JAR to each test profile mods directory and pass `-PsleepwearPackaged`; this excludes Sleepwear dev-source registration while keeping the local-only probe.
