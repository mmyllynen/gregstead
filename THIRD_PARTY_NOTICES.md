# License and provenance boundaries

The root automation license does not apply to upstream code.

| Component | Upstream | Local license |
| --- | --- | --- |
| GregTech | GregTechCEu/GregTech-Modern, base 9bd36746eee278716cc8f64b01306bf72836f924 | Source LGPL-3.0; public combined binary GPL-3.0 |
| EMI | emilyploszaj/emi, base 81f6453c0d0b6df2e503d0bc5f19f1c73fd12173 | Source MIT; public combined binary GPL-3.0 |
| LDLib | Low-Drag-MC/LDLib-Architectury, base 9fe36a4f96ccad937222e700cce88b7e92940510 | GPL-3.0 |
| Registrate | tterrag1098/Registrate, base 254c351564e8a6b46b22152f7217477881a81585 | MPL-2.0 |
| KubeJS | kube-mods/KubeJS, base ada579b4be8a266cf2b5435cdd250aa8b5985f09 | LGPL-3.0 |
| Transfer support | Port-specific code, including code derived from GregTech and LDLib | Public combined code GPL-3.0; original LGPL text retained in LGPL_ORIGIN.txt |
| Additional Placements | FirEmerald/AdditionalPlacements | MIT, original FirEmerald copyright retained |
| Calm | Original project copyright identifies Kickstart contributors | MIT, original notice retained |
| Altar | Gregstead contributors; NeoForge MDK template retained separately | Own code MIT; see TEMPLATE_LICENSE.txt |
| Corpse Persistence, UI Fixes, EMI Defaults | Gregstead contributors | MIT |

These are upstream base revisions, not commits of this locally modified port.
EMI's JEMI sources were reconstructed with Vineflower from the official
`emi-neoforge:1.1.24+1.21.1` binary because the upstream source JAR omitted them.
That origin is part of EMI; no separate JEMI upstream revision is asserted.

FTB sources are All Rights Reserved and are excluded. Only unmodified official
FTB JARs are downloaded for compilation; they are not bundled in the result.
Official runtime mods such
as AE2, GuideME, Jade and Curios are not republished as Gregstead-owned projects.
Their own licenses and distribution requirements remain authoritative.

The public build embeds full applicable license texts and provenance from
`licenses/` into each rebuilt core JAR. The original Configuration binary is
MIT licensed; its author credit and upstream license designation are retained.
The original jsr305 binary is Apache-2.0 licensed. Original component source
licenses are preserved; the combined binaries are distributed under GPL-3.0
because they include LDLib and LDLib-derived transfer code. Publish the exact
matching source archive/commit and build instructions with each binary release.

The original LDLib JetBrains Mono font is retained unchanged, including the
SIL Open Font License 1.1 text in `licenses/JetBrainsMono-OFL-1.1.txt` and the
rebuilt JAR notices. Editor layouts, compass XML and other resource formats are
source assets, not disposable build artifacts.
