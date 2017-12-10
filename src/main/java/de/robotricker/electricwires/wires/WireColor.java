package de.robotricker.electricwires.wires;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import de.robotricker.transportpipes.pipeutils.InventoryUtils;

public enum WireColor {

	WHITE("§f", (short) 40, (short) 41, (short) 15),
	BLUE("§1", (short) 40, (short) 41, (short) 4),
	RED("§4", (short) 40, (short) 41, (short) 1),
	YELLOW("§e", (short) 40, (short) 41, (short) 11),
	GREEN("§2", (short) 40, (short) 41, (short) 2),
	BLACK("§8", (short) 40, (short) 41, (short) 0);

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

	public static WireColor getPipeColorByPipeItem(ItemStack item) {
		if (item == null) {
			return null;
		}
		if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName() || item.getItemMeta().getDisplayName().length() <= 2) {
			return null;
		}
		if (item.hasItemMeta() && item.getItemMeta().hasDisplayName() && item.getItemMeta().getDisplayName().substring(2).equalsIgnoreCase(WireType.COLORED.getFormattedWireName())) {
			for (WireColor wireColor : WireColor.values()) {
				if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
					if (item.getItemMeta().getDisplayName().startsWith(wireColor.getColorCode())) {
						return wireColor;
					}
				}
			}
		}
		return null;
	}

}
