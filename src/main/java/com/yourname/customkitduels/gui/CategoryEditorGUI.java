package com.yourname.customkitduels.gui;

import com.yourname.customkitduels.CustomKitDuels;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class CategoryEditorGUI implements Listener {
    
    private final CustomKitDuels plugin;
    private final Player player;
    private final String categoryName;
    private final Inventory gui;
    private final List<ItemStack> categoryItems;
    private static final Map<UUID, CategoryEditorGUI> activeGuis = new HashMap<>();
    private boolean isActive = true;
    
    public CategoryEditorGUI(CustomKitDuels plugin, Player player, String categoryName) {
        this.plugin = plugin;
        this.player = player;
        this.categoryName = categoryName.toUpperCase();
        this.gui = Bukkit.createInventory(null, 54, ChatColor.DARK_GREEN + "Editing: " + this.categoryName);
        this.categoryItems = new ArrayList<>();
        
        plugin.getLogger().info("[DEBUG] Creating CategoryEditorGUI for player " + player.getName() + " category " + this.categoryName);
        
        loadCategoryItems();
        setupGUI();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    private void loadCategoryItems() {
        File categoriesFile = new File(plugin.getDataFolder(), "categories.yml");
        if (!categoriesFile.exists()) {
            loadDefaultItems();
            return;
        }
        
        FileConfiguration config = YamlConfiguration.loadConfiguration(categoriesFile);
        List<String> materialNames = config.getStringList(categoryName);
        
        if (materialNames.isEmpty()) {
            loadDefaultItems();
            return;
        }
        
        for (String materialName : materialNames) {
            try {
                Material material = Material.valueOf(materialName);
                categoryItems.add(new ItemStack(material));
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid material in category " + categoryName + ": " + materialName);
            }
        }
    }
    
    private void loadDefaultItems() {
        // Load default items based on category
        switch (categoryName) {
            case "WEAPONS":
                categoryItems.addAll(createBasicItems(Arrays.asList(
                    Material.WOODEN_SWORD, Material.STONE_SWORD, Material.IRON_SWORD, 
                    Material.GOLDEN_SWORD, Material.DIAMOND_SWORD, Material.NETHERITE_SWORD,
                    Material.BOW, Material.CROSSBOW, Material.TRIDENT
                )));
                break;
            case "ARMOR":
                categoryItems.addAll(createBasicItems(Arrays.asList(
                    Material.LEATHER_HELMET, Material.IRON_HELMET, Material.DIAMOND_HELMET,
                    Material.LEATHER_CHESTPLATE, Material.IRON_CHESTPLATE, Material.DIAMOND_CHESTPLATE,
                    Material.SHIELD
                )));
                break;
            case "FOOD":
                categoryItems.addAll(createBasicItems(Arrays.asList(
                    Material.APPLE, Material.GOLDEN_APPLE, Material.BREAD, Material.COOKED_BEEF,
                    Material.COOKED_CHICKEN, Material.CAKE
                )));
                break;
            // Add more defaults as needed
        }
    }
    
    private List<ItemStack> createBasicItems(List<Material> materials) {
        List<ItemStack> items = new ArrayList<>();
        for (Material material : materials) {
            items.add(new ItemStack(material));
        }
        return items;
    }
    
    private void setupGUI() {
        gui.clear();
        
        // Add existing items (slots 0-44)
        for (int i = 0; i < Math.min(categoryItems.size(), 45); i++) {
            gui.setItem(i, categoryItems.get(i).clone());
        }
        
        // Fill empty slots with air (so players can place items)
        for (int i = categoryItems.size(); i < 45; i++) {
            gui.setItem(i, null);
        }
        
        // Reset to default button
        ItemStack resetButton = new ItemStack(Material.YELLOW_STAINED_GLASS_PANE);
        ItemMeta resetMeta = resetButton.getItemMeta();
        resetMeta.setDisplayName(ChatColor.YELLOW + "Reset to Default");
        resetMeta.setLore(Arrays.asList(ChatColor.GRAY + "Load default items for this category"));
        resetButton.setItemMeta(resetMeta);
        gui.setItem(45, resetButton);
        
        // Clear all button
        ItemStack clearButton = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta clearMeta = clearButton.getItemMeta();
        clearMeta.setDisplayName(ChatColor.RED + "Clear All");
        clearMeta.setLore(Arrays.asList(ChatColor.GRAY + "Remove all items from category"));
        clearButton.setItemMeta(clearMeta);
        gui.setItem(46, clearButton);
        
        // Save button
        ItemStack saveButton = new ItemStack(Material.EMERALD);
        ItemMeta saveMeta = saveButton.getItemMeta();
        saveMeta.setDisplayName(ChatColor.GREEN + "Save Category");
        saveMeta.setLore(Arrays.asList(
            ChatColor.GRAY + "Save changes to " + categoryName,
            ChatColor.YELLOW + "Items: " + categoryItems.size()
        ));
        saveButton.setItemMeta(saveMeta);
        gui.setItem(49, saveButton);
        
        // Cancel button
        ItemStack cancelButton = new ItemStack(Material.BARRIER);
        ItemMeta cancelMeta = cancelButton.getItemMeta();
        cancelMeta.setDisplayName(ChatColor.RED + "Cancel");
        cancelMeta.setLore(Arrays.asList(ChatColor.GRAY + "Discard changes"));
        cancelButton.setItemMeta(cancelMeta);
        gui.setItem(53, cancelButton);
    }
    
    public void open() {
        plugin.getLogger().info("[DEBUG] Opening CategoryEditorGUI for " + player.getName());
        
        CategoryEditorGUI existing = activeGuis.get(player.getUniqueId());
        if (existing != null && existing != this) {
            plugin.getLogger().info("[DEBUG] Cleaning up existing CategoryEditorGUI for " + player.getName());
            existing.forceCleanup();
        }
        
        activeGuis.put(player.getUniqueId(), this);
        isActive = true;
        player.openInventory(gui);
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player clicker = (Player) event.getWhoClicked();
        
        if (!clicker.equals(player) || !isActive) {
            return;
        }
        
        // Only handle clicks in our GUI
        if (event.getInventory().equals(gui)) {
            int slot = event.getSlot();
            
            plugin.getLogger().info("[DEBUG] CategoryEditorGUI click event - Player: " + player.getName() + ", Slot: " + slot);
            
            // Handle control buttons
            if (slot == 45) { // Reset to default
                event.setCancelled(true);
                resetToDefault();
                return;
            }
            
            if (slot == 46) { // Clear all
                event.setCancelled(true);
                clearAllItems();
                return;
            }
            
            if (slot == 49) { // Save
                event.setCancelled(true);
                saveCategory();
                return;
            }
            
            if (slot == 53) { // Cancel
                event.setCancelled(true);
                forceCleanup();
                player.closeInventory();
                return;
            }
            
            // Allow normal inventory interaction for slots 0-44
            if (slot < 45) {
                // Don't cancel - allow placing/removing items
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    updateCategoryItems();
                }, 1L);
            } else {
                event.setCancelled(true);
            }
        }
    }
    
    private void updateCategoryItems() {
        categoryItems.clear();
        
        for (int i = 0; i < 45; i++) {
            ItemStack item = gui.getItem(i);
            if (item != null && item.getType() != Material.AIR) {
                categoryItems.add(new ItemStack(item.getType()));
            }
        }
        
        // Update save button with new count
        ItemStack saveButton = new ItemStack(Material.EMERALD);
        ItemMeta saveMeta = saveButton.getItemMeta();
        saveMeta.setDisplayName(ChatColor.GREEN + "Save Category");
        saveMeta.setLore(Arrays.asList(
            ChatColor.GRAY + "Save changes to " + categoryName,
            ChatColor.YELLOW + "Items: " + categoryItems.size()
        ));
        saveButton.setItemMeta(saveMeta);
        gui.setItem(49, saveButton);
    }
    
    private void resetToDefault() {
        categoryItems.clear();
        loadDefaultItems();
        setupGUI();
        player.sendMessage(ChatColor.YELLOW + "Reset category " + categoryName + " to default items!");
    }
    
    private void clearAllItems() {
        categoryItems.clear();
        setupGUI();
        player.sendMessage(ChatColor.YELLOW + "Cleared all items from category " + categoryName + "!");
    }
    
    private void saveCategory() {
        // Update items from GUI first
        updateCategoryItems();
        
        File categoriesFile = new File(plugin.getDataFolder(), "categories.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(categoriesFile);
        
        List<String> materialNames = new ArrayList<>();
        for (ItemStack item : categoryItems) {
            materialNames.add(item.getType().name());
        }
        
        config.set(categoryName, materialNames);
        
        try {
            config.save(categoriesFile);
            player.sendMessage(ChatColor.GREEN + "Category " + categoryName + " saved with " + categoryItems.size() + " items!");
            forceCleanup();
            player.closeInventory();
        } catch (IOException e) {
            player.sendMessage(ChatColor.RED + "Failed to save category: " + e.getMessage());
            plugin.getLogger().severe("Failed to save category " + categoryName + ": " + e.getMessage());
        }
    }
    
    private void forceCleanup() {
        plugin.getLogger().info("[DEBUG] Force cleanup CategoryEditorGUI for " + player.getName());
        isActive = false;
        activeGuis.remove(player.getUniqueId());
        InventoryClickEvent.getHandlerList().unregister(this);
        InventoryCloseEvent.getHandlerList().unregister(this);
        player.closeInventory();
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        Player closer = (Player) event.getPlayer();
        
        if (closer.equals(player) && event.getInventory().equals(gui)) {
            plugin.getLogger().info("[DEBUG] CategoryEditorGUI inventory closed by " + player.getName() + ", Active: " + isActive);
            
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (isActive && activeGuis.containsKey(player.getUniqueId())) {
                    plugin.getLogger().info("[DEBUG] Final cleanup CategoryEditorGUI for " + player.getName());
                    forceCleanup();
                }
            }, 3L);
        }
    }
}