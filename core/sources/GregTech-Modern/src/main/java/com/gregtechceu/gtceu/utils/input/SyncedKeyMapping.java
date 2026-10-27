package com.gregtechceu.gtceu.utils.input;

import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.network.packets.CPacketKeyDown;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.IKeyConflictContext;
import net.neoforged.neoforge.network.PacketDistributor;

import com.mojang.blaze3d.platform.InputConstants;
import it.unimi.dsi.fastutil.ints.Int2BooleanMap;
import it.unimi.dsi.fastutil.ints.Int2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import net.minecraft.resources.Identifier;
import com.lowdragmc.lowdraglib.transfer.input.KeyState;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Supplier;

public final class SyncedKeyMapping {

    private static final Int2ObjectMap<SyncedKeyMapping> KEYMAPPINGS = new Int2ObjectOpenHashMap<>();
    private static int syncIndex = 0;
    @OnlyIn(Dist.CLIENT)
    private static Object lastClientConnection;

    @OnlyIn(Dist.CLIENT)
    private KeyMapping keyMapping;
    @OnlyIn(Dist.CLIENT)
    private Supplier<Supplier<KeyMapping>> keyMappingGetter;
    private final boolean needsRegister;
    @OnlyIn(Dist.CLIENT)
    private int keyCode;
    @OnlyIn(Dist.CLIENT)
    private boolean isKeyDown;

    private static final Int2BooleanMap updatingKeyDown = new Int2BooleanOpenHashMap();

    private final KeyState<ServerPlayer> serverMapping = new KeyState<>();
    private static final Map<String, String> CATEGORY_LABELS = new LinkedHashMap<>();
    private final WeakHashMap<ServerPlayer, Set<IKeyPressedListener>> playerListeners = new WeakHashMap<>();
    private final Set<IKeyPressedListener> globalListeners = Collections.newSetFromMap(new WeakHashMap<>());

    private SyncedKeyMapping(Supplier<Supplier<KeyMapping>> mcKeyMapping) {
        if (GTCEu.isClientSide()) {
            this.keyMappingGetter = mcKeyMapping;
        }
        // Does not need to be registered, will be registered by MC
        this.needsRegister = false;

        KEYMAPPINGS.put(syncIndex++, this);
    }

    private SyncedKeyMapping(int keyCode) {
        if (GTCEu.isClientSide() && !GTCEu.isDataGen()) {
            this.keyCode = keyCode;
        }
        // Does not need to be registered, is not a configurable key mapping
        this.needsRegister = false;

        KEYMAPPINGS.put(syncIndex++, this);
    }

    private SyncedKeyMapping(String nameKey, IKeyConflictContext ctx, int keyCode, String category) {
        if (GTCEu.isClientSide() && !GTCEu.isDataGen()) {
            this.keyMapping = (KeyMapping) createKeyMapping(nameKey, ctx, keyCode, category);
        }
        this.needsRegister = true;
        KEYMAPPINGS.put(syncIndex++, this);
    }

    /**
     * Create a SyncedKeyMapping wrapper around a Minecraft {@link KeyMapping}.
     *
     * @param mcKeyMapping Doubly-wrapped supplier around a keymapping from
     *                     {@link net.minecraft.client.Options Minecraft.getInstance().options}.
     */
    public static @NotNull SyncedKeyMapping createFromMC(@NotNull Supplier<Supplier<KeyMapping>> mcKeyMapping) {
        return new SyncedKeyMapping(mcKeyMapping);
    }

    /**
     * Create a new SyncedKeyMapping for a specified key code.
     *
     * @param keyCode The key code.
     */
    public static @NotNull SyncedKeyMapping create(int keyCode) {
        return new SyncedKeyMapping(keyCode);
    }

    /**
     * Create a new SyncedKeyMapping with server held and pressed syncing to server.<br>
     * Will automatically create a keymapping entry in the MC options page under the GregTechCEu category.
     *
     * @param nameKey Translation key for the keymapping name.
     * @param ctx     Conflict context for the keymapping options category.
     * @param keyCode The key code, from {@link InputConstants}.
     */
    public static @NotNull SyncedKeyMapping createConfigurable(@NotNull String nameKey,
                                                               @NotNull IKeyConflictContext ctx,
                                                               int keyCode) {
        return createConfigurable(nameKey, ctx, keyCode, GTCEu.NAME);
    }

    /**
     * Create a new SyncedKeyMapping with server held and pressed syncing to server.<br>
     * Will automatically create a keymapping entry in the MC options page under the specified category.
     *
     * @param nameKey  Translation key for the keymapping name.
     * @param ctx      Conflict context for the keymapping options category.
     * @param keyCode  The key code, from {@link InputConstants}.
     * @param category The category in the MC options page.
     */
    public static @NotNull SyncedKeyMapping createConfigurable(@NotNull String nameKey,
                                                               @NotNull IKeyConflictContext ctx,
                                                               int keyCode, @NotNull String category) {
        return new SyncedKeyMapping(nameKey, ctx, keyCode, category);
    }

    @OnlyIn(Dist.CLIENT)
    private @NotNull Object createKeyMapping(@NotNull String nameKey, @NotNull IKeyConflictContext ctx, int keyCode,
                                             String category) {
        KeyMapping.Category nativeCategory = ClientCategories.CATEGORIES.computeIfAbsent(category, label -> {
            String path = GTCEu.NAME.equals(label) ? "controls" :
                    "controls/" + HexFormat.of().formatHex(label.getBytes(StandardCharsets.UTF_8));
            Identifier id = GTCEu.id(path);
            CATEGORY_LABELS.put(id.toLanguageKey("key.category"), label);
            return new KeyMapping.Category(id);
        });
        return new KeyMapping(nameKey, ctx, InputConstants.Type.KEYBOARD, keyCode, nativeCategory);
    }

