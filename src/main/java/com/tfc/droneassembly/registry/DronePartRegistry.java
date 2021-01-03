package com.tfc.droneassembly.registry;

import com.tfc.droneassembly.parts.CorePart;
import com.tfc.droneassembly.parts.DronePart;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

public class DronePartRegistry {
	public static final DeferredRegister<DronePart> PARTS = DeferredRegister.create(DronePart.class, "drone_assembly");
	
	public static final RegistryObject<DronePart> BASIC_PART = PARTS.register("basic_part", DronePart::new);
	public static final RegistryObject<DronePart> DRONE_CORE = PARTS.register("drone_core", CorePart::new);
}
