package org.yuyukon.softDeath;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.bukkit.Bukkit.getLogger;

public class DeepClone {

    //method-title                                                                                                      |====深度克隆====>
    public static ItemStack[] deepCloneInventory(ItemStack[] original) {
        if (original == null) {
            return new ItemStack[0];
        }
        ItemStack[] cloned = new ItemStack[original.length];
        for (int i = 0; i < original.length; i++) {
            if (original[i] == null) {
                cloned[i] = null;
                continue;
            }
            cloned[i] = original[i].clone();
        }
        return cloned;
    }

    //method-title                                                                                                      |====加载列表====>
    public static List<Material> convertStringListToMaterials(List<String> strings) {
        return strings.stream()
                .map(s -> {
                    try {
                        return Material.valueOf(s.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        getLogger().warning("Invalid material in config: " + s);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

}