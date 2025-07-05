package com.yourname.customkitduels.managers;

import com.yourname.customkitduels.CustomKitDuels;
import com.yourname.customkitduels.data.Arena;
import com.yourname.customkitduels.data.Duel;
import com.yourname.customkitduels.data.DuelRequest;
import com.yourname.customkitduels.data.Kit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class DuelManager {
    
    private final CustomKitDuels plugin;
    private final Map<UUID, DuelRequest> pendingRequests;
    private final Map<UUID, Duel> activeDuels;
    private final Map<UUID, Location> savedLocations;
    private final Set<UUID> playersInCountdown;
    
    public DuelManager(CustomKitDuels plugin) {
        this.plugin = plugin;
        this.pendingRequests = new HashMap<>();
        this.activeDuels = new HashMap<>();
        this.savedLocations = new HashMap<>();
        this.playersInCountdown = new HashSet<>();
    }
    
    public void sendDuelRequest(Player challenger, Player target, Kit kit) {
        // Check if players are already in duels
        if (activeDuels.containsKey(challenger.getUniqueId()) || playersInCountdown.contains(challenger.getUniqueId())) {
            challenger.sendMessage(ChatColor.RED + "You are already in a duel or countdown!");
            return;
        }
        
        if (activeDuels.containsKey(target.getUniqueId()) || playersInCountdown.contains(target.getUniqueId())) {
            challenger.sendMessage(ChatColor.RED + "That player is already in a duel or countdown!");
            return;
        }
        
        // Check if target has pending request
        if (pendingRequests.containsKey(target.getUniqueId())) {
            challenger.sendMessage(ChatColor.RED + "That player already has a pending duel request!");
            return;
        }
        
        // Check if arena is available
        Arena arena = plugin.getArenaManager().getRandomAvailableArena();
        if (arena == null) {
            challenger.sendMessage(ChatColor.RED + "No arenas are available for dueling!");
            return;
        }
        
        // Create duel request
        DuelRequest request = new DuelRequest(challenger, target, kit, arena);
        pendingRequests.put(target.getUniqueId(), request);
        
        // Send messages
        challenger.sendMessage(ChatColor.GREEN + "Duel request sent to " + target.getName() + " with kit '" + kit.getName() + "'!");
        target.sendMessage(ChatColor.YELLOW + challenger.getName() + " has challenged you to a duel with kit '" + kit.getName() + "'!");
        target.sendMessage(ChatColor.YELLOW + "Type /ckd accept to accept the duel.");
        
        // Auto-expire request after 30 seconds
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (pendingRequests.remove(target.getUniqueId()) != null) {
                challenger.sendMessage(ChatColor.RED + "Your duel request to " + target.getName() + " has expired.");
                target.sendMessage(ChatColor.RED + "The duel request from " + challenger.getName() + " has expired.");
            }
        }, 600L); // 30 seconds
    }
    
    public void acceptDuel(Player target) {
        DuelRequest request = pendingRequests.remove(target.getUniqueId());
        if (request == null) {
            target.sendMessage(ChatColor.RED + "You don't have any pending duel requests!");
            return;
        }
        
        Player challenger = request.getChallenger();
        if (!challenger.isOnline()) {
            target.sendMessage(ChatColor.RED + "The challenger is no longer online!");
            return;
        }
        
        // Check if players are still available
        if (activeDuels.containsKey(challenger.getUniqueId()) || activeDuels.containsKey(target.getUniqueId()) ||
            playersInCountdown.contains(challenger.getUniqueId()) || playersInCountdown.contains(target.getUniqueId())) {
            target.sendMessage(ChatColor.RED + "One of the players is already in a duel or countdown!");
            return;
        }
        
        startCountdown(challenger, target, request.getKit(), request.getArena());
    }
    
    private void startCountdown(Player challenger, Player target, Kit kit, Arena arena) {
        // Add players to countdown set
        playersInCountdown.add(challenger.getUniqueId());
        playersInCountdown.add(target.getUniqueId());
        
        // Save current locations
        savedLocations.put(challenger.getUniqueId(), challenger.getLocation());
        savedLocations.put(target.getUniqueId(), target.getLocation());
        
        // Teleport players to arena spawn points
        challenger.teleport(arena.getSpawn1());
        target.teleport(arena.getSpawn2());
        
        // Send initial message
        challenger.sendMessage(ChatColor.GREEN + "Duel accepted! Preparing for battle...");
        target.sendMessage(ChatColor.GREEN + "Duel accepted! Preparing for battle...");
        
        // Start countdown
        new BukkitRunnable() {
            int countdown = 4;
            
            @Override
            public void run() {
                // Check if players are still online and in countdown
                if (!challenger.isOnline() || !target.isOnline() ||
                    !playersInCountdown.contains(challenger.getUniqueId()) ||
                    !playersInCountdown.contains(target.getUniqueId())) {
                    
                    // Cancel countdown
                    playersInCountdown.remove(challenger.getUniqueId());
                    playersInCountdown.remove(target.getUniqueId());
                    
                    if (challenger.isOnline()) {
                        challenger.sendMessage(ChatColor.RED + "Duel cancelled - player disconnected!");
                        restorePlayer(challenger);
                    }
                    if (target.isOnline()) {
                        target.sendMessage(ChatColor.RED + "Duel cancelled - player disconnected!");
                        restorePlayer(target);
                    }
                    
                    this.cancel();
                    return;
                }
                
                if (countdown > 0) {
                    // Send countdown message
                    String message = ChatColor.YELLOW + "Duel starting in " + ChatColor.RED + countdown + ChatColor.YELLOW + "...";
                    challenger.sendTitle(ChatColor.RED + String.valueOf(countdown), message, 0, 20, 0);
                    target.sendTitle(ChatColor.RED + String.valueOf(countdown), message, 0, 20, 0);
                    
                    countdown--;
                } else {
                    // Start the duel
                    challenger.sendTitle(ChatColor.GREEN + "FIGHT!", ChatColor.YELLOW + "Duel has begun!", 0, 40, 10);
                    target.sendTitle(ChatColor.GREEN + "FIGHT!", ChatColor.YELLOW + "Duel has begun!", 0, 40, 10);
                    
                    // Remove from countdown and start actual duel
                    playersInCountdown.remove(challenger.getUniqueId());
                    playersInCountdown.remove(target.getUniqueId());
                    
                    startDuel(challenger, target, kit, arena);
                    this.cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 20L); // Run every second
    }
    
    private void startDuel(Player challenger, Player target, Kit kit, Arena arena) {
        // Create duel
        Duel duel = new Duel(challenger, target, kit, arena);
        activeDuels.put(challenger.getUniqueId(), duel);
        activeDuels.put(target.getUniqueId(), duel);
        
        // Prepare players
        preparePlayer(challenger, kit);
        preparePlayer(target, kit);
        
        // Send messages
        challenger.sendMessage(ChatColor.GREEN + "Duel started against " + target.getName() + "!");
        target.sendMessage(ChatColor.GREEN + "Duel started against " + challenger.getName() + "!");
        
        // Announce to arena
        String message = ChatColor.YELLOW + "Duel started: " + challenger.getName() + " vs " + target.getName();
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (isPlayerInArena(player, arena)) {
                player.sendMessage(message);
            }
        }
    }
    
    private void preparePlayer(Player player, Kit kit) {
        // Clear player
        player.getInventory().clear();
        player.getInventory().setArmorContents(new ItemStack[4]);
        player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
        
        // Set health and hunger
        player.setHealth(20.0);
        player.setFoodLevel(20);
        player.setSaturation(20);
        
        // Set gamemode
        player.setGameMode(GameMode.SURVIVAL);
        
        // Give kit
        player.getInventory().setContents(kit.getContents().clone());
        player.getInventory().setArmorContents(kit.getArmor().clone());
        
        // Update inventory
        player.updateInventory();
    }
    
    public void endDuel(Player player, boolean died) {
        Duel duel = activeDuels.remove(player.getUniqueId());
        if (duel == null) return;
        
        Player opponent = duel.getOpponent(player);
        if (opponent != null) {
            activeDuels.remove(opponent.getUniqueId());
        }
        
        // Determine winner
        Player winner = died ? opponent : player;
        Player loser = died ? player : opponent;
        
        // Send messages
        if (winner != null && loser != null) {
            String winMessage = ChatColor.GREEN + winner.getName() + " won the duel against " + loser.getName() + "!";
            winner.sendMessage(winMessage);
            loser.sendMessage(winMessage);
            
            // Announce to arena
            for (Player p : plugin.getServer().getOnlinePlayers()) {
                if (isPlayerInArena(p, duel.getArena())) {
                    p.sendMessage(winMessage);
                }
            }
        }
        
        // Restore players
        restorePlayer(player);
        if (opponent != null && opponent.isOnline()) {
            restorePlayer(opponent);
        }
    }
    
    private void restorePlayer(Player player) {
        // Clear inventory
        player.getInventory().clear();
        player.getInventory().setArmorContents(new ItemStack[4]);
        
        // Remove potion effects
        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
        
        // Reset health and hunger
        player.setHealth(20.0);
        player.setFoodLevel(20);
        player.setSaturation(20);
        
        // Set gamemode
        player.setGameMode(GameMode.SURVIVAL);
        
        // Teleport back
        Location savedLocation = savedLocations.remove(player.getUniqueId());
        if (savedLocation != null) {
            player.teleport(savedLocation);
        } else {
            // Fallback to spawn
            player.teleport(player.getWorld().getSpawnLocation());
        }
        
        player.updateInventory();
    }
    
    public boolean isInDuel(Player player) {
        return activeDuels.containsKey(player.getUniqueId()) || playersInCountdown.contains(player.getUniqueId());
    }
    
    public Duel getDuel(Player player) {
        return activeDuels.get(player.getUniqueId());
    }
    
    private boolean isPlayerInArena(Player player, Arena arena) {
        if (arena.getPos1() == null || arena.getPos2() == null) {
            return false;
        }
        
        Location loc = player.getLocation();
        Location pos1 = arena.getPos1();
        Location pos2 = arena.getPos2();
        
        double minX = Math.min(pos1.getX(), pos2.getX());
        double maxX = Math.max(pos1.getX(), pos2.getX());
        double minY = Math.min(pos1.getY(), pos2.getY());
        double maxY = Math.max(pos1.getY(), pos2.getY());
        double minZ = Math.min(pos1.getZ(), pos2.getZ());
        double maxZ = Math.max(pos1.getZ(), pos2.getZ());
        
        return loc.getX() >= minX && loc.getX() <= maxX &&
               loc.getY() >= minY && loc.getY() <= maxY &&
               loc.getZ() >= minZ && loc.getZ() <= maxZ;
    }
    
    public void cleanupAllDuels() {
        // End all active duels
        for (UUID playerId : new ArrayList<>(activeDuels.keySet())) {
            Player player = plugin.getServer().getPlayer(playerId);
            if (player != null) {
                endDuel(player, false);
            }
        }
        
        // Clear countdown players
        for (UUID playerId : new ArrayList<>(playersInCountdown)) {
            Player player = plugin.getServer().getPlayer(playerId);
            if (player != null) {
                restorePlayer(player);
            }
        }
        
        // Clear all data
        activeDuels.clear();
        pendingRequests.clear();
        savedLocations.clear();
        playersInCountdown.clear();
    }
}