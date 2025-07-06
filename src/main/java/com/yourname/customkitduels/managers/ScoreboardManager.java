package com.yourname.customkitduels.managers;

import com.yourname.customkitduels.CustomKitDuels;
import com.yourname.customkitduels.data.RoundsDuel;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScoreboardManager {
    
    private final CustomKitDuels plugin;
    private final File scoreboardFile;
    private FileConfiguration scoreboardConfig;
    private final Map<UUID, Scoreboard> playerScoreboards;
    private final Pattern hexPattern = Pattern.compile("&#([A-Fa-f0-9]{6})");
    
    public ScoreboardManager(CustomKitDuels plugin) {
        this.plugin = plugin;
        this.scoreboardFile = new File(plugin.getDataFolder(), "scoreboard.yml");
        this.playerScoreboards = new HashMap<>();
        
        loadScoreboardConfig();
    }
    
    private void loadScoreboardConfig() {
        if (!scoreboardFile.exists()) {
            createDefaultScoreboardConfig();
        }
        
        scoreboardConfig = YamlConfiguration.loadConfiguration(scoreboardFile);
    }
    
    private void createDefaultScoreboardConfig() {
        scoreboardConfig = new YamlConfiguration();
        
        scoreboardConfig.set("title", "&#FF6B6B&l⚔ &#FFE66D&lDUEL &#FF6B6B&l⚔");
        scoreboardConfig.set("lines", Arrays.asList(
            " ",
            "&#4ECDC4&l┌─── MATCH INFO ───┐",
            "&#95E1D3│ &#FFE66DMode: &#FF6B6BFirst to <rounds>",
            "&#95E1D3│ &#FFE66DRound: &#4ECDC4<current_round>&#95E1D3/&#4ECDC4<rounds>",
            "&#95E1D3│ &#FFE66DTime: &#4ECDC4<duration>",
            "&#4ECDC4&l└─────────────────┘",
            " ",
            "&#FF6B6B&l┌──── FIGHTERS ────┐",
            "&#FFB3BA│ &#4ECDC4<player_name>: &#95E1D3<player_score> &#FF6B6B❤<player_health>",
            "&#FFB3BA│ &#4ECDC4<opponent_name>: &#95E1D3<opponent_score> &#FF6B6B❤<opponent_health>",
            "&#FF6B6B&l└─────────────────┘",
            " ",
            "&#95E1D3&l┌──── STATUS ────┐",
            "&#C7ECEE│ &#FFE66D<status_message>",
            "&#95E1D3&l└───────────────┘",
            " ",
            "    &#4ECDC4&lpakmc.xyz"
        ));
        
        try {
            scoreboardConfig.save(scoreboardFile);
            plugin.getLogger().info("Created enhanced scoreboard.yml configuration");
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to create scoreboard.yml: " + e.getMessage());
        }
    }
    
    public void showDuelScoreboard(Player player, RoundsDuel roundsDuel) {
        org.bukkit.scoreboard.ScoreboardManager bukkitScoreboardManager = Bukkit.getScoreboardManager();
        if (bukkitScoreboardManager == null) return;
        
        Scoreboard scoreboard = bukkitScoreboardManager.getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("duel", "dummy", translateHexColors(scoreboardConfig.getString("title", "Duel")));
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        
        List<String> lines = scoreboardConfig.getStringList("lines");
        Player opponent = roundsDuel.getOpponent(player);
        
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            line = replacePlaceholders(line, player, opponent, roundsDuel);
            line = translateHexColors(line);
            
            // Bukkit scoreboard lines are limited to 40 characters
            if (line.length() > 40) {
                line = line.substring(0, 40);
            }
            
            Score score = objective.getScore(line);
            score.setScore(lines.size() - i);
        }
        
        player.setScoreboard(scoreboard);
        playerScoreboards.put(player.getUniqueId(), scoreboard);
    }
    
    public void updateDuelScoreboard(Player player, RoundsDuel roundsDuel) {
        if (playerScoreboards.containsKey(player.getUniqueId())) {
            showDuelScoreboard(player, roundsDuel); // Refresh the entire scoreboard
        }
    }
    
    public void removeDuelScoreboard(Player player) {
        playerScoreboards.remove(player.getUniqueId());
        
        // Reset to default scoreboard
        org.bukkit.scoreboard.ScoreboardManager bukkitScoreboardManager = Bukkit.getScoreboardManager();
        if (bukkitScoreboardManager != null) {
            player.setScoreboard(bukkitScoreboardManager.getMainScoreboard());
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
        
        // Get health information
        String playerHealth = String.format("%.1f", player.getHealth() / 2.0); // Convert to hearts
        String opponentHealth = String.format("%.1f", opponent.getHealth() / 2.0); // Convert to hearts
        
        // Determine status message
        String statusMessage;
        if (roundsDuel.getCurrentRound() == 1 && playerScore == 0 && opponentScore == 0) {
            statusMessage = "Fight!";
        } else if (playerScore > opponentScore) {
            statusMessage = "You're winning!";
        } else if (opponentScore > playerScore) {
            statusMessage = "You're behind!";
        } else {
            statusMessage = "It's tied!";
        }
        
        return line
            .replace("<rounds>", String.valueOf(roundsDuel.getTargetRounds()))
            .replace("<duration>", duration)
            .replace("<current_round>", String.valueOf(roundsDuel.getCurrentRound()))
            .replace("<player_score>", String.valueOf(playerScore))
            .replace("<opponent_score>", String.valueOf(opponentScore))
            .replace("<player_name>", truncateName(player.getName(), 8))
            .replace("<opponent_name>", truncateName(opponent.getName(), 8))
            .replace("<player_health>", playerHealth)
            .replace("<opponent_health>", opponentHealth)
            .replace("<status_message>", statusMessage);
    }
    
    private String truncateName(String name, int maxLength) {
        if (name.length() <= maxLength) {
            return name;
        }
        return name.substring(0, maxLength - 1) + "…";
    }
    
    private String translateHexColors(String text) {
        if (text == null) return "";
        
        // Convert &#RRGGBB to &x&R&R&G&G&B&B format for Bukkit
        Matcher matcher = hexPattern.matcher(text);
        StringBuffer buffer = new StringBuffer();
        
        while (matcher.find()) {
            String hex = matcher.group(1);
            StringBuilder replacement = new StringBuilder("&x");
            for (char c : hex.toCharArray()) {
                replacement.append("&").append(c);
            }
            matcher.appendReplacement(buffer, replacement.toString());
        }
        matcher.appendTail(buffer);
        
        // Translate color codes
        return ChatColor.translateAlternateColorCodes('&', buffer.toString());
    }
    
    public void reloadConfig() {
        loadScoreboardConfig();
    }
}