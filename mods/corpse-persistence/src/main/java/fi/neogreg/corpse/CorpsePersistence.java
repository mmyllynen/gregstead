package fi.neogreg.corpse;

import net.neoforged.fml.common.Mod;
import net.neoforged.api.distmarker.Dist;
import com.mojang.logging.LogUtils;

@Mod(value="neogreg_corpse_persistence",dist=Dist.DEDICATED_SERVER)
public final class CorpsePersistence {
    public CorpsePersistence() {
        LogUtils.getLogger().info("NeoGreg Corpse Persistence: automatic corpse expiry disabled; skeleton aging unchanged");
    }
}
