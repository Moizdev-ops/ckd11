package com.yourname.customkitduels.managers;

import com.yourname.customkitduels.CustomKitDuels;
import com.yourname.customkitduels.utils.ColorUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Simple and reliable health display manager
 * Uses custom names below player names - works on all servers
 */
public class HealthDisplayManager {
    
    private final CustomKitDuels plugin;
    private final Map<UUID, BukkitRunnable> healthTasks;
    private final Map<UUID, String> originalNames; // Store original custom names
    
    public HealthDisplayManager(CustomKitDuels plugin) {
        this.plugin = plugin;
        this.healthTasks = new HashMap<>();
        this.originalNames = new HashMap<>();
        
        plugin.getLogger().info("HealthDisplayManager initialized - using custom names for health display");
    }
    
    /**
     * Start health display for a player
     */
    public void startHealthDisplay(Player player) {
        stopHealthDisplay(player); // Stop any existing display
        
        // Store original custom name
        originalNames.put(player.getUniqueId(), player.getCustomName());
        
        BukkitRunnable healthTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
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
        
        // Restore original custom name
        String originalName = originalNames.remove(player.getUniqueId());
        player.setCustomName(originalName);
        player.setCustomNameVisible(originalName != null);
        
        plugin.getLogger().info("Stopped health display for " + player.getName());
    }
    
    /**
     * Update health display for a player
     */
    private void updateHealthDisplay(Player player) {
        double health = player.getHealth();
        double maxHealth = player.getMaxHealth();
        double hearts = health / 2.0; // Convert to hearts
        double maxHearts = maxHealth / 2.0;
        
        // Create health display with hearts
        String healthText = String.format("%.1f/%.1f ❤", hearts, maxHearts);
        
        // Add color based on health percentage
        double healthPercentage = health / maxHealth;
        ChatColor healthColor = ColorUtils.getHealthColor(healthPercentage);
        
        // Create display text: PlayerName <health> ❤
        String displayText = ChatColor.WHITE + player.getName() + " " + healthColor + healthText;
        
        // Set custom name
        player.setCustomName(displayText);
        player.setCustomNameVisible(true);
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
        
        // Restore all original names
        for (Map.Entry<UUID, String> entry : originalNames.entrySet()) {
            Player player = plugin.getServer().getPlayer(entry.getKey());
            if (player != null && player.isOnline()) {
                player.setCustomName(entry.getValue());
                player.setCustomNameVisible(entry.getValue() != null);
            }
        }
        originalNames.clear();
        
        plugin.getLogger().info("HealthDisplayManager cleaned up");
    }
}