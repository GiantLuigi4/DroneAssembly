package com.tfc.droneassembly.common;

import com.tfc.droneassembly.BulletPhysicsWorldCache;
import net.minecraftforge.event.TickEvent;

public class ServerTickHandler {
	public static void onTick(TickEvent.ServerTickEvent event) {
		BulletPhysicsWorldCache.tick();
//		System.out.println("tick");
	}
}
