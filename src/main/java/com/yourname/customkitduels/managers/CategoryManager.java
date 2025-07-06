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
    private final File categoriesFolder;
    private final Map<String, List<ItemStack>> categoryCache;
    
    public CategoryManager(CustomKitDuels plugin) {
        this.plugin = plugin;
        this.categoriesFolder = new File(plugin.getDataFolder(), "categories");
        this.categoryCache = new HashMap<>();
        
        if (!categoriesFolder.exists()) {
            categoriesFolder.mkdirs();
        }
        
        loadCategories();
    }
    
    private void loadCategories() {
        // Create default categories if they don't exist
        createDefaultCategoriesIfNeeded();
        
        // Load all category files
        File[] categoryFiles = categoriesFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (categoryFiles != null) {
            for (File file : categoryFiles) {
                String categoryName = file.getName().replace(".yml", "").toUpperCase();
                loadCategory(categoryName);
            }
        }
    }
    
    private void createDefaultCategoriesIfNeeded() {
        Map<String, List<String>> defaultCategories = new HashMap<>();
        
        // WEAPONS
        defaultCategories.put("WEAPONS", Arrays.asList(
            "WOODEN_SWORD", "STONE_SWORD", "IRON_SWORD", "GOLDEN_SWORD", "DIAMOND_SWORD", "NETHERITE_SWORD",
            "WOODEN_AXE", "STONE_AXE", "IRON_AXE", "GOLDEN_AXE", "DIAMOND_AXE", "NETHERITE_AXE",
            "BOW", "CROSSBOW", "TRIDENT", "MACE"
        ));
        
        // ARMOR
        defaultCategories.put("ARMOR", Arrays.asList(
            "LEATHER_HELMET", "LEATHER_CHESTPLATE", "LEATHER_LEGGINGS", "LEATHER_BOOTS",
            "CHAINMAIL_HELMET", "CHAINMAIL_CHESTPLATE", "CHAINMAIL_LEGGINGS", "CHAINMAIL_BOOTS",
            "IRON_HELMET", "IRON_CHESTPLATE", "IRON_LEGGINGS", "IRON_BOOTS",
            "GOLDEN_HELMET", "GOLDEN_CHESTPLATE", "GOLDEN_LEGGINGS", "GOLDEN_BOOTS",
            "DIAMOND_HELMET", "DIAMOND_CHESTPLATE", "DIAMOND_LEGGINGS", "DIAMOND_BOOTS",
            "NETHERITE_HELMET", "NETHERITE_CHESTPLATE", "NETHERITE_LEGGINGS", "NETHERITE_BOOTS",
            "TURTLE_HELMET", "SHIELD", "ELYTRA"
        ));
        
        // BLOCKS
        defaultCategories.put("BLOCKS", Arrays.asList(
            "STONE", "COBBLESTONE", "DIRT", "GRASS_BLOCK", "SAND", "GRAVEL",
            "OAK_LOG", "OAK_PLANKS", "GLASS", "OBSIDIAN", "BEDROCK",
            "IRON_BLOCK", "GOLD_BLOCK", "DIAMOND_BLOCK", "EMERALD_BLOCK",
            "TNT", "WHITE_WOOL", "BRICKS", "STONE_BRICKS", "NETHERRACK", "END_STONE",
            "RED_WOOL", "BLUE_WOOL", "GREEN_WOOL", "YELLOW_WOOL", "BLACK_WOOL",
            "DEEPSLATE", "COPPER_BLOCK", "AMETHYST_BLOCK", "CALCITE", "TUFF"
        ));
        
        // FOOD
        defaultCategories.put("FOOD", Arrays.asList(
            "APPLE", "GOLDEN_APPLE", "ENCHANTED_GOLDEN_APPLE", "BREAD", "COOKED_BEEF",
            "COOKED_PORKCHOP", "COOKED_CHICKEN", "COOKED_COD", "COOKED_SALMON",
            "CAKE", "COOKIE", "MELON_SLICE", "SWEET_BERRIES", "GLOW_BERRIES",
            "CARROT", "POTATO", "BAKED_POTATO", "BEETROOT", "MUSHROOM_STEW",
            "SUSPICIOUS_STEW", "RABBIT_STEW", "PUMPKIN_PIE", "DRIED_KELP"
        ));
        
        // POTIONS
        defaultCategories.put("POTIONS", Arrays.asList(
            "POTION", "SPLASH_POTION", "LINGERING_POTION",
            "GLASS_BOTTLE", "BREWING_STAND", "CAULDRON", "BLAZE_POWDER", "NETHER_WART",
            "SPIDER_EYE", "FERMENTED_SPIDER_EYE", "MAGMA_CREAM", "SUGAR",
            "GLISTERING_MELON_SLICE", "GOLDEN_CARROT", "RABBIT_FOOT", "DRAGON_BREATH",
            "GHAST_TEAR", "PHANTOM_MEMBRANE", "HONEY_BOTTLE", "MILK_BUCKET"
        ));
        
        // TOOLS
        defaultCategories.put("TOOLS", Arrays.asList(
            "WOODEN_PICKAXE", "STONE_PICKAXE", "IRON_PICKAXE", "GOLDEN_PICKAXE", "DIAMOND_PICKAXE", "NETHERITE_PICKAXE",
            "WOODEN_SHOVEL", "STONE_SHOVEL", "IRON_SHOVEL", "GOLDEN_SHOVEL", "DIAMOND_SHOVEL", "NETHERITE_SHOVEL",
            "WOODEN_HOE", "STONE_HOE", "IRON_HOE", "GOLDEN_HOE", "DIAMOND_HOE", "NETHERITE_HOE",
            "FISHING_ROD", "SHEARS", "FLINT_AND_STEEL", "BUCKET", "WATER_BUCKET", "LAVA_BUCKET",
            "COMPASS", "CLOCK", "SPYGLASS", "BRUSH"
        ));
        
        // UTILITY
        defaultCategories.put("UTILITY", Arrays.asList(
            "ENDER_PEARL", "ENDER_EYE", "FLINT_AND_STEEL", "FIRE_CHARGE",
            "SNOWBALL", "EGG", "FISHING_ROD", "COMPASS", "CLOCK",
            "FILLED_MAP", "LEAD", "NAME_TAG", "SADDLE", "OAK_BOAT",
            "MINECART", "CHEST_MINECART", "FURNACE_MINECART", "TNT_MINECART",
            "TOTEM_OF_UNDYING", "ELYTRA", "FIREWORK_ROCKET", "RECOVERY_COMPASS",
            "ECHO_SHARD", "GOAT_HORN", "WIND_CHARGE"
        ));
        
        // MISC
        defaultCategories.put("MISC", Arrays.asList(
            "BOOK", "PAPER", "FEATHER", "INK_SAC", "BONE", "STRING",
            "STICK", "COAL", "CHARCOAL", "DIAMOND", "EMERALD", "GOLD_INGOT",
            "IRON_INGOT", "REDSTONE", "GUNPOWDER", "GLOWSTONE_DUST",
            "EXPERIENCE_BOTTLE", "ENCHANTED_BOOK", "ANVIL", "ENCHANTING_TABLE",
            "NETHERITE_INGOT", "COPPER_INGOT", "AMETHYST_SHARD", "PRISMARINE_SHARD",
            "HEART_OF_THE_SEA", "NAUTILUS_SHELL", "DISC_FRAGMENT_5"
        ));
        
        // Create files for categories that don't exist
        for (Map.Entry<String, List<String>> entry : defaultCategories.entrySet()) {
            File categoryFile = new File(categoriesFolder, entry.getKey() + ".yml");
            if (!categoryFile.exists()) {
                FileConfiguration config = new YamlConfiguration();
                config.set("items", entry.getValue());
                try {
                    config.save(categoryFile);
                    plugin.getLogger().info("Created default category file: " + entry.getKey() + ".yml");
                } catch (IOException e) {
                    plugin.getLogger().severe("Failed to create category file " + entry.getKey() + ": " + e.getMessage());
                }
            }
        }
    }
    
    private void loadCategory(String categoryName) {
        File categoryFile = new File(categoriesFolder, categoryName + ".yml");
        if (!categoryFile.exists()) return;
        
        FileConfiguration config = YamlConfiguration.loadConfiguration(categoryFile);
        List<String> materialNames = config.getStringList("items");
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
        plugin.getLogger().info("Loaded category " + categoryName + " with " + items.size() + " items");
    }
    
    public List<ItemStack> getCategoryItems(String categoryName) {
        categoryName = categoryName.toUpperCase();
        List<ItemStack> items = categoryCache.get(categoryName);
        if (items == null) {
            // Try to load the category if it's not in cache
            loadCategory(categoryName);
            items = categoryCache.get(categoryName);
        }
        return items != null ? new ArrayList<>(items) : new ArrayList<>();
    }
    
    public void updateCategory(String categoryName, List<ItemStack> items) {
        categoryName = categoryName.toUpperCase();
        categoryCache.put(categoryName, new ArrayList<>(items));
        
        List<String> materialNames = new ArrayList<>();
        for (ItemStack item : items) {
            materialNames.add(item.getType().name());
        }
        
        File categoryFile = new File(categoriesFolder, categoryName + ".yml");
        FileConfiguration config = new YamlConfiguration();
        config.set("items", materialNames);
        
        try {
            config.save(categoryFile);
            plugin.getLogger().info("Saved category " + categoryName + " with " + items.size() + " items");
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save category " + categoryName + ": " + e.getMessage());
        }
    }
    
    public Set<String> getCategoryNames() {
        return new HashSet<>(categoryCache.keySet());
    }
    
    public void reloadCategories() {
        categoryCache.clear();
        loadCategories();
    }
}