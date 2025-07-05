package com.yourname.customkitduels;

import com.yourname.customkitduels.commands.CommandHandler;
import com.yourname.customkitduels.managers.ArenaManager;
import com.yourname.customkitduels.managers.DuelManager;
import com.yourname.customkitduels.managers.KitManager;
import com.yourname.customkitduels.listeners.PlayerListener;
import org.bukkit.plugin.java.JavaPlugin;

public class CustomKitDuels extends JavaPlugin {
    
    private static CustomKitDuels instance;
    private KitManager kitManager;
    private ArenaManager arenaManager;
    private DuelManager duelManager;
    
    @Override
    public void onEnable() {
        instance = this;
        
        // Save default config
        saveDefaultConfig();
        
        // Initialize managers
        kitManager = new KitManager(this);
        arenaManager = new ArenaManager(this);
        duelManager = new DuelManager(this);
        
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
    
    public void reloadPluginConfig() {
        reloadConfig();
        arenaManager.loadArenas();
        getLogger().info("Configuration reloaded!");
    }
}