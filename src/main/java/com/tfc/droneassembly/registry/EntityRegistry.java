package com.tfc.droneassembly.registry;

import com.tfc.assortedutils.entities.EntityRegistryHelper;
import com.tfc.droneassembly.DroneEntity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class EntityRegistry {
	public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITIES, "drone_assembly");
	
	public static final RegistryObject<EntityType<DroneEntity>> DroneEntity = ENTITIES.register("drone_entity",
			() ->
					EntityType.Builder.create(com.tfc.droneassembly.DroneEntity::new, EntityClassification.CREATURE)
							.setTrackingRange(64)
							.setUpdateInterval(1)
							.size(0.125f, 0.125f)
							.build("drone_assembly:drone_entity")
	);
}
