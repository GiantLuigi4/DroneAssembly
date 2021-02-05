package com.tfc.droneassembly.common;

import com.tfc.droneassembly.BulletPhysicsWorldCache;
import net.minecraftforge.fml.event.server.*;

public class ServerLifeCycleHandler {
	public static void onAboutToStart(FMLServerAboutToStartEvent event) {
		handledStop = false;
	}
	
	public static void onStarted(FMLServerStartedEvent event) {
		handledStop = false;
	}
	
	public static void onStarting(FMLServerStartingEvent event) {
		handledStop = false;
	}
	
	public static void onStopping(FMLServerStoppingEvent event) {
		if (!handledStop)
			BulletPhysicsWorldCache.INSTANCE.clear();
		handledStop = true;
	}
	
	public static void onStopped(FMLServerStoppedEvent event) {
		if (!handledStop)
			BulletPhysicsWorldCache.INSTANCE.clear();
		handledStop = true;
	}
	
	private static boolean handledStop = false;
}
