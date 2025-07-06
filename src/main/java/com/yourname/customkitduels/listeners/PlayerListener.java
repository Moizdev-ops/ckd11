package com.yourname.customkitduels.listeners;

import com.yourname.customkitduels.CustomKitDuels;
import com.yourname.customkitduels.data.Duel;
import com.yourname.customkitduels.data.RoundsDuel;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class PlayerListener implements Listener {
    
    private final CustomKitDuels plugin;
    
    public PlayerListener(CustomKitDuels plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Reset player health to default (10 hearts = 20 health points)
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            try {
                player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(20.0);
                player.setHealth(20.0);
                plugin.getLogger().info("Reset health for joining player: " + player.getName());
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to reset health for joining player " + player.getName() + ": " + e.getMessage());
            }
        }, 20L); // Wait 1 second after join
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        // Stop health display
        plugin.getHealthDisplayManager().stopHealthDisplay(player);
        
        // Check if player is in a duel
        if (plugin.getDuelManager().isInAnyDuel(player)) {
            // Get opponent before ending duel
            Player opponent = null;
            RoundsDuel roundsDuel = plugin.getDuelManager().getRoundsDuel(player);
            if (roundsDuel != null) {
                opponent = roundsDuel.getOpponent(player);
            } else {
                Duel duel = plugin.getDuelManager().getDuel(player);
                if (duel != null) {
                    opponent = duel.getOpponent(player);
                }
            }
            
            // End the duel when player quits
            plugin.getDuelManager().endDuel(player, true);
            
            // Notify opponent
            if (opponent != null && opponent.isOnline()) {
                opponent.sendMessage(ChatColor.RED + player.getName() + " disconnected! You win the duel!");
            }
            
            plugin.getLogger().info("Player " + player.getName() + " quit during duel - duel ended");
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        
        if (plugin.getDuelManager().isInAnyDuel(player)) {
            // Clear drops to prevent item loss
            event.getDrops().clear();
            event.setDroppedExp(0);
            
            // End the duel
            plugin.getDuelManager().endDuel(player, true);
            
            // Cancel death message
            event.setDeathMessage(null);
            
            plugin.getLogger().info("Player " + player.getName() + " died in duel - duel ended");
        }
    }
    
    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        
        if (plugin.getDuelManager().isInAnyDuel(player)) {
            // Check regular duel
            Duel duel = plugin.getDuelManager().getDuel(player);
            if (duel != null) {
                // Check if teleporting outside arena bounds
                if (!isInArena(event.getTo(), duel.getArena())) {
                    // Only cancel if it's not a plugin-initiated teleport
                    if (event.getCause() != PlayerTeleportEvent.TeleportCause.PLUGIN) {
                        event.setCancelled(true);
                        player.sendMessage(ChatColor.RED + "You cannot teleport during a duel!");
                    }
                }
                return;
            }
            
            // Check rounds duel
            RoundsDuel roundsDuel = plugin.getDuelManager().getRoundsDuel(player);
            if (roundsDuel != null) {
                // Check if teleporting outside arena bounds
                if (!isInArena(event.getTo(), roundsDuel.getArena())) {
                    // Only cancel if it's not a plugin-initiated teleport
                    if (event.getCause() != PlayerTeleportEvent.TeleportCause.PLUGIN) {
                        event.setCancelled(true);
                        player.sendMessage(ChatColor.RED + "You cannot teleport during a rounds duel!");
                    }
                }
            }
        }
    }
    
    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String command = event.getMessage().toLowerCase();
        
        if (plugin.getDuelManager().isInAnyDuel(player)) {
            // Block certain commands during duels
            if (command.startsWith("/tp") || command.startsWith("/teleport") ||
                command.startsWith("/home") || command.startsWith("/spawn") ||
                command.startsWith("/warp") || command.startsWith("/back")) {
                
                // Allow ckd commands
                if (!command.startsWith("/ckd")) {
                    event.setCancelled(true);
                    player.sendMessage(ChatColor.RED + "You cannot use that command during a duel!");
                }
            }
        }
    }
    
    private boolean isInArena(org.bukkit.Location location, com.yourname.customkitduels.data.Arena arena) {
        if (arena.getPos1() == null || arena.getPos2() == null) {
            return false;
        }
        
        org.bukkit.Location pos1 = arena.getPos1();
        org.bukkit.Location pos2 = arena.getPos2();
        
        double minX = Math.min(pos1.getX(), pos2.getX());
        double maxX = Math.max(pos1.getX(), pos2.getX());
        double minY = Math.min(pos1.getY(), pos2.getY());
        double maxY = Math.max(pos1.getY(), pos2.getY());
        double minZ = Math.min(pos1.getZ(), pos2.getZ());
        double maxZ = Math.max(pos1.getZ(), pos2.getZ());
        
        return location.getX() >= minX && location.getX() <= maxX &&
               location.getY() >= minY && location.getY() <= maxY &&
               location.getZ() >= minZ && location.getZ() <= maxZ;
    }
}