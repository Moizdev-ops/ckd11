package com.yourname.customkitduels.managers;

import com.yourname.customkitduels.CustomKitDuels;
import com.yourname.customkitduels.data.RoundsDuel;
import com.yourname.customkitduels.utils.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Enhanced ScoreboardManager with Adventure API support for hex colors
 * Uses native Bukkit scoreboards with proper color translation
 */
public class ScoreboardManager {
    
    private final CustomKitDuels plugin;
    private final File scoreboardFile;
    private FileConfiguration scoreboardConfig;
    private final Map<UUID, Scoreboard> playerBoards;
    private final Map<UUID, BukkitRunnable> updateTasks;
    
    public ScoreboardManager(CustomKitDuels plugin) {
        this.plugin = plugin;
        this.scoreboardFile = new File(plugin.getDataFolder(), "scoreboard.yml");
        this.playerBoards = new HashMap<>();
        this.updateTasks = new HashMap<>();
        
        loadScoreboardConfig();
        plugin.getLogger().info("ScoreboardManager initialized with Adventure API hex color support");
    }
    
    private void loadScoreboardConfig() {
        if (!scoreboardFile.exists()) {
            createDefaultScoreboardConfig();
        }
        
        scoreboardConfig = YamlConfiguration.loadConfiguration(scoreboardFile);
    }
    
    private void createDefaultScoreboardConfig() {
        scoreboardConfig = new YamlConfiguration();
        
        // Using MiniMessage format with gradients and hex colors
        scoreboardConfig.set("title", "<gradient:#00FF98:#C3F6E2><bold>PakMC</bold></gradient>");
        scoreboardConfig.set("lines", Arrays.asList(
            " ",
            " <#00FF98><bold>DUEL</bold> <gray>(FT<#C3F6E2><rounds></gray>)",
            " <#C3F6E2>│ Duration: <#00FF98><duration>",
            " <#C3F6E2>│ Round: <#00FF98><current_round>",
            " ",
            " <#00FF98><bold>SCORE</bold>",
            " <#C3F6E2>│ <green><player_score></green> <gray>-</gray> <red><opponent_score></red>",
            " <#C3F6E2>│ <gray><player_name> vs <opponent_name></gray>",
            " ",
            "    <#C3F6E2>pakmc.xyz"
        ));
        
        try {
            scoreboardConfig.save(scoreboardFile);
            plugin.getLogger().info("Created default scoreboard.yml with MiniMessage format and hex colors");
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to create scoreboard.yml: " + e.getMessage());
        }
    }
    
    public void showDuelScoreboard(Player player, RoundsDuel roundsDuel) {
        // Remove existing board if present
        removeDuelScoreboard(player);
        
        // Create new scoreboard
        org.bukkit.scoreboard.ScoreboardManager bukkitManager = Bukkit.getScoreboardManager();
        if (bukkitManager == null) return;
        
        Scoreboard board = bukkitManager.getNewScoreboard();
        String title = scoreboardConfig.getString("title", "<gradient:#00FF98:#C3F6E2><bold>PakMC</bold></gradient>");
        
        // Translate colors using Adventure API
        String translatedTitle = ColorUtils.translateColors(title);
        if (translatedTitle.length() > 32) {
            translatedTitle = ColorUtils.truncateWithColors(translatedTitle, 32);
        }
        
        Objective objective = board.registerNewObjective("duel", "dummy", translatedTitle);
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        
        playerBoards.put(player.getUniqueId(), board);
        player.setScoreboard(board);
        
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
        
        plugin.getLogger().info("Showing enhanced duel scoreboard for " + player.getName());
    }
    
    public void updateDuelScoreboard(Player player, RoundsDuel roundsDuel) {
        Scoreboard board = playerBoards.get(player.getUniqueId());
        if (board == null) return;
        
        Objective objective = board.getObjective("duel");
        if (objective == null) return;
        
        // Clear existing scores
        for (String entry : board.getEntries()) {
            board.resetScores(entry);
        }
        
        List<String> lines = scoreboardConfig.getStringList("lines");
        Player opponent = roundsDuel.getOpponent(player);
        
        // Process lines in reverse order (Bukkit scoreboards are weird)
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            String processedLine = replacePlaceholders(line, player, opponent, roundsDuel);
            
            // Translate colors using Adventure API
            processedLine = ColorUtils.translateColors(processedLine);
            
            // Ensure line is unique and not too long
            if (processedLine.length() > 40) {
                processedLine = ColorUtils.truncateWithColors(processedLine, 40);
            }
            
            // Make line unique by adding invisible characters if needed
            String uniqueLine = makeLineUnique(processedLine, board);
            
            Score score = objective.getScore(uniqueLine);
            score.setScore(lines.size() - i);
        }
    }
    
    /**
     * Make scoreboard line unique by adding invisible characters
     */
    private String makeLineUnique(String line, Scoreboard board) {
        String uniqueLine = line;
        int attempts = 0;
        
        while (board.getEntries().contains(uniqueLine) && attempts < 10) {
            uniqueLine = line + ChatColor.RESET.toString().repeat(attempts + 1);
            attempts++;
        }
        
        return uniqueLine;
    }
    
    public void removeDuelScoreboard(Player player) {
        // Cancel update task
        BukkitRunnable updateTask = updateTasks.remove(player.getUniqueId());
        if (updateTask != null) {
            updateTask.cancel();
        }
        
        // Remove scoreboard
        playerBoards.remove(player.getUniqueId());
        
        // Reset to main scoreboard
        org.bukkit.scoreboard.ScoreboardManager bukkitManager = Bukkit.getScoreboardManager();
        if (bukkitManager != null) {
            player.setScoreboard(bukkitManager.getMainScoreboard());
        }
        
        plugin.getLogger().info("Removed duel scoreboard for " + player.getName());
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
        plugin.getLogger().info("Scoreboard configuration reloaded with Adventure API support");
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
        
        // Reset all players to main scoreboard
        org.bukkit.scoreboard.ScoreboardManager bukkitManager = Bukkit.getScoreboardManager();
        if (bukkitManager != null) {
            Scoreboard mainBoard = bukkitManager.getMainScoreboard();
            for (UUID playerId : playerBoards.keySet()) {
                Player player = Bukkit.getPlayer(playerId);
                if (player != null && player.isOnline()) {
                    player.setScoreboard(mainBoard);
                }
            }
        }
        
        playerBoards.clear();
        plugin.getLogger().info("ScoreboardManager cleaned up");
    }
}