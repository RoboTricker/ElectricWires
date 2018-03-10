package de.robotricker.electricwires.config;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.SkullType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.material.MaterialData;

import de.robotricker.electricwires.ElectricWires;
import de.robotricker.electricwires.duct.wire.utils.WireColor;
import de.robotricker.electricwires.duct.wire.utils.WireType;
import de.robotricker.electricwires.utils.ductdetails.WireDetails;
import de.robotricker.transportpipes.TransportPipes;
import de.robotricker.transportpipes.duct.pipe.utils.PipeColor;
import de.robotricker.transportpipes.duct.pipe.utils.PipeType;
import de.robotricker.transportpipes.utils.config.Conf;
import de.robotricker.transportpipes.utils.crafting.TPShapedRecipe;
import de.robotricker.transportpipes.utils.crafting.TPShapelessRecipe;
import de.robotricker.transportpipes.utils.ductdetails.PipeDetails;
import de.robotricker.transportpipes.utils.staticutils.DuctItemUtils;

public class RecipesConf extends Conf {

	public RecipesConf() {
		super(new File(ElectricWires.instance.getDataFolder().getAbsolutePath() + File.separator + "recipes.yml"), ElectricWires.instance);
		saveShapedRecipeAsDefault("colored", 4, Arrays.asList("ggx", "gbg", "xgg"), "g", "20:0", "b", "280:0");
		saveShapelessRecipeAsDefault("colored.white", 1, "wire", "351:15");
		finishDefault();
	}

	private void saveShapedRecipeAsDefault(String name, int amount, List<String> shape, String... ingredients) {
		String fullKey = "recipe." + name;
		if (!getYamlConf().contains(fullKey + ".type")) {
			saveAsDefault(fullKey + ".type", "shaped");
			saveAsDefault(fullKey + ".amount", amount);
			saveAsDefault(fullKey + ".shape", shape);
			for (int i = 0; i < ingredients.length; i++) {
				char ingredientChar = ingredients[i].charAt(0);
				String ingredientValue = ingredients[++i];
				saveAsDefault(fullKey + ".ingredients." + ingredientChar, ingredientValue);
			}
		}
	}

	private void saveShapelessRecipeAsDefault(String name, int amount, String... ingredients) {
		String fullKey = "recipe." + name;
		if (!getYamlConf().contains(fullKey + ".type")) {
			saveAsDefault(fullKey + ".type", "shapeless");
			saveAsDefault(fullKey + ".amount", amount);
			saveAsDefault(fullKey + ".ingredients", Arrays.asList(ingredients));
		}
	}

	/**
	 * prevent unused key from removing only inside RecipesConf
	 */
	@Override
	protected void finishDefault() {
		saveToFile();
	}

	public Recipe createWireRecipe(WireType wt, WireColor wc) {
		Object nk = createRecipeKey("wire-" + wt + (wc != null ? "-" + wc : ""));

		String basePath = "recipe." + wt.name().toLowerCase(Locale.ENGLISH);
		if (wc != null) {
			basePath += "." + wc.name().toLowerCase(Locale.ENGLISH);
		}
		ItemStack resultItem;
		if (wt == WireType.COLORED) {
			resultItem = DuctItemUtils.getClonedDuctItem(new WireDetails(wc == null ? WireColor.WHITE : wc));
		} else {
			resultItem = DuctItemUtils.getClonedDuctItem(new WireDetails(wt));
		}
		resultItem.setAmount((int) read(basePath + ".amount"));
		if (((String) read(basePath + ".type")).equalsIgnoreCase("shaped")) {
			TPShapedRecipe recipe = nk != null ? new TPShapedRecipe((org.bukkit.NamespacedKey) nk, resultItem) : new TPShapedRecipe(resultItem);
			recipe.shape(((List<String>) read(basePath + ".shape")).toArray(new String[0]));
			Collection<String> subKeys = readSubKeys(basePath + ".ingredients");
			for (String key : subKeys) {
				String itemString = (String) read(basePath + ".ingredients." + key);
				recipe.setIngredient(key.charAt(0), itemString);
				// if (item.getData().getData() == 0) {
				// recipe.setIngredient(key.charAt(0), item.getType(), -1);
				// } else {
				// recipe.setIngredient(key.charAt(0), item.getData());
				// }
			}
			recipe.register();
			return recipe;
		} else if (((String) read(basePath + ".type")).equalsIgnoreCase("shapeless")) {
			TPShapelessRecipe recipe = nk != null ? new TPShapelessRecipe((org.bukkit.NamespacedKey) nk, resultItem) : new TPShapelessRecipe(resultItem);
			List<String> ingredients = (List<String>) read(basePath + ".ingredients");
			for (String itemString : ingredients) {
				recipe.addIngredient(itemString);
				// if (item.getData().getData() == 0) {
				// recipe.addIngredient(item.getType(), -1);
				// } else {
				// recipe.addIngredient(item.getData());
				// }
			}
			recipe.register();
			return recipe;
		}
		return null;
	}
	
	private Object createRecipeKey(String key) {
		try {
			Class.forName("org.bukkit.NamespacedKey");
		} catch (ClassNotFoundException e) {
			return null;
		}
		return new org.bukkit.NamespacedKey(TransportPipes.instance, key);
	}

}