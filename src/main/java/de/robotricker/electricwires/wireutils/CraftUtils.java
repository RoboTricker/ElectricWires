package de.robotricker.electricwires.wireutils;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

import de.robotricker.electricwires.ElectricWires;
import de.robotricker.electricwires.wires.WireColor;
import de.robotricker.electricwires.wires.WireType;
import de.robotricker.transportpipes.pipeutils.PipeItemUtils;

public class CraftUtils implements Listener {

	public static void initRecipes() {
		if (!ElectricWires.instance.generalConf.isCraftingEnabled()) {
			return;
		}
		for (WireType wt : WireType.values()) {
			Bukkit.addRecipe(ElectricWires.instance.recipesConf.createWireRecipe(wt, null));
			if (wt == WireType.COLORED) {
				for (WireColor wc : WireColor.values()) {
					Bukkit.addRecipe(ElectricWires.instance.recipesConf.createWireRecipe(wt, wc));
				}
			}
		}
	}

	@EventHandler
	public void onPrepareCraft(PrepareItemCraftEvent e) {
		Recipe r = e.getInventory().getRecipe();
		if (r == null || e.getViewers().size() != 1) {
			return;
		}

		Player viewer = (Player) e.getViewers().get(0);

		if (r.getResult() != null) {
			WireType wt = WireType.getFromWireItem(r.getResult());
			if (wt != null) {
				if (!viewer.hasPermission(wt.getCraftPermission())) {
					e.getInventory().setResult(null);
					return;
				}
			} else if (PipeItemUtils.isItemStackWrench(r.getResult())) {
				if (!viewer.hasPermission("transportpipes.craft.wrench")) {
					e.getInventory().setResult(null);
					return;
				}
			}
		}

		// prevent colored pipe crafting if the given pipe is not a colored pipe
		if (WireType.getFromWireItem(r.getResult()) != null) {
			boolean prevent = false;
			for (int i = 1; i < 10; i++) {
				ItemStack is = e.getInventory().getItem(i);
				if (is != null && is.getType() == Material.SKULL_ITEM && is.getDurability() == SkullType.PLAYER.ordinal()) {
					prevent |= WireType.getFromWireItem(is) == null;
				}
			}
			if (prevent) {
				e.getInventory().setResult(null);
			}
		}
	}

}
