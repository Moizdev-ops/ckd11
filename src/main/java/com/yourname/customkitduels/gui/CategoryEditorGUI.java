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
        this.gui = Bukkit.createInventory(null, 54, ChatColor.DARK_GREEN + "Editing Category: " + this.categoryName);
        this.categoryItems = new ArrayList<>();
        
        plugin.getLogger().info("[DEBUG] Creating CategoryEditorGUI for player " + player.getName() + " category " + this.categoryName);
        
        loadCategoryItems();
        setupGUI();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    private void loadCategoryItems() {
        File categoriesFile = new File(plugin.getDataFolder(), "categories.yml");
        if (!categoriesFile.exists()) {
            return;
        }
        
        FileConfiguration config = YamlConfiguration.loadConfiguration(categoriesFile);
        List<String> materialNames = config.getStringList(categoryName);
        
        for (String materialName : materialNames) {
            try {
                Material material = Material.valueOf(materialName);
                categoryItems.add(new ItemStack(material));
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid material in category " + categoryName + ": " + materialName);
            }
        }
    }
    
    private void setupGUI() {
        gui.clear();
        
        // Add existing items (slots 0-44)
        for (int i = 0; i < Math.min(categoryItems.size(), 45); i++) {
            gui.setItem(i, categoryItems.get(i).clone());
        }
        
        // Fill empty slots with glass panes
        for (int i = categoryItems.size(); i < 45; i++) {
            ItemStack glassPane = new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
            ItemMeta meta = glassPane.getItemMeta();
            meta.setDisplayName(ChatColor.GRAY + "Empty Slot");
            meta.setLore(Arrays.asList(ChatColor.GRAY + "Drop an item here to add it"));
            glassPane.setItemMeta(meta);
            gui.setItem(i, glassPane);
        }
        
        // Add item button
        ItemStack addButton = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        ItemMeta addMeta = addButton.getItemMeta();
        addMeta.setDisplayName(ChatColor.GREEN + "Add Item");
        addMeta.setLore(Arrays.asList(
            ChatColor.GRAY + "Click with an item in your cursor",
            ChatColor.GRAY + "to add it to this category"
        ));
        addButton.setItemMeta(addMeta);
        gui.setItem(45, addButton);
        
        // Remove item button
        ItemStack removeButton = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta removeMeta = removeButton.getItemMeta();
        removeMeta.setDisplayName(ChatColor.RED + "Remove Mode");
        removeMeta.setLore(Arrays.asList(
            ChatColor.GRAY + "Click on items to remove them",
            ChatColor.GRAY + "from this category"
        ));
        removeButton.setItemMeta(removeMeta);
        gui.setItem(46, removeButton);
        
        // Clear all button
        ItemStack clearButton = new ItemStack(Material.BARRIER);
        ItemMeta clearMeta = clearButton.getItemMeta();
        clearMeta.setDisplayName(ChatColor.DARK_RED + "Clear All");
        clearMeta.setLore(Arrays.asList(ChatColor.GRAY + "Remove all items from category"));
        clearButton.setItemMeta(clearMeta);
        gui.setItem(47, clearButton);
        
        // Save button
        ItemStack saveButton = new ItemStack(Material.EMERALD);
        ItemMeta saveMeta = saveButton.getItemMeta();
        saveMeta.setDisplayName(ChatColor.GREEN + "Save Category");
        saveMeta.setLore(Arrays.asList(ChatColor.GRAY + "Save changes to " + categoryName));
        saveButton.setItemMeta(saveMeta);
        gui.setItem(49, saveButton);
        
        // Cancel button
        ItemStack cancelButton = new ItemStack(Material.REDSTONE);
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
        
        if (!clicker.equals(player) || !event.getInventory().equals(gui) || !isActive) {
            return;
        }
        
        event.setCancelled(true);
        int slot = event.getSlot();
        
        plugin.getLogger().info("[DEBUG] CategoryEditorGUI click event - Player: " + player.getName() + ", Slot: " + slot);
        
        if (slot == 45) { // Add item
            ItemStack cursor = event.getCursor();
            if (cursor != null && cursor.getType() != Material.AIR) {
                addItem(cursor.clone());
                event.setCursor(null);
            } else {
                player.sendMessage(ChatColor.RED + "Hold an item in your cursor to add it!");
            }
            return;
        }
        
        if (slot == 46) { // Remove mode toggle
            player.sendMessage(ChatColor.YELLOW + "Remove mode: Click on items to remove them!");
            return;
        }
        
        if (slot == 47) { // Clear all
            clearAllItems();
            return;
        }
        
        if (slot == 49) { // Save
            saveCategory();
            return;
        }
        
        if (slot == 53) { // Cancel
            forceCleanup();
            player.closeInventory();
            return;
        }
        
        // Handle item removal (slots 0-44)
        if (slot < 45) {
            ItemStack clickedItem = gui.getItem(slot);
            if (clickedItem != null && clickedItem.getType() != Material.LIGHT_GRAY_STAINED_GLASS_PANE) {
                removeItem(slot);
            }
        }
    }
    
    private void addItem(ItemStack item) {
        // Check if item already exists
        for (ItemStack existing : categoryItems) {
            if (existing.getType() == item.getType()) {
                player.sendMessage(ChatColor.RED + "Item already exists in category!");
                return;
            }
        }
        
        categoryItems.add(new ItemStack(item.getType()));
        setupGUI();
        player.sendMessage(ChatColor.GREEN + "Added " + formatMaterialName(item.getType().name()) + " to category!");
    }
    
    private void removeItem(int slot) {
        if (slot < categoryItems.size()) {
            ItemStack removed = categoryItems.remove(slot);
            setupGUI();
            player.sendMessage(ChatColor.YELLOW + "Removed " + formatMaterialName(removed.getType().name()) + " from category!");
        }
    }
    
    private void clearAllItems() {
        categoryItems.clear();
        setupGUI();
        player.sendMessage(ChatColor.YELLOW + "Cleared all items from category " + categoryName + "!");
    }
    
    private void saveCategory() {
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
    
    private String formatMaterialName(String materialName) {
        String[] words = materialName.toLowerCase().split("_");
        StringBuilder formatted = new StringBuilder();
        
        for (String word : words) {
            if (formatted.length() > 0) {
                formatted.append(" ");
            }
            formatted.append(word.substring(0, 1).toUpperCase()).append(word.substring(1));
        }
        
        return formatted.toString();
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