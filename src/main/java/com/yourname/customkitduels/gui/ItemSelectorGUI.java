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

public class ItemSelectorGUI implements Listener {
    
    private final CustomKitDuels plugin;
    private final Player player;
    private final KitEditorGUI parentGUI;
    private final int targetSlot;
    private final String category;
    private final Inventory gui;
    private final List<ItemStack> categoryItems;
    private int currentPage = 0;
    private static final Map<UUID, ItemSelectorGUI> activeGuis = new HashMap<>();
    private boolean isActive = true;
    private boolean isNavigating = false;
    
    public ItemSelectorGUI(CustomKitDuels plugin, Player player, KitEditorGUI parentGUI, int targetSlot, String category) {
        this.plugin = plugin;
        this.player = player;
        this.parentGUI = parentGUI;
        this.targetSlot = targetSlot;
        this.category = category;
        this.categoryItems = plugin.getCategoryManager().getCategoryItems(category);
        this.gui = Bukkit.createInventory(null, 54, ChatColor.DARK_PURPLE + category + " Items");
        
        plugin.getLogger().info("[DEBUG] Creating ItemSelectorGUI for player " + player.getName() + " category " + category + " slot " + targetSlot);
        
        setupGUI();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    private void setupGUI() {
        gui.clear();
        
        int itemsPerPage = 45;
        int startIndex = currentPage * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, categoryItems.size());
        
        // Add items for current page
        for (int i = startIndex; i < endIndex; i++) {
            ItemStack item = categoryItems.get(i).clone();
            ItemMeta meta = item.getItemMeta();
            if (meta.getDisplayName() == null || meta.getDisplayName().isEmpty()) {
                meta.setDisplayName(ChatColor.WHITE + formatMaterialName(item.getType().name()));
            }
            item.setItemMeta(meta);
            gui.setItem(i - startIndex, item);
        }
        
        // Navigation buttons
        if (currentPage > 0) {
            ItemStack prevPage = new ItemStack(Material.ARROW);
            ItemMeta prevMeta = prevPage.getItemMeta();
            prevMeta.setDisplayName(ChatColor.YELLOW + "Previous Page");
            prevPage.setItemMeta(prevMeta);
            gui.setItem(45, prevPage);
        }
        
        if (endIndex < categoryItems.size()) {
            ItemStack nextPage = new ItemStack(Material.ARROW);
            ItemMeta nextMeta = nextPage.getItemMeta();
            nextMeta.setDisplayName(ChatColor.YELLOW + "Next Page");
            nextPage.setItemMeta(nextMeta);
            gui.setItem(53, nextPage);
        }
        
        // Back button
        ItemStack back = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.setDisplayName(ChatColor.RED + "Back");
        back.setItemMeta(backMeta);
        gui.setItem(49, back);
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
    
    public void open() {
        plugin.getLogger().info("[DEBUG] Opening ItemSelectorGUI for " + player.getName());
        
        // Clean up any existing item selector GUI for this player
        ItemSelectorGUI existing = activeGuis.get(player.getUniqueId());
        if (existing != null && existing != this) {
            plugin.getLogger().info("[DEBUG] Cleaning up existing ItemSelectorGUI for " + player.getName());
            existing.forceCleanup();
        }
        
        activeGuis.put(player.getUniqueId(), this);
        isActive = true;
        isNavigating = false;
        player.openInventory(gui);
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player clicker = (Player) event.getWhoClicked();
        
        if (!clicker.equals(player) || !event.getInventory().equals(gui) || !isActive || isNavigating) {
            return;
        }
        
        event.setCancelled(true);
        int slot = event.getSlot();
        
        plugin.getLogger().info("[DEBUG] ItemSelectorGUI click event - Player: " + player.getName() + ", Slot: " + slot);
        
        if (slot == 45 && currentPage > 0) { // Previous page
            plugin.getLogger().info("[DEBUG] Previous page clicked");
            currentPage--;
            setupGUI();
        } else if (slot == 53 && (currentPage + 1) * 45 < categoryItems.size()) { // Next page
            plugin.getLogger().info("[DEBUG] Next page clicked");
            currentPage++;
            setupGUI();
        } else if (slot == 49) { // Back button
            plugin.getLogger().info("[DEBUG] Back to category clicked");
            returnToCategory();
        } else if (slot < 45) { // Item selection
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem != null && clickedItem.getType() != Material.AIR) {
                plugin.getLogger().info("[DEBUG] Item selected: " + clickedItem.getType() + " for slot " + targetSlot);
                parentGUI.setSlotItem(targetSlot, clickedItem.clone());
                player.sendMessage(ChatColor.GREEN + "Item added to slot " + getSlotDisplayName(targetSlot) + "!");
                returnToParent();
            }
        }
    }
    
    private String getSlotDisplayName(int slot) {
        if (slot < 36) {
            return "#" + (slot + 1);
        } else if (slot < 40) {
            String[] armorSlots = {"Boots", "Leggings", "Chestplate", "Helmet"};
            return armorSlots[slot - 36];
        } else if (slot == 40) {
            return "Offhand";
        }
        return "Unknown";
    }
    
    private void returnToCategory() {
        plugin.getLogger().info("[DEBUG] Returning to category selector for player " + player.getName());
        isNavigating = true;
        forceCleanup();
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            new CategorySelectorGUI(plugin, player, parentGUI, targetSlot).open();
        }, 1L);
    }
    
    private void returnToParent() {
        plugin.getLogger().info("[DEBUG] Returning to parent GUI for player " + player.getName());
        isNavigating = true;
        forceCleanup();
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            parentGUI.refreshAndReopen();
        }, 1L);
    }
    
    private void forceCleanup() {
        plugin.getLogger().info("[DEBUG] Force cleanup ItemSelectorGUI for " + player.getName());
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
            plugin.getLogger().info("[DEBUG] ItemSelectorGUI inventory closed by " + player.getName() + ", Active: " + isActive + ", Navigating: " + isNavigating);
            
            // Only cleanup if not navigating
            if (!isNavigating) {
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    if (isActive && !isNavigating && activeGuis.containsKey(player.getUniqueId())) {
                        plugin.getLogger().info("[DEBUG] Final cleanup ItemSelectorGUI for " + player.getName());
                        forceCleanup();
                        parentGUI.refreshAndReopen();
                    }
                }, 3L);
            }
        }
    }
}