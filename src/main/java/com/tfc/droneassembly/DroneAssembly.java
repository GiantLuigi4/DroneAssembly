package com.tfc.droneassembly;

import com.tfc.droneassembly.client.ClientSetup;
import com.tfc.droneassembly.client.CollisionDebugRenderer;
import com.tfc.droneassembly.common.ServerLifeCycleHandler;
import com.tfc.droneassembly.common.ServerTickHandler;
import com.tfc.droneassembly.registries.PartRegistryBuilder;
import com.tfc.droneassembly.registry.DronePartRegistry;
import com.tfc.droneassembly.registry.EntityRegistry;
import net.minecraft.entity.ai.attributes.GlobalEntityTypeAttributes;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("drone_assembly")
public class DroneAssembly {
	
	// Directly reference a log4j logger.
	private static final Logger LOGGER = LogManager.getLogger();
	
	public DroneAssembly() {
		EntityRegistry.ENTITIES.register(FMLJavaModLoadingContext.get().getModEventBus());
		DronePartRegistry.PARTS.register(FMLJavaModLoadingContext.get().getModEventBus());
		
		if (FMLEnvironment.dist.isClient()) {
			FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientSetup::onSetup);
			MinecraftForge.EVENT_BUS.addListener(CollisionDebugRenderer::renderWorldLast);
		}
		
		FMLJavaModLoadingContext.get().getModEventBus().addListener(DroneAssembly::createRegistries);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(DroneAssembly::onCommonSetup);
		
		MinecraftForge.EVENT_BUS.addListener(ServerLifeCycleHandler::onAboutToStart);
		MinecraftForge.EVENT_BUS.addListener(ServerLifeCycleHandler::onStarted);
		MinecraftForge.EVENT_BUS.addListener(ServerLifeCycleHandler::onStarting);
		MinecraftForge.EVENT_BUS.addListener(ServerLifeCycleHandler::onStopping);
		MinecraftForge.EVENT_BUS.addListener(ServerLifeCycleHandler::onStopped);
		MinecraftForge.EVENT_BUS.addListener(ServerTickHandler::onTick);
	}
	
	public static void createRegistries(RegistryEvent.NewRegistry event) {
		new PartRegistryBuilder().create();
	}
	
	public static void onCommonSetup(FMLCommonSetupEvent event) {
		event.enqueueWork(DroneAssembly::afterCommonSetup);
	}
	
	public static void afterCommonSetup() {
		GlobalEntityTypeAttributes.put(EntityRegistry.DroneEntity.get(), DroneEntity.createModifiers());
	}
}
