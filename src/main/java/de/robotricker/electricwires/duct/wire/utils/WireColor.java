package de.robotricker.electricwires.duct.wire.utils;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import de.robotricker.transportpipes.utils.staticutils.InventoryUtils;

public enum WireColor {

	WHITE("§f", (short) 40, (short) 41, (short) 15);

	private String colorCode;
	private ItemStack modelledModel_midItem;
	private ItemStack modelledModel_connItem;
	private ItemStack dyeItem;

	WireColor(String colorCode, short midMetadata, short connMetadata, short dyeMetadata) {
		this.colorCode = colorCode;
		modelledModel_midItem = InventoryUtils.createToolItemStack(midMetadata);
		modelledModel_connItem = InventoryUtils.createToolItemStack(connMetadata);
		dyeItem = new ItemStack(Material.INK_SACK, 1, dyeMetadata);
	}

	public String getColorCode() {
		return colorCode;
	}

	public ItemStack getModelledModel_MidItem() {
		return modelledModel_midItem;
	}

	public ItemStack getModelledModel_ConnItem() {
		return modelledModel_connItem;
	}

	public ItemStack getDyeItem() {
		return dyeItem;
	}

}
