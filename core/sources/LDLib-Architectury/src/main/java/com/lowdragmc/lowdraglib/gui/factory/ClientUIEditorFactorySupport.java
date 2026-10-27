package com.lowdragmc.lowdraglib.gui.factory;

import com.lowdragmc.lowdraglib.LDLib;
import com.lowdragmc.lowdraglib.editor.ui.UIEditor;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

final class ClientUIEditorFactorySupport {
    private ClientUIEditorFactorySupport() {}
    static WidgetGroup create() { return new UIEditor(LDLib.MOD_ID); }
}
