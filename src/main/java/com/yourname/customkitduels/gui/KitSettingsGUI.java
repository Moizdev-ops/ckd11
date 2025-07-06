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

public class KitSettingsGUI implements Listener {
    
    private final CustomKitDuels plugin;
    private final Player player;
    private final KitEditorGUI parentGUI;
    private final String kitName;
    private final Inventory gui;
    private static final Map<UUID, KitSettingsGUI> activeGuis = new HashMap<>();
    private static final Set<UUID> waitingForHearts = new HashSet<>();
    private boolean isActive = true;
    
    public KitSettingsGUI(CustomKitDuels plugin, Player player, KitEditorGUI parentGUI, String kitName) {
        this.plugin = plugin;
        this.player = player;
        this.parentGUI = parentGUI;
        this.kitName = kitName;
        this.gui = Bukkit.createInventory(null, 27, ChatColor.DARK_PURPLE + "Kit Settings: " + kitName);
        
        plugin.getLogger().info("[DEBUG] Creating KitSettingsGUI for player " + player.getName() + " kit " + kitName);
        
        setupGUI();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    private void setupGUI() {
        gui.clear();
        
        // Get current settings
        double currentHearts = plugin.getKitManager().getKitHearts(player.getUniqueId(), kitName);
        boolean naturalRegen = plugin.getKitManager().getKitNaturalRegen(player.getUniqueId(), kitName);
        
        // Kit Hearts setting
        ItemStack heartsItem = new ItemStack(Material.RED_DYE);
        ItemMeta heartsMeta = heartsItem.getItemMeta();
        heartsMeta.setDisplayName(ChatColor.RED + "❤ Kit Hearts");
        heartsMeta.setLore(Arrays.asList(
            ChatColor.GRAY + "Current: " + ChatColor.WHITE + currentHearts + " hearts",
            ChatColor.GRAY + "Players will have this much health",
            ChatColor.YELLOW + "Range: 1-30 hearts",
            ChatColor.GREEN + "Click to change"
        ));
        heartsItem.setItemMeta(heartsMeta);
        gui.setItem(11, heartsItem);
        
        // Natural Health Regeneration setting
        ItemStack regenItem = new ItemStack(naturalRegen ? Material.GOLDEN_APPLE : Material.APPLE);
        ItemMeta regenMeta = regenItem.getItemMeta();
        regenMeta.setDisplayName(ChatColor.YELLOW + "🍖 Natural Health Regen");
        regenMeta.setLore(Arrays.asList(
            ChatColor.GRAY + "Status: " + (naturalRegen ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled"),
            ChatColor.GRAY + "Allow health regen from saturation",
            ChatColor.GREEN + "Click to toggle"
        ));
        regenItem.setItemMeta(regenMeta);
        gui.setItem(13, regenItem);
        
        // Reset to defaults button
        ItemStack resetItem = new ItemStack(Material.BARRIER);
        ItemMeta resetMeta = resetItem.getItemMeta();
        resetMeta.setDisplayName(ChatColor.YELLOW + "Reset to Defaults");
        resetMeta.setLore(Arrays.asList(
            ChatColor.GRAY + "10 hearts, natural regen enabled",
            ChatColor.GREEN + "Click to reset"
        ));
        resetItem.setItemMeta(resetMeta);
        gui.setItem(15, resetItem);
        
        // Back button
        ItemStack backItem = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta backMeta = backItem.getItemMeta();
        backMeta.setDisplayName(ChatColor.RED + "Back");
        backMeta.setLore(Arrays.asList(ChatColor.GRAY + "Return to kit editor"));
        backItem.setItemMeta(backMeta);
        gui.setItem(22, backItem);
    }
    
    public void open() {
        plugin.getLogger().info("[DEBUG] Opening KitSettingsGUI for " + player.getName());
        
        KitSettingsGUI existing = activeGuis.get(player.getUniqueId());
        if (existing != null && existing != this) {
            plugin.getLogger().info("[DEBUG] Cleaning up existing KitSettingsGUI for " + player.getName());
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
        
        plugin.getLogger().info("[DEBUG] KitSettingsGUI click event - Player: " + player.getName() + ", Slot: " + slot);
        
        switch (slot) {
            case 11: // Kit Hearts
                requestHearts();
                break;
            case 13: // Natural Regen
                toggleNaturalRegen();
                break;
            case 15: // Reset to defaults
                resetToDefaults();
                break;
            case 22: // Back
                returnToParent();
                break;
        }
    }
    
    private void requestHearts() {
        plugin.getLogger().info("[DEBUG] Requesting hearts from " + player.getName());
        waitingForHearts.add(player.getUniqueId());
        player.closeInventory();
        player.sendMessage(ChatColor.YELLOW + "Enter the number of hearts (1-30) in chat:");
        player.sendMessage(ChatColor.GRAY + "Example: 10 for 10 hearts (20 health)");
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (!waitingForHearts.contains(event.getPlayer().getUniqueId())) {
            return;
        }
        
        Player chatPlayer = event.getPlayer();
        if (!chatPlayer.equals(player)) {
            return;
        }
        
        event.setCancelled(true);
        waitingForHearts.remove(player.getUniqueId());
        
        String message = event.getMessage().trim();
        
        try {
            double hearts = Double.parseDouble(message);
            if (hearts < 1 || hearts > 30) {
                player.sendMessage(ChatColor.RED + "Invalid hearts! Must be between 1 and 30");
            } else {
                plugin.getKitManager().setKitHearts(player.getUniqueId(), kitName, hearts);
                player.sendMessage(ChatColor.GREEN + "Kit hearts set to " + hearts + "!");
                
                // Reopen the settings GUI
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    setupGUI();
                    player.openInventory(gui);
                });
                return;
            }
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Invalid number! Please enter a valid number of hearts.");
        }
        
        // Reopen the settings GUI if there was an error
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            player.openInventory(gui);
        });
    }
    
    private void toggleNaturalRegen() {
        boolean currentRegen = plugin.getKitManager().getKitNaturalRegen(player.getUniqueId(), kitName);
        boolean newRegen = !currentRegen;
        
        plugin.getKitManager().setKitNaturalRegen(player.getUniqueId(), kitName, newRegen);
        
        player.sendMessage(ChatColor.GREEN + "Natural health regeneration " + 
                          (newRegen ? "enabled" : "disabled") + " for kit " + kitName + "!");
        
        setupGUI(); // Refresh GUI
    }
    
    private void resetToDefaults() {
        plugin.getKitManager().setKitHearts(player.getUniqueId(), kitName, 10.0); // Changed default to 10
        plugin.getKitManager().setKitNaturalRegen(player.getUniqueId(), kitName, true);
        
        player.sendMessage(ChatColor.GREEN + "Kit settings reset to defaults!");
        setupGUI(); // Refresh GUI
    }
    
    private void returnToParent() {
        plugin.getLogger().info("[DEBUG] Returning to parent GUI for player " + player.getName());
        forceCleanup();
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            parentGUI.refreshAndReopen();
        }, 1L);
    }
    
    private void forceCleanup() {
        plugin.getLogger().info("[DEBUG] Force cleanup KitSettingsGUI for " + player.getName());
        isActive = false;
        activeGuis.remove(player.getUniqueId());
        waitingForHearts.remove(player.getUniqueId());
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
            plugin.getLogger().info("[DEBUG] KitSettingsGUI inventory closed by " + player.getName() + ", Active: " + isActive);
            
            // Don't cleanup if waiting for chat input
            if (waitingForHearts.contains(player.getUniqueId())) {
                return;
            }
            
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (isActive && activeGuis.containsKey(player.getUniqueId())) {
                    plugin.getLogger().info("[DEBUG] Final cleanup KitSettingsGUI for " + player.getName());
                    forceCleanup();
                    parentGUI.refreshAndReopen();
                }
            }, 3L);
        }
    }
}