package com.yourname.customkitduels.gui;

import com.yourname.customkitduels.CustomKitDuels;
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

public class CategorySelectorGUI implements Listener {
    
    private final CustomKitDuels plugin;
    private final Player player;
    private final KitEditorGUI parentGUI;
    private final int targetSlot;
    private final Inventory gui;
    private static final Map<UUID, CategorySelectorGUI> activeGuis = new HashMap<>();
    
    public CategorySelectorGUI(CustomKitDuels plugin, Player player, KitEditorGUI parentGUI, int targetSlot) {
        this.plugin = plugin;
        this.player = player;
        this.parentGUI = parentGUI;
        this.targetSlot = targetSlot;
        this.gui = Bukkit.createInventory(null, 27, ChatColor.DARK_GREEN + "Select Category");
        
        setupGUI();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    private void setupGUI() {
        // Weapons category
        ItemStack weapons = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta weaponsMeta = weapons.getItemMeta();
        weaponsMeta.setDisplayName(ChatColor.RED + "⚔ Weapons");
        weaponsMeta.setLore(Arrays.asList(ChatColor.GRAY + "Swords, axes, bows, and more"));
        weapons.setItemMeta(weaponsMeta);
        gui.setItem(10, weapons);
        
        // Armor category
        ItemStack armor = new ItemStack(Material.DIAMOND_CHESTPLATE);
        ItemMeta armorMeta = armor.getItemMeta();
        armorMeta.setDisplayName(ChatColor.BLUE + "🛡 Armor");
        armorMeta.setLore(Arrays.asList(ChatColor.GRAY + "Helmets, chestplates, leggings, boots"));
        armor.setItemMeta(armorMeta);
        gui.setItem(11, armor);
        
        // Blocks category
        ItemStack blocks = new ItemStack(Material.OBSIDIAN);
        ItemMeta blocksMeta = blocks.getItemMeta();
        blocksMeta.setDisplayName(ChatColor.DARK_GRAY + "🧱 Blocks");
        blocksMeta.setLore(Arrays.asList(ChatColor.GRAY + "Building and utility blocks"));
        blocks.setItemMeta(blocksMeta);
        gui.setItem(12, blocks);
        
        // Food category
        ItemStack food = new ItemStack(Material.GOLDEN_APPLE);
        ItemMeta foodMeta = food.getItemMeta();
        foodMeta.setDisplayName(ChatColor.GOLD + "🍖 Food");
        foodMeta.setLore(Arrays.asList(ChatColor.GRAY + "Food items and consumables"));
        food.setItemMeta(foodMeta);
        gui.setItem(13, food);
        
        // Potions category
        ItemStack potions = new ItemStack(Material.SPLASH_POTION);
        ItemMeta potionsMeta = potions.getItemMeta();
        potionsMeta.setDisplayName(ChatColor.LIGHT_PURPLE + "🧪 Potions");
        potionsMeta.setLore(Arrays.asList(ChatColor.GRAY + "Potions and brewing items"));
        potions.setItemMeta(potionsMeta);
        gui.setItem(14, potions);
        
        // Tools category
        ItemStack tools = new ItemStack(Material.DIAMOND_PICKAXE);
        ItemMeta toolsMeta = tools.getItemMeta();
        toolsMeta.setDisplayName(ChatColor.AQUA + "🧰 Tools");
        toolsMeta.setLore(Arrays.asList(ChatColor.GRAY + "Pickaxes, shovels, and other tools"));
        tools.setItemMeta(toolsMeta);
        gui.setItem(15, tools);
        
        // Utility category
        ItemStack utility = new ItemStack(Material.ENDER_PEARL);
        ItemMeta utilityMeta = utility.getItemMeta();
        utilityMeta.setDisplayName(ChatColor.GREEN + "🧨 Utility");
        utilityMeta.setLore(Arrays.asList(ChatColor.GRAY + "Ender pearls, flint and steel, etc."));
        utility.setItemMeta(utilityMeta);
        gui.setItem(16, utility);
        
        // Misc category
        ItemStack misc = new ItemStack(Material.BOOK);
        ItemMeta miscMeta = misc.getItemMeta();
        miscMeta.setDisplayName(ChatColor.YELLOW + "❓ Misc");
        miscMeta.setLore(Arrays.asList(ChatColor.GRAY + "Other miscellaneous items"));
        misc.setItemMeta(miscMeta);
        gui.setItem(19, misc);
        
        // Clear slot option
        ItemStack clear = new ItemStack(Material.BARRIER);
        ItemMeta clearMeta = clear.getItemMeta();
        clearMeta.setDisplayName(ChatColor.RED + "Clear Slot");
        clearMeta.setLore(Arrays.asList(ChatColor.GRAY + "Remove item from this slot"));
        clear.setItemMeta(clearMeta);
        gui.setItem(21, clear);
        
        // Back button
        ItemStack back = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.setDisplayName(ChatColor.RED + "Back");
        backMeta.setLore(Arrays.asList(ChatColor.GRAY + "Return to kit editor"));
        back.setItemMeta(backMeta);
        gui.setItem(22, back);
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
        
        switch (slot) {
            case 10: // Weapons
                openItemSelector(ItemCategory.WEAPONS);
                break;
            case 11: // Armor
                openItemSelector(ItemCategory.ARMOR);
                break;
            case 12: // Blocks
                openItemSelector(ItemCategory.BLOCKS);
                break;
            case 13: // Food
                openItemSelector(ItemCategory.FOOD);
                break;
            case 14: // Potions
                openItemSelector(ItemCategory.POTIONS);
                break;
            case 15: // Tools
                openItemSelector(ItemCategory.TOOLS);
                break;
            case 16: // Utility
                openItemSelector(ItemCategory.UTILITY);
                break;
            case 19: // Misc
                openItemSelector(ItemCategory.MISC);
                break;
            case 21: // Clear slot
                parentGUI.clearSlot(targetSlot);
                returnToParent();
                break;
            case 22: // Back
                returnToParent();
                break;
        }
    }
    
    private void openItemSelector(ItemCategory category) {
        cleanup();
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            new ItemSelectorGUI(plugin, player, parentGUI, targetSlot, category).open();
        }, 1L);
    }
    
    private void returnToParent() {
        cleanup();
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            parentGUI.refreshAndReopen();
        }, 1L);
    }
    
    private void cleanup() {
        activeGuis.remove(player.getUniqueId());
        InventoryClickEvent.getHandlerList().unregister(this);
        InventoryCloseEvent.getHandlerList().unregister(this);
        player.closeInventory();
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        Player closer = (Player) event.getPlayer();
        
        if (closer.equals(player) && event.getInventory().equals(gui)) {
            // Delay cleanup to allow for navigation
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (activeGuis.containsKey(player.getUniqueId())) {
                    cleanup();
                    parentGUI.refreshAndReopen();
                }
            }, 2L);
        }
    }
    
    public enum ItemCategory {
        WEAPONS, ARMOR, BLOCKS, FOOD, POTIONS, TOOLS, UTILITY, MISC
    }
}