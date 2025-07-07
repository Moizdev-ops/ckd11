package com.yourname.customkitduels.gui;

import com.yourname.customkitduels.CustomKitDuels;
import com.yourname.customkitduels.data.Arena;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class ArenaEditorGUI implements Listener {
    
    private final CustomKitDuels plugin;
    private final Player player;
    private final Arena arena;
    private final Inventory gui;
    private static final Map<UUID, ArenaEditorGUI> activeGuis = new HashMap<>();
    private static final Set<UUID> waitingForPosition = new HashSet<>();
    private static final Map<UUID, String> positionType = new HashMap<>();
    private boolean isActive = true;
    
    public ArenaEditorGUI(CustomKitDuels plugin, Player player, Arena arena) {
        this.plugin = plugin;
        this.player = player;
        this.arena = arena;
        this.gui = Bukkit.createInventory(null, 45, ChatColor.DARK_GREEN + "Arena Editor: " + arena.getName());
        
        plugin.getLogger().info("[DEBUG] Creating ArenaEditorGUI for player " + player.getName() + " arena " + arena.getName());
        
        setupGUI();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    private void setupGUI() {
        gui.clear();
        
        // Arena info display
        ItemStack infoItem = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = infoItem.getItemMeta();
        infoMeta.setDisplayName(ChatColor.GOLD + "Arena: " + arena.getName());
        infoMeta.setLore(Arrays.asList(
            ChatColor.GRAY + "Configure your arena settings",
            ChatColor.YELLOW + "Status: " + (arena.isComplete() ? ChatColor.GREEN + "Complete" : ChatColor.RED + "Incomplete"),
            ChatColor.AQUA + "Regeneration: " + (arena.hasRegeneration() ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled")
        ));
        infoItem.setItemMeta(infoMeta);
        gui.setItem(4, infoItem);
        
        // Position 1 button
        ItemStack pos1Item = new ItemStack(arena.getPos1() != null ? Material.LIME_CONCRETE : Material.RED_CONCRETE);
        ItemMeta pos1Meta = pos1Item.getItemMeta();
        pos1Meta.setDisplayName(ChatColor.YELLOW + "Position 1 (Corner 1)");
        if (arena.getPos1() != null) {
            pos1Meta.setLore(Arrays.asList(
                ChatColor.GREEN + "✓ Set at: " + formatLocation(arena.getPos1()),
                ChatColor.GRAY + "Click to change position",
                ChatColor.AQUA + "Then shift+left-click in air"
            ));
        } else {
            pos1Meta.setLore(Arrays.asList(
                ChatColor.RED + "✗ Not set",
                ChatColor.GRAY + "Click to set position",
                ChatColor.AQUA + "Then shift+left-click in air"
            ));
        }
        pos1Item.setItemMeta(pos1Meta);
        gui.setItem(10, pos1Item);
        
        // Position 2 button
        ItemStack pos2Item = new ItemStack(arena.getPos2() != null ? Material.LIME_CONCRETE : Material.RED_CONCRETE);
        ItemMeta pos2Meta = pos2Item.getItemMeta();
        pos2Meta.setDisplayName(ChatColor.YELLOW + "Position 2 (Corner 2)");
        if (arena.getPos2() != null) {
            pos2Meta.setLore(Arrays.asList(
                ChatColor.GREEN + "✓ Set at: " + formatLocation(arena.getPos2()),
                ChatColor.GRAY + "Click to change position",
                ChatColor.AQUA + "Then shift+left-click in air"
            ));
        } else {
            pos2Meta.setLore(Arrays.asList(
                ChatColor.RED + "✗ Not set",
                ChatColor.GRAY + "Click to set position",
                ChatColor.AQUA + "Then shift+left-click in air"
            ));
        }
        pos2Item.setItemMeta(pos2Meta);
        gui.setItem(12, pos2Item);
        
        // Spawn 1 button
        ItemStack spawn1Item = new ItemStack(arena.getSpawn1() != null ? Material.LIME_WOOL : Material.RED_WOOL);
        ItemMeta spawn1Meta = spawn1Item.getItemMeta();
        spawn1Meta.setDisplayName(ChatColor.AQUA + "Spawn Point 1");
        if (arena.getSpawn1() != null) {
            spawn1Meta.setLore(Arrays.asList(
                ChatColor.GREEN + "✓ Set at: " + formatLocation(arena.getSpawn1()),
                ChatColor.GRAY + "Click to change spawn point",
                ChatColor.AQUA + "Then shift+left-click in air"
            ));
        } else {
            spawn1Meta.setLore(Arrays.asList(
                ChatColor.RED + "✗ Not set",
                ChatColor.GRAY + "Click to set spawn point",
                ChatColor.AQUA + "Then shift+left-click in air"
            ));
        }
        spawn1Item.setItemMeta(spawn1Meta);
        gui.setItem(14, spawn1Item);
        
        // Spawn 2 button
        ItemStack spawn2Item = new ItemStack(arena.getSpawn2() != null ? Material.LIME_WOOL : Material.RED_WOOL);
        ItemMeta spawn2Meta = spawn2Item.getItemMeta();
        spawn2Meta.setDisplayName(ChatColor.AQUA + "Spawn Point 2");
        if (arena.getSpawn2() != null) {
            spawn2Meta.setLore(Arrays.asList(
                ChatColor.GREEN + "✓ Set at: " + formatLocation(arena.getSpawn2()),
                ChatColor.GRAY + "Click to change spawn point",
                ChatColor.AQUA + "Then shift+left-click in air"
            ));
        } else {
            spawn2Meta.setLore(Arrays.asList(
                ChatColor.RED + "✗ Not set",
                ChatColor.GRAY + "Click to set spawn point",
                ChatColor.AQUA + "Then shift+left-click in air"
            ));
        }
        spawn2Item.setItemMeta(spawn2Meta);
        gui.setItem(16, spawn2Item);
        
        // Regeneration toggle
        ItemStack regenItem = new ItemStack(arena.hasRegeneration() ? Material.EMERALD : Material.REDSTONE);
        ItemMeta regenMeta = regenItem.getItemMeta();
        regenMeta.setDisplayName(ChatColor.LIGHT_PURPLE + "Arena Regeneration");
        if (arena.hasRegeneration()) {
            regenMeta.setLore(Arrays.asList(
                ChatColor.GREEN + "✓ Enabled",
                ChatColor.GRAY + "Arena will regenerate after each round",
                ChatColor.YELLOW + "Schematic: " + arena.getSchematicName(),
                ChatColor.AQUA + "Click to disable"
            ));
        } else {
            regenMeta.setLore(Arrays.asList(
                ChatColor.RED + "✗ Disabled",
                ChatColor.GRAY + "Arena will not regenerate",
                ChatColor.AQUA + "Click to enable",
                ChatColor.YELLOW + "Note: Requires FAWE plugin"
            ));
        }
        regenItem.setItemMeta(regenMeta);
        gui.setItem(22, regenItem);
        
        // Save button
        ItemStack saveItem = new ItemStack(Material.EMERALD_BLOCK);
        ItemMeta saveMeta = saveItem.getItemMeta();
        saveMeta.setDisplayName(ChatColor.GREEN + "Save Arena");
        saveMeta.setLore(Arrays.asList(
            ChatColor.GRAY + "Save all changes to the arena",
            arena.isComplete() ? ChatColor.GREEN + "Arena is ready for use!" : ChatColor.RED + "Arena needs all positions set"
        ));
        saveItem.setItemMeta(saveMeta);
        gui.setItem(39, saveItem);
        
        // Cancel button
        ItemStack cancelItem = new ItemStack(Material.BARRIER);
        ItemMeta cancelMeta = cancelItem.getItemMeta();
        cancelMeta.setDisplayName(ChatColor.RED + "Cancel");
        cancelMeta.setLore(Arrays.asList(ChatColor.GRAY + "Close without saving"));
        cancelItem.setItemMeta(cancelMeta);
        gui.setItem(41, cancelItem);
    }
    
    private String formatLocation(org.bukkit.Location loc) {
        return String.format("%.0f, %.0f, %.0f", loc.getX(), loc.getY(), loc.getZ());
    }
    
    public void open() {
        plugin.getLogger().info("[DEBUG] Opening ArenaEditorGUI for " + player.getName());
        
        ArenaEditorGUI existing = activeGuis.get(player.getUniqueId());
        if (existing != null && existing != this) {
            plugin.getLogger().info("[DEBUG] Cleaning up existing ArenaEditorGUI for " + player.getName());
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
        
        plugin.getLogger().info("[DEBUG] ArenaEditorGUI click event - Player: " + player.getName() + ", Slot: " + slot);
        
        switch (slot) {
            case 10: // Position 1
                startPositionSetting("pos1");
                break;
            case 12: // Position 2
                startPositionSetting("pos2");
                break;
            case 14: // Spawn 1
                startPositionSetting("spawn1");
                break;
            case 16: // Spawn 2
                startPositionSetting("spawn2");
                break;
            case 22: // Regeneration toggle
                toggleRegeneration();
                break;
            case 39: // Save
                saveArena();
                break;
            case 41: // Cancel
                forceCleanup();
                break;
        }
    }
    
    private void startPositionSetting(String type) {
        waitingForPosition.add(player.getUniqueId());
        positionType.put(player.getUniqueId(), type);
        
        player.closeInventory();
        
        String typeName = type.equals("pos1") ? "Position 1" : 
                         type.equals("pos2") ? "Position 2" :
                         type.equals("spawn1") ? "Spawn Point 1" : "Spawn Point 2";
        
        player.sendMessage(ChatColor.YELLOW + "Setting " + typeName + " for arena " + arena.getName());
        player.sendMessage(ChatColor.AQUA + "Shift+Left-click in air to set the position");
        
        // Send action bar message using Spigot API
        try {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.GOLD + "Shift+Left-click in air to set position"));
        } catch (Exception e) {
            // Fallback if action bar fails
            player.sendMessage(ChatColor.GOLD + "Shift+Left-click in air to set position");
        }
        
        player.sendMessage(ChatColor.GRAY + "Type 'cancel' in chat to cancel");
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!waitingForPosition.contains(event.getPlayer().getUniqueId())) {
            return;
        }
        
        Player interactPlayer = event.getPlayer();
        if (!interactPlayer.equals(player)) {
            return;
        }
        
        if (event.getAction() == Action.LEFT_CLICK_AIR && interactPlayer.isSneaking()) {
            event.setCancelled(true);
            
            String type = positionType.remove(player.getUniqueId());
            waitingForPosition.remove(player.getUniqueId());
            
            org.bukkit.Location playerLocation = player.getLocation();
            
            switch (type) {
                case "pos1":
                    arena.setPos1(playerLocation);
                    player.sendMessage(ChatColor.GREEN + "Position 1 set at " + formatLocation(playerLocation));
                    break;
                case "pos2":
                    arena.setPos2(playerLocation);
                    player.sendMessage(ChatColor.GREEN + "Position 2 set at " + formatLocation(playerLocation));
                    break;
                case "spawn1":
                    arena.setSpawn1(playerLocation);
                    player.sendMessage(ChatColor.GREEN + "Spawn Point 1 set at " + formatLocation(playerLocation));
                    break;
                case "spawn2":
                    arena.setSpawn2(playerLocation);
                    player.sendMessage(ChatColor.GREEN + "Spawn Point 2 set at " + formatLocation(playerLocation));
                    break;
            }
            
            // Reopen GUI after a short delay
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                setupGUI();
                player.openInventory(gui);
            }, 10L);
        }
    }
    
    private void toggleRegeneration() {
        arena.setRegeneration(!arena.hasRegeneration());
        
        if (arena.hasRegeneration()) {
            player.sendMessage(ChatColor.GREEN + "Arena regeneration enabled!");
            // Auto-generate schematic if positions are set
            if (arena.getPos1() != null && arena.getPos2() != null) {
                try {
                    plugin.getArenaManager().generateSchematic(arena, player);
                    player.sendMessage(ChatColor.GREEN + "Arena regeneration enabled!");
                } catch (Exception e) {
                    player.sendMessage(ChatColor.RED + "Failed to enable regeneration: " + e.getMessage());
                    arena.setRegeneration(false); // Disable if failed
                }
            } else {
                player.sendMessage(ChatColor.YELLOW + "Arena regeneration enabled! Set positions to generate schematic.");
            }
        } else {
            player.sendMessage(ChatColor.YELLOW + "Arena regeneration disabled.");
        }
        
        setupGUI(); // Refresh GUI
    }
    
    private void saveArena() {
        plugin.getArenaManager().saveArena(arena);
        
        if (arena.isComplete()) {
            player.sendMessage(ChatColor.GREEN + "Arena '" + arena.getName() + "' saved successfully!");
            player.sendMessage(ChatColor.AQUA + "Arena is ready for duels!");
        } else {
            player.sendMessage(ChatColor.YELLOW + "Arena '" + arena.getName() + "' saved, but it's not complete yet.");
            player.sendMessage(ChatColor.GRAY + "Set all positions and spawn points to make it available for duels.");
        }
        
        forceCleanup();
    }
    
    private void forceCleanup() {
        plugin.getLogger().info("[DEBUG] Force cleanup ArenaEditorGUI for " + player.getName());
        isActive = false;
        activeGuis.remove(player.getUniqueId());
        waitingForPosition.remove(player.getUniqueId());
        positionType.remove(player.getUniqueId());
        InventoryClickEvent.getHandlerList().unregister(this);
        InventoryCloseEvent.getHandlerList().unregister(this);
        PlayerInteractEvent.getHandlerList().unregister(this);
        player.closeInventory();
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        Player closer = (Player) event.getPlayer();
        
        if (closer.equals(player) && event.getInventory().equals(gui)) {
            plugin.getLogger().info("[DEBUG] ArenaEditorGUI inventory closed by " + player.getName() + ", Active: " + isActive);
            
            // Don't cleanup if waiting for position setting
            if (waitingForPosition.contains(player.getUniqueId())) {
                return;
            }
            
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (isActive && activeGuis.containsKey(player.getUniqueId())) {
                    plugin.getLogger().info("[DEBUG] Final cleanup ArenaEditorGUI for " + player.getName());
                    forceCleanup();
                }
            }, 3L);
        }
    }
}