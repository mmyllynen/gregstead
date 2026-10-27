package com.gregtechceu.gtceu.api.gui.editor;

import com.lowdragmc.lowdraglib.LDLib;
import com.lowdragmc.lowdraglib.editor.Icons;
import com.lowdragmc.lowdraglib.registry.annotation.LDLRegister;
import com.lowdragmc.lowdraglib.editor.ui.UIEditor;
import com.lowdragmc.lowdraglib.editor.ui.tool.WidgetToolBox;

import static com.lowdragmc.lowdraglib.editor.ui.tool.WidgetToolBox.Default.registerTab;

@LDLRegister(name = "editor.gtceu", group = "editor", registry = "ldlib:widget")
public class GTUIEditor extends UIEditor {

    public static final WidgetToolBox.Default GT_CONTAINER = registerTab("widget.gtm_container",
            Icons.WIDGET_CONTAINER);

    public GTUIEditor() {
        super(LDLib.getAssetsDir());
    }
}
