package fi.neogreg.calm;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.cubemob.AbstractCubeMob;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.*;

/** Transient server-authoritative state; automatic sync includes newly tracking clients. */
public final class CalmVisualState {
    private static final DeferredRegister<AttachmentType<?>> TYPES=DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES,"neogreg_calm");
    public static final DeferredHolder<AttachmentType<?>,AttachmentType<Boolean>> CALMED=TYPES.register("calmed_visual",()->AttachmentType.builder(()->false).sync(ByteBufCodecs.BOOL).build());
    static void register(IEventBus bus){TYPES.register(bus);}
    public static boolean active(LivingEntity entity){return entity.hasData(CALMED.get())&&entity.getData(CALMED.get());}
    static void set(LivingEntity entity,boolean value){if(entity.level().isClientSide())return;if(active(entity)!=value)entity.setData(CALMED.get(),value);}
    static void refresh(LivingEntity entity){
        boolean value=NeoGregCalm.managed(entity)&&!NeoGregCalm.provoked(entity)&&CalmAreas.contains(entity)
            &&!(NeoGregCalm.settings.allowSlimeContactDamage&&entity instanceof AbstractCubeMob)
            &&!(entity instanceof Creeper c&&c.isIgnited());
        set(entity,value);
    }
}
