package com.yourname.customkitduels.gui;

import com.yourname.customkitduels.CustomKitDuels;
import com.yourname.customkitduels.data.Arena;
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

public class ArenaListGUI implements Listener {
    
    private final CustomKitDuels plugin;
    private final Player player;
    private final Inventory gui;
    private final List<String> arenaNames;
    private static final Map<UUID, ArenaListGUI> activeGuis = new HashMap<>();
    private boolean isActive = true;
    
    public ArenaListGUI(CustomKitDuels plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.arenaNames = plugin.getArenaManager().getAllArenas();
        this.gui = Bukkit.createInventory(null, 54, ChatColor.GOLD + "Select Arena to Edit");
        
        plugin.getLogger().info("[DEBUG] Creating ArenaListGUI for player " + player.getName());
        
        setupGUI();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    private void setupGUI() {
        gui.clear();
        
        if (arenaNames.isEmpty()) {
            // No arenas message
            ItemStack noArenas = new ItemStack(Material.BARRIER);
            ItemMeta noArenasMeta = noArenas.getItemMeta();
            noArenasMeta.setDisplayName(ChatColor.RED + "No Arenas Found");
            noArenasMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "Create an arena first with:",
                ChatColor.YELLOW + "/ckd arena create <name>"
            ));
            noArenas.setItemMeta(noArenasMeta);
            gui.setItem(22, noArenas);
            return;
        }
        
        // Add arena items
        for (int i = 0; i < Math.min(arenaNames.size(), 45); i++) {
            String arenaName = arenaNames.get(i);
            Arena arena = plugin.getArenaManager().getArena(arenaName);
            
            ItemStack arenaItem = new ItemStack(arena.isComplete() ? Material.EMERALD_BLOCK : Material.REDSTONE_BLOCK);
            ItemMeta arenaMeta = arenaItem.getItemMeta();
            arenaMeta.setDisplayName(ChatColor.AQUA + arenaName);
            
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Status: " + (arena.isComplete() ? ChatColor.GREEN + "Complete" : ChatColor.RED + "Incomplete"));
            lore.add(ChatColor.GRAY + "Regeneration: " + (arena.hasRegeneration() ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled"));
            lore.add(ChatColor.YELLOW + "Click to edit this arena");
            
            arenaMeta.setLore(lore);
            arenaItem.setItemMeta(arenaMeta);
            gui.setItem(i, arenaItem);
        }
        
        // Back button
        ItemStack backButton = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta backMeta = backButton.getItemMeta();
        backMeta.setDisplayName(ChatColor.RED + "Close");
        backMeta.setLore(Arrays.asList(ChatColor.GRAY + "Close arena editor"));
        backButton.setItemMeta(backMeta);
        gui.setItem(53, backButton);
    }
    
    public void open() {
        plugin.getLogger().info("[DEBUG] Opening ArenaListGUI for " + player.getName());
        
        ArenaListGUI existing = activeGuis.get(player.getUniqueId());
        if (existing != null && existing != this) {
            plugin.getLogger().info("[DEBUG] Cleaning up existing ArenaListGUI for " + player.getName());
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
        
        plugin.getLogger().info("[DEBUG] ArenaListGUI click event - Player: " + player.getName() + ", Slot: " + slot);
        
        if (slot == 53) { // Close button
            forceCleanup();
            return;
        }
        
        // Handle arena selection
        if (slot < arenaNames.size()) {
            String arenaName = arenaNames.get(slot);
            Arena arena = plugin.getArenaManager().getArena(arenaName);
            
            if (arena != null) {
                plugin.getLogger().info("[DEBUG] Opening arena editor for " + arenaName);
                forceCleanup();
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    new ArenaEditorGUI(plugin, player, arena).open();
                }, 1L);
            }
        }
    }
    
    private void forceCleanup() {
        plugin.getLogger().info("[DEBUG] Force cleanup ArenaListGUI for " + player.getName());
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
            plugin.getLogger().info("[DEBUG] ArenaListGUI inventory closed by " + player.getName() + ", Active: " + isActive);
            
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (isActive && activeGuis.containsKey(player.getUniqueId())) {
                    plugin.getLogger().info("[DEBUG] Final cleanup ArenaListGUI for " + player.getName());
                    forceCleanup();
                }
            }, 3L);
        }
    }
}