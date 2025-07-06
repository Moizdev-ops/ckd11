package com.yourname.customkitduels.gui;

import com.yourname.customkitduels.CustomKitDuels;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

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
        this.categoryItems = new ArrayList<>(plugin.getCategoryManager().getCategoryItems(this.categoryName));
        
        plugin.getLogger().info("[DEBUG] Creating CategoryEditorGUI for player " + player.getName() + " category " + this.categoryName);
        
        setupGUI();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
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
        
        if (!clicker.equals(player) || !event.getInventory().equals(gui) || !isActive) {
            return;
        }
        
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
        // Clear current items and reload defaults
        categoryItems.clear();
        categoryItems.addAll(plugin.getCategoryManager().getCategoryItems(categoryName));
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
        
        // Save to category manager
        plugin.getCategoryManager().updateCategory(categoryName, categoryItems);
        
        player.sendMessage(ChatColor.GREEN + "Category " + categoryName + " saved with " + categoryItems.size() + " items!");
        forceCleanup();
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