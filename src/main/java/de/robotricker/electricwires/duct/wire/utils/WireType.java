package de.robotricker.electricwires.duct.wire.utils;

import de.robotricker.electricwires.config.LocConf;

public enum WireType {

	COLORED(0, "", LocConf.WIRES_COLORED, "electricwires.craft.coloredwire");

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

	public static WireType getFromId(int id) {
		for (WireType pt : WireType.values()) {
			if (pt.getId() == id) {
				return pt;
			}
		}
		return null;
	}

}
