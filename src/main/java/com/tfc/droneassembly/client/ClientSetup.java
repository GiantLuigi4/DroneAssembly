package com.tfc.droneassembly.client;

import com.tfc.droneassembly.registry.EntityRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class ClientSetup {
	public static void onSetup(FMLClientSetupEvent event) {
		RenderingRegistry.registerEntityRenderingHandler(EntityRegistry.DroneEntity.get(), DroneRenderer::new);
	}
}