    /**
     * Check if the current client-side player is holding down this key.
     *
     * @return If the key is held. Always returns false on the server.
     */
    public boolean isKeyDown() {
        if (GTCEu.isClientSide()) {
            return isKeyDownClient();
        }
        return false;
    }

    /**
     * Check if a player is currently holding down this key.
     *
     * @param player The player to check.
     *
     * @return If the key is held.
     */
    public boolean isKeyDown(@NotNull Player player) {
        if (player.level().isClientSide()) {
            return isKeyDownClient();
        }
        return serverMapping.isDown((ServerPlayer) player);
    }

    /**
     * Is only safe to call on the client side
     *
     * @return if the key is down
     */
    private void resolveVanillaKeyMapping() {
        if (keyMappingGetter != null && Minecraft.getInstance().options != null) {
            keyMapping = keyMappingGetter.get().get();
            keyMappingGetter = null;
        }
    }

    private boolean isKeyDownClient() {
        resolveVanillaKeyMapping();
        if (keyMapping != null) {
            return keyMapping.isDown();
        }

        return InputConstants.isKeyDown(keyCode);
    }

    /**
     * Registers an {@link IKeyPressedListener} to this key, which will have its {@link IKeyPressedListener#onKeyPressed
     * onKeyPressed} method called when the provided player presses this key.
     *
     * @param player   The player who owns this listener.
     * @param listener The handler for the key clicked event.
     */
    public @NotNull SyncedKeyMapping registerPlayerListener(@NotNull ServerPlayer player,
                                                            @NotNull IKeyPressedListener listener) {
        Set<IKeyPressedListener> listenerSet = playerListeners
                .computeIfAbsent(player, $ -> Collections.newSetFromMap(new WeakHashMap<>()));
        listenerSet.add(listener);
        return this;
    }

    @ApiStatus.Internal
    public static void onRegisterKeyBinds(@NotNull RegisterKeyMappingsEvent event) {
        for (KeyMapping.Category category : ClientCategories.CATEGORIES.values()) event.registerCategory(category);
        for (SyncedKeyMapping value : KEYMAPPINGS.values()) {
            if (value.keyMapping != null && value.needsRegister) {
                event.register(value.keyMapping);
            }
        }
    }

    /**
     * Remove a player's listener on this keymapping for a provided player.
     *
     * @param player   The player who owns this listener.
     * @param listener The handler for the key clicked event.
     */
    public void removePlayerListener(@NotNull ServerPlayer player, @NotNull IKeyPressedListener listener) {
        Set<IKeyPressedListener> listenerSet = playerListeners.get(player);
        if (listenerSet != null) {
            listenerSet.remove(listener);
        }
    }

    /**
     * Registers an {@link IKeyPressedListener} to this key, which will have its {@link IKeyPressedListener#onKeyPressed
     * onKeyPressed} method called when any player presses this key.
     *
     * @param listener The handler for the key clicked event.
     */
    public @NotNull SyncedKeyMapping registerGlobalListener(@NotNull IKeyPressedListener listener) {
        globalListeners.add(listener);
        return this;
    }

    /**
     * Remove a global listener on this keybinding.
     *
     * @param listener The handler for the key clicked event.
     */
    public void removeGlobalListener(@NotNull IKeyPressedListener listener) {
        globalListeners.remove(listener);
    }

    @ApiStatus.Internal
    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void onClientTick(ClientTickEvent.Pre event) {
        updatingKeyDown.clear();
        var connection = Minecraft.getInstance().getConnection();
        if (connection == null) {
            lastClientConnection = null;
            return;
        }
        boolean fullSync = connection != lastClientConnection;
        lastClientConnection = connection;
        for (var entry : KEYMAPPINGS.int2ObjectEntrySet()) {
            SyncedKeyMapping keyMapping = entry.getValue();
            keyMapping.resolveVanillaKeyMapping();
            boolean previousKeyDown = keyMapping.isKeyDown;

            if (keyMapping.keyMapping != null) {
                keyMapping.isKeyDown = keyMapping.keyMapping.isDown();
            } else {

                keyMapping.isKeyDown = InputConstants.isKeyDown(keyMapping.keyCode);
            }

            if (fullSync || previousKeyDown != keyMapping.isKeyDown) {
                updatingKeyDown.put(entry.getIntKey(), keyMapping.isKeyDown);
            }
        }
        if (!updatingKeyDown.isEmpty()) {
            ClientPacketDistributor.sendToServer(new CPacketKeyDown(updatingKeyDown));
        }
    }

    @ApiStatus.Internal
    public void serverActivate(boolean keyDown, ServerPlayer player) {
        if (!this.serverMapping.update(player, keyDown)) return;

        // Player listeners
        Set<IKeyPressedListener> listenerSet = playerListeners.get(player);
        if (listenerSet != null && !listenerSet.isEmpty()) {
            for (IKeyPressedListener listener : List.copyOf(listenerSet)) {
                listener.onKeyPressed(player, this, keyDown);
            }
        }
        // Global listeners
        for (IKeyPressedListener listener : List.copyOf(globalListeners)) {
            listener.onKeyPressed(player, this, keyDown);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static final class ClientCategories {
        private static final Map<String, KeyMapping.Category> CATEGORIES = new LinkedHashMap<>();
    }

    public static Map<String, String> categoryLabels() { return Map.copyOf(CATEGORY_LABELS); }

    @ApiStatus.Internal
    public static SyncedKeyMapping getFromSyncId(int id) {
        return KEYMAPPINGS.get(id);
    }
}
