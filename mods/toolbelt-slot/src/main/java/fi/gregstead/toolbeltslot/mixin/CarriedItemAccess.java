package fi.gregstead.toolbeltslot.mixin;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MultiPlayerGameMode.class)
public interface CarriedItemAccess {
    @Invoker("ensureHasSentCarriedItem")
    void gregstead$syncCarriedItem();
}
