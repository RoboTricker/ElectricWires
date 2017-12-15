package de.robotricker.electricwires.rendersystem.modelled.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import de.robotricker.electricwires.duct.wire.Wire;
import de.robotricker.electricwires.duct.wire.utils.WireType;
import de.robotricker.electricwires.rendersystem.modelled.ModelledWireCOLOREDModel;
import de.robotricker.electricwires.rendersystem.modelled.ModelledWireModel;
import de.robotricker.transportpipes.duct.Duct;
import de.robotricker.transportpipes.duct.DuctType;
import de.robotricker.transportpipes.duct.pipe.Pipe;
import de.robotricker.transportpipes.protocol.ArmorStandData;
import de.robotricker.transportpipes.protocol.DuctManager;
import de.robotricker.transportpipes.rendersystem.RenderSystem;
import de.robotricker.transportpipes.rendersystem.modelled.ModelledPipeModel;
import de.robotricker.transportpipes.rendersystem.modelled.utils.ModelledPipeConnModelData;
import de.robotricker.transportpipes.rendersystem.modelled.utils.ModelledPipeMidModelData;
import de.robotricker.transportpipes.utils.WrappedDirection;
import de.robotricker.transportpipes.utils.hitbox.AxisAlignedBB;
import de.robotricker.transportpipes.utils.staticutils.ProtocolUtils;

public class ModelledWireRenderSystem extends RenderSystem {

	private Map<Wire, ArmorStandData> midAsd = new HashMap<>();
	private Map<Wire, Map<WrappedDirection, ArmorStandData>> connsAsd = new HashMap<>();
	private AxisAlignedBB midAABB;
	private Map<WrappedDirection, AxisAlignedBB> connsAABBs = new HashMap<>();

	private Map<WireType, ModelledWireModel> models = new HashMap<>();

	public ModelledWireRenderSystem(DuctManager ductManager) {
		super(ductManager);
		models.put(WireType.COLORED, new ModelledWireCOLOREDModel());
		
		midAABB = new AxisAlignedBB(6d / 16d, 6d / 16d, 6d / 16d, 10d / 16d, 10d / 16d, 10d / 16d);
		connsAABBs.put(WrappedDirection.NORTH, new AxisAlignedBB(6d / 16d, 6d / 16d, 0d / 16d, 10d / 16d, 10d / 16d, 6d / 16d));
		connsAABBs.put(WrappedDirection.EAST, new AxisAlignedBB(10d / 16d, 6d / 16d, 6d / 16d, 16d / 16d, 10d / 16d, 10d / 16d));
		connsAABBs.put(WrappedDirection.SOUTH, new AxisAlignedBB(6d / 16d, 6d / 16d, 10d / 16d, 10d / 16d, 10d / 16d, 16d / 16d));
		connsAABBs.put(WrappedDirection.WEST, new AxisAlignedBB(0d / 16d, 6d / 16d, 6d / 16d, 6d / 16d, 10d / 16d, 10d / 16d));
		connsAABBs.put(WrappedDirection.UP, new AxisAlignedBB(6d / 16d, 10d / 16d, 6d / 16d, 10d / 16d, 16d / 16d, 10d / 16d));
		connsAABBs.put(WrappedDirection.DOWN, new AxisAlignedBB(6d / 16d, 0d / 16d, 6d / 16d, 10d / 16d, 6d / 16d, 10d / 16d));
	}

	@Override
	public void createDuctASD(Duct duct, Collection<WrappedDirection> allConnections) {
		if (midAsd.containsKey(duct)) {
			return;
		}
		Wire wire = (Wire) duct;

		ModelledWireModel model = models.get(wire.getWireType());
		midAsd.put(wire, model.createMidASD(ModelledWireMidModelData.createModelData(wire)));
		Map<WrappedDirection, ArmorStandData> connsMap = new HashMap<>();
		connsAsd.put(wire, connsMap);
		for (WrappedDirection conn : allConnections) {
			connsMap.put(conn, model.createConnASD(ModelledWireConnModelData.createModelData(wire, conn)));
		}
	}

