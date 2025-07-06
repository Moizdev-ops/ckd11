package com.yourname.customkitduels.managers;

import com.yourname.customkitduels.CustomKitDuels;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class CategoryManager {
    
    private final CustomKitDuels plugin;
    private final File categoriesFile;
    private FileConfiguration categoriesConfig;
    private final Map<String, List<ItemStack>> categoryCache;
    
    public CategoryManager(CustomKitDuels plugin) {
        this.plugin = plugin;
        this.categoriesFile = new File(plugin.getDataFolder(), "categories.yml");
        this.categoryCache = new HashMap<>();
        
        loadCategories();
    }
    
    private void loadCategories() {
        if (!categoriesFile.exists()) {
            createDefaultCategories();
        }
        
        categoriesConfig = YamlConfiguration.loadConfiguration(categoriesFile);
        
        // Load all categories into cache
        for (String categoryName : categoriesConfig.getKeys(false)) {
            List<String> materialNames = categoriesConfig.getStringList(categoryName);
            List<ItemStack> items = new ArrayList<>();
            
            for (String materialName : materialNames) {
                try {
                    Material material = Material.valueOf(materialName);
                    items.add(new ItemStack(material));
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid material in category " + categoryName + ": " + materialName);
                }
            }
            
            categoryCache.put(categoryName, items);
        }
    }
    
    private void createDefaultCategories() {
        categoriesConfig = new YamlConfiguration();
        
        // WEAPONS
        categoriesConfig.set("WEAPONS", Arrays.asList(
            "WOODEN_SWORD", "STONE_SWORD", "IRON_SWORD", "GOLDEN_SWORD", "DIAMOND_SWORD", "NETHERITE_SWORD",
            "WOODEN_AXE", "STONE_AXE", "IRON_AXE", "GOLDEN_AXE", "DIAMOND_AXE", "NETHERITE_AXE",
            "BOW", "CROSSBOW", "TRIDENT", "MACE"
        ));
        
        // ARMOR
        categoriesConfig.set("ARMOR", Arrays.asList(
            "LEATHER_HELMET", "LEATHER_CHESTPLATE", "LEATHER_LEGGINGS", "LEATHER_BOOTS",
            "CHAINMAIL_HELMET", "CHAINMAIL_CHESTPLATE", "CHAINMAIL_LEGGINGS", "CHAINMAIL_BOOTS",
            "IRON_HELMET", "IRON_CHESTPLATE", "IRON_LEGGINGS", "IRON_BOOTS",
            "GOLDEN_HELMET", "GOLDEN_CHESTPLATE", "GOLDEN_LEGGINGS", "GOLDEN_BOOTS",
            "DIAMOND_HELMET", "DIAMOND_CHESTPLATE", "DIAMOND_LEGGINGS", "DIAMOND_BOOTS",
            "NETHERITE_HELMET", "NETHERITE_CHESTPLATE", "NETHERITE_LEGGINGS", "NETHERITE_BOOTS",
            "TURTLE_HELMET", "SHIELD", "ELYTRA"
        ));
        
        // BLOCKS
        categoriesConfig.set("BLOCKS", Arrays.asList(
            "STONE", "COBBLESTONE", "DIRT", "GRASS_BLOCK", "SAND", "GRAVEL",
            "OAK_LOG", "OAK_PLANKS", "GLASS", "OBSIDIAN", "BEDROCK",
            "IRON_BLOCK", "GOLD_BLOCK", "DIAMOND_BLOCK", "EMERALD_BLOCK",
            "TNT", "WHITE_WOOL", "BRICKS", "STONE_BRICKS", "NETHERRACK", "END_STONE"
        ));
        
        // FOOD
        categoriesConfig.set("FOOD", Arrays.asList(
            "APPLE", "GOLDEN_APPLE", "ENCHANTED_GOLDEN_APPLE", "BREAD", "COOKED_BEEF",
            "COOKED_PORKCHOP", "COOKED_CHICKEN", "COOKED_COD", "COOKED_SALMON",
            "CAKE", "COOKIE", "MELON_SLICE", "SWEET_BERRIES", "GLOW_BERRIES",
            "CARROT", "POTATO", "BAKED_POTATO", "BEETROOT", "MUSHROOM_STEW"
        ));
        
        // POTIONS
        categoriesConfig.set("POTIONS", Arrays.asList(
            "POTION", "SPLASH_POTION", "LINGERING_POTION",
            "GLASS_BOTTLE", "BREWING_STAND", "CAULDRON", "BLAZE_POWDER", "NETHER_WART",
            "SPIDER_EYE", "FERMENTED_SPIDER_EYE", "MAGMA_CREAM", "SUGAR",
            "GLISTERING_MELON_SLICE", "GOLDEN_CARROT", "RABBIT_FOOT", "DRAGON_BREATH"
        ));
        
        // TOOLS
        categoriesConfig.set("TOOLS", Arrays.asList(
            "WOODEN_PICKAXE", "STONE_PICKAXE", "IRON_PICKAXE", "GOLDEN_PICKAXE", "DIAMOND_PICKAXE", "NETHERITE_PICKAXE",
            "WOODEN_SHOVEL", "STONE_SHOVEL", "IRON_SHOVEL", "GOLDEN_SHOVEL", "DIAMOND_SHOVEL", "NETHERITE_SHOVEL",
            "WOODEN_HOE", "STONE_HOE", "IRON_HOE", "GOLDEN_HOE", "DIAMOND_HOE", "NETHERITE_HOE",
            "FISHING_ROD", "SHEARS", "FLINT_AND_STEEL", "BUCKET", "WATER_BUCKET", "LAVA_BUCKET"
        ));
        
        // UTILITY
        categoriesConfig.set("UTILITY", Arrays.asList(
            "ENDER_PEARL", "ENDER_EYE", "FLINT_AND_STEEL", "FIRE_CHARGE",
            "SNOWBALL", "EGG", "FISHING_ROD", "COMPASS", "CLOCK",
            "FILLED_MAP", "LEAD", "NAME_TAG", "SADDLE", "OAK_BOAT",
            "MINECART", "CHEST_MINECART", "FURNACE_MINECART", "TNT_MINECART",
            "TOTEM_OF_UNDYING", "ELYTRA", "FIREWORK_ROCKET", "WIND_CHARGE"
        ));
        
        // MISC
        categoriesConfig.set("MISC", Arrays.asList(
            "BOOK", "PAPER", "FEATHER", "INK_SAC", "BONE", "STRING",
            "STICK", "COAL", "CHARCOAL", "DIAMOND", "EMERALD", "GOLD_INGOT",
            "IRON_INGOT", "REDSTONE", "GUNPOWDER", "GLOWSTONE_DUST",
            "EXPERIENCE_BOTTLE", "ENCHANTED_BOOK", "ANVIL", "ENCHANTING_TABLE"
        ));
        
        saveCategories();
    }
    
    public List<ItemStack> getCategoryItems(String categoryName) {
        return categoryCache.getOrDefault(categoryName.toUpperCase(), new ArrayList<>());
    }
    
    public void updateCategory(String categoryName, List<ItemStack> items) {
        categoryName = categoryName.toUpperCase();
        categoryCache.put(categoryName, new ArrayList<>(items));
        
        List<String> materialNames = new ArrayList<>();
        for (ItemStack item : items) {
            materialNames.add(item.getType().name());
        }
        
        categoriesConfig.set(categoryName, materialNames);
        saveCategories();
    }
    
    private void saveCategories() {
        try {
            categoriesConfig.save(categoriesFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save categories: " + e.getMessage());
        }
    }
    
    public Set<String> getCategoryNames() {
        return categoryCache.keySet();
    }
    
    public void reloadCategories() {
        categoryCache.clear();
        loadCategories();
    }
}