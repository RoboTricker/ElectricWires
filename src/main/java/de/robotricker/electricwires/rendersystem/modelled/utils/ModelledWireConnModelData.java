package de.robotricker.electricwires.rendersystem.modelled.utils;

import de.robotricker.electricwires.wires.WireColor;
import de.robotricker.electricwires.wires.WireType;
import de.robotricker.electricwires.wires.types.Wire;
import de.robotricker.transportpipes.pipes.WrappedDirection;
import de.robotricker.transportpipes.pipes.PipeType;
import de.robotricker.transportpipes.pipes.colored.PipeColor;
import de.robotricker.transportpipes.pipes.goldenpipe.GoldenPipeColor;
import de.robotricker.transportpipes.pipes.types.ColoredPipe;
import de.robotricker.transportpipes.pipes.types.ExtractionPipe;
import de.robotricker.transportpipes.pipes.types.IronPipe;
import de.robotricker.transportpipes.pipes.types.Pipe;

public class ModelledWireConnModelData {

	private WireType wireType;
	private WrappedDirection connDirection;
	private WireColor coloredWire_wireColor;

	public ModelledWireConnModelData(WireType pipeType, WrappedDirection connDirection) {
		this.wireType = pipeType;
		this.connDirection = connDirection;
	}

	public ModelledWireConnModelData(WireType wireType, WrappedDirection connDirection, WireColor coloredWire_wireColor) {
		this(wireType, connDirection);
		this.coloredWire_wireColor = coloredWire_wireColor;
	}

	public WireType getWireType() {
		return wireType;
	}

	public WrappedDirection getConnDirection() {
		return connDirection;
	}

	public WireColor getColoredWire_wireColor() {
		return coloredWire_wireColor;
	}

	public static ModelledWireConnModelData createModelData(Wire wire, WrappedDirection connDirection) {
		switch (wire.getWireType()) {
		case COLORED:
			return new ModelledWireConnModelData(wire.getWireType(), connDirection, ((ColoredWire) wire).getWireColor());
		default:
			return new ModelledWireConnModelData(wire.getWireType(), connDirection);
		}
	}

}
