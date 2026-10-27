package com.gregtechceu.gtceu.api.gui.editor;

import com.lowdragmc.lowdraglib.registry.annotation.LDLRegisterClient;
import com.lowdragmc.lowdraglib.editor.ui.menu.MenuTab;
import com.lowdragmc.lowdraglib.gui.util.TreeBuilder;

@LDLRegisterClient(name = "template_tab", group = "editor.gtceu", registry = "ldlib:menu_tab")
public class TemplateTab extends MenuTab {

    protected TreeBuilder.Menu createMenu() {
        return TreeBuilder.Menu.start();
    }
}
