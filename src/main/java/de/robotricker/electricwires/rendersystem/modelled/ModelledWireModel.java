package de.robotricker.electricwires.rendersystem.modelled;

import de.robotricker.electricwires.rendersystem.modelled.utils.ModelledWireConnModelData;
import de.robotricker.electricwires.rendersystem.modelled.utils.ModelledWireMidModelData;
import de.robotricker.transportpipes.protocol.ArmorStandData;
import de.robotricker.transportpipes.rendersystem.Model;

public abstract class ModelledWireModel extends Model {

	public abstract ArmorStandData createMidASD(ModelledWireMidModelData data);

	public abstract ArmorStandData createConnASD(ModelledWireConnModelData data);

}
