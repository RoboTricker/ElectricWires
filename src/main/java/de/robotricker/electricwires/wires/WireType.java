package de.robotricker.electricwires.wires;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import de.robotricker.electricwires.wires.types.Wire;
import de.robotricker.transportpipes.pipes.colored.PipeColor;
import de.robotricker.transportpipes.pipes.types.ColoredPipe;
import de.robotricker.transportpipes.pipes.types.ExtractionPipe;
import de.robotricker.transportpipes.pipes.types.GoldenPipe;
import de.robotricker.transportpipes.pipes.types.IcePipe;
import de.robotricker.transportpipes.pipes.types.IronPipe;
import de.robotricker.transportpipes.pipes.types.Pipe;
import de.robotricker.transportpipes.pipes.types.VoidPipe;
import de.robotricker.transportpipes.pipeutils.config.LocConf;

public enum WireType {

	COLORED(0, "", LocConf.PIPES_COLORED, "electricwires.craft.coloredwire");

	private int id;
	private String wireName_colorCode;
	private String wireName_locConfKey;
	private String craft_permission;

	WireType(int id, String wireName_colorCode, String wireName_locConfKey, String craft_permission) {
		this.id = id;
		this.wireName_colorCode = wireName_colorCode;
		this.wireName_locConfKey = wireName_locConfKey;
		this.craft_permission = craft_permission;
	}

	public int getId() {
		return id;
	}

	public String getFormattedWireName() {
		return wireName_colorCode + LocConf.load(wireName_locConfKey);
	}
	
	public String getCraftPermission() {
		return craft_permission;
	}

	public Wire createWire(Location blockLoc, WireColor wc) {
		if (this == COLORED) {
			return new ColoredWire(blockLoc, wc);
		}
		return null;
	}

	public static WireType getFromId(int id) {
		for (WireType pt : WireType.values()) {
			if (pt.getId() == id) {
				return pt;
			}
		}
		return null;
	}

	/**
	 * returns the pipeType you can place with this item, or null if there is no pipe available for this item
	 */
	public static WireType getFromWireItem(ItemStack item) {
		if (item == null) {
			return null;
		}
		if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
			String displayName = item.getItemMeta().getDisplayName();
			if (WireColor.getWireColorByWireItem(item) != null) {
				return WireType.COLORED;
			}
			for (WireType pt : WireType.values()) {
				if (displayName.equals(pt.getFormattedWireName())) {
					return pt;
				}
			}
		}
		return null;
	}

}
