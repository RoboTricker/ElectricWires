package de.robotricker.electricwires;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

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
import de.robotricker.transportpipes.utils.WrappedDirection;

public class WireNetwork {

	private long networkCharge;
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

	public long getNetworkCharge() {
		return networkCharge;
	}

	public void update() {
		int internalStorage = 0;
		int transferRate = 100;

		// calculate needed energy
		int neededEnergy = 0;
		for (EnergyInput ei : inputBlocks) {
			int putRate = (int) ei.getMaxEnergyReceive();
			neededEnergy += Math.min(Math.min(putRate, transferRate), ei.getFreeSpace());
		}

		// calculate extracted energy
		Map<EnergyOutput, AtomicInteger> energyTaken = new HashMap<>();
		int totalEnergyTaken = 0;
		while (totalEnergyTaken < neededEnergy) {
			boolean extracted = false;
			for (EnergyOutput eo : outputBlocks) {
				int extractRate = (int) eo.getMaxEnergyExtract();
				int takeEnergy = Math.min(Math.min(extractRate, transferRate), (int) eo.getStoredEnergy());
				int alreadyTakenEnergy = energyTaken.computeIfAbsent(eo, (t) -> new AtomicInteger(0)).get();
				if (alreadyTakenEnergy < takeEnergy && totalEnergyTaken < neededEnergy) {
					energyTaken.get(eo).incrementAndGet();
					totalEnergyTaken++;
					extracted = true;
				}
			}
			// there is the need to extract more energy but all EnergyOutputs are empty
			if (!extracted) {
				break;
			}
		}

		// calculate put energy
		Map<EnergyInput, AtomicInteger> energyPut = new HashMap<>();
		int totalEnergyPut = 0;
		while (totalEnergyPut < internalStorage) {
			boolean put = false;
			for (EnergyInput ei : inputBlocks) {
				int putRate = (int) ei.getMaxEnergyReceive();
				int putEnergy = Math.min(Math.min(putRate, transferRate), (int) ei.getFreeSpace());
				int alreadyPutEnergy = energyPut.computeIfAbsent(ei, (t) -> new AtomicInteger(0)).get();
				if (alreadyPutEnergy < putEnergy && totalEnergyPut < internalStorage) {
					energyPut.get(ei).incrementAndGet();
					totalEnergyPut++;
					put = true;
				}
			}
			// there is the need to extract more energy but all EnergyOutputs are empty
			if (!put) {
				break;
			}
		}

		for (EnergyInput e : inputBlocks) {
			if (e instanceof EnergyOutput) {
				int putEnergy = energyPut.containsKey(e) ? energyPut.get(e).get() : 0;
				int takeEnergy = energyTaken.containsKey(e) ? energyTaken.get(e).get() : 0;
				if (putEnergy > takeEnergy) {
					energyPut.computeIfAbsent(e, k -> new AtomicInteger(0)).addAndGet(-takeEnergy);
					energyTaken.computeIfAbsent((EnergyOutput) e, k -> new AtomicInteger(0)).set(0);
				} else {
					energyTaken.computeIfAbsent((EnergyOutput) e, k -> new AtomicInteger(0)).addAndGet(-putEnergy);
					energyPut.computeIfAbsent(e, k -> new AtomicInteger(0)).set(0);
				}
			}
		}

		// extract energy
		for (EnergyOutput eo : outputBlocks) {
			if (energyTaken.containsKey(eo)) {
				internalStorage += eo.extractEnergy(LogisticBlockFace.NORTH, energyTaken.get(eo).get(), false);
			}
		}

		networkCharge = internalStorage;

		// put energy
		for (EnergyInput ei : inputBlocks) {
			if (energyPut.containsKey(ei)) {
				ei.receiveEnergy(LogisticBlockFace.NORTH, energyPut.get(ei).get(), false);
			}
		}

		if (networkCharge > 0) {
			for (final Wire wire : wires) {
				ElectricWires.runTask(new Runnable() {

					@Override
					public void run() {
						spawnParticleForWire(wire);
					}
				});
			}
		}
	}

	public void addWire(Wire wire) {
		if (wires.add(wire)) {
			updateEnergyStorages();
		}
	}

	public void addWireSilent(Wire wire) {
		wires.add(wire);
	}

	public void removeWire(Wire wire) {
		if (wires.remove(wire)) {
			updateEnergyStorages();
		}
	}

	public Set<Wire> getWires() {
		return wires;
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
