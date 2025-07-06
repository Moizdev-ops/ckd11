package com.yourname.customkitduels.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Advanced color utility using Adventure API for proper hex color support
 * Supports MiniMessage format, hex colors, and legacy colors
 */
public class ColorUtils {
    
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private static final Pattern MINI_HEX_PATTERN = Pattern.compile("<#([A-Fa-f0-9]{6})>");
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacyAmpersand();
    
    /**
     * Translates text with Adventure API support for hex colors and MiniMessage
     * Supports &#RRGGBB, <#RRGGBB>, MiniMessage format, and legacy &a format
     */
    public static String translateColors(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        try {
            // First convert &#RRGGBB to <#RRGGBB> format for MiniMessage
            text = convertLegacyHexToMini(text);
            
            // Try to parse as MiniMessage first (supports gradients, hex, etc.)
            Component component = MINI_MESSAGE.deserialize(text);
            
            // Convert back to legacy format for Bukkit compatibility
            return LEGACY_SERIALIZER.serialize(component);
            
        } catch (Exception e) {
            // Fallback to simple hex conversion if MiniMessage parsing fails
            return translateSimpleColors(text);
        }
    }
    
    /**
     * Simple color translation for basic hex and legacy colors
     */
    public static String translateSimpleColors(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        // Convert &#RRGGBB to Bukkit hex format
        text = convertHexColors(text);
        
        // Convert <#RRGGBB> to Bukkit hex format
        text = convertMiniHexColors(text);
        
        // Handle legacy color codes
        text = ChatColor.translateAlternateColorCodes('&', text);
        
        return text;
    }
    
    /**
     * Converts &#RRGGBB format to <#RRGGBB> for MiniMessage
     */
    private static String convertLegacyHexToMini(String text) {
        return HEX_PATTERN.matcher(text).replaceAll("<#$1>");
    }
    
    /**
     * Converts &#RRGGBB format to Bukkit hex format (fallback)
     */
    private static String convertHexColors(String text) {
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
    
    /**
     * Converts <#RRGGBB> format to Bukkit hex format (fallback)
     */
    private static String convertMiniHexColors(String text) {
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
    
    /**
     * Strips all color codes from text
     */
    public static String stripColors(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        try {
            // Parse with MiniMessage and convert to plain text
            Component component = MINI_MESSAGE.deserialize(text);
            return LegacyComponentSerializer.legacySection().serialize(component).replaceAll("§.", "");
        } catch (Exception e) {
            // Fallback to simple stripping
            text = HEX_PATTERN.matcher(text).replaceAll("");
            text = MINI_HEX_PATTERN.matcher(text).replaceAll("");
            return ChatColor.stripColor(text);
        }
    }
    
    /**
     * Truncates text to specified length while preserving color codes
     */
    public static String truncateWithColors(String text, int maxLength) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        String stripped = stripColors(text);
        if (stripped.length() <= maxLength) {
            return text;
        }
        
        // Simple truncation - find position that gives us maxLength visible characters
        String result = text;
        while (stripColors(result).length() > maxLength && result.length() > 0) {
            result = result.substring(0, result.length() - 1);
        }
        
        return result;
    }
    
    /**
     * Get health color based on percentage
     */
    public static ChatColor getHealthColor(double healthPercentage) {
        if (healthPercentage > 0.6) {
            return ChatColor.GREEN;
        } else if (healthPercentage > 0.3) {
            return ChatColor.YELLOW;
        } else {
            return ChatColor.RED;
        }
    }
    
    /**
     * Get health color as hex string based on percentage
     */
    public static String getHealthColorHex(double healthPercentage) {
        if (healthPercentage > 0.6) {
            return "<#00FF00>"; // Green
        } else if (healthPercentage > 0.3) {
            return "<#FFFF00>"; // Yellow
        } else {
            return "<#FF0000>"; // Red
        }
    }
    
    /**
     * Create a health display string with proper colors
     */
    public static String createHealthDisplay(String playerName, double health, double maxHealth) {
        double hearts = health / 2.0;
        double maxHearts = maxHealth / 2.0;
        double healthPercentage = health / maxHealth;
        
        String healthColor = getHealthColorHex(healthPercentage);
        String healthText = String.format("%.1f/%.1f", hearts, maxHearts);
        
        return "<white>" + playerName + " " + healthColor + healthText + " ❤</white>";
    }
}