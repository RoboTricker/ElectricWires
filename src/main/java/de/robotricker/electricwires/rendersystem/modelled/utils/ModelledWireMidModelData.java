package de.robotricker.electricwires.rendersystem.modelled.utils;

import de.robotricker.electricwires.wires.WireColor;
import de.robotricker.electricwires.wires.WireType;
import de.robotricker.electricwires.wires.types.Wire;
import de.robotricker.transportpipes.pipes.PipeType;
import de.robotricker.transportpipes.pipes.colored.PipeColor;
import de.robotricker.transportpipes.pipes.types.ColoredPipe;
import de.robotricker.transportpipes.pipes.types.Pipe;

public class ModelledWireMidModelData {

	private WireType wireType;
	private WireColor coloredWire_wireColor;

	public ModelledWireMidModelData(WireType wireType) {
		this.wireType = wireType;
	}

	public ModelledWireMidModelData(WireType wireType, WireColor coloredWire_wireColor) {
		this(wireType);
		this.coloredWire_wireColor = coloredWire_wireColor;
	}

	public WireType getWireType() {
		return wireType;
	}

	public WireColor getColoredWire_wireColor() {
		return coloredWire_wireColor;
	}

	public static ModelledWireMidModelData createModelData(Wire wire) {
		switch (wire.getWireType()) {
		case COLORED:
			return new ModelledWireMidModelData(wire.getWireType(), ((ColoredWire) wire).getWireColor());
		default:
			return new ModelledWireMidModelData(wire.getWireType());
		}
	}

}
