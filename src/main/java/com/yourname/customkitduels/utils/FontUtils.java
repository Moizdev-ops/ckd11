package com.yourname.customkitduels.utils;

/**
 * Utility class for converting text to small caps Unicode font
 * Converts regular text to "ᴛᴇꜱᴛ" style font
 */
public class FontUtils {
    
    // Unicode small caps mapping
    private static final String NORMAL = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final String SMALL_CAPS = "ᴀʙᴄᴅᴇꜰɢʜɪᴊᴋʟᴍɴᴏᴘǫʀꜱᴛᴜᴠᴡxʏᴢᴀʙᴄᴅᴇꜰɢʜɪᴊᴋʟᴍɴᴏᴘǫʀꜱᴛᴜᴠᴡxʏᴢ0123456789";
    
    /**
     * Convert regular text to small caps Unicode font
     */
    public static String toSmallCaps(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        StringBuilder result = new StringBuilder();
        
        for (char c : text.toCharArray()) {
            int index = NORMAL.indexOf(c);
            if (index != -1) {
                result.append(SMALL_CAPS.charAt(index));
            } else {
                result.append(c); // Keep special characters as is
            }
        }
        
        return result.toString();
    }
    
    /**
     * Convert text to small caps with color codes preserved
     */
    public static String toSmallCapsWithColor(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        StringBuilder result = new StringBuilder();
        boolean inColorCode = false;
        
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            
            // Check for color code start
            if (c == '§' || c == '&') {
                inColorCode = true;
                result.append(c);
                continue;
            }
            
            // If we're in a color code, skip the next character
            if (inColorCode) {
                inColorCode = false;
                result.append(c);
                continue;
            }
            
            // Convert regular characters
            int index = NORMAL.indexOf(c);
            if (index != -1) {
                result.append(SMALL_CAPS.charAt(index));
            } else {
                result.append(c);
            }
        }
        
        return result.toString();
    }
}