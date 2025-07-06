package com.yourname.customkitduels;

import com.yourname.customkitduels.commands.CommandHandler;
import com.yourname.customkitduels.managers.*;
import com.yourname.customkitduels.listeners.PlayerListener;
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
        
        // Save default config
        saveDefaultConfig();
        
        // Initialize managers
        categoryManager = new CategoryManager(this);
        kitManager = new KitManager(this);
        arenaManager = new ArenaManager(this);
        healthDisplayManager = new HealthDisplayManager(this);
        scoreboardManager = new ScoreboardManager(this);
        spawnManager = new SpawnManager(this);
        duelManager = new DuelManager(this);
        
        // Register commands
        CommandHandler commandHandler = new CommandHandler(this);
        getCommand("ckd").setExecutor(commandHandler);
        getCommand("ckd").setTabCompleter(commandHandler);
        
        // Register listeners
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        
        // Setup health display for already online players (in case of /reload)
        getServer().getScheduler().runTaskLater(this, () -> {
            healthDisplayManager.setupHealthDisplayForAll();
        }, 20L);
        
        getLogger().info("CustomKitDuels has been enabled!");
        getLogger().info("Features:");
        getLogger().info("- FastBoard scoreboards with Adventure API hex color support");
        getLogger().info("- Below-name health indicators using scoreboard objectives");
        getLogger().info("- Proper health restoration system");
        getLogger().info("- Player disconnection handling");
        getLogger().info("- Full compatibility with all server versions");
    }
    
    @Override
    public void onDisable() {
        // Clean up all managers
        if (duelManager != null) {
            duelManager.cleanupAllDuels();
        }
        
        if (scoreboardManager != null) {
            scoreboardManager.cleanup();
        }
        
        if (healthDisplayManager != null) {
            healthDisplayManager.cleanup();
        }
        
        getLogger().info("CustomKitDuels has been disabled!");
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
        getLogger().info("Configuration reloaded!");
    }
}