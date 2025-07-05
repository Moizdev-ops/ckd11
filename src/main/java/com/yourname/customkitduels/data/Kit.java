package com.yourname.customkitduels.data;

import org.bukkit.inventory.ItemStack;

public class Kit {
    
    private final String name;
    private final String displayName;
    private final ItemStack[] contents;
    private final ItemStack[] armor;
    
    public Kit(String name, String displayName, ItemStack[] contents, ItemStack[] armor) {
        this.name = name;
        this.displayName = displayName;
        this.contents = contents != null ? contents : new ItemStack[36];
        this.armor = armor != null ? armor : new ItemStack[4];
    }
    
    public String getName() {
        return name;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public ItemStack[] getContents() {
        return contents;
    }
    
    public ItemStack[] getArmor() {
        return armor;
    }
}