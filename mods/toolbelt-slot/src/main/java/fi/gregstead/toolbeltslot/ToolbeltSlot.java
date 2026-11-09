package fi.gregstead.toolbeltslot;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.logging.LogUtils;
import com.tiviacz.travelertoolbelt.ToolBeltHelper;
import com.tiviacz.travelertoolbelt.client.screens.BeltScreen;
import com.tiviacz.travelertoolbelt.handlers.ModClientHandler;
import com.tiviacz.travelertoolbelt.item.ToolBeltItem;
import fi.gregstead.toolbeltslot.mixin.CarriedItemAccess;
import java.io.IOException;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.GameType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = "gregstead_toolbelt_slot", dist = Dist.CLIENT)
public final class ToolbeltSlot {
    public static final KeyMapping BIND = new KeyMapping("key.gregstead_toolbelt_slot.bind",
        InputConstants.UNKNOWN.getValue(), ModClientHandler.CATEGORY);
    public static final KeyMapping OPEN_HERE = new KeyMapping("key.gregstead_toolbelt_slot.open_here",
        InputConstants.UNKNOWN.getValue(), ModClientHandler.CATEGORY);
    public static final KeyMapping CYCLE = new KeyMapping("key.gregstead_toolbelt_slot.cycle",
        InputConstants.UNKNOWN.getValue(), ModClientHandler.CATEGORY);
    private static final BindingStore BINDING = new BindingStore(
        FMLPaths.CONFIGDIR.get().resolve("gregstead-toolbelt-slot.properties"));

    public ToolbeltSlot(IEventBus bus) {
        bus.addListener(ToolbeltSlot::registerKeys);
        NeoForge.EVENT_BUS.addListener(ToolCycle::key);
        NeoForge.EVENT_BUS.addListener(ToolCycle::mouse);
        try { BINDING.load(); }
        catch (IOException | IllegalArgumentException error) {
            LogUtils.getLogger().error("Could not load toolbelt hotbar binding; using unbound mode", error);
        }
    }

    private static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(BIND);
        event.register(OPEN_HERE);
        event.register(CYCLE);
    }

    /** Saved binding, independent of the current selection or a temporary override opening. */
    public static int boundSlot() { return BINDING.slot(); }

    private static boolean consume(KeyMapping key) {
        boolean pressed = false;
        while (key.consumeClick()) pressed = true;
        return pressed;
    }

    /** Called at the upstream key handler, so listener ordering cannot steal its click. */
    public static boolean tick() {
        var mc = Minecraft.getInstance();
        boolean bind = consume(BIND);
        boolean override = consume(OPEN_HERE);
        boolean inGame = mc.player != null && mc.gameMode != null && mc.gui.screen() == null && !mc.gui.hud.isHidden()
            && mc.gameMode.getPlayerMode() != GameType.SPECTATOR;
        if (bind && inGame) {
            try {
                BINDING.bind(mc.player.getInventory().getSelectedSlot());
                ToolCycle.reset();
                mc.player.sendOverlayMessage(Component.translatable("message.gregstead_toolbelt_slot.bound", BINDING.slot() + 1));
            } catch (IOException error) {
                LogUtils.getLogger().error("Could not save toolbelt hotbar binding", error);
                mc.player.sendOverlayMessage(Component.translatable("message.gregstead_toolbelt_slot.save_failed"));
            }
        }
        boolean managed = BINDING.slot() >= 0 || override;
        boolean normal = managed && consume(ModClientHandler.OPEN_TOOLBELT);
        ToolCycle.tick(inGame, bind || normal || override);
        if (!managed) return false; // Entire upstream path until first binding.
        if (!inGame || (!normal && !override) || ToolBeltHelper.getEquippedToolBelt(mc.player).isEmpty()) return true;

        var session = BeltSession.open(BINDING.slot(), mc.player.getInventory().getSelectedSlot(), override);
        if (mc.player.getInventory().getItem(session.slot()).getItem() instanceof ToolBeltItem) {
            mc.player.sendOverlayMessage(Component.translatable("screen.travelertoolbelt.cannot_open"));
            return true;
        }
        synchronize(session);
        var screen = new BeltScreen();
        ((BeltScreenAccess) screen).gregstead$configure(session, override ? OPEN_HERE : ModClientHandler.OPEN_TOOLBELT);
        mc.gui.setScreen(screen);
        return true;
    }

    public static void synchronize(BeltSession session) {
        var mc = Minecraft.getInstance();
        if (mc.player == null || mc.gameMode == null) return;
        session.synchronize(mc.player.getInventory()::setSelectedSlot,
            ((CarriedItemAccess) mc.gameMode)::gregstead$syncCarriedItem);
    }

    public static void swap(BeltSession session, Runnable send) {
        var mc = Minecraft.getInstance();
        if (mc.player == null || mc.gameMode == null) return;
        session.swap(mc.player.getInventory()::setSelectedSlot,
            ((CarriedItemAccess) mc.gameMode)::gregstead$syncCarriedItem, send);
    }
}
