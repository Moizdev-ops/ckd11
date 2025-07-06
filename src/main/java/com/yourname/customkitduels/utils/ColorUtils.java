package com.yourname.customkitduels.utils;

import org.bukkit.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Simple utility class for handling colors and text formatting
 * Uses native Bukkit ChatColor with hex support for 1.16+
 */
public class ColorUtils {
    
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private static final Pattern MINI_HEX_PATTERN = Pattern.compile("<#([A-Fa-f0-9]{6})>");
    
    /**
     * Translates color codes including hex colors to Bukkit format
     * Supports &#RRGGBB, <#RRGGBB>, and legacy &a format
     */
    public static String translateColors(String text) {
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
     * Converts &#RRGGBB format to Bukkit hex format
     */
    private static String convertHexColors(String text) {
        Matcher matcher = HEX_PATTERN.matcher(text);
        StringBuffer buffer = new StringBuffer();
        
        while (matcher.find()) {
            String hex = matcher.group(1);
            String replacement = net.md_5.bungee.api.ChatColor.of("#" + hex).toString();
            matcher.appendReplacement(buffer, replacement);
        }
        matcher.appendTail(buffer);
        
        return buffer.toString();
    }
    
    /**
     * Converts <#RRGGBB> format to Bukkit hex format
     */
    private static String convertMiniHexColors(String text) {
        Matcher matcher = MINI_HEX_PATTERN.matcher(text);
        StringBuffer buffer = new StringBuffer();
        
        while (matcher.find()) {
            String hex = matcher.group(1);
            String replacement = net.md_5.bungee.api.ChatColor.of("#" + hex).toString();
            matcher.appendReplacement(buffer, replacement);
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
        
        // Remove hex colors first
        text = HEX_PATTERN.matcher(text).replaceAll("");
        text = MINI_HEX_PATTERN.matcher(text).replaceAll("");
        
        // Remove legacy colors
        return ChatColor.stripColor(text);
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
}