package de.robotricker.electricwires;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;

import com.comphenix.protocol.wrappers.EnumWrappers.Particle;
import com.logisticscraft.logisticsapi.LogisticsApi;
import com.logisticscraft.logisticsapi.block.LogisticBlock;
import com.logisticscraft.logisticsapi.data.LogisticBlockFace;
import com.logisticscraft.logisticsapi.energy.EnergyInput;
import com.logisticscraft.logisticsapi.energy.EnergyOutput;
import com.logisticscraft.logisticsapi.energy.EnergyStorage;

import de.robotricker.electricwires.duct.wire.Wire;
import de.robotricker.electricwires.utils.staticutils.ProtocolUtils;
import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.utils.WrappedDirection;

public class WireNetwork {

	private long networkCharge;
	private long maxCharge;
	private World world;
	private Set<Wire> wires;
	private Set<EnergyOutput> outputBlocks;
	private Set<EnergyInput> inputBlocks;

	public WireNetwork(World world) {
		this.world = world;
		this.wires = new HashSet<>();
		this.outputBlocks = new HashSet<>();
		this.inputBlocks = new HashSet<>();
	}

	public World getWorld() {
		return world;
	}

	public void update() {
		// take energy
		if (networkCharge < maxCharge) {
			for (EnergyOutput output : outputBlocks) {
				long chargeUp = maxCharge - networkCharge;
				if (chargeUp > output.getMaxEnergyExtract()) {
					chargeUp = output.getMaxEnergyExtract();
				}
				networkCharge += output.extractEnergy(LogisticBlockFace.NORTH, chargeUp, false);
				if (networkCharge >= maxCharge) {
					break;
				}
			}
		}
		// put energy
		if (networkCharge > 0) {
			for (final Wire wire : wires) {
				Bukkit.getScheduler().runTask(TransportPipes.instance, new Runnable() {

					@Override
					public void run() {
						spawnParticleForWire(wire);
					}
				});
			}
			int avgEnergy = (int) (networkCharge / inputBlocks.size());
			int overflow = (int) networkCharge % inputBlocks.size();
			int i = 0;
			for (EnergyInput input : inputBlocks) {
				networkCharge -= input.receiveEnergy(LogisticBlockFace.NORTH, avgEnergy + (i == 0 ? overflow : 0), false);
				if (networkCharge <= 0) {
					break;
				}
				i++;
			}
		}
		//remove energy if no energystorage blocks are connected
		if(inputBlocks.isEmpty() && outputBlocks.isEmpty()) {
			networkCharge = Math.max(0, networkCharge - 50);
		}

	}

	public void addWire(Wire wire) {
		if (wires.add(wire)) {
			updateEnergyStorages();
			calcMaxCharge();
		}
	}

	public void addWireSilent(Wire wire) {
		wires.add(wire);
	}

	public void removeWire(Wire wire) {
		if (wires.remove(wire)) {
			updateEnergyStorages();
			calcMaxCharge();
		}
	}

	public Set<Wire> getWires() {
		return wires;
	}

	public void calcMaxCharge() {
		maxCharge = wires.size() * 10;
		if (networkCharge > maxCharge) {
			networkCharge = maxCharge;
		}
	}

	public void updateEnergyStorages() {
		inputBlocks.clear();
		outputBlocks.clear();
		Map<Chunk, Map<Location, LogisticBlock>> lbMap = LogisticsApi.getInstance().getBlockManager().getPlacedBlocks();
		synchronized (lbMap) {
			for (Chunk lbChunk : lbMap.keySet()) {
				if (!lbChunk.getWorld().equals(world)) {
					continue;
				}
				for (Location lbLoc : lbMap.get(lbChunk).keySet()) {
					LogisticBlock lb = lbMap.get(lbChunk).get(lbLoc);
					if (lb instanceof EnergyStorage) {
						boolean connected = false;
						dirLoop: for (WrappedDirection dir : WrappedDirection.values()) {
							Location newLoc = new Location(world, lbLoc.getBlockX() + dir.getX(), lbLoc.getBlockY() + dir.getY(), lbLoc.getBlockZ() + dir.getZ());
							for (Wire wire : wires) {
								if (wire.getBlockLoc().equals(newLoc)) {
									connected = true;
									break dirLoop;
								}
							}
						}
						if (connected) {
							if (lb instanceof EnergyInput) {
								inputBlocks.add((EnergyInput) lb);
							}
							if (lb instanceof EnergyOutput) {
								outputBlocks.add((EnergyOutput) lb);
							}
						}
					}
				}
			}
		}
	}

	private void spawnParticleForWire(Wire wire) {
		Collection<WrappedDirection> connections = wire.getAllConnections();
		float xD = 0f;
		float yD = 0f;
		float zD = 0f;
		if (connections.size() == 2 && connections.contains(WrappedDirection.NORTH) && connections.contains(WrappedDirection.SOUTH)) {
			zD = 0.23f;
		} else if (connections.size() == 2 && connections.contains(WrappedDirection.EAST) && connections.contains(WrappedDirection.WEST)) {
			xD = 0.23f;
		} else if (connections.size() == 2 && connections.contains(WrappedDirection.UP) && connections.contains(WrappedDirection.DOWN)) {
			yD = 0.23f;
		}
		ProtocolUtils.createParticle(Particle.REDSTONE, world, (float) (wire.getBlockLoc().getBlockX() + 0.5), (float) (wire.getBlockLoc().getBlockY() + 0.5), (float) (wire.getBlockLoc().getBlockZ() + 0.5), xD, yD, zD, 0, new int[0], false, 3);
	}

}