	@Override
	public void updateDuctASD(Duct duct) {
		Wire wire = (Wire) duct;
		
		if (!midAsd.containsKey(wire) || !connsAsd.containsKey(wire) || connsAsd.get(wire) == null) {
			return;
		}

		List<ArmorStandData> removedASD = new ArrayList<>();
		List<ArmorStandData> addedASD = new ArrayList<>();

		Map<WrappedDirection, ArmorStandData> connsMap = connsAsd.get(wire);
		ModelledWireModel model = models.get(wire.getWireType());

		Collection<WrappedDirection> newConns = wire.getAllConnections();
		for (WrappedDirection pd : WrappedDirection.values()) {
			if (connsMap.containsKey(pd) && newConns.contains(pd)) {
				// direction was active before and after update
				ArmorStandData newASD = model.createConnASD(ModelledWireConnModelData.createModelData(wire, pd));
				if (!connsMap.get(pd).isSimilar(newASD)) {
					// ASD changed after update in this direction
					removedASD.add(connsMap.get(pd));
					addedASD.add(newASD);
					connsMap.put(pd, newASD);
				}
			} else if (!connsMap.containsKey(pd) && newConns.contains(pd)) {
				// direction wasn't active before update but direction is active after update
				ArmorStandData newASD = model.createConnASD(ModelledWireConnModelData.createModelData(wire, pd));
				addedASD.add(newASD);
				connsMap.put(pd, newASD);
			} else if (connsMap.containsKey(pd) && !newConns.contains(pd)) {
				// direction was active before update but isn't active after update
				removedASD.add(connsMap.get(pd));
				connsMap.remove(pd);
			}
		}

		// SEND TO CLIENTS
		List<Player> players = ductManager.getAllPlayersWithRenderSystem(this);
		int[] removedIds = ProtocolUtils.convertArmorStandListToEntityIdArray(removedASD);
		for (Player p : players) {
			ductManager.getProtocol().removeArmorStandDatas(p, removedIds);
			ductManager.getProtocol().sendArmorStandDatas(p, wire.getBlockLoc(), addedASD);
		}
	}

	@Override
	public void destroyDuctASD(Duct duct) {
		Wire wire = (Wire) duct;
		
		if (!midAsd.containsKey(wire) || !connsAsd.containsKey(wire) || connsAsd.get(wire) == null) {
			return;
		}
		midAsd.remove(wire);
		connsAsd.remove(wire);
	}

	@Override
	public List<ArmorStandData> getASDForDuct(Duct duct) {
		Wire wire = (Wire) duct;
		
		List<ArmorStandData> ASD = new ArrayList<>();
		if (midAsd.containsKey(wire)) {
			ASD.add(midAsd.get(wire));
		}
		if (connsAsd.containsKey(wire)) {
			ASD.addAll(connsAsd.get(wire).values());
		}
		return ASD;
	}

	@Override
	public WrappedDirection getClickedDuctFace(Player player, Duct duct) {
		if (duct == null) {
			return null;
		}
		Wire wire = (Wire) duct;

		Vector ray = player.getEyeLocation().getDirection();
		Vector origin = player.getEyeLocation().toVector();

		Collection<WrappedDirection> wireConns = wire.getAllConnections();
		WrappedDirection clickedMidFace = midAABB.rayIntersection(ray, origin, wire.getBlockLoc());
		if (clickedMidFace != null && !wireConns.contains(clickedMidFace)) {
			return clickedMidFace;
		} else {
			double nearestDistanceSquared = Double.MAX_VALUE;
			WrappedDirection currentClickedConnFace = null;
			for (WrappedDirection pd : wireConns) {
				AxisAlignedBB connAABB = connsAABBs.get(pd);
				double newDistanceSquared = connAABB.getAABBMiddle(wire.getBlockLoc()).distanceSquared(origin);
				if (newDistanceSquared < nearestDistanceSquared) {
					WrappedDirection clickedConnFace = connAABB.rayIntersection(ray, origin, wire.getBlockLoc());
					if (clickedConnFace != null) {
						nearestDistanceSquared = newDistanceSquared;
						currentClickedConnFace = clickedConnFace;
					}
				}
			}
			return currentClickedConnFace;
		}
	}

	@Override
	public AxisAlignedBB getOuterHitbox(Duct duct) {
		if (duct == null) {
			return null;
		}
		Wire wire = (Wire) duct;

		List<AxisAlignedBB> aabbs = new ArrayList<AxisAlignedBB>();
		aabbs.add(midAABB);
		Collection<WrappedDirection> pipeConns = wire.getAllConnections();
		for (WrappedDirection pd : pipeConns) {
			aabbs.add(connsAABBs.get(pd));
		}
		double minx = Double.MAX_VALUE;
		double miny = Double.MAX_VALUE;
		double minz = Double.MAX_VALUE;
		double maxx = Double.MIN_VALUE;
		double maxy = Double.MIN_VALUE;
		double maxz = Double.MIN_VALUE;
		for (AxisAlignedBB aabb : aabbs) {
			minx = Math.min(aabb.minx, minx);
			miny = Math.min(aabb.miny, miny);
			minz = Math.min(aabb.minz, minz);
			maxx = Math.max(aabb.maxx, maxx);
			maxy = Math.max(aabb.maxy, maxy);
			maxz = Math.max(aabb.maxz, maxz);
		}
		return new AxisAlignedBB(minx, miny, minz, maxx, maxy, maxz);
	}

	@Override
	public void initPlayer(Player p) {

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
