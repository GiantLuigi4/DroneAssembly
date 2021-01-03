package com.tfc.droneassembly.registries;

import com.tfc.droneassembly.parts.DronePart;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.RegistryBuilder;

public class PartRegistryBuilder extends RegistryBuilder<DronePart> {
	public PartRegistryBuilder() {
		onAdd((owner, stage, id, obj, oldObj) -> DroneParts.register(obj.getRegistryName(), obj));
		onClear((owner, stage) -> DroneParts.clear());
		onCreate((owner, state) -> {
		});
		onBake(((owner, stage) -> {
		}));
		onValidate((owner, stage, id, key, obj) -> {
		});
		setType(DronePart.class);
		allowModification();
		tagFolder("drone_parts");
		setDefaultKey(new ResourceLocation("unknown:null"));
		missing((name, isNetwork) -> null);
		setName(new ResourceLocation("drone_assembly:parts"));
		disableSync();
	}
}
