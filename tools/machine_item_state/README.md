# Machine world saves and portable item state

Issues: [#8](https://github.com/mmyllynen/gregstead/issues/8),
[#37](https://github.com/mmyllynen/gregstead/issues/37).
Adapted behavior from upstream #5226 and #5123 to Minecraft 26.3.

World saves retain the existing complete managed schema, including pending miner
positions and the original batch size. `miningStateVersion=1` distinguishes the
new complete queue schema. Old miners rescan once because their saved cursor may
have advanced beyond an unsaved pending batch. Already mined blocks are replaced
in the world and are not awarded again. A saved origin inconsistent with the
actual machine position is repaired. The user's pause selection is retained.
New-format reloads preserve completed and partial jobs exactly.

Machine drops use explicit `machine_item_data` plus the existing typed storage
components instead of `block_entity_data`. `@SaveField(item=...)` is independent
of world persistence. `CHILDREN` traverses only managed child projections;
ordinary scalar leaves are excluded by default. `VALUE` is an explicit resource
or configuration decision, `GHOST_CIRCUIT` depends on the circuit mode, and `NONE`
excludes an entire subtree. Regenerate the review inventory with `python3
tools/machine_item_state/audit.py`.

Portable: fluids, energy, thermal stores, selected operating settings, filters,
virtual circuits, painting. Native drum, quantum, creative and taped
crate components remain authoritative for their stored contents. Physical
inventories drop according to their existing rules, or pack into their container
once; they do not also appear in generic machine state. Jobs, recipe inputs
already consumed into progress, positions, mining queues, progress, formed
structures, cached network identities and computed visuals do not travel.
Monitor arrangements contain world positions and are reconstructed at the new
site; their physical modules continue to drop.

AE2 normal import/output buffers and pattern-buffer internal materials are real
resources and travel with the machine. They are not additionally returned to ME
from the destruction callback: loot capture can run before that callback.
Stocking buses/hatches carry configuration only, never their virtual ME stock.
First and last array slots are tested, as are disconnected buffers. Grid node
identities and previous ownership are excluded; placement assigns the new owner. Covers drop separately exactly once; `cover_item_data`
preserves configuration while their inventory/filter drops remain separate.

Legacy full-NBT machine items pass through the same allowlist when placed. The
placement context is scoped to an exact level and block position, so unrelated
chunk loads on the same thread cannot accidentally be treated as item loads.
Ordinary world loads never use the item filter. Constructor defaults are omitted
to allow plain empty machine items to stack with freshly crafted ones.

Tests run through the development-only probe in the sole persistent world
`.test-client/saves/Gregstead-Regression`. Commands: `machines`,
`machine-resources`, then normal `stop` and relaunch, then `machines-persist`.
Additional commands: `machine-storage` checks native typed containers and empty
machine stacking; `miner-active-start`/`miner-active-verify` exercise actual
server-tick mining; `machine-client-prepare`/`machine-client-verify LABEL` check
paint, energy and pause synchronisation on the client. `shaders true`/`shaders
false` allow checking the retained painted model with the pinned pack enabled
and disabled. The prepared fixtures are persistent: do not overwrite an occupied
fixture without examining its existing contents and the preceding report.
Detailed reports remain local; the probe classes are excluded from release JARs.
