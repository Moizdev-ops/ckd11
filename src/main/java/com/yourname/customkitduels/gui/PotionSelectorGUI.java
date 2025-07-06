package com.yourname.customkitduels.gui;

import com.yourname.customkitduels.CustomKitDuels;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

import java.util.*;

public class PotionSelectorGUI implements Listener {
    
    private final CustomKitDuels plugin;
    private final Player player;
    private final ItemModificationGUI parentGUI;
    private ItemStack targetItem;
    private final Inventory gui;
    private final List<PotionType> availablePotions;
    private static final Map<UUID, PotionSelectorGUI> activeGuis = new HashMap<>();
    private boolean isActive = true;
    
    public PotionSelectorGUI(CustomKitDuels plugin, Player player, ItemModificationGUI parentGUI, ItemStack targetItem) {
        this.plugin = plugin;
        this.player = player;
        this.parentGUI = parentGUI;
        this.targetItem = targetItem.clone();
        this.availablePotions = getAvailablePotionTypes();
        this.gui = Bukkit.createInventory(null, 54, ChatColor.DARK_PURPLE + "Select Potion Type");
        
        plugin.getLogger().info("[DEBUG] Creating PotionSelectorGUI for player " + player.getName());
        
        setupGUI();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    private List<PotionType> getAvailablePotionTypes() {
        List<PotionType> potions = new ArrayList<>();
        
        // Add common potion types
        potions.addAll(Arrays.asList(
            PotionType.WATER,
            PotionType.MUNDANE,
            PotionType.THICK,
            PotionType.AWKWARD,
            PotionType.NIGHT_VISION,
            PotionType.INVISIBILITY,
            PotionType.JUMP,
            PotionType.FIRE_RESISTANCE,
            PotionType.SPEED,
            PotionType.SLOWNESS,
            PotionType.WATER_BREATHING,
            PotionType.INSTANT_HEAL,
            PotionType.INSTANT_DAMAGE,
            PotionType.POISON,
            PotionType.REGEN,
            PotionType.STRENGTH,
            PotionType.WEAKNESS,
            PotionType.LUCK,
            PotionType.TURTLE_MASTER,
            PotionType.SLOW_FALLING
        ));
        
        return potions;
    }
    
    private void setupGUI() {
        gui.clear();
        
        // Display current item
        gui.setItem(4, targetItem.clone());
        
        // Add potion options
        int slot = 9;
        for (PotionType potionType : availablePotions) {
            if (slot >= 44) break; // Don't overflow into control area
            
            ItemStack potionItem = new ItemStack(targetItem.getType());
            PotionMeta meta = (PotionMeta) potionItem.getItemMeta();
            
            if (meta != null) {
                // Set the potion data
                PotionData potionData = new PotionData(potionType, false, false);
                meta.setBasePotionData(potionData);
                
                String potionName = formatPotionName(potionType.name());
                meta.setDisplayName(ChatColor.LIGHT_PURPLE + potionName);
                
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.GRAY + "Click to select this potion type");
                lore.add(ChatColor.YELLOW + "Extended: Right-click");
                lore.add(ChatColor.YELLOW + "Upgraded: Shift-click");
                meta.setLore(lore);
                
                potionItem.setItemMeta(meta);
            }
            
            gui.setItem(slot, potionItem);
            slot++;
        }
        
        // Apply changes button
        ItemStack applyButton = new ItemStack(Material.EMERALD);
        ItemMeta applyMeta = applyButton.getItemMeta();
        applyMeta.setDisplayName(ChatColor.GREEN + "Apply Changes");
        applyMeta.setLore(Arrays.asList(ChatColor.GRAY + "Click to apply potion type"));
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
    
    private String formatPotionName(String potionName) {
        String[] words = potionName.toLowerCase().split("_");
        StringBuilder formatted = new StringBuilder();
        
        for (String word : words) {
            if (formatted.length() > 0) {
                formatted.append(" ");
            }
            formatted.append(word.substring(0, 1).toUpperCase()).append(word.substring(1));
        }
        
        return formatted.toString();
    }
    
    public void open() {
        plugin.getLogger().info("[DEBUG] Opening PotionSelectorGUI for " + player.getName());
        
        PotionSelectorGUI existing = activeGuis.get(player.getUniqueId());
        if (existing != null && existing != this) {
            plugin.getLogger().info("[DEBUG] Cleaning up existing PotionSelectorGUI for " + player.getName());
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
        
        if (!clicker.getUniqueId().equals(player.getUniqueId()) || !event.getInventory().equals(gui) || !isActive) {
            return;
        }
        
        event.setCancelled(true);
        int slot = event.getSlot();
        
        if (slot == 49) { // Apply changes
            applyChanges();
            return;
        }
        
        if (slot == 53) { // Back
            returnToParent();
            return;
        }
        
        // Handle potion selection
        if (slot >= 9 && slot < 44) {
            int potionIndex = slot - 9;
            if (potionIndex < availablePotions.size()) {
                PotionType potionType = availablePotions.get(potionIndex);
                handlePotionClick(potionType, event);
            }
        }
    }
    
    private void handlePotionClick(PotionType potionType, InventoryClickEvent event) {
        if (!(targetItem.getItemMeta() instanceof PotionMeta)) return;
        
        PotionMeta meta = (PotionMeta) targetItem.getItemMeta();
        
        boolean extended = event.isRightClick();
        boolean upgraded = event.isShiftClick();
        
        // Some potions can't be extended or upgraded
        if (extended && !potionType.isExtendable()) {
            extended = false;
        }
        if (upgraded && !potionType.isUpgradeable()) {
            upgraded = false;
        }
        
        // Can't have both extended and upgraded
        if (extended && upgraded) {
            upgraded = false;
        }
        
        PotionData potionData = new PotionData(potionType, extended, upgraded);
        meta.setBasePotionData(potionData);
        targetItem.setItemMeta(meta);
        
        String potionName = formatPotionName(potionType.name());
        String modifiers = "";
        if (extended) modifiers += " (Extended)";
        if (upgraded) modifiers += " (Upgraded)";
        
        player.sendMessage(ChatColor.GREEN + "Selected " + potionName + modifiers + "!");
        
        // Update the display item
        gui.setItem(4, targetItem.clone());
    }
    
    private void applyChanges() {
        plugin.getLogger().info("[DEBUG] Applying potion changes for " + player.getName());
        parentGUI.updateItem(targetItem);
        player.sendMessage(ChatColor.GREEN + "Potion type applied!");
        returnToParent();
    }
    
    private void returnToParent() {
        plugin.getLogger().info("[DEBUG] Returning to parent GUI for player " + player.getName());
        isActive = false;
        forceCleanup();
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            parentGUI.refreshAndReopen();
        }, 2L);
    }
    
    private void forceCleanup() {
        plugin.getLogger().info("[DEBUG] Force cleanup PotionSelectorGUI for " + player.getName());
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
        
        if (closer.getUniqueId().equals(player.getUniqueId()) && event.getInventory().equals(gui)) {
            plugin.getLogger().info("[DEBUG] PotionSelectorGUI inventory closed by " + player.getName() + ", Active: " + isActive);
            
            if (isActive) {
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    if (isActive && activeGuis.containsKey(player.getUniqueId())) {
                        plugin.getLogger().info("[DEBUG] Final cleanup PotionSelectorGUI for " + player.getName());
                        forceCleanup();
                        parentGUI.refreshAndReopen();
                    }
                }, 5L);
            }
        }
    }
}