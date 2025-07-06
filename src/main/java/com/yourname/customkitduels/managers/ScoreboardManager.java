package com.yourname.customkitduels.managers;

import com.yourname.customkitduels.CustomKitDuels;
import com.yourname.customkitduels.data.RoundsDuel;
import com.yourname.customkitduels.utils.ColorUtils;
import fr.mrmicky.fastboard.FastBoard;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Enhanced ScoreboardManager using FastBoard for better performance and hex color support
 * Uses Adventure API for modern text formatting
 */
public class ScoreboardManager {
    
    private final CustomKitDuels plugin;
    private final File scoreboardFile;
    private FileConfiguration scoreboardConfig;
    private final Map<UUID, FastBoard> playerBoards;
    private final Map<UUID, BukkitRunnable> updateTasks;
    
    public ScoreboardManager(CustomKitDuels plugin) {
        this.plugin = plugin;
        this.scoreboardFile = new File(plugin.getDataFolder(), "scoreboard.yml");
        this.playerBoards = new HashMap<>();
        this.updateTasks = new HashMap<>();
        
        loadScoreboardConfig();
        plugin.getLogger().info("ScoreboardManager initialized with FastBoard and Adventure API support");
    }
    
    private void loadScoreboardConfig() {
        if (!scoreboardFile.exists()) {
            createDefaultScoreboardConfig();
        }
        
        scoreboardConfig = YamlConfiguration.loadConfiguration(scoreboardFile);
    }
    
    private void createDefaultScoreboardConfig() {
        scoreboardConfig = new YamlConfiguration();
        
        // Using MiniMessage format for better hex support
        scoreboardConfig.set("title", "<gradient:#00FF98:#C3F6E2><bold>PakMC</bold></gradient>");
        scoreboardConfig.set("lines", Arrays.asList(
            " ",
            " <#00FF98><bold>DUEL</bold> <gray>(FT<#C3F6E2><rounds></gray>)",
            " <#C3F6E2>│ Duration: <#00FF98><duration>",
            " <#C3F6E2>│ Round: <#00FF98><current_round>",
            " ",
            " <#00FF98><bold>SCORE</bold>",
            " <#C3F6E2>│ <green><player_score></green> - <red><opponent_score></red>",
            " <#C3F6E2>│ <gray><player_name> vs <opponent_name></gray>",
            " ",
            "    <#C3F6E2>pakmc.xyz"
        ));
        
        try {
            scoreboardConfig.save(scoreboardFile);
            plugin.getLogger().info("Created default scoreboard.yml with MiniMessage format");
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to create scoreboard.yml: " + e.getMessage());
        }
    }
    
    public void showDuelScoreboard(Player player, RoundsDuel roundsDuel) {
        // Remove existing board if present
        removeDuelScoreboard(player);
        
        // Create new FastBoard
        String title = scoreboardConfig.getString("title", "<gradient:#00FF98:#C3F6E2><bold>PakMC</bold></gradient>");
        FastBoard board = new FastBoard(player);
        board.updateTitle(ColorUtils.translateColors(title));
        
        playerBoards.put(player.getUniqueId(), board);
        
        // Start update task for real-time updates
        BukkitRunnable updateTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline() || !playerBoards.containsKey(player.getUniqueId()) || !roundsDuel.isActive()) {
                    this.cancel();
                    updateTasks.remove(player.getUniqueId());
                    return;
                }
                
                updateDuelScoreboard(player, roundsDuel);
            }
        };
        
        updateTask.runTaskTimer(plugin, 0L, 10L); // Update every 0.5 seconds
        updateTasks.put(player.getUniqueId(), updateTask);
        
        // Initial update
        updateDuelScoreboard(player, roundsDuel);
        
        plugin.getLogger().info("Showing duel scoreboard for " + player.getName() + " with FastBoard");
    }
    
    public void updateDuelScoreboard(Player player, RoundsDuel roundsDuel) {
        FastBoard board = playerBoards.get(player.getUniqueId());
        if (board == null) return;
        
        List<String> lines = scoreboardConfig.getStringList("lines");
        List<String> processedLines = new ArrayList<>();
        
        Player opponent = roundsDuel.getOpponent(player);
        
        for (String line : lines) {
            String processedLine = replacePlaceholders(line, player, opponent, roundsDuel);
            processedLine = ColorUtils.translateColors(processedLine);
            
            // FastBoard handles line length automatically, but we'll limit to 40 chars for safety
            if (ColorUtils.stripColors(processedLine).length() > 40) {
                processedLine = ColorUtils.truncateWithColors(processedLine, 40);
            }
            
            processedLines.add(processedLine);
        }
        
        board.updateLines(processedLines);
    }
    
    public void removeDuelScoreboard(Player player) {
        // Cancel update task
        BukkitRunnable updateTask = updateTasks.remove(player.getUniqueId());
        if (updateTask != null) {
            updateTask.cancel();
        }
        
        // Remove FastBoard
        FastBoard board = playerBoards.remove(player.getUniqueId());
        if (board != null) {
            board.delete();
            plugin.getLogger().info("Removed duel scoreboard for " + player.getName());
        }
    }
    
    private String replacePlaceholders(String line, Player player, Player opponent, RoundsDuel roundsDuel) {
        if (opponent == null) return line;
        
        // Calculate duration
        long durationMs = System.currentTimeMillis() - roundsDuel.getStartTime();
        long durationSeconds = durationMs / 1000;
        long minutes = durationSeconds / 60;
        long seconds = durationSeconds % 60;
        String duration = String.format("%02d:%02d", minutes, seconds);
        
        // Get scores
        int playerScore = player.equals(roundsDuel.getPlayer1()) ? roundsDuel.getPlayer1Wins() : roundsDuel.getPlayer2Wins();
        int opponentScore = player.equals(roundsDuel.getPlayer1()) ? roundsDuel.getPlayer2Wins() : roundsDuel.getPlayer1Wins();
        
        return line
            .replace("<rounds>", String.valueOf(roundsDuel.getTargetRounds()))
            .replace("<duration>", duration)
            .replace("<current_round>", String.valueOf(roundsDuel.getCurrentRound()))
            .replace("<player_score>", String.valueOf(playerScore))
            .replace("<opponent_score>", String.valueOf(opponentScore))
            .replace("<player_name>", truncateName(player.getName(), 8))
            .replace("<opponent_name>", truncateName(opponent.getName(), 8));
    }
    
    private String truncateName(String name, int maxLength) {
        if (name.length() <= maxLength) {
            return name;
        }
        return name.substring(0, maxLength - 1) + "…";
    }
    
    public void reloadConfig() {
        loadScoreboardConfig();
        plugin.getLogger().info("Scoreboard configuration reloaded");
    }
    
    /**
     * Clean up all scoreboards and tasks
     */
    public void cleanup() {
        // Cancel all update tasks
        for (BukkitRunnable task : updateTasks.values()) {
            task.cancel();
        }
        updateTasks.clear();
        
        // Remove all boards
        for (FastBoard board : playerBoards.values()) {
            board.delete();
        }
        playerBoards.clear();
        
        plugin.getLogger().info("ScoreboardManager cleaned up");
    }
}