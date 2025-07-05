package com.yourname.customkitduels.listeners;

import com.yourname.customkitduels.CustomKitDuels;
import com.yourname.customkitduels.data.Duel;
import com.yourname.customkitduels.data.RoundsDuel;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class PlayerListener implements Listener {
    
    private final CustomKitDuels plugin;
    
    public PlayerListener(CustomKitDuels plugin) {
        this.plugin = plugin;
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
        }
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        if (plugin.getDuelManager().isInAnyDuel(player)) {
            // End the duel when player quits
            plugin.getDuelManager().endDuel(player, true);
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