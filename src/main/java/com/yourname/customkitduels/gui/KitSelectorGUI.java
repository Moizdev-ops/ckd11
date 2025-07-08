package com.yourname.customkitduels.gui;

import com.yourname.customkitduels.CustomKitDuels;
import com.yourname.customkitduels.data.Kit;
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

public class KitSelectorGUI implements Listener {
    
    private final CustomKitDuels plugin;
    private final Player challenger;
    private final Player target;
    private final Inventory gui;
    private final List<Kit> playerKits;
    private static final Map<UUID, KitSelectorGUI> activeGuis = new HashMap<>();
    private boolean isActive = true;
    
    public KitSelectorGUI(CustomKitDuels plugin, Player challenger, Player target) {
        this.plugin = plugin;
        this.challenger = challenger;
        this.target = target;
        this.playerKits = plugin.getKitManager().getPlayerKits(challenger.getUniqueId());
        this.gui = Bukkit.createInventory(null, 27, ChatColor.DARK_RED + "select your kit");
        
        plugin.getLogger().info("[DEBUG] Creating KitSelectorGUI for challenger " + challenger.getName() + " vs " + target.getName());
        
        setupGUI();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    private void setupGUI() {
        gui.clear();
        
        if (playerKits.isEmpty()) {
            // No kits message
            ItemStack noKits = new ItemStack(Material.BARRIER);
            ItemMeta noKitsMeta = noKits.getItemMeta();
            noKitsMeta.setDisplayName(ChatColor.RED + "no kits found");
            noKitsMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "you don't have any kits!",
                ChatColor.YELLOW + "create one with /customkit create"
            ));
            noKits.setItemMeta(noKitsMeta);
            gui.setItem(13, noKits);
            return;
        }
        
        // Add kit items (middle row - slots 10-16)
        int[] kitSlots = {10, 11, 12, 13, 14, 15, 16};
        for (int i = 0; i < Math.min(playerKits.size(), 7); i++) {
            Kit kit = playerKits.get(i);
            
            // Use book as kit icon
            ItemStack kitItem = new ItemStack(Material.BOOK);
            ItemMeta kitMeta = kitItem.getItemMeta();
            kitMeta.setDisplayName(ChatColor.RED + kit.getName());
            
            // Get kit settings for display
            double hearts = plugin.getKitManager().getKitHearts(challenger.getUniqueId(), kit.getName());
            boolean naturalRegen = plugin.getKitManager().getKitNaturalRegen(challenger.getUniqueId(), kit.getName());
            boolean healthIndicators = plugin.getKitManager().getKitHealthIndicators(challenger.getUniqueId(), kit.getName());
            
            kitMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "challenge " + target.getName() + " with this kit",
                ChatColor.GRAY + "hearts: " + ChatColor.WHITE + hearts,
                ChatColor.GRAY + "natural regen: " + (naturalRegen ? ChatColor.GREEN + "enabled" : ChatColor.RED + "disabled"),
                "",
                ChatColor.GREEN + "click to select"
            ));
            kitItem.setItemMeta(kitMeta);
            gui.setItem(kitSlots[i], kitItem);
        }
        
        // Cancel button
        ItemStack cancelItem = new ItemStack(Material.BARRIER);
        ItemMeta cancelMeta = cancelItem.getItemMeta();
        cancelMeta.setDisplayName(ChatColor.RED + "cancel");
        cancelMeta.setLore(Arrays.asList(ChatColor.GRAY + "cancel duel request"));
        cancelItem.setItemMeta(cancelMeta);
        gui.setItem(22, cancelItem);
    }
    
    public void open() {
        plugin.getLogger().info("[DEBUG] Opening KitSelectorGUI for " + challenger.getName());
        
        KitSelectorGUI existing = activeGuis.get(challenger.getUniqueId());
        if (existing != null && existing != this) {
            plugin.getLogger().info("[DEBUG] Cleaning up existing KitSelectorGUI for " + challenger.getName());
            existing.forceCleanup();
        }
        
        activeGuis.put(challenger.getUniqueId(), this);
        isActive = true;
        challenger.openInventory(gui);
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player clicker = (Player) event.getWhoClicked();
        
        if (!clicker.equals(challenger) || !event.getInventory().equals(gui) || !isActive) {
            return;
        }
        
        event.setCancelled(true);
        int slot = event.getSlot();
        
        plugin.getLogger().info("[DEBUG] KitSelectorGUI click event - Player: " + challenger.getName() + ", Slot: " + slot);
        
        if (slot == 22) { // Cancel
            plugin.getLogger().info("[DEBUG] Duel cancelled by " + challenger.getName());
            challenger.sendMessage(ChatColor.RED + "duel request cancelled.");
            forceCleanup();
            return;
        }
        
        // Handle kit selection (slots 10-16)
        int[] kitSlots = {10, 11, 12, 13, 14, 15, 16};
        for (int i = 0; i < kitSlots.length; i++) {
            if (slot == kitSlots[i] && i < playerKits.size()) {
                Kit selectedKit = playerKits.get(i);
                plugin.getLogger().info("[DEBUG] Selected kit: " + selectedKit.getName());
                
                // Open rounds selector
                forceCleanup();
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    new RoundsSelectorGUI(plugin, challenger, target, selectedKit).open();
                }, 1L);
                return;
            }
        }
    }
    
    private void forceCleanup() {
        plugin.getLogger().info("[DEBUG] Force cleanup KitSelectorGUI for " + challenger.getName());
        isActive = false;
        activeGuis.remove(challenger.getUniqueId());
        InventoryClickEvent.getHandlerList().unregister(this);
        InventoryCloseEvent.getHandlerList().unregister(this);
        challenger.closeInventory();
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        Player closer = (Player) event.getPlayer();
        
        if (closer.equals(challenger) && event.getInventory().equals(gui)) {
            plugin.getLogger().info("[DEBUG] KitSelectorGUI inventory closed by " + challenger.getName() + ", Active: " + isActive);
            
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (isActive && activeGuis.containsKey(challenger.getUniqueId())) {
                    plugin.getLogger().info("[DEBUG] Final cleanup KitSelectorGUI for " + challenger.getName());
                    forceCleanup();
                }
            }, 3L);
        }
    }
}