package fi.gregstead.toolbeltslot;

import com.mojang.blaze3d.platform.InputConstants;
import com.tiviacz.travelertoolbelt.ToolBeltHelper;
import com.tiviacz.travelertoolbelt.network.ServerboundSwapToolPacket;
import com.tiviacz.travelertoolbelt.platform.PlatformClient;
import com.tiviacz.travelertoolbelt.toolbelt.ToolBeltWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.InputEvent;

public final class ToolCycle {
    private static final CycleQueue<ItemStack> QUEUE = new CycleQueue<>(ItemStack::copy, ItemStack::matches, ItemStack::isEmpty);
    private static int incoming;
    private static LocalPlayer previousPlayer;
    private static int previousBinding = -1;
    private static Item previousBelt;

    private ToolCycle() {}
    public static void reset() { QUEUE.reset(); incoming = 0; }

    public static void key(InputEvent.Key event) {
        if (event.getAction() == InputConstants.PRESS && ToolbeltSlot.CYCLE.matches(event.getKeyEvent())) press();
    }
    public static void mouse(InputEvent.MouseButton.Post event) {
        if (event.getAction() == InputConstants.PRESS && ToolbeltSlot.CYCLE.matchesMouse(new MouseButtonEvent(0, 0, event.getMouseButtonInfo()))) press();
    }
    private static void press() {
        var mc = Minecraft.getInstance();
        if (mc.player != null && mc.gui.screen() == null && incoming < Integer.MAX_VALUE) incoming++;
    }

    public static void tick(boolean inGame, boolean manual) {
        var mc = Minecraft.getInstance();
        int presses = incoming; incoming = 0;
        while (ToolbeltSlot.CYCLE.consumeClick()) {} // Ignore vanilla repeat clicks: only PRESS events count.
        int binding = ToolbeltSlot.boundSlot();
        if (mc.player != previousPlayer || binding != previousBinding) {
            QUEUE.reset(); previousPlayer = mc.player; previousBinding = binding; previousBelt = null;
        }
        if (mc.player == null || mc.gameMode == null || binding < 0) { QUEUE.reset(); return; }
        var belt = ToolBeltHelper.getEquippedToolBelt(mc.player);
        if (belt.isEmpty()) { QUEUE.reset(); previousBelt = null; return; }
        if (previousBelt != belt.get().getItem()) { QUEUE.reset(); previousBelt = belt.get().getItem(); }
        var inventory = mc.player.getInventory();
        var tools = ToolBeltWrapper.fromStack(belt.get()).getBeltContents();
        if (!inGame || manual) {
            QUEUE.cancel();
            QUEUE.poll(tools, inventory.getItem(binding), System.nanoTime());
            return;
        }
        if (inventory.getSelectedSlot() != binding) {
            QUEUE.cancel();
            if (presses == 0) return;
            ToolbeltSlot.synchronize(new BeltSession(binding));
            presses--; // Focusing the slot is the complete action of this press.
        }
        for (int i = 0; i < presses; i++) QUEUE.press();
        ItemStack held = inventory.getItem(binding);
        if (mc.player.isUsingItem() || (!held.isEmpty() && !ToolBeltHelper.isItemValid(held))) { QUEUE.cancel(); return; }
        int next = QUEUE.poll(tools, held, System.nanoTime());
        if (next >= 0) ToolbeltSlot.swap(new BeltSession(binding),
            () -> PlatformClient.sendToServer(new ServerboundSwapToolPacket(next, 0)));
    }

    public static void manualSwap(BeltSession session, ServerboundSwapToolPacket packet) {
        var mc = Minecraft.getInstance();
        if (session.slot() != ToolbeltSlot.boundSlot() || packet.button() != 0 || packet.slot() < 0 || mc.player == null || mc.player.isUsingItem()) return;
        ItemStack held = mc.player.getInventory().getItem(session.slot());
        if (!held.isEmpty() && !ToolBeltHelper.isItemValid(held)) return;
        var belt = ToolBeltHelper.getEquippedToolBelt(mc.player);
        if (belt.isPresent()) {
            var tools = ToolBeltWrapper.fromStack(belt.get()).getBeltContents();
            if (packet.slot() < tools.size() && !tools.get(packet.slot()).isEmpty()) {
                previousPlayer = mc.player; previousBinding = ToolbeltSlot.boundSlot(); previousBelt = belt.get().getItem();
                QUEUE.remember(packet.slot());
            }
        }
    }
}
