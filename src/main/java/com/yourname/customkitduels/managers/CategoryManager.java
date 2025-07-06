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
            "BOW", "CROSSBOW", "TRIDENT", "MACE", "ARROW", "SPECTRAL_ARROW", "TIPPED_ARROW"
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
            "STONE", "COBBLESTONE", "DIRT", "GRASS_BLOCK", "SAND", "GRAVEL", "CLAY",
            "OAK_LOG", "OAK_PLANKS", "GLASS", "OBSIDIAN", "BEDROCK", "END_STONE",
            "IRON_BLOCK", "GOLD_BLOCK", "DIAMOND_BLOCK", "EMERALD_BLOCK", "NETHERITE_BLOCK",
            "TNT", "WHITE_WOOL", "RED_WOOL", "BLUE_WOOL", "GREEN_WOOL", "YELLOW_WOOL", "BLACK_WOOL",
            "ORANGE_WOOL", "PINK_WOOL", "PURPLE_WOOL", "BROWN_WOOL", "GRAY_WOOL", "LIGHT_GRAY_WOOL",
            "CYAN_WOOL", "LIGHT_BLUE_WOOL", "LIME_WOOL", "MAGENTA_WOOL",
            "BRICKS", "STONE_BRICKS", "NETHERRACK", "DEEPSLATE", "COPPER_BLOCK", "AMETHYST_BLOCK",
            "CALCITE", "TUFF", "DRIPSTONE_BLOCK", "MOSS_BLOCK", "ROOTED_DIRT"
        ));
        
        // FOOD
        defaultCategories.put("FOOD", Arrays.asList(
            "APPLE", "GOLDEN_APPLE", "ENCHANTED_GOLDEN_APPLE", "BREAD", "COOKED_BEEF",
            "COOKED_PORKCHOP", "COOKED_CHICKEN", "COOKED_COD", "COOKED_SALMON", "COOKED_MUTTON",
            "COOKED_RABBIT", "CAKE", "COOKIE", "MELON_SLICE", "SWEET_BERRIES", "GLOW_BERRIES",
            "CARROT", "POTATO", "BAKED_POTATO", "BEETROOT", "MUSHROOM_STEW", "BEETROOT_SOUP",
            "SUSPICIOUS_STEW", "RABBIT_STEW", "PUMPKIN_PIE", "DRIED_KELP", "HONEY_BOTTLE",
            "CHORUS_FRUIT", "POISONOUS_POTATO", "SPIDER_EYE", "ROTTEN_FLESH", "PUFFERFISH",
            "TROPICAL_FISH", "COD", "SALMON", "BEEF", "PORKCHOP", "CHICKEN", "MUTTON", "RABBIT"
        ));
        
        // POTIONS
        defaultCategories.put("POTIONS", Arrays.asList(
            "POTION", "SPLASH_POTION", "LINGERING_POTION", "GLASS_BOTTLE", "HONEY_BOTTLE",
            "BREWING_STAND", "CAULDRON", "BLAZE_POWDER", "NETHER_WART", "REDSTONE",
            "GLOWSTONE_DUST", "SPIDER_EYE", "FERMENTED_SPIDER_EYE", "MAGMA_CREAM", "SUGAR",
            "GLISTERING_MELON_SLICE", "GOLDEN_CARROT", "RABBIT_FOOT", "DRAGON_BREATH",
            "GHAST_TEAR", "PHANTOM_MEMBRANE", "MILK_BUCKET", "GUNPOWDER"
        ));
        
        // TOOLS
        defaultCategories.put("TOOLS", Arrays.asList(
            "WOODEN_PICKAXE", "STONE_PICKAXE", "IRON_PICKAXE", "GOLDEN_PICKAXE", "DIAMOND_PICKAXE", "NETHERITE_PICKAXE",
            "WOODEN_SHOVEL", "STONE_SHOVEL", "IRON_SHOVEL", "GOLDEN_SHOVEL", "DIAMOND_SHOVEL", "NETHERITE_SHOVEL",
            "WOODEN_HOE", "STONE_HOE", "IRON_HOE", "GOLDEN_HOE", "DIAMOND_HOE", "NETHERITE_HOE",
            "FISHING_ROD", "SHEARS", "FLINT_AND_STEEL", "BUCKET", "WATER_BUCKET", "LAVA_BUCKET",
            "POWDER_SNOW_BUCKET", "AXOLOTL_BUCKET", "COD_BUCKET", "SALMON_BUCKET", "TROPICAL_FISH_BUCKET", "PUFFERFISH_BUCKET",
            "COMPASS", "CLOCK", "SPYGLASS", "BRUSH", "RECOVERY_COMPASS"
        ));
        
        // UTILITY
        defaultCategories.put("UTILITY", Arrays.asList(
            "ENDER_PEARL", "ENDER_EYE", "FLINT_AND_STEEL", "FIRE_CHARGE", "WIND_CHARGE",
            "SNOWBALL", "EGG", "FISHING_ROD", "COMPASS", "CLOCK", "RECOVERY_COMPASS",
            "FILLED_MAP", "LEAD", "NAME_TAG", "SADDLE", "OAK_BOAT", "OAK_CHEST_BOAT",
            "SPRUCE_BOAT", "BIRCH_BOAT", "JUNGLE_BOAT", "ACACIA_BOAT", "DARK_OAK_BOAT", "MANGROVE_BOAT", "CHERRY_BOAT", "BAMBOO_RAFT",
            "MINECART", "CHEST_MINECART", "FURNACE_MINECART", "TNT_MINECART", "HOPPER_MINECART",
            "TOTEM_OF_UNDYING", "ELYTRA", "FIREWORK_ROCKET", "FIREWORK_STAR",
            "ECHO_SHARD", "GOAT_HORN", "SPYGLASS", "BUNDLE"
        ));
        
        // MISC
        defaultCategories.put("MISC", Arrays.asList(
            "BOOK", "WRITABLE_BOOK", "WRITTEN_BOOK", "PAPER", "FEATHER", "INK_SAC", "BONE", "STRING",
            "STICK", "COAL", "CHARCOAL", "DIAMOND", "EMERALD", "GOLD_INGOT", "IRON_INGOT",
            "COPPER_INGOT", "NETHERITE_INGOT", "REDSTONE", "GUNPOWDER", "GLOWSTONE_DUST",
            "EXPERIENCE_BOTTLE", "ENCHANTED_BOOK", "ANVIL", "ENCHANTING_TABLE", "ENDER_CHEST",
            "AMETHYST_SHARD", "PRISMARINE_SHARD", "PRISMARINE_CRYSTALS", "HEART_OF_THE_SEA", "NAUTILUS_SHELL",
            "MUSIC_DISC_13", "MUSIC_DISC_CAT", "MUSIC_DISC_BLOCKS", "MUSIC_DISC_CHIRP", "MUSIC_DISC_FAR",
            "MUSIC_DISC_MALL", "MUSIC_DISC_MELLOHI", "MUSIC_DISC_STAL", "MUSIC_DISC_STRAD", "MUSIC_DISC_WARD",
            "MUSIC_DISC_11", "MUSIC_DISC_WAIT", "MUSIC_DISC_OTHERSIDE", "MUSIC_DISC_5", "MUSIC_DISC_PIGSTEP",
            "DISC_FRAGMENT_5", "NETHER_STAR", "WITHER_SKELETON_SKULL", "DRAGON_HEAD", "PLAYER_HEAD",
            "ZOMBIE_HEAD", "CREEPER_HEAD", "SKELETON_SKULL", "BELL", "LANTERN", "SOUL_LANTERN"
        ));
        
        // Create files for categories that don't exist or are empty
        for (Map.Entry<String, List<String>> entry : defaultCategories.entrySet()) {
            File categoryFile = new File(categoriesFolder, entry.getKey() + ".yml");
            boolean shouldCreate = false;
            
            if (!categoryFile.exists()) {
                shouldCreate = true;
            } else {
                // Check if file is empty or has no items
                FileConfiguration existingConfig = YamlConfiguration.loadConfiguration(categoryFile);
                List<String> existingItems = existingConfig.getStringList("items");
                if (existingItems.isEmpty()) {
                    shouldCreate = true;
                }
            }
            
            if (shouldCreate) {
                FileConfiguration config = new YamlConfiguration();
                config.set("items", entry.getValue());
                try {
                    config.save(categoryFile);
                    plugin.getLogger().info("Created/Updated default category file: " + entry.getKey() + ".yml with " + entry.getValue().size() + " items");
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