package org.yuyukon.softDeath;

import org.bukkit.inventory.ItemStack;

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
}