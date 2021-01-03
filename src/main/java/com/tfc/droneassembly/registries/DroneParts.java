package com.tfc.droneassembly.registries;

import net.minecraft.util.ResourceLocation;
import com.tfc.droneassembly.parts.DronePart;

import java.util.HashMap;

public class DroneParts {
	private static final HashMap<ResourceLocation,DronePart> parts = new HashMap<>();
	
	protected static void register(ResourceLocation name, DronePart part) {
		if (part.getRegistryName() == null) parts.put(name,part.setRegistryName(name));
		else parts.put(name,part);
	}
	
	protected static void clear() {
		parts.clear();
	}
	
	public static DronePart get(String name) {
		return parts.get(new ResourceLocation(name));
	}
}
