# Release queue

## 1.0.32 — machine persistence fixes

- Gregstead 8.0.15; other components unchanged from 1.0.31.
- Issues #8 and #37: world queue persistence and explicit portable machine resources/settings.
- Verified 1,234 machine variants, 92 cover types, inventory/resource conservation, real world restart, legacy migration and actual mining. Client synchronization checked with pinned shaders enabled and disabled; prior drum regression passed.
- All 2,290 release main-class files match the tested classes; ZIP CRC, checksums, ten-mod update bundle and unchanged other-mod hashes verified.
- Regression probes and field-policy audit: `tools/machine_item_state/README.md`.
- Publication/deployment verification is recorded separately from the historical queue below.

## 1.0.31 — verified source-fluid pickup release sources

- Gregstead 8.0.14; other components unchanged from 1.0.30.
- 21 in-game checks passed in the persistent regression world with the pinned shader enabled, including restart persistence and inventory overflow.
- Source tags: v1.0.31 and gregstead-8.0.14. Full client, ten-own-client-mod update and server ZIPs have matching sources and checksums.
- No private Mushroom components are distributed. Deployment receipts remain local and under Mushroom ops.


## 1.0.30 — local regression candidate

- Sleepwear 0.1.0 and Gregstead 8.0.13; Calm remains 2.2.4.
- Mobs Be Calm 3.0.0 and related changes excluded.
- GitHub publication and local client/Mushroom deployment follow successful game tests.

## Already published since the historical queue below

- GitHub pack 1.0.28 and 1.0.29 are published; 1.0.29 is deployed to local NeoGreg and Mushroom.
- Gregstead 8.0.12 and 8.0.13 are separately published; both targets currently use 8.0.13.
- Historical GitHub-publication and OPAC pack tasks below are completed; CurseForge approval remains separate.


## 1.0.28 — GitHub release ready; CurseForge approval pending

Completed:
- PRs #4–#6 closed; their changes were already released in 1.0.27.
- Chunk Loaders removed from client/server pack inputs, default configs and EMI
  defaults. OPAC server defaults provide 100 chunks/player with party scaling.
- EMI Defaults 0.1.3 built, smoke-tested and submitted to CurseForge (9538176).
- Exact client candidate started in the sole persistent regression world;
  closed and saved normally. Nine-own-client-mod archive checks passed.
- Mushroom now has all 63 pinned public server JARs with exact matching hashes.
  Seven old JARs were replaced after a world/config backup at
  backups/release-1.0.28-20261110-072221 on Mushroom. Startup, OPAC and BlueMap
  passed verification; receipt: mc/neogreg/release-1.0.28-deployment.json.
- Public full import and nine-own-mod update candidates are built under
  dist/github/1.0.28 with checksums. Private server additions are excluded.

Remaining:
- Publish matching GitHub source tag and both archives (authorized; in progress).
- Recheck CurseForge approvals for GregTech, AE2 Fixes, Toolbelt Slot and new EMI
  Defaults. Do not build/submit the CurseForge pack until all are approved.
- Submit client and linked server packs to Gregstead (project 1768761), then
  report actual moderation status. Token stays in KWallet and process memory.
- User's fresh CurseForge import and Mushroom join test remains pending.

The user's ordinary client was not modified. The older deployment notes below
are historical evidence; their 'future release' tasks are included in 1.0.28.

## OPAC-only chunk loading and BlueMap claims (2026-11-10)

### Installed on Mushroom

- OPAC base forceload budget 100, additional party-member budget 100:
  one player gets 100; a two-member party gets 200. Claim budgets unchanged.
- Offline forceload remains disabled in the default and both saved player configs.
- Chunk Loaders removed from active server mods, without migrating its areas.
- Optional server-only OPAC BlueMap integration port installed; existing claims
  appear on the web map with OPAC names/colors. No production claims changed.
- Server startup completed; HTTP marker endpoints verified for all three maps:
  overworld has one claim polygon, Nether/End have empty claim layers.
- Previous config and Chunk Loaders JAR retained under
  `backups/opac-only-20261110-062359` on Mushroom.
- Compressed world and BlueMap/config backup retained under
  `backups/opac-bluemap-20261110-064140` on Mushroom. Deployment receipt:
  `mc/neogreg/opac-bluemap-deployment.json`.

Integration artifact: `gregstead-opac-bluemap-1.2.1-gregstead.1-mc26.3.jar`.
SHA-256: `da649c723d5ca1b8f5337a7e4b5f0c6a410f50a5a990f590f54b4bf90ddaf815`.
Built from the general-purpose source under `mods/opac-bluemap`; upstream
revision, MIT license and compile-input hashes are retained there. The
`pack-addon` project kind excludes it from normal public modpacks and `all` builds.
Mushroom-specific configuration and world data remain outside this repository.

### Verified locally

Only the existing persistent `Gregstead-Regression` world was used. Chunk
Loaders was removed from that profile; the user's ordinary client was untouched.

- Solo limit 100 and two-member party limit 200.
- 100th forceload accepted; 101st rejected. Offline synthetic-owner metadata
  fixtures avoid generating/loading terrain for the budget test.
- Same-coordinate claims in two dimensions; unclaim removes the correct marker.
- Fresh API resync and real BlueMap service updates.
- After graceful restart, HTTP endpoints automatically contained two overworld
  fixture polygons and one Nether fixture polygon.
- Clean offline build reproduced the exact tested/deployed JAR hash.
- Test probe code excluded from the deployment JAR.

Exact profile JAR hashes and results are in `.test-client/test-state.json`;
probe results are in `.test-client/opac-bluemap-test.txt`. These are local state,
not release inputs. Re-preparing the profile from the unchanged public pack inputs
will restore the old pack mod list until the queued pack changes are applied.

### Pending for a future combined release

- Remove Chunk Loaders from pack inputs, CurseForge inventory and defaults.
- Remove its EMI recipe defaults and update generation provenance.
- Add OPAC server defaults for 100 forceloads per player and party scaling.
- Document removal of old Chunk Loaders JARs and loss of their loading behavior.
- Select pack/component release versions together with other queued changes.
- Run normal pack validation, GitHub distribution and CurseForge approval flows
  only when publication is requested. Preserve the nine-own-client-mod bundle.

No GitHub/CurseForge publication or tag was created. Public pack version remains
unchanged; the integration's version identifies the local server artifact only.
