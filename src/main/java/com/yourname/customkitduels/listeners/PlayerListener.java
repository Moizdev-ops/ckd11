package com.yourname.customkitduels.listeners;

import com.yourname.customkitduels.CustomKitDuels;
import com.yourname.customkitduels.data.Duel;
import com.yourname.customkitduels.data.RoundsDuel;
import java.util.Map;
import java.util.UUID;
import java.util.Map;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRegainHealthEvent;
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
    public void onEntityRegainHealth(EntityRegainHealthEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        
        Player player = (Player) event.getEntity();
        
        // Check if player is in duel and has natural regen disabled
        if (plugin.getDuelManager().isInAnyDuel(player)) {
            Map<UUID, Boolean> playerNaturalRegenState = plugin.getDuelManager().getPlayerNaturalRegenState();
            Boolean hasNaturalRegen = playerNaturalRegenState.get(player.getUniqueId());
            
            // If natural regen is disabled for this player, cancel natural healing
            if (hasNaturalRegen != null && !hasNaturalRegen) {
                if (event.getRegainReason() == EntityRegainHealthEvent.RegainReason.SATIATED) {
                    event.setCancelled(true);
                    plugin.getLogger().info("Cancelled natural health regen for " + player.getName() + " (kit setting)");
                }
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        // Stop health display
        plugin.getHealthDisplayManager().stopHealthDisplay(player);
        
        // Check if player is in a duel
        if (plugin.getDuelManager().isInAnyDuel(player)) {
            // Get opponent before ending duel - make variables final for lambda
            final Player opponent;
            RoundsDuel roundsDuel = plugin.getDuelManager().getRoundsDuel(player);
            if (roundsDuel != null) {
                opponent = roundsDuel.getOpponent(player);
                
                // Award all remaining rounds to opponent
                if (opponent != null && opponent.isOnline()) {
                    int remainingRounds = roundsDuel.getTargetRounds() - Math.max(roundsDuel.getPlayer1Wins(), roundsDuel.getPlayer2Wins());
                    
                    // Set opponent as winner of all remaining rounds
                    if (opponent.equals(roundsDuel.getPlayer1())) {
                        while (roundsDuel.getPlayer1Wins() < roundsDuel.getTargetRounds()) {
                            roundsDuel.addWin(opponent);
                        }
                    } else {
                        while (roundsDuel.getPlayer2Wins() < roundsDuel.getTargetRounds()) {
                            roundsDuel.addWin(opponent);
                        }
                    }
                    
                    // Mark duel as inactive to prevent further processing
                    roundsDuel.setActive(false);
                    
                    // Remove from active duels
                    Map<UUID, RoundsDuel> activeRoundsDuels = plugin.getDuelManager().getActiveRoundsDuels();
                    activeRoundsDuels.remove(player.getUniqueId());
                    activeRoundsDuels.remove(opponent.getUniqueId());
                    
                    // Clean up opponent
                    plugin.getScoreboardManager().removeDuelScoreboard(opponent);
                    plugin.getHealthDisplayManager().stopHealthDisplay(opponent);
                    
                    // Send win message to opponent
                    opponent.sendMessage(ChatColor.GREEN + player.getName() + " disconnected! You win the duel!");
                    
                    // Restore opponent and teleport to spawn
                    plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                        if (opponent.isOnline()) {
                            // Restore opponent's health and inventory
                            restorePlayerQuick(opponent);
                            
                            // Teleport to spawn
                            Location spawn = plugin.getSpawnManager().getSpawn();
                            if (spawn != null) {
                                opponent.teleport(spawn);
                                opponent.sendMessage(ChatColor.GREEN + "You have been teleported to spawn.");
                            } else {
                                opponent.teleport(opponent.getWorld().getSpawnLocation());
                            }
                        }
                    }, 20L); // 1 second delay
                }
            } else {
                Duel duel = plugin.getDuelManager().getDuel(player);
                if (duel != null) {
                    opponent = duel.getOpponent(player);
                    
                    // End regular duel
                    plugin.getDuelManager().endDuel(player, true);
                    
                    // Notify opponent and teleport them to spawn
                    if (opponent != null && opponent.isOnline()) {
                        opponent.sendMessage(ChatColor.GREEN + player.getName() + " disconnected! You win the duel!");
                        
                        // Teleport opponent to spawn after a short delay
                        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                            if (opponent.isOnline()) {
                                Location spawn = plugin.getSpawnManager().getSpawn();
                                if (spawn != null) {
                                    opponent.teleport(spawn);
                                    opponent.sendMessage(ChatColor.GREEN + "You have been teleported to spawn.");
                                } else {
                                    opponent.teleport(opponent.getWorld().getSpawnLocation());
                                }
                            }
                        }, 20L); // 1 second delay
                    }
                } else {
                    opponent = null;
                }
            }
            
            plugin.getLogger().info("Player " + player.getName() + " quit during duel - duel ended");
        }
    }
    
    private void restorePlayerQuick(Player player) {
        // Clear inventory
        player.getInventory().clear();
        player.getInventory().setArmorContents(new org.bukkit.inventory.ItemStack[4]);
        player.getInventory().setItemInOffHand(null);
        
        // Remove potion effects
        for (org.bukkit.potion.PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
        
        // Reset health to default
        try {
            player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(20.0);
            player.setHealth(20.0);
        } catch (Exception e) {
            player.setHealth(Math.min(20.0, player.getMaxHealth()));
        }
        player.setFoodLevel(20);
        player.setSaturation(20);
        
        // Set gamemode
        player.setGameMode(org.bukkit.GameMode.SURVIVAL);
        
        player.updateInventory();
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