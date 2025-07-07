package com.yourname.customkitduels.managers;

import com.yourname.customkitduels.CustomKitDuels;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Enhanced health display manager that shows health below player name tags
 * Uses scoreboard objectives to display health information below names
 */
public class HealthDisplayManager {
    
    private final CustomKitDuels plugin;
    private final Map<UUID, BukkitRunnable> healthTasks;
    private final Map<UUID, Scoreboard> originalScoreboards; // Store original scoreboards
    private Scoreboard healthScoreboard; // Shared scoreboard for health display
    private Objective healthObjective; // Health objective
    
    public HealthDisplayManager(CustomKitDuels plugin) {
        this.plugin = plugin;
        this.healthTasks = new HashMap<>();
        this.originalScoreboards = new HashMap<>();
        
        // Create shared health scoreboard
        createHealthScoreboard();
        
        plugin.getLogger().info("HealthDisplayManager initialized with below-name health display");
    }
    
    /**
     * Create the shared health scoreboard
     */
    private void createHealthScoreboard() {
        org.bukkit.scoreboard.ScoreboardManager manager = Bukkit.getScoreboardManager();
        if (manager == null) {
            plugin.getLogger().severe("ScoreboardManager is null! Cannot create health display.");
            return;
        }
        
        healthScoreboard = manager.getNewScoreboard();
        
        // Create health objective that displays below name
        healthObjective = healthScoreboard.registerNewObjective("health", "health", ChatColor.RED + "❤");
        healthObjective.setDisplaySlot(DisplaySlot.BELOW_NAME);
        
        plugin.getLogger().info("Created shared health scoreboard with below-name display");
    }
    
    /**
     * Start health display for a player
     */
    public void startHealthDisplay(Player player) {
        stopHealthDisplay(player); // Stop any existing display
        
        // Store original scoreboard
        originalScoreboards.put(player.getUniqueId(), player.getScoreboard());
        
        // Set player to use health scoreboard
        player.setScoreboard(healthScoreboard);
        
        // Start health update task
        BukkitRunnable healthTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline() || !plugin.getDuelManager().isInAnyDuel(player)) {
                    this.cancel();
                    healthTasks.remove(player.getUniqueId());
                    return;
                }
                
                updateHealthDisplay(player);
            }
        };
        
        healthTask.runTaskTimer(plugin, 0L, 10L); // Update every 0.5 seconds
        healthTasks.put(player.getUniqueId(), healthTask);
        
        // Initial update
        updateHealthDisplay(player);
        
        plugin.getLogger().info("Started below-name health display for " + player.getName());
    }
    
    /**
     * Stop health display for a player
     */
    public void stopHealthDisplay(Player player) {
        BukkitRunnable task = healthTasks.remove(player.getUniqueId());
        if (task != null) {
            task.cancel();
        }
        
        // Restore original scoreboard
        Scoreboard originalBoard = originalScoreboards.remove(player.getUniqueId());
        if (originalBoard != null) {
            player.setScoreboard(originalBoard);
        } else {
            // Fallback to main scoreboard
            org.bukkit.scoreboard.ScoreboardManager manager = Bukkit.getScoreboardManager();
            if (manager != null) {
                player.setScoreboard(manager.getMainScoreboard());
            }
        }
        
        plugin.getLogger().info("Stopped below-name health display for " + player.getName());
    }
    
    /**
     * Update health display for a player
     */
    private void updateHealthDisplay(Player player) {
        if (healthObjective == null) {
            return;
        }
        
        // Update health score for this player
        double health = player.getHealth();
        double maxHealth = player.getMaxHealth();
        double hearts = health / 2.0; // Convert health points to hearts
        
        // Set the score to show hearts (rounded to nearest 0.5)
        int displayHearts = (int) Math.round(hearts * 2); // Multiply by 2 to show half hearts
        Score score = healthObjective.getScore(player.getName());
        score.setScore(displayHearts);
        
        // Also update for opponent if they're in the same duel
        var roundsDuel = plugin.getDuelManager().getRoundsDuel(player);
        if (roundsDuel != null) {
            Player opponent = roundsDuel.getOpponent(player);
            if (opponent != null && opponent.isOnline()) {
                // Make sure opponent also uses the health scoreboard
                if (!opponent.getScoreboard().equals(healthScoreboard)) {
                    originalScoreboards.put(opponent.getUniqueId(), opponent.getScoreboard());
                    opponent.setScoreboard(healthScoreboard);
                }
                
                // Update opponent's health display
                double opponentHealth = opponent.getHealth();
                double opponentMaxHealth = opponent.getMaxHealth();
                double opponentHearts = opponentHealth / 2.0;
                int opponentDisplayHearts = (int) Math.round(opponentHearts * 2);
                Score opponentScore = healthObjective.getScore(opponent.getName());
                opponentScore.setScore(opponentDisplayHearts);
            }
        } else {
            // Check regular duel
            var duel = plugin.getDuelManager().getDuel(player);
            if (duel != null) {
                Player opponent = duel.getOpponent(player);
                if (opponent != null && opponent.isOnline()) {
                    // Make sure opponent also uses the health scoreboard
                    if (!opponent.getScoreboard().equals(healthScoreboard)) {
                        originalScoreboards.put(opponent.getUniqueId(), opponent.getScoreboard());
                        opponent.setScoreboard(healthScoreboard);
                    }
                    
                    // Update opponent's health display
                    double opponentHealth = opponent.getHealth();
                    double opponentMaxHealth = opponent.getMaxHealth();
                    double opponentHearts = opponentHealth / 2.0;
                    int opponentDisplayHearts = (int) Math.round(opponentHearts * 2);
                    Score opponentScore = healthObjective.getScore(opponent.getName());
                    opponentScore.setScore(opponentDisplayHearts);
                }
            }
        }
    }
    
    /**
     * Setup health display for all online players (used on plugin enable/reload)
     */
    public void setupHealthDisplayForAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (plugin.getDuelManager().isInAnyDuel(player)) {
                startHealthDisplay(player);
            }
        }
    }
    
    /**
     * Check if health display is active for a player
     */
    public boolean isHealthDisplayActive(Player player) {
        return healthTasks.containsKey(player.getUniqueId());
    }
    
    /**
     * Clean up all health displays
     */
    public void cleanup() {
        // Cancel all tasks
        for (BukkitRunnable task : healthTasks.values()) {
            task.cancel();
        }
        healthTasks.clear();
        
        // Restore all original scoreboards
        for (Map.Entry<UUID, Scoreboard> entry : originalScoreboards.entrySet()) {
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player != null && player.isOnline()) {
                player.setScoreboard(entry.getValue());
            }
        }
        originalScoreboards.clear();
        
        // Clean up health scoreboard
        if (healthScoreboard != null && healthObjective != null) {
            healthObjective.unregister();
        }
        
        plugin.getLogger().info("HealthDisplayManager cleaned up");
    }
}