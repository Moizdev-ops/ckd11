package com.yourname.customkitduels.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for handling colors and text formatting
 * Uses Adventure API for modern hex color support
 */
public class ColorUtils {
    
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacyAmpersand();
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    
    /**
     * Translates color codes including hex colors to legacy format for Bukkit compatibility
     * Supports both &#RRGGBB and MiniMessage format
     */
    public static String translateColors(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        // First convert &#RRGGBB to MiniMessage format
        text = convertHexToMiniMessage(text);
        
        // Parse with MiniMessage and convert to legacy
        try {
            Component component = MINI_MESSAGE.deserialize(text);
            return LEGACY_SERIALIZER.serialize(component);
        } catch (Exception e) {
            // Fallback to basic color code translation
            return ChatColor.translateAlternateColorCodes('&', text);
        }
    }
    
    /**
     * Converts &#RRGGBB format to MiniMessage <#RRGGBB> format
     */
    private static String convertHexToMiniMessage(String text) {
        Matcher matcher = HEX_PATTERN.matcher(text);
        StringBuffer buffer = new StringBuffer();
        
        while (matcher.find()) {
            String hex = matcher.group(1);
            matcher.appendReplacement(buffer, "<#" + hex + ">");
        }
        matcher.appendTail(buffer);
        
        return buffer.toString();
    }
    
    /**
     * Creates an Adventure Component from text with color support
     */
    public static Component createComponent(String text) {
        if (text == null || text.isEmpty()) {
            return Component.empty();
        }
        
        // Convert &#RRGGBB to MiniMessage format
        text = convertHexToMiniMessage(text);
        
        try {
            return MINI_MESSAGE.deserialize(text);
        } catch (Exception e) {
            // Fallback to legacy parsing
            return LEGACY_SERIALIZER.deserialize(ChatColor.translateAlternateColorCodes('&', text));
        }
    }
    
    /**
     * Strips all color codes from text
     */
    public static String stripColors(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        // Remove hex colors
        text = HEX_PATTERN.matcher(text).replaceAll("");
        
        // Remove legacy colors
        return ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', text));
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
        
        // Find the position in the original text that corresponds to maxLength in stripped text
        int originalPos = 0;
        int strippedPos = 0;
        
        while (originalPos < text.length() && strippedPos < maxLength) {
            if (text.charAt(originalPos) == '&' && originalPos + 1 < text.length()) {
                // Skip color code
                originalPos += 2;
            } else if (text.startsWith("&#", originalPos) && originalPos + 8 < text.length()) {
                // Skip hex color code
                originalPos += 8;
            } else {
                // Regular character
                originalPos++;
                strippedPos++;
            }
        }
        
        return text.substring(0, originalPos);
    }
}