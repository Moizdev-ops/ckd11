package com.yourname.customkitduels.gui;

import com.yourname.customkitduels.CustomKitDuels;
import com.yourname.customkitduels.data.Kit;
import com.yourname.customkitduels.utils.FontUtils;
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

public class KitListGUI implements Listener {
    
    private final CustomKitDuels plugin;
    private final Player player;
    private final Inventory gui;
    private final List<Kit> playerKits;
    private static final Map<UUID, KitListGUI> activeGuis = new HashMap<>();
    private boolean isActive = true;
    
    public KitListGUI(CustomKitDuels plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.playerKits = plugin.getKitManager().getPlayerKits(player.getUniqueId());
        this.gui = Bukkit.createInventory(null, 54, ChatColor.DARK_RED + FontUtils.toSmallCaps("edit your kits"));
        
        plugin.getLogger().info("[DEBUG] Creating KitListGUI for player " + player.getName());
        
        setupGUI();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    private void setupGUI() {
        gui.clear();
        
        if (playerKits.isEmpty()) {
            // No kits message
            ItemStack noKits = new ItemStack(Material.BARRIER);
            ItemMeta noKitsMeta = noKits.getItemMeta();
            noKitsMeta.setDisplayName(ChatColor.RED + FontUtils.toSmallCaps("no kits found"));
            noKitsMeta.setLore(Arrays.asList(
                ChatColor.GRAY + FontUtils.toSmallCaps("you don't have any kits!"),
                ChatColor.YELLOW + FontUtils.toSmallCaps("create one with /customkit create")
            ));
            noKits.setItemMeta(noKitsMeta);
            gui.setItem(22, noKits);
            return;
        }
        
        // Add kit items
        for (int i = 0; i < Math.min(playerKits.size(), 45); i++) {
            Kit kit = playerKits.get(i);
            
            ItemStack kitItem = new ItemStack(Material.BOOK);
            ItemMeta kitMeta = kitItem.getItemMeta();
            kitMeta.setDisplayName(ChatColor.RED + FontUtils.toSmallCaps(kit.getName()));
            
            // Get kit settings for display
            double hearts = plugin.getKitManager().getKitHearts(player.getUniqueId(), kit.getName());
            boolean naturalRegen = plugin.getKitManager().getKitNaturalRegen(player.getUniqueId(), kit.getName());
            boolean healthIndicators = plugin.getKitManager().getKitHealthIndicators(player.getUniqueId(), kit.getName());
            
            kitMeta.setLore(Arrays.asList(
                ChatColor.GRAY + FontUtils.toSmallCaps("hearts: ") + ChatColor.WHITE + hearts,
                ChatColor.GRAY + FontUtils.toSmallCaps("natural regen: ") + (naturalRegen ? ChatColor.GREEN + FontUtils.toSmallCaps("enabled") : ChatColor.RED + FontUtils.toSmallCaps("disabled")),
                ChatColor.GRAY + FontUtils.toSmallCaps("health indicators: ") + (healthIndicators ? ChatColor.GREEN + FontUtils.toSmallCaps("enabled") : ChatColor.RED + FontUtils.toSmallCaps("disabled")),
                "",
                ChatColor.YELLOW + FontUtils.toSmallCaps("click to edit this kit")
            ));
            kitItem.setItemMeta(kitMeta);
            gui.setItem(i, kitItem);
        }
        
        // Back button
        ItemStack backButton = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta backMeta = backButton.getItemMeta();
        backMeta.setDisplayName(ChatColor.RED + FontUtils.toSmallCaps("close"));
        backMeta.setLore(Arrays.asList(ChatColor.GRAY + FontUtils.toSmallCaps("close kit editor")));
        backButton.setItemMeta(backMeta);
        gui.setItem(53, backButton);
    }
    
    public void open() {
        plugin.getLogger().info("[DEBUG] Opening KitListGUI for " + player.getName());
        
        KitListGUI existing = activeGuis.get(player.getUniqueId());
        if (existing != null && existing != this) {
            plugin.getLogger().info("[DEBUG] Cleaning up existing KitListGUI for " + player.getName());
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
        
        plugin.getLogger().info("[DEBUG] KitListGUI click event - Player: " + player.getName() + ", Slot: " + slot);
        
        if (slot == 53) { // Close button
            forceCleanup();
            return;
        }
        
        // Handle kit selection
        if (slot < playerKits.size()) {
            Kit selectedKit = playerKits.get(slot);
            
            if (selectedKit != null) {
                plugin.getLogger().info("[DEBUG] Opening kit editor for " + selectedKit.getName());
                forceCleanup();
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    new KitEditorGUI(plugin, player, selectedKit.getName(), false).open();
                }, 1L);
            }
        }
    }
    
    private void forceCleanup() {
        plugin.getLogger().info("[DEBUG] Force cleanup KitListGUI for " + player.getName());
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
            plugin.getLogger().info("[DEBUG] KitListGUI inventory closed by " + player.getName() + ", Active: " + isActive);
            
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (isActive && activeGuis.containsKey(player.getUniqueId())) {
                    plugin.getLogger().info("[DEBUG] Final cleanup KitListGUI for " + player.getName());
                    forceCleanup();
                }
            }, 3L);
        }
    }
}