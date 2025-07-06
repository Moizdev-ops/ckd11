package com.yourname.customkitduels.managers;

import com.yourname.customkitduels.CustomKitDuels;
import com.yourname.customkitduels.data.*;
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
    private final Map<UUID, RoundsDuelRequest> pendingRoundsRequests;
    private final Map<UUID, Duel> activeDuels;
    private final Map<UUID, RoundsDuel> activeRoundsDuels;
    private final Map<UUID, Location> savedLocations;
    private final Set<UUID> playersInCountdown;
    
    public DuelManager(CustomKitDuels plugin) {
        this.plugin = plugin;
        this.pendingRequests = new HashMap<>();
        this.pendingRoundsRequests = new HashMap<>();
        this.activeDuels = new HashMap<>();
        this.activeRoundsDuels = new HashMap<>();
        this.savedLocations = new HashMap<>();
        this.playersInCountdown = new HashSet<>();
    }
    
    public void sendDuelRequest(Player challenger, Player target, Kit kit) {
        // Check if players are already in duels
        if (isInAnyDuel(challenger)) {
            challenger.sendMessage(ChatColor.RED + "You are already in a duel or countdown!");
            return;
        }
        
        if (isInAnyDuel(target)) {
            challenger.sendMessage(ChatColor.RED + "That player is already in a duel or countdown!");
            return;
        }
        
        // Check if target has pending request
        if (pendingRequests.containsKey(target.getUniqueId()) || pendingRoundsRequests.containsKey(target.getUniqueId())) {
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
    
    public void sendRoundsDuelRequest(Player challenger, Player target, Kit kit, int targetRounds) {
        // Check if players are already in duels
        if (isInAnyDuel(challenger)) {
            challenger.sendMessage(ChatColor.RED + "You are already in a duel or countdown!");
            return;
        }
        
        if (isInAnyDuel(target)) {
            challenger.sendMessage(ChatColor.RED + "That player is already in a duel or countdown!");
            return;
        }
        
        // Check if target has pending request
        if (pendingRequests.containsKey(target.getUniqueId()) || pendingRoundsRequests.containsKey(target.getUniqueId())) {
            challenger.sendMessage(ChatColor.RED + "That player already has a pending duel request!");
            return;
        }
        
        // Check if arena is available
        Arena arena = plugin.getArenaManager().getRandomAvailableArena();
        if (arena == null) {
            challenger.sendMessage(ChatColor.RED + "No arenas are available for dueling!");
            return;
        }
        
        // Create rounds duel request
        RoundsDuelRequest request = new RoundsDuelRequest(challenger, target, kit, arena, targetRounds);
        pendingRoundsRequests.put(target.getUniqueId(), request);
        
        // Send messages
        challenger.sendMessage(ChatColor.GREEN + "Rounds duel request sent to " + target.getName() + " with kit '" + kit.getName() + "' (First to " + targetRounds + ")!");
        target.sendMessage(ChatColor.YELLOW + challenger.getName() + " has challenged you to a rounds duel!");
        target.sendMessage(ChatColor.YELLOW + "Kit: " + kit.getName() + " | First to " + targetRounds + " rounds");
        target.sendMessage(ChatColor.YELLOW + "Type /ckd accept to accept the duel.");
        
        // Auto-expire request after 30 seconds
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (pendingRoundsRequests.remove(target.getUniqueId()) != null) {
                challenger.sendMessage(ChatColor.RED + "Your rounds duel request to " + target.getName() + " has expired.");
                target.sendMessage(ChatColor.RED + "The rounds duel request from " + challenger.getName() + " has expired.");
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
        if (isInAnyDuel(challenger) || isInAnyDuel(target)) {
            target.sendMessage(ChatColor.RED + "One of the players is already in a duel or countdown!");
            return;
        }
        
        startCountdown(challenger, target, request.getKit(), request.getArena());
    }
    
    public void acceptRoundsDuel(Player target) {
        RoundsDuelRequest request = pendingRoundsRequests.remove(target.getUniqueId());
        if (request == null) {
            // Try regular duel request as fallback
            acceptDuel(target);
            return;
        }
        
        Player challenger = request.getChallenger();
        if (!challenger.isOnline()) {
            target.sendMessage(ChatColor.RED + "The challenger is no longer online!");
            return;
        }
        
        // Check if players are still available
        if (isInAnyDuel(challenger) || isInAnyDuel(target)) {
            target.sendMessage(ChatColor.RED + "One of the players is already in a duel or countdown!");
            return;
        }
        
        startRoundsCountdown(challenger, target, request.getKit(), request.getArena(), request.getTargetRounds());
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
    
    private void startRoundsCountdown(Player challenger, Player target, Kit kit, Arena arena, int targetRounds) {
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
        challenger.sendMessage(ChatColor.GREEN + "Rounds duel accepted! First to " + targetRounds + " rounds wins!");
        target.sendMessage(ChatColor.GREEN + "Rounds duel accepted! First to " + targetRounds + " rounds wins!");
        
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
                        challenger.sendMessage(ChatColor.RED + "Rounds duel cancelled - player disconnected!");
                        restorePlayer(challenger);
                    }
                    if (target.isOnline()) {
                        target.sendMessage(ChatColor.RED + "Rounds duel cancelled - player disconnected!");
                        restorePlayer(target);
                    }
                    
                    this.cancel();
                    return;
                }
                
                if (countdown > 0) {
                    // Send countdown message
                    String message = ChatColor.YELLOW + "Round 1 starting in " + ChatColor.RED + countdown + ChatColor.YELLOW + "...";
                    challenger.sendTitle(ChatColor.RED + String.valueOf(countdown), message, 0, 20, 0);
                    target.sendTitle(ChatColor.RED + String.valueOf(countdown), message, 0, 20, 0);
                    
                    countdown--;
                } else {
                    // Start the rounds duel
                    challenger.sendTitle(ChatColor.GREEN + "FIGHT!", ChatColor.YELLOW + "Round 1 - First to " + targetRounds + "!", 0, 40, 10);
                    target.sendTitle(ChatColor.GREEN + "FIGHT!", ChatColor.YELLOW + "Round 1 - First to " + targetRounds + "!", 0, 40, 10);
                    
                    // Remove from countdown and start actual rounds duel
                    playersInCountdown.remove(challenger.getUniqueId());
                    playersInCountdown.remove(target.getUniqueId());
                    
                    startRoundsDuel(challenger, target, kit, arena, targetRounds);
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
    
    private void startRoundsDuel(Player challenger, Player target, Kit kit, Arena arena, int targetRounds) {
        // Create rounds duel
        RoundsDuel roundsDuel = new RoundsDuel(challenger, target, kit, arena, targetRounds);
        activeRoundsDuels.put(challenger.getUniqueId(), roundsDuel);
        activeRoundsDuels.put(target.getUniqueId(), roundsDuel);
        
        // Prepare players
        preparePlayer(challenger, kit);
        preparePlayer(target, kit);
        
        // Send messages
        challenger.sendMessage(ChatColor.GREEN + "Rounds duel started! " + roundsDuel.getProgressString());
        target.sendMessage(ChatColor.GREEN + "Rounds duel started! " + roundsDuel.getProgressString());
        
        // Announce to arena
        String message = ChatColor.YELLOW + "Rounds duel started: " + challenger.getName() + " vs " + target.getName() + " (First to " + targetRounds + ")";
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
        
        // Give kit - handle main inventory (36 slots)
        ItemStack[] contents = kit.getContents();
        if (contents != null) {
            // Set main inventory (slots 0-35)
            ItemStack[] mainInventory = new ItemStack[36];
            System.arraycopy(contents, 0, mainInventory, 0, Math.min(contents.length, 36));
            player.getInventory().setContents(mainInventory);
            
            // Set offhand (slot 36 in our extended array)
            if (contents.length > 36 && contents[36] != null) {
                player.getInventory().setItemInOffHand(contents[36]);
            }
        }
        
        // Give armor
        if (kit.getArmor() != null) {
            player.getInventory().setArmorContents(kit.getArmor().clone());
        }
        
        // Update inventory
        player.updateInventory();
    }
    
    public void endDuel(Player player, boolean died) {
        // Check if it's a rounds duel first
        RoundsDuel roundsDuel = activeRoundsDuels.get(player.getUniqueId());
        if (roundsDuel != null) {
            endRoundsDuelRound(player, died);
            return;
        }
        
        // Handle regular duel
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
    
    private void endRoundsDuelRound(Player player, boolean died) {
        RoundsDuel roundsDuel = activeRoundsDuels.get(player.getUniqueId());
        if (roundsDuel == null || !roundsDuel.isActive()) return;
        
        Player opponent = roundsDuel.getOpponent(player);
        Player roundWinner = died ? opponent : player;
        Player roundLoser = died ? player : opponent;
        
        // Add win to the winner
        roundsDuel.addWin(roundWinner);
        
        // Send round result messages
        if (roundWinner != null && roundLoser != null) {
            String roundMessage = ChatColor.YELLOW + roundWinner.getName() + " won round " + (roundsDuel.getCurrentRound() - 1) + "!";
            roundWinner.sendMessage(roundMessage);
            roundLoser.sendMessage(roundMessage);
            
            // Show current score
            String scoreMessage = ChatColor.AQUA + roundsDuel.getScoreString();
            roundWinner.sendMessage(scoreMessage);
            roundLoser.sendMessage(scoreMessage);
        }
        
        // Check if duel is complete
        if (roundsDuel.isComplete()) {
            // End the entire rounds duel
            Player overallWinner = roundsDuel.getOverallWinner();
            Player overallLoser = overallWinner.equals(roundsDuel.getPlayer1()) ? roundsDuel.getPlayer2() : roundsDuel.getPlayer1();
            
            // Remove from active duels
            activeRoundsDuels.remove(roundsDuel.getPlayer1().getUniqueId());
            activeRoundsDuels.remove(roundsDuel.getPlayer2().getUniqueId());
            
            // Send final messages
            String finalMessage = ChatColor.GOLD + "🏆 " + overallWinner.getName() + " won the rounds duel " + roundsDuel.getScoreString() + "! 🏆";
            overallWinner.sendMessage(finalMessage);
            overallLoser.sendMessage(finalMessage);
            
            // Announce to arena
            for (Player p : plugin.getServer().getOnlinePlayers()) {
                if (isPlayerInArena(p, roundsDuel.getArena())) {
                    p.sendMessage(finalMessage);
                }
            }
            
            // Restore players
            restorePlayer(roundsDuel.getPlayer1());
            restorePlayer(roundsDuel.getPlayer2());
        } else {
            // Regenerate arena if enabled
            if (roundsDuel.getArena().hasRegeneration()) {
                plugin.getArenaManager().regenerateArena(roundsDuel.getArena());
            }
            
            // Start next round after a short delay
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (roundsDuel.isActive() && roundWinner.isOnline() && roundLoser.isOnline()) {
                    startNextRound(roundsDuel);
                }
            }, 60L); // 3 second delay
        }
    }
    
    private void startNextRound(RoundsDuel roundsDuel) {
        Player player1 = roundsDuel.getPlayer1();
        Player player2 = roundsDuel.getPlayer2();
        
        if (!player1.isOnline() || !player2.isOnline()) {
            // End duel if someone disconnected
            activeRoundsDuels.remove(player1.getUniqueId());
            activeRoundsDuels.remove(player2.getUniqueId());
            if (player1.isOnline()) restorePlayer(player1);
            if (player2.isOnline()) restorePlayer(player2);
            return;
        }
        
        // Teleport players back to spawn points
        player1.teleport(roundsDuel.getArena().getSpawn1());
        player2.teleport(roundsDuel.getArena().getSpawn2());
        
        // Prepare players for next round
        preparePlayer(player1, roundsDuel.getKit());
        preparePlayer(player2, roundsDuel.getKit());
        
        // Send round start messages
        String roundMessage = ChatColor.GREEN + "Round " + roundsDuel.getCurrentRound() + " starting!";
        String progressMessage = ChatColor.AQUA + roundsDuel.getProgressString();
        
        player1.sendMessage(roundMessage);
        player1.sendMessage(progressMessage);
        player2.sendMessage(roundMessage);
        player2.sendMessage(progressMessage);
        
        // Start countdown for next round
        new BukkitRunnable() {
            int countdown = 3;
            
            @Override
            public void run() {
                if (!player1.isOnline() || !player2.isOnline() || !roundsDuel.isActive()) {
                    this.cancel();
                    return;
                }
                
                if (countdown > 0) {
                    String message = ChatColor.YELLOW + "Round " + roundsDuel.getCurrentRound() + " in " + ChatColor.RED + countdown + ChatColor.YELLOW + "...";
                    player1.sendTitle(ChatColor.RED + String.valueOf(countdown), message, 0, 20, 0);
                    player2.sendTitle(ChatColor.RED + String.valueOf(countdown), message, 0, 20, 0);
                    countdown--;
                } else {
                    player1.sendTitle(ChatColor.GREEN + "FIGHT!", ChatColor.YELLOW + "Round " + roundsDuel.getCurrentRound() + "!", 0, 40, 10);
                    player2.sendTitle(ChatColor.GREEN + "FIGHT!", ChatColor.YELLOW + "Round " + roundsDuel.getCurrentRound() + "!", 0, 40, 10);
                    this.cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }
    
    private void restorePlayer(Player player) {
        // Clear inventory
        player.getInventory().clear();
        player.getInventory().setArmorContents(new ItemStack[4]);
        player.getInventory().setItemInOffHand(null);
        
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
    
    public boolean isInRoundsDuel(Player player) {
        return activeRoundsDuels.containsKey(player.getUniqueId());
    }
    
    public boolean isInAnyDuel(Player player) {
        return isInDuel(player) || isInRoundsDuel(player);
    }
    
    public Duel getDuel(Player player) {
        return activeDuels.get(player.getUniqueId());
    }
    
    public RoundsDuel getRoundsDuel(Player player) {
        return activeRoundsDuels.get(player.getUniqueId());
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
        
        // End all active rounds duels
        for (UUID playerId : new ArrayList<>(activeRoundsDuels.keySet())) {
            Player player = plugin.getServer().getPlayer(playerId);
            if (player != null) {
                RoundsDuel roundsDuel = activeRoundsDuels.get(playerId);
                if (roundsDuel != null) {
                    roundsDuel.setActive(false);
                    restorePlayer(player);
                }
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
        activeRoundsDuels.clear();
        pendingRequests.clear();
        pendingRoundsRequests.clear();
        savedLocations.clear();
        playersInCountdown.clear();
    }
}