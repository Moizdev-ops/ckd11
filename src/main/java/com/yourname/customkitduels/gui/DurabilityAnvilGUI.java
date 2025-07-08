package com.yourname.customkitduels.gui;

import com.yourname.customkitduels.CustomKitDuels;
import com.yourname.customkitduels.utils.FontUtils;
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

import java.util.*;

public class DurabilityAnvilGUI implements Listener {
    
    private final CustomKitDuels plugin;
    private final Player player;
    private final KitEditorGUI parentGUI;
    private final int targetSlot;
    private ItemStack targetItem;
    private final Inventory gui;
    private static final Map<UUID, DurabilityAnvilGUI> activeGuis = new HashMap<>();
    private boolean isActive = true;
    private int selectedDurability = -1;
    
    public DurabilityAnvilGUI(CustomKitDuels plugin, Player player, KitEditorGUI parentGUI, int targetSlot, ItemStack targetItem) {
        this.plugin = plugin;
        this.player = player;
        this.parentGUI = parentGUI;
        this.targetSlot = targetSlot;
        this.targetItem = targetItem.clone();
        this.gui = Bukkit.createInventory(null, 27, ChatColor.DARK_GRAY + FontUtils.toSmallCaps("durability editor"));
        
        plugin.getLogger().info("[DEBUG] Creating DurabilityAnvilGUI for player " + player.getName());
        
        setupGUI();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    private void setupGUI() {
        gui.clear();
        
        // Input slot - show the item being modified
        gui.setItem(10, targetItem.clone());
        
        // Input slot for durability number (text box simulation)
        ItemStack textBox = new ItemStack(Material.PAPER);
        ItemMeta textMeta = textBox.getItemMeta();
        textMeta.setDisplayName(ChatColor.YELLOW + FontUtils.toSmallCaps("enter durability"));
        textMeta.setLore(Arrays.asList(
            ChatColor.GRAY + FontUtils.toSmallCaps("current durability: ") + (targetItem.getType().getMaxDurability() - targetItem.getDurability()),
            ChatColor.GRAY + FontUtils.toSmallCaps("max durability: ") + targetItem.getType().getMaxDurability(),
            ChatColor.YELLOW + FontUtils.toSmallCaps("click numbers below to set durability")
        ));
        textBox.setItemMeta(textMeta);
        gui.setItem(13, textBox);
        
        // Number buttons (0-9)
        for (int i = 0; i <= 9; i++) {
            ItemStack numberItem = new ItemStack(Material.STONE_BUTTON);
            ItemMeta numberMeta = numberItem.getItemMeta();
            numberMeta.setDisplayName(ChatColor.WHITE + String.valueOf(i));
            numberMeta.setLore(Arrays.asList(ChatColor.GRAY + FontUtils.toSmallCaps("click to add ") + i));
            numberItem.setItemMeta(numberMeta);
            
            // Place numbers in bottom row
            int slot = 18 + (i % 5); // First 5 numbers in slots 18-22
            if (i >= 5) {
                slot = 23 + (i - 5); // Last 5 numbers in slots 23-27 (but 27 doesn't exist, so 23-26)
            }
            if (i == 9) slot = 22; // Put 9 in slot 22
            
            gui.setItem(slot, numberItem);
        }
        
        // Clear button
        ItemStack clearButton = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta clearMeta = clearButton.getItemMeta();
        clearMeta.setDisplayName(ChatColor.RED + FontUtils.toSmallCaps("clear"));
        clearMeta.setLore(Arrays.asList(ChatColor.GRAY + FontUtils.toSmallCaps("clear entered number")));
        clearButton.setItemMeta(clearMeta);
        gui.setItem(19, clearButton);
        
        // Output slot - show result
        updateOutputSlot();
        
        // Apply button
        ItemStack applyButton = new ItemStack(Material.EMERALD);
        ItemMeta applyMeta = applyButton.getItemMeta();
        applyMeta.setDisplayName(ChatColor.GREEN + FontUtils.toSmallCaps("apply durability"));
        applyMeta.setLore(Arrays.asList(
            ChatColor.GRAY + FontUtils.toSmallCaps("click to apply the new durability"),
            selectedDurability >= 0 ? ChatColor.YELLOW + FontUtils.toSmallCaps("new durability: ") + selectedDurability : ChatColor.RED + FontUtils.toSmallCaps("no durability entered")
        ));
        applyButton.setItemMeta(applyMeta);
        gui.setItem(25, applyButton);
        
        // Back button
        ItemStack backButton = new ItemStack(Material.BARRIER);
        ItemMeta backMeta = backButton.getItemMeta();
        backMeta.setDisplayName(ChatColor.RED + FontUtils.toSmallCaps("back"));
        backMeta.setLore(Arrays.asList(ChatColor.GRAY + FontUtils.toSmallCaps("return to item editor")));
        backButton.setItemMeta(backMeta);
        gui.setItem(26, backButton);
    }
    
    private void updateOutputSlot() {
        if (selectedDurability >= 0) {
            ItemStack outputItem = targetItem.clone();
            
            // Calculate damage value (durability is inverted)
            int maxDurability = targetItem.getType().getMaxDurability();
            int damageValue = Math.max(0, maxDurability - selectedDurability);
            
            outputItem.setDurability((short) damageValue);
            
            ItemMeta outputMeta = outputItem.getItemMeta();
            if (outputMeta != null) {
                List<String> lore = outputMeta.getLore();
                if (lore == null) lore = new ArrayList<>();
                lore.add(ChatColor.GREEN + FontUtils.toSmallCaps("durability: ") + selectedDurability + "/" + maxDurability);
                outputMeta.setLore(lore);
                outputItem.setItemMeta(outputMeta);
            }
            
            gui.setItem(16, outputItem);
        } else {
            // Show placeholder
            ItemStack placeholder = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
            ItemMeta placeholderMeta = placeholder.getItemMeta();
            placeholderMeta.setDisplayName(ChatColor.GRAY + FontUtils.toSmallCaps("result"));
            placeholderMeta.setLore(Arrays.asList(ChatColor.GRAY + FontUtils.toSmallCaps("enter durability to see result")));
            placeholder.setItemMeta(placeholderMeta);
            gui.setItem(16, placeholder);
        }
        
        // Update text box
        ItemStack textBox = new ItemStack(Material.PAPER);
        ItemMeta textMeta = textBox.getItemMeta();
        textMeta.setDisplayName(ChatColor.YELLOW + FontUtils.toSmallCaps("enter durability"));
        textMeta.setLore(Arrays.asList(
            ChatColor.GRAY + FontUtils.toSmallCaps("current durability: ") + (targetItem.getType().getMaxDurability() - targetItem.getDurability()),
            ChatColor.GRAY + FontUtils.toSmallCaps("max durability: ") + targetItem.getType().getMaxDurability(),
            selectedDurability >= 0 ? ChatColor.GREEN + FontUtils.toSmallCaps("entered: ") + selectedDurability : ChatColor.YELLOW + FontUtils.toSmallCaps("click numbers below to set durability")
        ));
        textBox.setItemMeta(textMeta);
        gui.setItem(13, textBox);
    }
    
    public void open() {
        plugin.getLogger().info("[DEBUG] Opening DurabilityAnvilGUI for " + player.getName());
        
        DurabilityAnvilGUI existing = activeGuis.get(player.getUniqueId());
        if (existing != null && existing != this) {
            plugin.getLogger().info("[DEBUG] Cleaning up existing DurabilityAnvilGUI for " + player.getName());
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
        
        plugin.getLogger().info("[DEBUG] DurabilityAnvilGUI click event - Player: " + player.getName() + ", Slot: " + slot);
        
        // Handle number buttons (18-26, excluding some slots)
        if ((slot >= 18 && slot <= 22) || (slot >= 23 && slot <= 26)) {
            if (slot == 19) { // Clear button
                selectedDurability = -1;
                updateOutputSlot();
                return;
            }
            
            if (slot == 25) { // Apply button
                applyDurability();
                return;
            }
            
            if (slot == 26) { // Back button
                returnToParent();
                return;
            }
            
            // Number buttons
            int number = getNumberFromSlot(slot);
            if (number >= 0) {
                addNumberToDurability(number);
            }
        }
    }
    
    private int getNumberFromSlot(int slot) {
        // Map slots to numbers
        switch (slot) {
            case 18: return 0;
            case 20: return 1;
            case 21: return 2;
            case 22: return 9; // Special case for 9
            case 23: return 5;
            case 24: return 6;
            default:
                // Calculate for remaining slots
                if (slot >= 18 && slot <= 22) {
                    return slot - 18;
                } else if (slot >= 23 && slot <= 26) {
                    return slot - 18;
                }
                return -1;
        }
    }
    
    private void addNumberToDurability(int number) {
        if (selectedDurability < 0) {
            selectedDurability = number;
        } else {
            // Append digit
            int newDurability = selectedDurability * 10 + number;
            int maxDurability = targetItem.getType().getMaxDurability();
            
            if (newDurability <= maxDurability) {
                selectedDurability = newDurability;
            } else {
                player.sendMessage(ChatColor.RED + FontUtils.toSmallCaps("durability cannot exceed ") + maxDurability + "!");
                return;
            }
        }
        
        updateOutputSlot();
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
    }
    
    private void applyDurability() {
        if (selectedDurability < 0) {
            player.sendMessage(ChatColor.RED + FontUtils.toSmallCaps("please enter a durability value first!"));
            return;
        }
        
        int maxDurability = targetItem.getType().getMaxDurability();
        if (selectedDurability > maxDurability) {
            player.sendMessage(ChatColor.RED + FontUtils.toSmallCaps("durability cannot exceed ") + maxDurability + "!");
            return;
        }
        
        // Apply durability (durability is inverted - damage = max - current)
        int damageValue = Math.max(0, maxDurability - selectedDurability);
        targetItem.setDurability((short) damageValue);
        
        parentGUI.setSlotItem(targetSlot, targetItem);
        player.sendMessage(ChatColor.GREEN + FontUtils.toSmallCaps("durability set to ") + selectedDurability + "/" + maxDurability + "!");
        
        returnToParent();
    }
    
    private void returnToParent() {
        plugin.getLogger().info("[DEBUG] Returning to parent GUI for player " + player.getName());
        forceCleanup();
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            new EnhancedItemModificationGUI(plugin, player, parentGUI, targetSlot, targetItem).open();
        }, 1L);
    }
    
    private void forceCleanup() {
        plugin.getLogger().info("[DEBUG] Force cleanup DurabilityAnvilGUI for " + player.getName());
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
            plugin.getLogger().info("[DEBUG] DurabilityAnvilGUI inventory closed by " + player.getName() + ", Active: " + isActive);
            
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (isActive && activeGuis.containsKey(player.getUniqueId())) {
                    plugin.getLogger().info("[DEBUG] Final cleanup DurabilityAnvilGUI for " + player.getName());
                    forceCleanup();
                    new EnhancedItemModificationGUI(plugin, player, parentGUI, targetSlot, targetItem).open();
                }
            }, 3L);
        }
    }
}