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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RoundsSelectorGUI implements Listener {
    
    private final CustomKitDuels plugin;
    private final Player challenger;
    private final Player target;
    private final Kit kit;
    private final Inventory gui;
    private static final Map<UUID, RoundsSelectorGUI> activeGuis = new HashMap<>();
    private boolean isActive = true;
    
    public RoundsSelectorGUI(CustomKitDuels plugin, Player challenger, Player target, Kit kit) {
        this.plugin = plugin;
        this.challenger = challenger;
        this.target = target;
        this.kit = kit;
        this.gui = Bukkit.createInventory(null, 27, ChatColor.GOLD + "Select Rounds to Win");
        
        plugin.getLogger().info("[DEBUG] Creating RoundsSelectorGUI for challenger " + challenger.getName() + " vs " + target.getName());
        
        setupGUI();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    private void setupGUI() {
        gui.clear();
        
        // Title item
        ItemStack titleItem = new ItemStack(Material.GOLDEN_SWORD);
        ItemMeta titleMeta = titleItem.getItemMeta();
        titleMeta.setDisplayName(ChatColor.GOLD + "Duel: " + challenger.getName() + " vs " + target.getName());
        titleMeta.setLore(Arrays.asList(
            ChatColor.GRAY + "Kit: " + kit.getName(),
            ChatColor.GRAY + "Select how many rounds to win the duel"
        ));
        titleItem.setItemMeta(titleMeta);
        gui.setItem(4, titleItem);
        
        // Round options (First to 1-10) using PAPER
        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21};
        
        for (int i = 0; i < 10; i++) {
            int rounds = i + 1;
            ItemStack roundItem = new ItemStack(Material.PAPER, rounds);
            ItemMeta roundMeta = roundItem.getItemMeta();
            roundMeta.setDisplayName(ChatColor.YELLOW + "First to " + rounds + " Round" + (rounds > 1 ? "s" : ""));
            roundMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "First player to win " + rounds + " round" + (rounds > 1 ? "s" : "") + " wins the duel",
                ChatColor.GREEN + "Click to select"
            ));
            roundItem.setItemMeta(roundMeta);
            gui.setItem(slots[i], roundItem);
        }
        
        // Cancel button
        ItemStack cancelItem = new ItemStack(Material.BARRIER);
        ItemMeta cancelMeta = cancelItem.getItemMeta();
        cancelMeta.setDisplayName(ChatColor.RED + "Cancel Duel");
        cancelMeta.setLore(Arrays.asList(ChatColor.GRAY + "Cancel the duel request"));
        cancelItem.setItemMeta(cancelMeta);
        gui.setItem(22, cancelItem);
    }
    
    public void open() {
        plugin.getLogger().info("[DEBUG] Opening RoundsSelectorGUI for " + challenger.getName());
        
        RoundsSelectorGUI existing = activeGuis.get(challenger.getUniqueId());
        if (existing != null && existing != this) {
            plugin.getLogger().info("[DEBUG] Cleaning up existing RoundsSelectorGUI for " + challenger.getName());
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
        
        plugin.getLogger().info("[DEBUG] RoundsSelectorGUI click event - Player: " + challenger.getName() + ", Slot: " + slot);
        
        if (slot == 22) { // Cancel
            plugin.getLogger().info("[DEBUG] Duel cancelled by " + challenger.getName());
            challenger.sendMessage(ChatColor.RED + "Duel request cancelled.");
            forceCleanup();
            return;
        }
        
        // Handle round selection
        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21};
        for (int i = 0; i < slots.length; i++) {
            if (slot == slots[i]) {
                int targetRounds = i + 1;
                plugin.getLogger().info("[DEBUG] Selected " + targetRounds + " rounds for duel");
                
                // Send duel request with rounds
                plugin.getDuelManager().sendRoundsDuelRequest(challenger, target, kit, targetRounds);
                forceCleanup();
                return;
            }
        }
    }
    
    private void forceCleanup() {
        plugin.getLogger().info("[DEBUG] Force cleanup RoundsSelectorGUI for " + challenger.getName());
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
            plugin.getLogger().info("[DEBUG] RoundsSelectorGUI inventory closed by " + challenger.getName() + ", Active: " + isActive);
            
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (isActive && activeGuis.containsKey(challenger.getUniqueId())) {
                    plugin.getLogger().info("[DEBUG] Final cleanup RoundsSelectorGUI for " + challenger.getName());
                    forceCleanup();
                }
            }, 3L);
        }
    }
}