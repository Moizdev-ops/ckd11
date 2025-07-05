package com.yourname.customkitduels.gui;

import com.yourname.customkitduels.CustomKitDuels;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class EnchantmentSelectorGUI implements Listener {
    
    private final CustomKitDuels plugin;
    private final Player player;
    private final ItemModificationGUI parentGUI;
    private ItemStack targetItem;
    private final Inventory gui;
    private final List<Enchantment> availableEnchantments;
    private static final Map<UUID, EnchantmentSelectorGUI> activeGuis = new HashMap<>();
    private boolean isActive = true;
    
    // Enchantment conflict groups
    private static final Set<Enchantment> PROTECTION_ENCHANTS = new HashSet<>(Arrays.asList(
        Enchantment.PROTECTION, Enchantment.FIRE_PROTECTION, 
        Enchantment.BLAST_PROTECTION, Enchantment.PROJECTILE_PROTECTION
    ));
    
    private static final Set<Enchantment> DAMAGE_ENCHANTS = new HashSet<>(Arrays.asList(
        Enchantment.SHARPNESS, Enchantment.SMITE, Enchantment.BANE_OF_ARTHROPODS
    ));
    
    private static final Set<Enchantment> FORTUNE_SILK_TOUCH = new HashSet<>(Arrays.asList(
        Enchantment.FORTUNE, Enchantment.SILK_TOUCH
    ));
    
    private static final Set<Enchantment> INFINITY_MENDING = new HashSet<>(Arrays.asList(
        Enchantment.INFINITY, Enchantment.MENDING
    ));
    
    private static final Set<Enchantment> DEPTH_STRIDER_FROST_WALKER = new HashSet<>(Arrays.asList(
        Enchantment.DEPTH_STRIDER, Enchantment.FROST_WALKER
    ));
    
    private static final Set<Enchantment> MULTISHOT_PIERCING = new HashSet<>(Arrays.asList(
        Enchantment.MULTISHOT, Enchantment.PIERCING
    ));
    
    private static final Set<Enchantment> LOYALTY_RIPTIDE = new HashSet<>(Arrays.asList(
        Enchantment.LOYALTY, Enchantment.RIPTIDE
    ));
    
    private static final Set<Enchantment> CHANNELING_RIPTIDE = new HashSet<>(Arrays.asList(
        Enchantment.CHANNELING, Enchantment.RIPTIDE
    ));
    
    public EnchantmentSelectorGUI(CustomKitDuels plugin, Player player, ItemModificationGUI parentGUI, ItemStack targetItem) {
        this.plugin = plugin;
        this.player = player;
        this.parentGUI = parentGUI;
        this.targetItem = targetItem.clone();
        this.availableEnchantments = getRelevantEnchantments(targetItem.getType());
        this.gui = Bukkit.createInventory(null, 54, ChatColor.DARK_PURPLE + "Select Enchantments");
        
        plugin.getLogger().info("[DEBUG] Creating EnchantmentSelectorGUI for player " + player.getName());
        
        setupGUI();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    private List<Enchantment> getRelevantEnchantments(Material material) {
        List<Enchantment> enchantments = new ArrayList<>();
        String materialName = material.toString();
        
        // Weapon enchantments
        if (materialName.contains("SWORD") || materialName.contains("AXE") || material == Material.MACE) {
            enchantments.addAll(Arrays.asList(
                Enchantment.SHARPNESS, Enchantment.SMITE, Enchantment.BANE_OF_ARTHROPODS,
                Enchantment.KNOCKBACK, Enchantment.FIRE_ASPECT, Enchantment.LOOTING,
                Enchantment.SWEEPING_EDGE, Enchantment.UNBREAKING, Enchantment.MENDING
            ));
            
            if (material == Material.MACE) {
                enchantments.addAll(Arrays.asList(Enchantment.DENSITY, Enchantment.BREACH, Enchantment.WIND_BURST));
            }
        }
        
        // Tool enchantments
        if (materialName.contains("PICKAXE")) {
            enchantments.addAll(Arrays.asList(
                Enchantment.EFFICIENCY, Enchantment.FORTUNE, Enchantment.SILK_TOUCH,
                Enchantment.UNBREAKING, Enchantment.MENDING
            ));
        }
        
        if (materialName.contains("SHOVEL")) {
            enchantments.addAll(Arrays.asList(
                Enchantment.EFFICIENCY, Enchantment.FORTUNE, Enchantment.SILK_TOUCH,
                Enchantment.UNBREAKING, Enchantment.MENDING
            ));
        }
        
        if (materialName.contains("HOE")) {
            enchantments.addAll(Arrays.asList(
                Enchantment.EFFICIENCY, Enchantment.FORTUNE, Enchantment.SILK_TOUCH,
                Enchantment.UNBREAKING, Enchantment.MENDING
            ));
        }
        
        // Bow enchantments
        if (material == Material.BOW) {
            enchantments.addAll(Arrays.asList(
                Enchantment.POWER, Enchantment.PUNCH, Enchantment.FLAME,
                Enchantment.INFINITY, Enchantment.UNBREAKING, Enchantment.MENDING
            ));
        }
        
        // Crossbow enchantments
        if (material == Material.CROSSBOW) {
            enchantments.addAll(Arrays.asList(
                Enchantment.QUICK_CHARGE, Enchantment.MULTISHOT, Enchantment.PIERCING,
                Enchantment.UNBREAKING, Enchantment.MENDING
            ));
        }
        
        // Trident enchantments
        if (material == Material.TRIDENT) {
            enchantments.addAll(Arrays.asList(
                Enchantment.LOYALTY, Enchantment.CHANNELING, Enchantment.RIPTIDE,
                Enchantment.IMPALING, Enchantment.UNBREAKING, Enchantment.MENDING
            ));
        }
        
        // Armor enchantments
        if (materialName.contains("HELMET") || materialName.contains("CHESTPLATE") || 
            materialName.contains("LEGGINGS") || materialName.contains("BOOTS")) {
            enchantments.addAll(Arrays.asList(
                Enchantment.PROTECTION, Enchantment.FIRE_PROTECTION, Enchantment.BLAST_PROTECTION,
                Enchantment.PROJECTILE_PROTECTION, Enchantment.UNBREAKING, Enchantment.MENDING
            ));
            
            if (materialName.contains("HELMET")) {
                enchantments.addAll(Arrays.asList(Enchantment.AQUA_AFFINITY, Enchantment.RESPIRATION));
            }
            
            if (materialName.contains("CHESTPLATE")) {
                enchantments.add(Enchantment.THORNS);
            }
            
            if (materialName.contains("BOOTS")) {
                enchantments.addAll(Arrays.asList(
                    Enchantment.FEATHER_FALLING, Enchantment.DEPTH_STRIDER, 
                    Enchantment.FROST_WALKER, Enchantment.SOUL_SPEED
                ));
            }
        }
        
        // Shield enchantments
        if (material == Material.SHIELD) {
            enchantments.addAll(Arrays.asList(Enchantment.UNBREAKING, Enchantment.MENDING));
        }
        
        // Fishing rod enchantments
        if (material == Material.FISHING_ROD) {
            enchantments.addAll(Arrays.asList(
                Enchantment.LUCK_OF_THE_SEA, Enchantment.LURE, Enchantment.UNBREAKING, Enchantment.MENDING
            ));
        }
        
        // Other tool enchantments
        if (material == Material.SHEARS || material == Material.FLINT_AND_STEEL ||
            material == Material.CARROT_ON_A_STICK || material == Material.WARPED_FUNGUS_ON_A_STICK) {
            enchantments.addAll(Arrays.asList(Enchantment.UNBREAKING, Enchantment.MENDING));
        }
        
        // Elytra enchantments
        if (material == Material.ELYTRA) {
            enchantments.addAll(Arrays.asList(Enchantment.UNBREAKING, Enchantment.MENDING));
        }
        
        return enchantments;
    }
    
    private void setupGUI() {
        gui.clear();
        
        // Display current item
        gui.setItem(4, targetItem.clone());
        
        // Add enchantment options
        int slot = 9;
        for (Enchantment enchantment : availableEnchantments) {
            if (slot >= 44) break; // Don't overflow into control area
            
            ItemStack enchantItem = new ItemStack(Material.ENCHANTED_BOOK);
            ItemMeta meta = enchantItem.getItemMeta();
            
            String enchantName = formatEnchantmentName(enchantment.getKey().getKey());
            int currentLevel = targetItem.getEnchantmentLevel(enchantment);
            int maxLevel = enchantment.getMaxLevel();
            
            meta.setDisplayName(ChatColor.LIGHT_PURPLE + enchantName);
            
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Current Level: " + (currentLevel > 0 ? currentLevel : "None"));
            lore.add(ChatColor.GRAY + "Max Level: " + maxLevel);
            lore.add(ChatColor.YELLOW + "Left-click to increase level");
            lore.add(ChatColor.YELLOW + "Right-click to decrease level");
            lore.add(ChatColor.RED + "Shift-click to remove");
            
            // Add conflict warnings
            if (hasConflictingEnchantments(enchantment)) {
                lore.add(ChatColor.RED + "⚠ Conflicts with existing enchantments!");
            }
            
            meta.setLore(lore);
            enchantItem.setItemMeta(meta);
            
            gui.setItem(slot, enchantItem);
            slot++;
        }
        
        // Clear all enchantments button
        ItemStack clearButton = new ItemStack(Material.BARRIER);
        ItemMeta clearMeta = clearButton.getItemMeta();
        clearMeta.setDisplayName(ChatColor.RED + "Clear All Enchantments");
        clearMeta.setLore(Arrays.asList(ChatColor.GRAY + "Remove all enchantments from this item"));
        clearButton.setItemMeta(clearMeta);
        gui.setItem(45, clearButton);
        
        // Apply changes button
        ItemStack applyButton = new ItemStack(Material.EMERALD);
        ItemMeta applyMeta = applyButton.getItemMeta();
        applyMeta.setDisplayName(ChatColor.GREEN + "Apply Changes");
        applyMeta.setLore(Arrays.asList(ChatColor.GRAY + "Click to apply enchantments"));
        applyButton.setItemMeta(applyMeta);
        gui.setItem(49, applyButton);
        
        // Back button
        ItemStack backButton = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta backMeta = backButton.getItemMeta();
        backMeta.setDisplayName(ChatColor.RED + "Back");
        backMeta.setLore(Arrays.asList(ChatColor.GRAY + "Return without saving"));
        backButton.setItemMeta(backMeta);
        gui.setItem(53, backButton);
    }
    
    private boolean hasConflictingEnchantments(Enchantment enchantment) {
        Map<Enchantment, Integer> currentEnchants = targetItem.getEnchantments();
        
        for (Enchantment current : currentEnchants.keySet()) {
            if (areEnchantmentsConflicting(enchantment, current)) {
                return true;
            }
        }
        
        return false;
    }
    
    private boolean areEnchantmentsConflicting(Enchantment ench1, Enchantment ench2) {
        if (ench1.equals(ench2)) return false;
        
        // Check all conflict groups
        return (PROTECTION_ENCHANTS.contains(ench1) && PROTECTION_ENCHANTS.contains(ench2)) ||
               (DAMAGE_ENCHANTS.contains(ench1) && DAMAGE_ENCHANTS.contains(ench2)) ||
               (FORTUNE_SILK_TOUCH.contains(ench1) && FORTUNE_SILK_TOUCH.contains(ench2)) ||
               (INFINITY_MENDING.contains(ench1) && INFINITY_MENDING.contains(ench2)) ||
               (DEPTH_STRIDER_FROST_WALKER.contains(ench1) && DEPTH_STRIDER_FROST_WALKER.contains(ench2)) ||
               (MULTISHOT_PIERCING.contains(ench1) && MULTISHOT_PIERCING.contains(ench2)) ||
               (LOYALTY_RIPTIDE.contains(ench1) && LOYALTY_RIPTIDE.contains(ench2)) ||
               (CHANNELING_RIPTIDE.contains(ench1) && CHANNELING_RIPTIDE.contains(ench2));
    }
    
    private void removeConflictingEnchantments(Enchantment newEnchantment) {
        Map<Enchantment, Integer> currentEnchants = new HashMap<>(targetItem.getEnchantments());
        
        for (Enchantment current : currentEnchants.keySet()) {
            if (areEnchantmentsConflicting(newEnchantment, current)) {
                targetItem.removeEnchantment(current);
                player.sendMessage(ChatColor.YELLOW + "Removed conflicting enchantment: " + formatEnchantmentName(current.getKey().getKey()));
            }
        }
    }
    
    private String formatEnchantmentName(String key) {
        String[] words = key.split("_");
        StringBuilder formatted = new StringBuilder();
        
        for (String word : words) {
            if (formatted.length() > 0) {
                formatted.append(" ");
            }
            formatted.append(word.substring(0, 1).toUpperCase()).append(word.substring(1).toLowerCase());
        }
        
        return formatted.toString();
    }
    
    public void open() {
        plugin.getLogger().info("[DEBUG] Opening EnchantmentSelectorGUI for " + player.getName());
        
        EnchantmentSelectorGUI existing = activeGuis.get(player.getUniqueId());
        if (existing != null && existing != this) {
            plugin.getLogger().info("[DEBUG] Cleaning up existing EnchantmentSelectorGUI for " + player.getName());
            existing.forceCleanup();
        }
        
        activeGuis.put(player.getUniqueId(), this);
        isActive = true;
        player.openInventory(gui);
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player clicker = (Player) event.getWhoClicked();
        
        if (!clicker.equals(player) || !event.getInventory().equals(gui) || !isActive) {
            return;
        }
        
        event.setCancelled(true);
        int slot = event.getSlot();
        
        if (slot == 45) { // Clear all enchantments
            clearAllEnchantments();
            return;
        }
        
        if (slot == 49) { // Apply changes
            applyChanges();
            return;
        }
        
        if (slot == 53) { // Back
            returnToParent();
            return;
        }
        
        // Handle enchantment clicks
        if (slot >= 9 && slot < 44) {
            int enchantIndex = slot - 9;
            if (enchantIndex < availableEnchantments.size()) {
                Enchantment enchantment = availableEnchantments.get(enchantIndex);
                handleEnchantmentClick(enchantment, event);
            }
        }
    }
    
    private void clearAllEnchantments() {
        Map<Enchantment, Integer> currentEnchants = new HashMap<>(targetItem.getEnchantments());
        for (Enchantment enchant : currentEnchants.keySet()) {
            targetItem.removeEnchantment(enchant);
        }
        player.sendMessage(ChatColor.YELLOW + "All enchantments cleared!");
        setupGUI(); // Refresh the GUI
    }
    
    private void handleEnchantmentClick(Enchantment enchantment, InventoryClickEvent event) {
        int currentLevel = targetItem.getEnchantmentLevel(enchantment);
        int maxLevel = enchantment.getMaxLevel();
        
        if (event.isShiftClick()) {
            // Remove enchantment
            if (currentLevel > 0) {
                targetItem.removeEnchantment(enchantment);
                player.sendMessage(ChatColor.YELLOW + "Removed " + formatEnchantmentName(enchantment.getKey().getKey()));
            }
        } else if (event.isLeftClick()) {
            // Increase level
            if (currentLevel < maxLevel) {
                // Remove conflicting enchantments before adding
                removeConflictingEnchantments(enchantment);
                targetItem.addUnsafeEnchantment(enchantment, currentLevel + 1);
                player.sendMessage(ChatColor.GREEN + "Increased " + formatEnchantmentName(enchantment.getKey().getKey()) + " to level " + (currentLevel + 1));
            }
        } else if (event.isRightClick()) {
            // Decrease level
            if (currentLevel > 1) {
                targetItem.addUnsafeEnchantment(enchantment, currentLevel - 1);
                player.sendMessage(ChatColor.YELLOW + "Decreased " + formatEnchantmentName(enchantment.getKey().getKey()) + " to level " + (currentLevel - 1));
            } else if (currentLevel == 1) {
                targetItem.removeEnchantment(enchantment);
                player.sendMessage(ChatColor.YELLOW + "Removed " + formatEnchantmentName(enchantment.getKey().getKey()));
            }
        }
        
        setupGUI(); // Refresh the GUI to show updated levels
    }
    
    private void applyChanges() {
        plugin.getLogger().info("[DEBUG] Applying enchantment changes for " + player.getName());
        parentGUI.updateItem(targetItem);
        player.sendMessage(ChatColor.GREEN + "Enchantments applied!");
        returnToParent();
    }
    
    private void returnToParent() {
        plugin.getLogger().info("[DEBUG] Returning to parent GUI for player " + player.getName());
        forceCleanup();
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            parentGUI.refreshAndReopen();
        }, 1L);
    }
    
    private void forceCleanup() {
        plugin.getLogger().info("[DEBUG] Force cleanup EnchantmentSelectorGUI for " + player.getName());
        isActive = false;
        activeGuis.remove(player.getUniqueId());
        InventoryClickEvent.getHandlerList().unregister(this);
        InventoryCloseEvent.getHandlerList().unregister(this);
        player.closeInventory();
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        Player closer = (Player) event.getPlayer();
        
        if (closer.equals(player) && event.getInventory().equals(gui)) {
            plugin.getLogger().info("[DEBUG] EnchantmentSelectorGUI inventory closed by " + player.getName() + ", Active: " + isActive);
            
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (isActive && activeGuis.containsKey(player.getUniqueId())) {
                    plugin.getLogger().info("[DEBUG] Final cleanup EnchantmentSelectorGUI for " + player.getName());
                    forceCleanup();
                    parentGUI.refreshAndReopen();
                }
            }, 3L);
        }
    }
}