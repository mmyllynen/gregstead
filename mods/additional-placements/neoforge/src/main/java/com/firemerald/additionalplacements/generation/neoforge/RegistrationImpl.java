package com.firemerald.additionalplacements.generation.neoforge;

import com.firemerald.additionalplacements.block.AdditionalPlacementBlock;
import com.firemerald.additionalplacements.generation.GenerationType;
import com.firemerald.additionalplacements.generation.Registration;
import com.firemerald.additionalplacements.generation.RegistrationInitializer;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;

public class RegistrationImpl {
    private static List<RegistrationInitializer> registrators = new ArrayList<>();

    public static void addRegistration(RegistrationInitializer listener) {
        if (registrators == null) throw new IllegalStateException("A mod tried to register a registrator too late into the load sequence! Registrators should be registered in your mod constructor.");
        registrators.add(listener);
    }

    public static void registerTypes() {
        registrators.forEach(Registration::register);
        registrators = null;
    }

    public static <T extends Block, U extends AdditionalPlacementBlock<T>, V extends GenerationType<T, U>> void addBlacklisters(Class<T> clazz, V type) {
        registrators.forEach(registrator -> registrator.addBlacklisters(clazz, type, blacklister -> type.addBlacklister(blacklister)));
    }
}
