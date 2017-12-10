package de.robotricker.electricwires.rendersystem.modelled.utils;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import de.robotricker.electricwires.wires.WireType;
import de.robotricker.electricwires.wires.types.Wire;
import de.robotricker.transportpipes.pipes.Duct;
import de.robotricker.transportpipes.pipes.DuctType;
import de.robotricker.transportpipes.pipes.WrappedDirection;
import de.robotricker.transportpipes.pipes.types.Pipe;
import de.robotricker.transportpipes.pipeutils.hitbox.AxisAlignedBB;
import de.robotricker.transportpipes.protocol.ArmorStandData;
import de.robotricker.transportpipes.protocol.ArmorStandProtocol;
import de.robotricker.transportpipes.rendersystem.RenderSystem;
import de.robotricker.transportpipes.rendersystem.modelled.ModelledPipeModel;

public class ModelledWireRenderSystem extends RenderSystem {

	private Map<Wire, ArmorStandData> wireMidAsd = new HashMap<>();
	private Map<Wire, Map<WrappedDirection, ArmorStandData>> wireConnsAsd = new HashMap<>();
	private AxisAlignedBB wireMidAABB;
	private Map<WrappedDirection, AxisAlignedBB> wireConnsAABBs = new HashMap<>();

	private Map<WireType, ModelledPipeModel> wireModels = new HashMap<>();

	public ModelledWireRenderSystem(ArmorStandProtocol protocol) {
		super(protocol);
		
		wireMidAABB = new AxisAlignedBB(6d / 16d, 6d / 16d, 6d / 16d, 10d / 16d, 10d / 16d, 10d / 16d);
		wireConnsAABBs.put(WrappedDirection.NORTH, new AxisAlignedBB(6d / 16d, 6d / 16d, 0d / 16d, 10d / 16d, 10d / 16d, 6d / 16d));
		wireConnsAABBs.put(WrappedDirection.EAST, new AxisAlignedBB(10d / 16d, 6d / 16d, 6d / 16d, 16d / 16d, 10d / 16d, 10d / 16d));
		wireConnsAABBs.put(WrappedDirection.SOUTH, new AxisAlignedBB(6d / 16d, 6d / 16d, 10d / 16d, 10d / 16d, 10d / 16d, 16d / 16d));
		wireConnsAABBs.put(WrappedDirection.WEST, new AxisAlignedBB(0d / 16d, 6d / 16d, 6d / 16d, 6d / 16d, 10d / 16d, 10d / 16d));
		wireConnsAABBs.put(WrappedDirection.UP, new AxisAlignedBB(6d / 16d, 10d / 16d, 6d / 16d, 10d / 16d, 16d / 16d, 10d / 16d));
		wireConnsAABBs.put(WrappedDirection.DOWN, new AxisAlignedBB(6d / 16d, 0d / 16d, 6d / 16d, 10d / 16d, 6d / 16d, 10d / 16d));
	}

	@Override
	public void createDuctASD(Duct duct, Collection<WrappedDirection> allConnections) {

	}

	@Override
	public void updateDuctASD(Duct duct) {

	}

	@Override
	public void destroyDuctASD(Duct duct) {

	}

	@Override
	public List<ArmorStandData> getASDForDuct(Duct duct) {
		return null;
	}

	@Override
	public WrappedDirection getClickedDuctFace(Player player, Duct duct) {
		return null;
	}

	@Override
	public AxisAlignedBB getOuterHitbox(Duct duct) {
		return null;
	}

	@Override
	public void initPlayer(Player p) {

	}

	@Override
	public String getPipeRenderSystemName() {
		return null;
	}

	@Override
	public ItemStack getRepresentationItem() {
		return null;
	}

	@Override
	public int[] getRenderSystemIds() {
		return new int[]{0, 1};
	}
	
	@Override
	public DuctType getDuctType() {
		return DuctType.WIRE;
	}

}
