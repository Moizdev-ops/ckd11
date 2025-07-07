package com.yourname.customkitduels.managers;

import com.yourname.customkitduels.CustomKitDuels;
import com.yourname.customkitduels.data.RoundsDuel;
import fr.mrmicky.fastboard.FastBoard;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Enhanced ScoreboardManager using FastBoard with proper color support
 * Provides high-performance scoreboards with hex color support
 */
public class ScoreboardManager {
    
    private final CustomKitDuels plugin;
    private final File scoreboardFile;
    private FileConfiguration scoreboardConfig;
    private final Map<UUID, FastBoard> playerBoards;
    private final Map<UUID, BukkitRunnable> updateTasks;
    
    // Color patterns for proper hex color support
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private static final Pattern MINI_HEX_PATTERN = Pattern.compile("<#([A-Fa-f0-9]{6})>");
    private static final Pattern GRADIENT_PATTERN = Pattern.compile("<gradient:(#[A-Fa-f0-9]{6}):(#[A-Fa-f0-9]{6})>(.*?)</gradient>");
    
    public ScoreboardManager(CustomKitDuels plugin) {
        this.plugin = plugin;
        this.scoreboardFile = new File(plugin.getDataFolder(), "scoreboard.yml");
        this.playerBoards = new HashMap<>();
        this.updateTasks = new HashMap<>();
        
        loadScoreboardConfig();
        plugin.getLogger().info("ScoreboardManager initialized with FastBoard and proper color support");
    }
    
    private void loadScoreboardConfig() {
        if (!scoreboardFile.exists()) {
            createDefaultScoreboardConfig();
        }
        
        scoreboardConfig = YamlConfiguration.loadConfiguration(scoreboardFile);
    }
    
    private void createDefaultScoreboardConfig() {
        scoreboardConfig = new YamlConfiguration();
        
        // Using proper color codes that will be translated
        scoreboardConfig.set("title", "&#00FF98&lPakMC");
        scoreboardConfig.set("lines", Arrays.asList(
            " ",
            " &#00FF98&lDUEL &7(FT&#C3F6E2<rounds>&7)",
            " &#C3F6E2│ Duration: &#00FF98<duration>",
            " &#C3F6E2│ Round: &#00FF98<current_round>",
            " ",
            " &#00FF98&lSCORE",
            " &#C3F6E2│ &a<player_score> &7- &c<opponent_score>",
            " &#C3F6E2│ &7<player_name> vs <opponent_name>",
            " ",
            "    &#C3F6E2pakmc.xyz"
        ));
        
        try {
            scoreboardConfig.save(scoreboardFile);
            plugin.getLogger().info("Created default scoreboard.yml with proper color format");
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to create scoreboard.yml: " + e.getMessage());
        }
    }
    
    public void showDuelScoreboard(Player player, RoundsDuel roundsDuel) {
        // Remove existing board if present
        removeDuelScoreboard(player);
        
        // Get title and translate colors
        String title = scoreboardConfig.getString("title", "&#00FF98&lPakMC");
        String translatedTitle = translateColors(title);
        
        // Create FastBoard
        FastBoard board = new FastBoard(player);
        board.updateTitle(translatedTitle);
        
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
        
        plugin.getLogger().info("Showing FastBoard duel scoreboard for " + player.getName());
    }
    
    public void updateDuelScoreboard(Player player, RoundsDuel roundsDuel) {
        FastBoard board = playerBoards.get(player.getUniqueId());
        if (board == null) return;
        
        List<String> lines = scoreboardConfig.getStringList("lines");
        Player opponent = roundsDuel.getOpponent(player);
        
        if (opponent == null) return;
        
        List<String> processedLines = new ArrayList<>();
        
        // Process each line
        for (String line : lines) {
            String processedLine = replacePlaceholders(line, player, opponent, roundsDuel);
            
            // Translate colors properly
            processedLine = translateColors(processedLine);
            
            processedLines.add(processedLine);
        }
        
        // Update FastBoard with processed lines
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
        }
        
        plugin.getLogger().info("Removed FastBoard duel scoreboard for " + player.getName());
    }
    
    private String replacePlaceholders(String line, Player player, Player opponent, RoundsDuel roundsDuel) {
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
    
    /**
     * Properly translate colors including hex colors
     */
    private String translateColors(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        // Convert &#RRGGBB to proper hex format
        text = convertHexColors(text);
        
        // Convert <#RRGGBB> to proper hex format
        text = convertMiniHexColors(text);
        
        // Handle gradients (simplified - just use first color)
        text = convertGradients(text);
        
        // Handle legacy color codes
        text = ChatColor.translateAlternateColorCodes('&', text);
        
        return text;
    }
    
    private String convertHexColors(String text) {
        Matcher matcher = HEX_PATTERN.matcher(text);
        StringBuffer buffer = new StringBuffer();
        
        while (matcher.find()) {
            String hex = matcher.group(1);
            try {
                String replacement = net.md_5.bungee.api.ChatColor.of("#" + hex).toString();
                matcher.appendReplacement(buffer, replacement);
            } catch (Exception e) {
                // Fallback to legacy color if hex fails
                matcher.appendReplacement(buffer, ChatColor.WHITE.toString());
            }
        }
        matcher.appendTail(buffer);
        
        return buffer.toString();
    }
    
    private String convertMiniHexColors(String text) {
        Matcher matcher = MINI_HEX_PATTERN.matcher(text);
        StringBuffer buffer = new StringBuffer();
        
        while (matcher.find()) {
            String hex = matcher.group(1);
            try {
                String replacement = net.md_5.bungee.api.ChatColor.of("#" + hex).toString();
                matcher.appendReplacement(buffer, replacement);
            } catch (Exception e) {
                // Fallback to legacy color if hex fails
                matcher.appendReplacement(buffer, ChatColor.WHITE.toString());
            }
        }
        matcher.appendTail(buffer);
        
        return buffer.toString();
    }
    
    private String convertGradients(String text) {
        Matcher matcher = GRADIENT_PATTERN.matcher(text);
        StringBuffer buffer = new StringBuffer();
        
        while (matcher.find()) {
            String startColor = matcher.group(1);
            String content = matcher.group(3);
            try {
                String replacement = net.md_5.bungee.api.ChatColor.of(startColor).toString() + content;
                matcher.appendReplacement(buffer, replacement);
            } catch (Exception e) {
                // Fallback to just the content
                matcher.appendReplacement(buffer, content);
            }
        }
        matcher.appendTail(buffer);
        
        return buffer.toString();
    }
    
    public void reloadConfig() {
        loadScoreboardConfig();
        plugin.getLogger().info("Scoreboard configuration reloaded with proper color support");
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
        
        // Delete all FastBoards
        for (FastBoard board : playerBoards.values()) {
            board.delete();
        }
        playerBoards.clear();
        
        plugin.getLogger().info("ScoreboardManager cleaned up");
    }
}