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
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class ItemModificationGUI implements Listener {
    
    private final CustomKitDuels plugin;
    private final Player player;
    private final KitEditorGUI parentGUI;
    private final int targetSlot;
    private ItemStack targetItem;
    private final Inventory gui;
    private static final Map<UUID, ItemModificationGUI> activeGuis = new HashMap<>();
    private static final Set<UUID> waitingForStackSize = new HashSet<>();
    private boolean isActive = true;
    private boolean isNavigating = false;
    
    public ItemModificationGUI(CustomKitDuels plugin, Player player, KitEditorGUI parentGUI, int targetSlot, ItemStack targetItem) {
        this.plugin = plugin;
        this.player = player;
        this.parentGUI = parentGUI;
        this.targetSlot = targetSlot;
        this.targetItem = targetItem.clone();
        this.gui = Bukkit.createInventory(null, 27, ChatColor.DARK_RED + "Modify Item");
        
        plugin.getLogger().info("[DEBUG] Creating ItemModificationGUI for player " + player.getName() + " slot " + targetSlot);
        
        setupGUI();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    private void setupGUI() {
        gui.clear();
        
        // Display the item being modified
        gui.setItem(4, targetItem.clone());
        
        // Stack size option (only for stackable items)
        if (targetItem.getMaxStackSize() > 1) {
            ItemStack stackSizeItem = new ItemStack(Material.PAPER);
            ItemMeta stackMeta = stackSizeItem.getItemMeta();
            stackMeta.setDisplayName(ChatColor.YELLOW + "Change Stack Size");
            stackMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "Current: " + targetItem.getAmount(),
                ChatColor.GRAY + "Max: " + targetItem.getMaxStackSize(),
                ChatColor.GRAY + "Click to change stack size"
            ));
            stackSizeItem.setItemMeta(stackMeta);
            gui.setItem(10, stackSizeItem);
        }
        
        // Enchantments option (for enchantable items)
        if (canBeEnchanted(targetItem.getType())) {
            ItemStack enchantItem = new ItemStack(Material.ENCHANTED_BOOK);
            ItemMeta enchantMeta = enchantItem.getItemMeta();
            enchantMeta.setDisplayName(ChatColor.LIGHT_PURPLE + "Modify Enchantments");
            enchantMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "Add or modify enchantments",
                ChatColor.GRAY + "Click to open enchantment menu"
            ));
            enchantItem.setItemMeta(enchantMeta);
            gui.setItem(12, enchantItem);
        }
        
        // Potion effects (for potions)
        if (isPotionItem(targetItem.getType())) {
            ItemStack potionItem = new ItemStack(Material.BREWING_STAND);
            ItemMeta potionMeta = potionItem.getItemMeta();
            potionMeta.setDisplayName(ChatColor.AQUA + "Change Potion Type");
            potionMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "Change the potion effect",
                ChatColor.GRAY + "Click to open potion menu"
            ));
            potionItem.setItemMeta(potionMeta);
            gui.setItem(14, potionItem);
        }
        
        // Remove item option
        ItemStack removeItem = new ItemStack(Material.BARRIER);
        ItemMeta removeMeta = removeItem.getItemMeta();
        removeMeta.setDisplayName(ChatColor.RED + "Remove Item");
        removeMeta.setLore(Arrays.asList(ChatColor.GRAY + "Remove this item from the slot"));
        removeItem.setItemMeta(removeMeta);
        gui.setItem(16, removeItem);
        
        // Back button
        ItemStack backItem = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta backMeta = backItem.getItemMeta();
        backMeta.setDisplayName(ChatColor.RED + "Back");
        backMeta.setLore(Arrays.asList(ChatColor.GRAY + "Return to kit editor"));
        backItem.setItemMeta(backMeta);
        gui.setItem(22, backItem);
    }
    
    private boolean canBeEnchanted(Material material) {
        return material.toString().contains("SWORD") || 
               material.toString().contains("AXE") ||
               material.toString().contains("PICKAXE") ||
               material.toString().contains("SHOVEL") ||
               material.toString().contains("HOE") ||
               material.toString().contains("BOW") ||
               material.toString().contains("CROSSBOW") ||
               material.toString().contains("TRIDENT") ||
               material.toString().contains("HELMET") ||
               material.toString().contains("CHESTPLATE") ||
               material.toString().contains("LEGGINGS") ||
               material.toString().contains("BOOTS") ||
               material == Material.SHIELD ||
               material == Material.FISHING_ROD ||
               material == Material.SHEARS ||
               material == Material.FLINT_AND_STEEL ||
               material == Material.CARROT_ON_A_STICK ||
               material == Material.WARPED_FUNGUS_ON_A_STICK ||
               material == Material.ELYTRA ||
               material == Material.MACE;
    }
    
    private boolean isPotionItem(Material material) {
        return material == Material.POTION || 
               material == Material.SPLASH_POTION || 
               material == Material.LINGERING_POTION;
    }
    
    public void open() {
        plugin.getLogger().info("[DEBUG] Opening ItemModificationGUI for " + player.getName());
        
        ItemModificationGUI existing = activeGuis.get(player.getUniqueId());
        if (existing != null && existing != this) {
            plugin.getLogger().info("[DEBUG] Cleaning up existing ItemModificationGUI for " + player.getName());
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
        
        plugin.getLogger().info("[DEBUG] ItemModificationGUI click event - Player: " + player.getName() + ", Slot: " + slot);
        
        switch (slot) {
            case 10: // Stack size
                if (targetItem.getMaxStackSize() > 1) {
                    requestStackSize();
                }
                break;
            case 12: // Enchantments
                if (canBeEnchanted(targetItem.getType())) {
                    openEnchantmentMenu();
                }
                break;
            case 14: // Potion effects
                if (isPotionItem(targetItem.getType())) {
                    openPotionMenu();
                }
                break;
            case 16: // Remove item
                removeItem();
                break;
            case 22: // Back
                returnToParent();
                break;
        }
    }
    
    private void requestStackSize() {
        plugin.getLogger().info("[DEBUG] Requesting stack size from " + player.getName());
        waitingForStackSize.add(player.getUniqueId());
        isNavigating = true;
        player.closeInventory();
        player.sendMessage(ChatColor.YELLOW + "Enter the new stack size (1-" + targetItem.getMaxStackSize() + ") in chat:");
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (!waitingForStackSize.contains(event.getPlayer().getUniqueId())) {
            return;
        }
        
        Player chatPlayer = event.getPlayer();
        if (!chatPlayer.equals(player)) {
            return;
        }
        
        event.setCancelled(true);
        waitingForStackSize.remove(player.getUniqueId());
        
        String message = event.getMessage().trim();
        
        try {
            int stackSize = Integer.parseInt(message);
            if (stackSize < 1 || stackSize > targetItem.getMaxStackSize()) {
                player.sendMessage(ChatColor.RED + "Invalid stack size! Must be between 1 and " + targetItem.getMaxStackSize());
            } else {
                targetItem.setAmount(stackSize);
                player.sendMessage(ChatColor.GREEN + "Stack size set to " + stackSize + "!");
                
                // Update the item in the parent GUI
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    parentGUI.setSlotItem(targetSlot, targetItem);
                    returnToParent();
                });
                return;
            }
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Invalid number! Please enter a valid stack size.");
        }
        
        // Reopen the modification GUI if there was an error
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            isNavigating = false;
            player.openInventory(gui);
        });
    }
    
    private void openEnchantmentMenu() {
        plugin.getLogger().info("[DEBUG] Opening enchantment menu for " + player.getName());
        isNavigating = true;
        
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            new EnchantmentSelectorGUI(plugin, player, this, targetItem).open();
        }, 1L);
    }
    
    private void openPotionMenu() {
        plugin.getLogger().info("[DEBUG] Opening potion menu for " + player.getName());
        isNavigating = true;
        
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            new PotionSelectorGUI(plugin, player, this, targetItem).open();
        }, 1L);
    }
    
    private void removeItem() {
        plugin.getLogger().info("[DEBUG] Removing item from slot " + targetSlot);
        parentGUI.clearSlot(targetSlot);
        player.sendMessage(ChatColor.YELLOW + "Item removed from slot!");
        returnToParent();
    }
    
    private void returnToParent() {
        plugin.getLogger().info("[DEBUG] Returning to parent GUI for player " + player.getName());
        isNavigating = true;
        forceCleanup();
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            parentGUI.refreshAndReopen();
        }, 1L);
    }
    
    public void updateItem(ItemStack newItem) {
        this.targetItem = newItem.clone();
        parentGUI.setSlotItem(targetSlot, targetItem);
        setupGUI(); // Refresh the modification GUI
    }
    
    public void refreshAndReopen() {
        plugin.getLogger().info("[DEBUG] Refreshing and reopening ItemModificationGUI for " + player.getName());
        isNavigating = false;
        setupGUI();
        
        if (!player.getOpenInventory().getTopInventory().equals(gui)) {
            player.openInventory(gui);
        }
    }
    
    private void forceCleanup() {
        plugin.getLogger().info("[DEBUG] Force cleanup ItemModificationGUI for " + player.getName());
        isActive = false;
        activeGuis.remove(player.getUniqueId());
        waitingForStackSize.remove(player.getUniqueId());
        InventoryClickEvent.getHandlerList().unregister(this);
        InventoryCloseEvent.getHandlerList().unregister(this);
        AsyncPlayerChatEvent.getHandlerList().unregister(this);
        player.closeInventory();
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        Player closer = (Player) event.getPlayer();
        
        if (closer.equals(player) && event.getInventory().equals(gui)) {
            plugin.getLogger().info("[DEBUG] ItemModificationGUI inventory closed by " + player.getName() + ", Active: " + isActive + ", Navigating: " + isNavigating);
            
            // Don't cleanup if waiting for chat input or navigating
            if (waitingForStackSize.contains(player.getUniqueId()) || isNavigating) {
                return;
            }
            
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (isActive && !isNavigating && activeGuis.containsKey(player.getUniqueId())) {
                    plugin.getLogger().info("[DEBUG] Final cleanup ItemModificationGUI for " + player.getName());
                    forceCleanup();
                    parentGUI.refreshAndReopen();
                }
            }, 3L);
        }
    }
}