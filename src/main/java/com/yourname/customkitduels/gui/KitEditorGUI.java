package com.yourname.customkitduels.gui;

import com.yourname.customkitduels.CustomKitDuels;
import com.yourname.customkitduels.data.Kit;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class KitEditorGUI implements Listener {
    
    private final CustomKitDuels plugin;
    private final Player player;
    private final String kitName;
    private final Inventory gui;
    private final ItemStack[] kitContents;
    private final ItemStack[] kitArmor;
    private ItemStack offhandItem;
    private static final Map<UUID, KitEditorGUI> activeGuis = new HashMap<>();
    
    public KitEditorGUI(CustomKitDuels plugin, Player player, String kitName) {
        this.plugin = plugin;
        this.player = player;
        this.kitName = kitName;
        this.gui = Bukkit.createInventory(null, 54, ChatColor.DARK_BLUE + "Editing Kit " + kitName);
        this.kitContents = new ItemStack[36];
        this.kitArmor = new ItemStack[4];
        this.offhandItem = null;
        
        // Load existing kit if editing
        Kit existingKit = plugin.getKitManager().getKit(player.getUniqueId(), kitName);
        if (existingKit != null) {
            System.arraycopy(existingKit.getContents(), 0, kitContents, 0, Math.min(existingKit.getContents().length, 36));
            System.arraycopy(existingKit.getArmor(), 0, kitArmor, 0, 4);
            // Load offhand if available (stored in slot 36 of contents array)
            if (existingKit.getContents().length > 36 && existingKit.getContents()[36] != null) {
                this.offhandItem = existingKit.getContents()[36];
            }
        }
        
        setupGUI();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    private void setupGUI() {
        // Clear GUI first
        gui.clear();
        
        // Fill main inventory slots (0-35) with colored glass panes or items
        for (int i = 0; i < 36; i++) {
            if (kitContents[i] != null) {
                gui.setItem(i, kitContents[i].clone());
            } else {
                ItemStack glassPane = new ItemStack(Material.PURPLE_STAINED_GLASS_PANE);
                ItemMeta meta = glassPane.getItemMeta();
                meta.setDisplayName(ChatColor.AQUA + "Slot #" + (i + 1));
                meta.setLore(Arrays.asList(ChatColor.GRAY + "Click to add an item"));
                glassPane.setItemMeta(meta);
                gui.setItem(i, glassPane);
            }
        }
        
        // Fill armor slots (36-39) with colored glass panes or items
        String[] armorSlots = {"Boots", "Leggings", "Chestplate", "Helmet"};
        for (int i = 0; i < 4; i++) {
            if (kitArmor[i] != null) {
                gui.setItem(36 + i, kitArmor[i].clone());
            } else {
                ItemStack glassPane = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
                ItemMeta meta = glassPane.getItemMeta();
                meta.setDisplayName(ChatColor.AQUA + armorSlots[i] + " Slot");
                meta.setLore(Arrays.asList(ChatColor.GRAY + "Click to add armor"));
                glassPane.setItemMeta(meta);
                gui.setItem(36 + i, glassPane);
            }
        }
        
        // Add offhand slot (slot 40)
        if (offhandItem != null) {
            gui.setItem(40, offhandItem.clone());
        } else {
            ItemStack offhandPane = new ItemStack(Material.ORANGE_STAINED_GLASS_PANE);
            ItemMeta offhandMeta = offhandPane.getItemMeta();
            offhandMeta.setDisplayName(ChatColor.AQUA + "Offhand Slot");
            offhandMeta.setLore(Arrays.asList(ChatColor.GRAY + "Click to add offhand item"));
            offhandPane.setItemMeta(offhandMeta);
            gui.setItem(40, offhandPane);
        }
        
        // Add control buttons
        ItemStack saveButton = new ItemStack(Material.EMERALD);
        ItemMeta saveMeta = saveButton.getItemMeta();
        saveMeta.setDisplayName(ChatColor.GREEN + "Save Kit");
        saveMeta.setLore(Arrays.asList(ChatColor.GRAY + "Click to save this kit"));
        saveButton.setItemMeta(saveMeta);
        gui.setItem(45, saveButton);
        
        ItemStack cancelButton = new ItemStack(Material.REDSTONE);
        ItemMeta cancelMeta = cancelButton.getItemMeta();
        cancelMeta.setDisplayName(ChatColor.RED + "Cancel");
        cancelMeta.setLore(Arrays.asList(ChatColor.GRAY + "Click to cancel"));
        cancelButton.setItemMeta(cancelMeta);
        gui.setItem(53, cancelButton);
        
        // Clear button
        ItemStack clearButton = new ItemStack(Material.BARRIER);
        ItemMeta clearMeta = clearButton.getItemMeta();
        clearMeta.setDisplayName(ChatColor.YELLOW + "Clear All");
        clearMeta.setLore(Arrays.asList(ChatColor.GRAY + "Click to clear all slots"));
        clearButton.setItemMeta(clearMeta);
        gui.setItem(49, clearButton);
    }
    
    public void open() {
        activeGuis.put(player.getUniqueId(), this);
        player.openInventory(gui);
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player clicker = (Player) event.getWhoClicked();
        
        if (!clicker.equals(player) || !event.getInventory().equals(gui)) {
            return;
        }
        
        event.setCancelled(true);
        int slot = event.getSlot();
        
        // Handle control buttons
        if (slot == 45) { // Save button
            saveKit();
            return;
        }
        
        if (slot == 53) { // Cancel button
            cleanup();
            player.closeInventory();
            return;
        }
        
        if (slot == 49) { // Clear button
            clearAllSlots();
            return;
        }
        
        // Handle slot selection (0-40: main inventory, armor, and offhand)
        if (slot <= 40) {
            // Open category selector for this slot
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                new CategorySelectorGUI(plugin, player, this, slot).open();
            }, 1L);
        }
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        Player closer = (Player) event.getPlayer();
        
        if (closer.equals(player) && event.getInventory().equals(gui)) {
            // Only cleanup if this is a final close (not a temporary one for navigation)
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (!player.getOpenInventory().getTopInventory().equals(gui)) {
                    cleanup();
                }
            }, 2L);
        }
    }
    
    private void cleanup() {
        activeGuis.remove(player.getUniqueId());
        InventoryClickEvent.getHandlerList().unregister(this);
        InventoryCloseEvent.getHandlerList().unregister(this);
    }
    
    public void setSlotItem(int slot, ItemStack item) {
        if (slot < 36) {
            kitContents[slot] = item;
        } else if (slot < 40) {
            kitArmor[slot - 36] = item;
        } else if (slot == 40) {
            offhandItem = item;
        }
        setupGUI(); // Refresh the entire GUI
    }
    
    public void clearSlot(int slot) {
        setSlotItem(slot, null);
    }
    
    private void clearAllSlots() {
        for (int i = 0; i < 36; i++) {
            kitContents[i] = null;
        }
        for (int i = 0; i < 4; i++) {
            kitArmor[i] = null;
        }
        offhandItem = null;
        setupGUI(); // Refresh the entire GUI
        player.sendMessage(ChatColor.YELLOW + "All slots cleared!");
    }
    
    private void saveKit() {
        // Create extended contents array to include offhand
        ItemStack[] extendedContents = new ItemStack[37];
        System.arraycopy(kitContents, 0, extendedContents, 0, 36);
        extendedContents[36] = offhandItem;
        
        Kit kit = new Kit(kitName, kitName, extendedContents, kitArmor.clone());
        plugin.getKitManager().saveKit(player.getUniqueId(), kit);
        
        player.sendMessage(ChatColor.GREEN + "Kit '" + kitName + "' saved successfully!");
        cleanup();
        player.closeInventory();
    }
    
    public Inventory getGui() {
        return gui;
    }
    
    public void refreshAndReopen() {
        setupGUI();
        if (!player.getOpenInventory().getTopInventory().equals(gui)) {
            player.openInventory(gui);
        }
    }
}