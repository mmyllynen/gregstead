package com.firemerald.additionalplacements.network.neoforge;

import com.firemerald.additionalplacements.network.CheckDataConfigurationTask;
import com.firemerald.additionalplacements.network.client.neoforge.CheckDataClientPacketImpl;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.configuration.ICustomConfigurationTask;

import java.util.function.Consumer;

public class CheckDataConfigurationTaskImpl extends CheckDataConfigurationTask implements ICustomConfigurationTask {
    @Override
    public void run(Consumer<CustomPacketPayload> sender) {
        sender.accept(new CheckDataClientPacketImpl());
    }
}
