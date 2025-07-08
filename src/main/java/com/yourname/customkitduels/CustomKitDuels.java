package com.yourname.customkitduels;

import com.yourname.customkitduels.commands.CommandHandler;
import com.yourname.customkitduels.managers.*;
import com.yourname.customkitduels.listeners.PlayerListener;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public class CustomKitDuels extends JavaPlugin {
    
    private static CustomKitDuels instance;
    private KitManager kitManager;
    private ArenaManager arenaManager;
    private DuelManager duelManager;
    private CategoryManager categoryManager;
    private ScoreboardManager scoreboardManager;
    private SpawnManager spawnManager;
    private HealthDisplayManager healthDisplayManager;
    
    @Override
    public void onEnable() {
        instance = this;
        
        // Fancy startup messages
        getLogger().info("§c╔══════════════════════════════════════╗");
        getLogger().info("§c║           §fCustomKit Duels          §c║");
        getLogger().info("§c║              §7v1.0.0                §c║");
        getLogger().info("§c╠══════════════════════════════════════╣");
        getLogger().info("§c║ §fStarting plugin initialization...  §c║");
        getLogger().info("§c╚══════════════════════════════════════╝");
        
        // Save default config
        saveDefaultConfig();
        
        // Initialize managers
        getLogger().info("§c[CustomKit] §7Loading category manager...");
        categoryManager = new CategoryManager(this);
        
        getLogger().info("§c[CustomKit] §7Loading kit manager...");
        kitManager = new KitManager(this);
        
        getLogger().info("§c[CustomKit] §7Loading arena manager...");
        arenaManager = new ArenaManager(this);
        
        getLogger().info("§c[CustomKit] §7Loading health display manager...");
        healthDisplayManager = new HealthDisplayManager(this);
        
        getLogger().info("§c[CustomKit] §7Loading scoreboard manager...");
        scoreboardManager = new ScoreboardManager(this);
        
        getLogger().info("§c[CustomKit] §7Loading spawn manager...");
        spawnManager = new SpawnManager(this);
        
        getLogger().info("§c[CustomKit] §7Loading duel manager...");
        duelManager = new DuelManager(this);
        
        // Register commands
        getLogger().info("§c[CustomKit] §7Registering commands...");
        CommandHandler commandHandler = new CommandHandler(this);
        getCommand("customkit").setExecutor(commandHandler);
        getCommand("customkit").setTabCompleter(commandHandler);
        
        // Register listeners
        getLogger().info("§c[CustomKit] §7Registering event listeners...");
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        
        // Setup health display for already online players (in case of /reload)
        getServer().getScheduler().runTaskLater(this, () -> {
            healthDisplayManager.setupHealthDisplayForAll();
        }, 20L);
        
        // Final startup messages
        getLogger().info("§c╔══════════════════════════════════════╗");
        getLogger().info("§c║        §aSuccessfully Enabled!       §c║");
        getLogger().info("§c║                                      §c║");
        getLogger().info("§c║ §fFeatures:                          §c║");
        getLogger().info("§c║ §7• Enhanced kit creation system     §c║");
        getLogger().info("§c║ §7• Advanced enchantment editor      §c║");
        getLogger().info("§c║ §7• Stackable item count editor      §c║");
        getLogger().info("§c║ §7• Rounds-based duel system         §c║");
        getLogger().info("§c║ §7• Arena regeneration support       §c║");
        getLogger().info("§c║ §7• Real-time health indicators      §c║");
        getLogger().info("§c║                                      §c║");
        getLogger().info("§c║        §d❤ made with love by moiz ❤   §c║");
        getLogger().info("§c╚══════════════════════════════════════╝");
    }
    
    @Override
    public void onDisable() {
        // Fancy shutdown messages
        getLogger().info("§c╔══════════════════════════════════════╗");
        getLogger().info("§c║          §fShutting Down...          §c║");
        getLogger().info("§c╚══════════════════════════════════════╝");
        
        // Clean up all managers
        if (duelManager != null) {
            getLogger().info("§c[CustomKit] §7Cleaning up active duels...");
            duelManager.cleanupAllDuels();
        }
        
        if (scoreboardManager != null) {
            getLogger().info("§c[CustomKit] §7Cleaning up scoreboards...");
            scoreboardManager.cleanup();
        }
        
        if (healthDisplayManager != null) {
            getLogger().info("§c[CustomKit] §7Cleaning up health displays...");
            healthDisplayManager.cleanup();
        }
        
        getLogger().info("§c╔══════════════════════════════════════╗");
        getLogger().info("§c║         §cSuccessfully Disabled      §c║");
        getLogger().info("§c║                                      §c║");
        getLogger().info("§c║      §dthanks for using customkit!   §c║");
        getLogger().info("§c║        §d❤ made with love by moiz ❤   §c║");
        getLogger().info("§c╚══════════════════════════════════════╝");
    }
    
    public static CustomKitDuels getInstance() {
        return instance;
    }
    
    public KitManager getKitManager() {
        return kitManager;
    }
    
    public ArenaManager getArenaManager() {
        return arenaManager;
    }
    
    public DuelManager getDuelManager() {
        return duelManager;
    }
    
    public CategoryManager getCategoryManager() {
        return categoryManager;
    }
    
    public ScoreboardManager getScoreboardManager() {
        return scoreboardManager;
    }
    
    public SpawnManager getSpawnManager() {
        return spawnManager;
    }
    
    public HealthDisplayManager getHealthDisplayManager() {
        return healthDisplayManager;
    }
    
    public void reloadPluginConfig() {
        reloadConfig();
        arenaManager.loadArenas();
        categoryManager.reloadCategories();
        scoreboardManager.reloadConfig();
        getLogger().info("§c[CustomKit] §aconfiguration reloaded!");
    }
}