package com.yourname.customkitduels.managers;

import com.yourname.customkitduels.CustomKitDuels;
import com.yourname.customkitduels.data.RoundsDuel;
import com.yourname.customkitduels.utils.ColorUtils;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Enhanced health display manager with better visual indicators
 * Uses action bar messages for real-time health display
 */
public class HealthDisplayManager {
    
    private final CustomKitDuels plugin;
    private final Map<UUID, BukkitRunnable> healthTasks;
    
    public HealthDisplayManager(CustomKitDuels plugin) {
        this.plugin = plugin;
        this.healthTasks = new HashMap<>();
        
        plugin.getLogger().info("HealthDisplayManager initialized with action bar support");
    }
    
    /**
     * Start health display for a player
     */
    public void startHealthDisplay(Player player) {
        stopHealthDisplay(player); // Stop any existing display
        
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
        
        plugin.getLogger().info("Started health display for " + player.getName());
    }
    
    /**
     * Stop health display for a player
     */
    public void stopHealthDisplay(Player player) {
        BukkitRunnable task = healthTasks.remove(player.getUniqueId());
        if (task != null) {
            task.cancel();
        }
        
        // Clear action bar
        try {
            player.sendActionBar("");
        } catch (Exception e) {
            // Ignore if action bar is not supported
        }
        
        plugin.getLogger().info("Stopped health display for " + player.getName());
    }
    
    /**
     * Update health display for a player using action bar
     */
    private void updateHealthDisplay(Player player) {
        // Get opponent
        Player opponent = null;
        RoundsDuel roundsDuel = plugin.getDuelManager().getRoundsDuel(player);
        if (roundsDuel != null) {
            opponent = roundsDuel.getOpponent(player);
        } else {
            // Try regular duel
            var duel = plugin.getDuelManager().getDuel(player);
            if (duel != null) {
                opponent = duel.getOpponent(player);
            }
        }
        
        if (opponent == null || !opponent.isOnline()) {
            return;
        }
        
        // Create health display
        String healthDisplay = createHealthDisplay(player, opponent);
        
        // Send as action bar
        try {
            player.sendActionBar(ColorUtils.translateColors(healthDisplay));
        } catch (Exception e) {
            // Fallback to chat message if action bar fails
            player.sendMessage(ColorUtils.translateColors(healthDisplay));
        }
    }
    
    /**
     * Create health display string for both players
     */
    private String createHealthDisplay(Player player, Player opponent) {
        // Player health
        double playerHealth = player.getHealth();
        double playerMaxHealth = player.getMaxHealth();
        double playerHearts = playerHealth / 2.0;
        double playerMaxHearts = playerMaxHealth / 2.0;
        double playerHealthPercentage = playerHealth / playerMaxHealth;
        
        // Opponent health
        double opponentHealth = opponent.getHealth();
        double opponentMaxHealth = opponent.getMaxHealth();
        double opponentHearts = opponentHealth / 2.0;
        double opponentMaxHearts = opponentMaxHealth / 2.0;
        double opponentHealthPercentage = opponentHealth / opponentMaxHealth;
        
        // Create health bars
        String playerHealthBar = createHealthBar(playerHealthPercentage);
        String opponentHealthBar = createHealthBar(opponentHealthPercentage);
        
        // Get colors based on health
        String playerColor = ColorUtils.getHealthColorHex(playerHealthPercentage);
        String opponentColor = ColorUtils.getHealthColorHex(opponentHealthPercentage);
        
        // Format health text
        String playerHealthText = String.format("%.1f/%.1f", playerHearts, playerMaxHearts);
        String opponentHealthText = String.format("%.1f/%.1f", opponentHearts, opponentMaxHearts);
        
        // Create display
        return String.format(
            "<white>You: %s%s ❤ %s</white> <gray>|</gray> <white>%s: %s%s ❤ %s</white>",
            playerColor, playerHealthText, playerHealthBar,
            opponent.getName(), opponentColor, opponentHealthText, opponentHealthBar
        );
    }
    
    /**
     * Create a visual health bar
     */
    private String createHealthBar(double healthPercentage) {
        int totalBars = 10;
        int filledBars = (int) Math.round(healthPercentage * totalBars);
        
        StringBuilder healthBar = new StringBuilder();
        healthBar.append("<gray>[</gray>");
        
        for (int i = 0; i < totalBars; i++) {
            if (i < filledBars) {
                if (healthPercentage > 0.6) {
                    healthBar.append("<green>|</green>");
                } else if (healthPercentage > 0.3) {
                    healthBar.append("<yellow>|</yellow>");
                } else {
                    healthBar.append("<red>|</red>");
                }
            } else {
                healthBar.append("<dark_gray>|</dark_gray>");
            }
        }
        
        healthBar.append("<gray>]</gray>");
        return healthBar.toString();
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
        
        // Clear action bars for all online players
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            try {
                player.sendActionBar("");
            } catch (Exception e) {
                // Ignore if action bar is not supported
            }
        }
        
        plugin.getLogger().info("HealthDisplayManager cleaned up");
    }
}