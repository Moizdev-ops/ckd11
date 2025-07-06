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
    
    @Override
    public void onEnable() {
        instance = this;
        
        // Save default config
        saveDefaultConfig();
        
        // Initialize managers
        categoryManager = new CategoryManager(this);
        kitManager = new KitManager(this);
        arenaManager = new ArenaManager(this);
        duelManager = new DuelManager(this);
        scoreboardManager = new ScoreboardManager(this);
        spawnManager = new SpawnManager(this);
        
        // Register commands
        CommandHandler commandHandler = new CommandHandler(this);
        getCommand("ckd").setExecutor(commandHandler);
        getCommand("ckd").setTabCompleter(commandHandler);
        
        // Register listeners
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        
        getLogger().info("CustomKitDuels has been enabled!");
    }
    
    @Override
    public void onDisable() {
        // Clean up any ongoing duels
        if (duelManager != null) {
            duelManager.cleanupAllDuels();
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
    
    public void reloadPluginConfig() {
        reloadConfig();
        arenaManager.loadArenas();
        categoryManager.reloadCategories();
        scoreboardManager.reloadConfig();
        getLogger().info("Configuration reloaded!");
    }
}