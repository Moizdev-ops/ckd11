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

import java.util.*;

public class ArmorSelectorGUI implements Listener {
    
    private final CustomKitDuels plugin;
    private final Player player;
    private final KitEditorGUI parentGUI;
    private final int targetSlot;
    private final Inventory gui;
    private final List<ItemStack> armorItems;
    private static final Map<UUID, ArmorSelectorGUI> activeGuis = new HashMap<>();
    private boolean isActive = true;
    
    public ArmorSelectorGUI(CustomKitDuels plugin, Player player, KitEditorGUI parentGUI, int targetSlot) {
        this.plugin = plugin;
        this.player = player;
        this.parentGUI = parentGUI;
        this.targetSlot = targetSlot;
        this.armorItems = getArmorForSlot(targetSlot);
        
        String armorType = getArmorTypeName(targetSlot);
        this.gui = Bukkit.createInventory(null, 54, ChatColor.DARK_GREEN + armorType + " Selection");
        
        plugin.getLogger().info("[DEBUG] Creating ArmorSelectorGUI for player " + player.getName() + " slot " + targetSlot + " type " + armorType);
        
        setupGUI();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    private String getArmorTypeName(int slot) {
        switch (slot) {
            case 36: return "Boots";
            case 37: return "Leggings";
            case 38: return "Chestplate";
            case 39: return "Helmet";
            default: return "Armor";
        }
    }
    
    private List<ItemStack> getArmorForSlot(int slot) {
        List<ItemStack> items = new ArrayList<>();
        
        switch (slot) {
            case 36: // Boots
                items.addAll(createBasicItems(Arrays.asList(
                    Material.LEATHER_BOOTS, Material.CHAINMAIL_BOOTS, Material.IRON_BOOTS,
                    Material.GOLDEN_BOOTS, Material.DIAMOND_BOOTS, Material.NETHERITE_BOOTS
                )));
                break;
            case 37: // Leggings
                items.addAll(createBasicItems(Arrays.asList(
                    Material.LEATHER_LEGGINGS, Material.CHAINMAIL_LEGGINGS, Material.IRON_LEGGINGS,
                    Material.GOLDEN_LEGGINGS, Material.DIAMOND_LEGGINGS, Material.NETHERITE_LEGGINGS
                )));
                break;
            case 38: // Chestplate
                items.addAll(createBasicItems(Arrays.asList(
                    Material.LEATHER_CHESTPLATE, Material.CHAINMAIL_CHESTPLATE, Material.IRON_CHESTPLATE,
                    Material.GOLDEN_CHESTPLATE, Material.DIAMOND_CHESTPLATE, Material.NETHERITE_CHESTPLATE,
                    Material.ELYTRA
                )));
                break;
            case 39: // Helmet
                items.addAll(createBasicItems(Arrays.asList(
                    Material.LEATHER_HELMET, Material.CHAINMAIL_HELMET, Material.IRON_HELMET,
                    Material.GOLDEN_HELMET, Material.DIAMOND_HELMET, Material.NETHERITE_HELMET,
                    Material.TURTLE_HELMET
                )));
                break;
        }
        
        return items;
    }
    
    private List<ItemStack> createBasicItems(List<Material> materials) {
        List<ItemStack> items = new ArrayList<>();
        for (Material material : materials) {
            items.add(new ItemStack(material));
        }
        return items;
    }
    
    private void setupGUI() {
        gui.clear();
        
        // Add armor items
        for (int i = 0; i < Math.min(armorItems.size(), 45); i++) {
            ItemStack item = armorItems.get(i).clone();
            ItemMeta meta = item.getItemMeta();
            if (meta.getDisplayName() == null || meta.getDisplayName().isEmpty()) {
                meta.setDisplayName(ChatColor.WHITE + formatMaterialName(item.getType().name()));
            }
            item.setItemMeta(meta);
            gui.setItem(i, item);
        }
        
        // Clear slot option
        ItemStack clear = new ItemStack(Material.BARRIER);
        ItemMeta clearMeta = clear.getItemMeta();
        clearMeta.setDisplayName(ChatColor.RED + "Clear Slot");
        clearMeta.setLore(Arrays.asList(ChatColor.GRAY + "Remove armor from this slot"));
        clear.setItemMeta(clearMeta);
        gui.setItem(45, clear);
        
        // Back button
        ItemStack back = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.setDisplayName(ChatColor.RED + "Back");
        backMeta.setLore(Arrays.asList(ChatColor.GRAY + "Return to kit editor"));
        back.setItemMeta(backMeta);
        gui.setItem(53, back);
    }
    
    private String formatMaterialName(String materialName) {
        String[] words = materialName.toLowerCase().split("_");
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
        plugin.getLogger().info("[DEBUG] Opening ArmorSelectorGUI for " + player.getName());
        
        ArmorSelectorGUI existing = activeGuis.get(player.getUniqueId());
        if (existing != null && existing != this) {
            plugin.getLogger().info("[DEBUG] Cleaning up existing ArmorSelectorGUI for " + player.getName());
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
        
        plugin.getLogger().info("[DEBUG] ArmorSelectorGUI click event - Player: " + player.getName() + ", Slot: " + slot);
        
        if (slot == 45) { // Clear slot
            plugin.getLogger().info("[DEBUG] Clear armor slot clicked for slot " + targetSlot);
            parentGUI.clearSlot(targetSlot);
            returnToParent();
        } else if (slot == 53) { // Back button
            plugin.getLogger().info("[DEBUG] Back button clicked");
            returnToParent();
        } else if (slot < 45) { // Armor selection
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem != null && clickedItem.getType() != Material.AIR) {
                plugin.getLogger().info("[DEBUG] Armor selected: " + clickedItem.getType() + " for slot " + targetSlot);
                parentGUI.setSlotItem(targetSlot, clickedItem.clone());
                player.sendMessage(ChatColor.GREEN + "Armor added to " + getArmorTypeName(targetSlot).toLowerCase() + " slot!");
                returnToParent();
            }
        }
    }
    
    private void returnToParent() {
        plugin.getLogger().info("[DEBUG] Returning to parent GUI for player " + player.getName());
        forceCleanup();
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            parentGUI.refreshAndReopen();
        }, 1L);
    }
    
    private void forceCleanup() {
        plugin.getLogger().info("[DEBUG] Force cleanup ArmorSelectorGUI for " + player.getName());
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
            plugin.getLogger().info("[DEBUG] ArmorSelectorGUI inventory closed by " + player.getName() + ", Active: " + isActive);
            
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (isActive && activeGuis.containsKey(player.getUniqueId())) {
                    plugin.getLogger().info("[DEBUG] Final cleanup ArmorSelectorGUI for " + player.getName());
                    forceCleanup();
                    parentGUI.refreshAndReopen();
                }
            }, 3L);
        }
    }
}