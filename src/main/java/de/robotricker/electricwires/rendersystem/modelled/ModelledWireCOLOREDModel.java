package de.robotricker.electricwires.rendersystem.modelled;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import de.robotricker.electricwires.rendersystem.modelled.utils.ModelledWireConnModelData;
import de.robotricker.electricwires.rendersystem.modelled.utils.ModelledWireMidModelData;
import de.robotricker.transportpipes.pipeitems.RelLoc;
import de.robotricker.transportpipes.pipes.WrappedDirection;
import de.robotricker.transportpipes.protocol.ArmorStandData;
import de.robotricker.transportpipes.rendersystem.modelled.utils.ModelledPipeConnModelData;
import de.robotricker.transportpipes.rendersystem.modelled.utils.ModelledPipeMidModelData;

public class ModelledWireCOLOREDModel extends ModelledWireModel {

	@Override
	public ArmorStandData createMidASD(ModelledWireMidModelData data) {
		ItemStack item = data.getColoredWire_wireColor().getModelledModel_MidItem();

		return new ArmorStandData(new RelLoc(0.5f, 0.5f - 1.1875f, 0.5f), new Vector(1, 0, 0), false, item, null, new Vector(180f, 0f, 0f), new Vector(0f, 0f, 0f));
	}

	@Override
	public ArmorStandData createConnASD(ModelledWireConnModelData data) {
		ItemStack item = data.getColoredWire_wireColor().getModelledModel_ConnItem();
		ArmorStandData asd;

		if (data.getConnDirection() == WrappedDirection.UP) {
			asd = new ArmorStandData(new RelLoc(0.75f, 0.5f - 1.4369f, 0.5f), new Vector(1, 0, 0), false, item, null, new Vector(-90f, 0f, 0f), new Vector(0f, 0f, 0f));
		} else if (data.getConnDirection() == WrappedDirection.DOWN) {
			asd = new ArmorStandData(new RelLoc(0.25f, 0.5f - 1.1885f - 0.25f, 0.5f), new Vector(1, 0, 0), false, item, null, new Vector(90f, 0f, 0f), new Vector(0f, 0f, 0f));
		} else {
			asd = new ArmorStandData(new RelLoc(0.5f, 0.5f - 1.1875f, 0.5f), new Vector(data.getConnDirection().getX(), 0, data.getConnDirection().getZ()), false, item, null, new Vector(180f, 180f, 0f), new Vector(0f, 0f, 0f));
		}

		return asd;
	}

}
