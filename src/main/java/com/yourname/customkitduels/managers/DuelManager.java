package com.yourname.customkitduels.managers;

import com.yourname.customkitduels.CustomKitDuels;
import com.yourname.customkitduels.data.*;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
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
    private final Map<UUID, BukkitRunnable> arenaBoundsCheckers;
    
    public DuelManager(CustomKitDuels plugin) {
        this.plugin = plugin;
        this.pendingRequests = new HashMap<>();
        this.pendingRoundsRequests = new HashMap<>();
        this.activeDuels = new HashMap<>();
        this.activeRoundsDuels = new HashMap<>();
        this.savedLocations = new HashMap<>();
        this.playersInCountdown = new HashSet<>();
        this.arenaBoundsCheckers = new HashMap<>();
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
            int countdown = 5;
            
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
                    // Send countdown message with proper title
                    String countdownText = ChatColor.RED + "" + ChatColor.BOLD + countdown;
                    String subtitle = ChatColor.YELLOW + "Duel starting in " + countdown + "...";
                    
                    challenger.sendTitle(countdownText, subtitle, 0, 20, 0);
                    target.sendTitle(countdownText, subtitle, 0, 20, 0);
                    
                    // Play note block sound
                    challenger.playSound(challenger.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
                    target.playSound(target.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
                    
                    countdown--;
                } else {
                    // Start the duel
                    String fightTitle = ChatColor.GREEN + "" + ChatColor.BOLD + "FIGHT!";
                    String fightSubtitle = ChatColor.YELLOW + "Duel has begun!";
                    
                    challenger.sendTitle(fightTitle, fightSubtitle, 0, 40, 10);
                    target.sendTitle(fightTitle, fightSubtitle, 0, 40, 10);
                    
                    // Play start sound
                    challenger.playSound(challenger.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 2.0f);
                    target.playSound(target.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 2.0f);
                    
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
            int countdown = 5;
            
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
                    // Send countdown message with proper title
                    String countdownText = ChatColor.RED + "" + ChatColor.BOLD + countdown;
                    String subtitle = ChatColor.YELLOW + "Round 1 starting in " + countdown + "...";
                    
                    challenger.sendTitle(countdownText, subtitle, 0, 20, 0);
                    target.sendTitle(countdownText, subtitle, 0, 20, 0);
                    
                    // Play note block sound
                    challenger.playSound(challenger.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
                    target.playSound(target.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
                    
                    countdown--;
                } else {
                    // Start the rounds duel
                    String fightTitle = ChatColor.GREEN + "" + ChatColor.BOLD + "FIGHT!";
                    String fightSubtitle = ChatColor.YELLOW + "Round 1 - First to " + targetRounds + "!";
                    
                    challenger.sendTitle(fightTitle, fightSubtitle, 0, 40, 10);
                    target.sendTitle(fightTitle, fightSubtitle, 0, 40, 10);
                    
                    // Play start sound
                    challenger.playSound(challenger.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 2.0f);
                    target.playSound(target.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 2.0f);
                    
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
        
        // Prepare players with SAME kit settings
        preparePlayer(challenger, kit, challenger.getUniqueId());
        preparePlayer(target, kit, challenger.getUniqueId()); // Use challenger's kit settings for both
        
        // Start arena bounds checking
        startArenaBoundsChecking(challenger, arena);
        startArenaBoundsChecking(target, arena);
        
        // Start health display if enabled
        if (plugin.getKitManager().getKitHealthIndicators(challenger.getUniqueId(), kit.getName())) {
            plugin.getHealthDisplayManager().startHealthDisplay(challenger);
            plugin.getHealthDisplayManager().startHealthDisplay(target);
        }
        
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
        
        // Prepare players with SAME kit settings
        preparePlayer(challenger, kit, challenger.getUniqueId());
        preparePlayer(target, kit, challenger.getUniqueId()); // Use challenger's kit settings for both
        
        // Start arena bounds checking
        startArenaBoundsChecking(challenger, arena);
        startArenaBoundsChecking(target, arena);
        
        // Start health display if enabled
        if (plugin.getKitManager().getKitHealthIndicators(challenger.getUniqueId(), kit.getName())) {
            plugin.getHealthDisplayManager().startHealthDisplay(challenger);
            plugin.getHealthDisplayManager().startHealthDisplay(target);
        }
        
        // Show scoreboard
        plugin.getScoreboardManager().showDuelScoreboard(challenger, roundsDuel);
        plugin.getScoreboardManager().showDuelScoreboard(target, roundsDuel);
        
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
    
    private void startArenaBoundsChecking(Player player, Arena arena) {
        BukkitRunnable boundsChecker = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline() || !isInAnyDuel(player)) {
                    this.cancel();
                    arenaBoundsCheckers.remove(player.getUniqueId());
                    return;
                }
                
                if (!isPlayerInArena(player, arena)) {
                    // Player is outside arena bounds - teleport back to spawn
                    RoundsDuel roundsDuel = activeRoundsDuels.get(player.getUniqueId());
                    if (roundsDuel != null) {
                        Location spawnPoint = player.equals(roundsDuel.getPlayer1()) ? 
                            arena.getSpawn1() : arena.getSpawn2();
                        player.teleport(spawnPoint);
                        player.sendMessage(ChatColor.RED + "You cannot leave the arena during a duel!");
                        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
                    } else {
                        Duel duel = activeDuels.get(player.getUniqueId());
                        if (duel != null) {
                            Location spawnPoint = player.equals(duel.getPlayer1()) ? 
                                arena.getSpawn1() : arena.getSpawn2();
                            player.teleport(spawnPoint);
                            player.sendMessage(ChatColor.RED + "You cannot leave the arena during a duel!");
                            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
                        }
                    }
                }
            }
        };
        
        boundsChecker.runTaskTimer(plugin, 20L, 20L); // Check every second
        arenaBoundsCheckers.put(player.getUniqueId(), boundsChecker);
    }
    
    public void endDuel(Player player, boolean died) {
        // Stop arena bounds checking
        BukkitRunnable boundsChecker = arenaBoundsCheckers.remove(player.getUniqueId());
        if (boundsChecker != null) {
            boundsChecker.cancel();
        }
        
        // Stop health display
        plugin.getHealthDisplayManager().stopHealthDisplay(player);
        
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
            // Stop opponent's bounds checking and health display too
            BukkitRunnable opponentBoundsChecker = arenaBoundsCheckers.remove(opponent.getUniqueId());
            if (opponentBoundsChecker != null) {
                opponentBoundsChecker.cancel();
            }
            plugin.getHealthDisplayManager().stopHealthDisplay(opponent);
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
        
        // Restore players after 2 seconds
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            restorePlayer(player);
            if (opponent != null && opponent.isOnline()) {
                restorePlayer(opponent);
            }
        }, 40L); // 2 seconds
    }
    
    private void endRoundsDuelRound(Player player, boolean died) {
        RoundsDuel roundsDuel = activeRoundsDuels.get(player.getUniqueId());
        if (roundsDuel == null || !roundsDuel.isActive()) return;
        
        Player opponent = roundsDuel.getOpponent(player);
        Player roundWinner = died ? opponent : player;
        Player roundLoser = died ? player : opponent;
        
        // Add win to the winner
        roundsDuel.addWin(roundWinner);
        
        // Update scoreboards
        plugin.getScoreboardManager().updateDuelScoreboard(roundsDuel.getPlayer1(), roundsDuel);
        plugin.getScoreboardManager().updateDuelScoreboard(roundsDuel.getPlayer2(), roundsDuel);
        
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
            
            // Stop arena bounds checking and health display
            BukkitRunnable boundsChecker1 = arenaBoundsCheckers.remove(roundsDuel.getPlayer1().getUniqueId());
            if (boundsChecker1 != null) boundsChecker1.cancel();
            BukkitRunnable boundsChecker2 = arenaBoundsCheckers.remove(roundsDuel.getPlayer2().getUniqueId());
            if (boundsChecker2 != null) boundsChecker2.cancel();
            
            plugin.getHealthDisplayManager().stopHealthDisplay(roundsDuel.getPlayer1());
            plugin.getHealthDisplayManager().stopHealthDisplay(roundsDuel.getPlayer2());
            
            // Remove scoreboards
            plugin.getScoreboardManager().removeDuelScoreboard(roundsDuel.getPlayer1());
            plugin.getScoreboardManager().removeDuelScoreboard(roundsDuel.getPlayer2());
            
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
            
            // Restore players after 2 seconds
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                restorePlayer(roundsDuel.getPlayer1());
                restorePlayer(roundsDuel.getPlayer2());
            }, 40L); // 2 seconds
        } else {
            // Regenerate arena if enabled
            if (roundsDuel.getArena().hasRegeneration()) {
                plugin.getArenaManager().regenerateArena(roundsDuel.getArena());
            }
            
            // Start next round after a 2 second delay
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (roundsDuel.isActive() && roundWinner.isOnline() && roundLoser.isOnline()) {
                    startNextRound(roundsDuel);
                }
            }, 40L); // 2 second delay
        }
    }
    
    private void startNextRound(RoundsDuel roundsDuel) {
        Player player1 = roundsDuel.getPlayer1();
        Player player2 = roundsDuel.getPlayer2();
        
        if (!player1.isOnline() || !player2.isOnline()) {
            // End duel if someone disconnected
            activeRoundsDuels.remove(player1.getUniqueId());
            activeRoundsDuels.remove(player2.getUniqueId());
            plugin.getScoreboardManager().removeDuelScoreboard(player1);
            plugin.getScoreboardManager().removeDuelScoreboard(player2);
            if (player1.isOnline()) restorePlayer(player1);
            if (player2.isOnline()) restorePlayer(player2);
            return;
        }
        
        // Teleport players back to spawn points
        player1.teleport(roundsDuel.getArena().getSpawn1());
        player2.teleport(roundsDuel.getArena().getSpawn2());
        
        // Prepare players for next round with SAME kit settings
        preparePlayer(player1, roundsDuel.getKit(), roundsDuel.getPlayer1().getUniqueId());
        preparePlayer(player2, roundsDuel.getKit(), roundsDuel.getPlayer1().getUniqueId());
        
        // Restart health display if enabled
        if (plugin.getKitManager().getKitHealthIndicators(roundsDuel.getPlayer1().getUniqueId(), roundsDuel.getKit().getName())) {
            plugin.getHealthDisplayManager().startHealthDisplay(player1);
            plugin.getHealthDisplayManager().startHealthDisplay(player2);
        }
        
        // Update scoreboards
        plugin.getScoreboardManager().updateDuelScoreboard(player1, roundsDuel);
        plugin.getScoreboardManager().updateDuelScoreboard(player2, roundsDuel);
        
        // Send round start messages
        String roundMessage = ChatColor.GREEN + "Round " + roundsDuel.getCurrentRound() + " starting!";
        String progressMessage = ChatColor.AQUA + roundsDuel.getProgressString();
        
        player1.sendMessage(roundMessage);
        player1.sendMessage(progressMessage);
        player2.sendMessage(roundMessage);
        player2.sendMessage(progressMessage);
        
        // Start countdown for next round with 5 seconds for inventory organization
        new BukkitRunnable() {
            int countdown = 5;
            
            @Override
            public void run() {
                if (!player1.isOnline() || !player2.isOnline() || !roundsDuel.isActive()) {
                    this.cancel();
                    return;
                }
                
                if (countdown > 0) {
                    String countdownText = ChatColor.RED + "" + ChatColor.BOLD + countdown;
                    String subtitle = ChatColor.YELLOW + "Round " + roundsDuel.getCurrentRound() + " in " + countdown + "...";
                    
                    player1.sendTitle(countdownText, subtitle, 0, 20, 0);
                    player2.sendTitle(countdownText, subtitle, 0, 20, 0);
                    
                    // Play note block sound
                    player1.playSound(player1.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
                    player2.playSound(player2.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
                    
                    countdown--;
                } else {
                    String fightTitle = ChatColor.GREEN + "" + ChatColor.BOLD + "FIGHT!";
                    String fightSubtitle = ChatColor.YELLOW + "Round " + roundsDuel.getCurrentRound() + "!";
                    
                    player1.sendTitle(fightTitle, fightSubtitle, 0, 40, 10);
                    player2.sendTitle(fightTitle, fightSubtitle, 0, 40, 10);
                    
                    // Play start sound
                    player1.playSound(player1.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 2.0f);
                    player2.playSound(player2.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 2.0f);
                    
                    this.cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }
    
    private void restorePlayer(Player player) {
        // Stop health display
        plugin.getHealthDisplayManager().stopHealthDisplay(player);
        
        // Clear inventory
        player.getInventory().clear();
        player.getInventory().setArmorContents(new ItemStack[4]);
        player.getInventory().setItemInOffHand(null);
        
        // Remove potion effects
        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
        
        // FIXED: Reset health to exactly 10 hearts (20 health points)
        try {
            player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(20.0);
            player.setHealth(20.0);
            plugin.getLogger().info("Restored health to 20 (10 hearts) for player " + player.getName());
        } catch (Exception e) {
            // Fallback if attribute access fails
            plugin.getLogger().warning("Failed to reset max health for player " + player.getName() + ": " + e.getMessage());
            player.setHealth(Math.min(20.0, player.getMaxHealth()));
        }
        player.setFoodLevel(20);
        player.setSaturation(20);
        
        // Restore natural regeneration gamerule
        World world = player.getWorld();
        world.setGameRule(GameRule.NATURAL_REGENERATION, true);
        
        // Set gamemode
        player.setGameMode(GameMode.SURVIVAL);
        
        // Teleport back to spawn
        Location spawn = plugin.getSpawnManager().getSpawn();
        if (spawn != null) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline()) {
                    player.teleport(spawn);
                    player.sendMessage(ChatColor.GREEN + "You have been teleported to spawn.");
                }
            }, 40L); // 2 second delay
        } else {
            // Fallback to saved location or world spawn
            Location savedLocation = savedLocations.remove(player.getUniqueId());
            if (savedLocation != null) {
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    if (player.isOnline()) {
                        player.teleport(savedLocation);
                    }
                }, 40L);
            } else {
                // Fallback to world spawn
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    if (player.isOnline()) {
                        player.teleport(player.getWorld().getSpawnLocation());
                    }
                }, 40L);
            }
        }
        
        player.updateInventory();
    }
    
    private void preparePlayer(Player player, Kit kit, UUID kitOwnerUUID) {
        // Clear player
        player.getInventory().clear();
        player.getInventory().setArmorContents(new ItemStack[4]);
        player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
        
        // Get kit settings from the kit owner (challenger)
        double kitHearts = plugin.getKitManager().getKitHearts(kitOwnerUUID, kit.getName());
        boolean naturalRegen = plugin.getKitManager().getKitNaturalRegen(kitOwnerUUID, kit.getName());
        
        // Set health based on kit settings (convert hearts to health points)
        double maxHealth = kitHearts * 2.0; // 1 heart = 2 health points
        try {
            player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(maxHealth);
            player.setHealth(maxHealth);
            plugin.getLogger().info("Set health to " + maxHealth + " (" + kitHearts + " hearts) for player " + player.getName() + " in duel");
        } catch (Exception e) {
            // Fallback if attribute access fails
            plugin.getLogger().warning("Failed to set max health for player " + player.getName() + ": " + e.getMessage());
            player.setHealth(Math.min(maxHealth, player.getMaxHealth()));
        }
        
        // Set hunger
        player.setFoodLevel(20);
        player.setSaturation(20);
        
        // Handle natural health regeneration setting
        if (!naturalRegen) {
            // Disable natural regeneration by setting the gamerule for this player's world
            World world = player.getWorld();
            world.setGameRule(GameRule.NATURAL_REGENERATION, false);
            plugin.getLogger().info("Disabled natural regeneration for world " + world.getName() + " during duel");
        }
        
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
        // Cancel all arena bounds checkers
        for (BukkitRunnable checker : arenaBoundsCheckers.values()) {
            checker.cancel();
        }
        arenaBoundsCheckers.clear();
        
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
                    plugin.getScoreboardManager().removeDuelScoreboard(player);
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