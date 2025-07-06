package com.yourname.customkitduels.managers;

import com.yourname.customkitduels.CustomKitDuels;
import com.yourname.customkitduels.utils.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages health display for players during duels
 * Uses multiple methods for maximum compatibility
 */
public class HealthDisplayManager {
    
    private final CustomKitDuels plugin;
    private final Map<UUID, BukkitRunnable> healthTasks;
    private final boolean useHolographicDisplays;
    private final boolean useProtocolLib;
    
    public HealthDisplayManager(CustomKitDuels plugin) {
        this.plugin = plugin;
        this.healthTasks = new HashMap<>();
        
        // Check for optional dependencies
        this.useHolographicDisplays = Bukkit.getPluginManager().isPluginEnabled("HolographicDisplays");
        this.useProtocolLib = Bukkit.getPluginManager().isPluginEnabled("ProtocolLib");
        
        plugin.getLogger().info("HealthDisplayManager initialized - HolographicDisplays: " + useHolographicDisplays + ", ProtocolLib: " + useProtocolLib);
    }
    
    /**
     * Start health display for a player
     */
    public void startHealthDisplay(Player player) {
        stopHealthDisplay(player); // Stop any existing display
        
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
        
        // Clear custom name
        clearHealthDisplay(player);
        
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
        
        // Create health display with hearts and percentage
        String healthText = String.format("%.1f/%.1f ❤", hearts, maxHearts);
        
        // Add color based on health percentage
        double healthPercentage = health / maxHealth;
        String colorCode;
        if (healthPercentage > 0.6) {
            colorCode = "<green>";
        } else if (healthPercentage > 0.3) {
            colorCode = "<yellow>";
        } else {
            colorCode = "<red>";
        }
        
        String displayText = colorCode + healthText;
        
        // Method 1: Custom name (most compatible)
        setCustomNameHealth(player, displayText);
        
        // Method 2: HolographicDisplays (if available)
        if (useHolographicDisplays) {
            setHologramHealth(player, displayText);
        }
        
        // Method 3: ProtocolLib packets (if available)
        if (useProtocolLib) {
            setPacketHealth(player, displayText);
        }
    }
    
    /**
     * Method 1: Use custom name for health display
     */
    private void setCustomNameHealth(Player player, String healthText) {
        String displayName = ColorUtils.translateColors("<white>" + player.getName() + " " + healthText);
        player.setCustomName(displayName);
        player.setCustomNameVisible(true);
    }
    
    /**
     * Method 2: Use HolographicDisplays for floating health
     */
    private void setHologramHealth(Player player, String healthText) {
        try {
            // This would require HolographicDisplays API
            // For now, we'll use the custom name method as fallback
            // In a real implementation, you'd create floating holograms above players
            plugin.getLogger().fine("HolographicDisplays health display for " + player.getName());
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to set hologram health for " + player.getName() + ": " + e.getMessage());
        }
    }
    
    /**
     * Method 3: Use ProtocolLib for packet-based health display
     */
    private void setPacketHealth(Player player, String healthText) {
        try {
            // This would require ProtocolLib API
            // For now, we'll use the custom name method as fallback
            // In a real implementation, you'd send custom packets for health display
            plugin.getLogger().fine("ProtocolLib health display for " + player.getName());
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to set packet health for " + player.getName() + ": " + e.getMessage());
        }
    }
    
    /**
     * Clear health display for a player
     */
    private void clearHealthDisplay(Player player) {
        // Clear custom name
        player.setCustomName(null);
        player.setCustomNameVisible(false);
        
        // Clear hologram if using HolographicDisplays
        if (useHolographicDisplays) {
            clearHologramHealth(player);
        }
        
        // Clear packet display if using ProtocolLib
        if (useProtocolLib) {
            clearPacketHealth(player);
        }
    }
    
    private void clearHologramHealth(Player player) {
        try {
            // Clear holographic display
            plugin.getLogger().fine("Cleared hologram health for " + player.getName());
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to clear hologram health for " + player.getName() + ": " + e.getMessage());
        }
    }
    
    private void clearPacketHealth(Player player) {
        try {
            // Clear packet-based display
            plugin.getLogger().fine("Cleared packet health for " + player.getName());
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to clear packet health for " + player.getName() + ": " + e.getMessage());
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
        for (BukkitRunnable task : healthTasks.values()) {
            task.cancel();
        }
        healthTasks.clear();
        
        plugin.getLogger().info("HealthDisplayManager cleaned up");
    }
}