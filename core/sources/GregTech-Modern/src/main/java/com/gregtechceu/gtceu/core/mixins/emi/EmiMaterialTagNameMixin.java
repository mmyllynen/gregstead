package com.gregtechceu.gtceu.core.mixins.emi;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Known material tags use the same localized material/prefix names as their actual items. */
@Mixin(value=dev.emi.emi.runtime.EmiTagKey.class, remap=false)
public abstract class EmiMaterialTagNameMixin {
    @Shadow public abstract TagKey<?> raw();

    private Component gtceu$materialTagName() {
        var tag=raw();
        if(!tag.isFor(Registries.ITEM))return null;
        @SuppressWarnings("unchecked") var entry=ChemicalHelper.getMaterialEntry((TagKey<Item>)tag);
        if(entry.isEmpty()||entry.material().isNull()||
                !entry.tagPrefix().getItemTags(entry.material()).contains(tag))return null;
        var language=net.minecraft.locale.Language.getInstance();
        if(!language.has(entry.material().getUnlocalizedName())||
                !language.has(entry.tagPrefix().getUnlocalizedName(entry.material())))return null;
        return entry.tagPrefix().getLocalizedName(entry.material());
    }

    @Inject(method="getTagName", at=@At("RETURN"), cancellable=true)
    private void gtceu$nameKnownMaterialTag(CallbackInfoReturnable<Component> callback) {
        if(!callback.getReturnValue().getString().equals("#"+raw().location()))return;
        var name=gtceu$materialTagName();if(name!=null)callback.setReturnValue(name);
    }

    @Inject(method="hasTranslation", at=@At("RETURN"), cancellable=true)
    private void gtceu$knownMaterialNameIsLocalized(CallbackInfoReturnable<Boolean> callback) {
        if(!callback.getReturnValue()&&gtceu$materialTagName()!=null)callback.setReturnValue(true);
    }
}
