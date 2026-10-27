package com.lowdragmc.lowdraglib.gui.factory;

import com.lowdragmc.lowdraglib.LDLib;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.gui.modular.IUIHolder;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

public class UIEditorFactory extends UIFactory<UIEditorFactory> implements IUIHolder {

	public static final UIEditorFactory INSTANCE = new UIEditorFactory();

	private UIEditorFactory(){
		super(LDLib.id("ui_editor"));
	}

	@Override
	protected ModularUI createUITemplate(UIEditorFactory holder, Player entityPlayer) {
		return createUI(entityPlayer);
	}

	@Override
	protected UIEditorFactory readHolderFromSyncData(RegistryFriendlyByteBuf syncData) {
		return this;
	}

	@Override
	protected void writeHolderToSyncData(RegistryFriendlyByteBuf syncData, UIEditorFactory holder) {

	}

	@Override
	public ModularUI createUI(Player entityPlayer) {
		var ui = new ModularUI(this, entityPlayer);
        // Editor is a client-side widget: the server retains its original 10x10 protocol node.
        WidgetGroup editor = entityPlayer.level().isClientSide()
                ? ClientUIEditorFactorySupport.create()
                : new WidgetGroup(0, 0, 10, 10);
        editor.setClientSideWidget();
        ui.mainGroup.setClientSideWidget();
        ui.mainGroup.setAllowXEIIngredientOverMouse(false);
        return ui.widget(editor);
	}

	@Override
	public boolean isInvalid() {
		return false;
	}

	@Override
	public boolean isRemote() {
		return LDLib.isRemote();
	}

	@Override
	public void markAsDirty() {

	}
}
