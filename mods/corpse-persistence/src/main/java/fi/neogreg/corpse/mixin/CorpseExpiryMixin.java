package fi.neogreg.corpse.mixin;

import de.maxhenkel.corpse.entities.CorpseEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Suppress only the two automatic expiry paths; explicit removal remains available. */
@Mixin(value=CorpseEntity.class,remap=false)
public abstract class CorpseExpiryMixin {
    @Redirect(method="tick",at=@At(value="INVOKE",target="Lde/maxhenkel/corpse/entities/CorpseEntity;discard()V"),require=2,expect=2,allow=2)
    private void neogreg$keepCorpse(CorpseEntity corpse) {
        // Age and empty-age still advance, so ordinary skeleton conversion is preserved.
    }
}
