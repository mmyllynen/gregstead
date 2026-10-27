package com.lowdragmc.lowdraglib.editor.accessors;

import com.lowdragmc.lowdraglib.LDLib;
import com.lowdragmc.lowdraglib.registry.annotation.LDLRegisterClient;
import com.lowdragmc.lowdraglib.editor.annotation.DefaultValue;
import com.lowdragmc.lowdraglib.editor.configurator.Configurator;
import com.lowdragmc.lowdraglib.editor.configurator.StringConfigurator;
import net.minecraft.resources.Identifier;

import java.lang.reflect.Field;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author KilaBash
 * @date 2022/12/3
 * @implNote ResourceLocationAccessor
 */
@LDLRegisterClient(name = "resource_location", registry = "ldlib:configurator_accessor")
public class ResourceLocationAccessor extends TypesAccessor<Identifier> {

    public ResourceLocationAccessor() {
        super(Identifier.class);
    }

    @Override
    public Identifier defaultValue(Field field, Class<?> type) {
        if (field.isAnnotationPresent(DefaultValue.class)) {
            return Identifier.parse(field.getAnnotation(DefaultValue.class).stringValue()[0]);
        }
        return Identifier.fromNamespaceAndPath(LDLib.MOD_ID, "default");
    }

    @Override
    public Configurator create(String name, Supplier<Identifier> supplier, Consumer<Identifier> consumer, boolean forceUpdate, Field field) {
        var configurator = new StringConfigurator(name, () -> supplier.get().toString(), s -> consumer.accept(Identifier.parse(s)), defaultValue(field, String.class).toString(), forceUpdate);
        configurator.setResourceLocation(true);
        return configurator;
    }
}
