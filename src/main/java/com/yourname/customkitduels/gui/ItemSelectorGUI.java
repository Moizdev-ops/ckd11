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

import java.util.*;

public class ItemSelectorGUI implements Listener {
    
    private final CustomKitDuels plugin;
    private final Player player;
    private final KitEditorGUI parentGUI;
    private final int targetSlot;
    private final CategorySelectorGUI.ItemCategory category;
    private final Inventory gui;
    private final List<Material> categoryItems;
    private int currentPage = 0;
    private static final Map<UUID, ItemSelectorGUI> activeGuis = new HashMap<>();
    
    public ItemSelectorGUI(CustomKitDuels plugin, Player player, KitEditorGUI parentGUI, int targetSlot, CategorySelectorGUI.ItemCategory category) {
        this.plugin = plugin;
        this.player = player;
        this.parentGUI = parentGUI;
        this.targetSlot = targetSlot;
        this.category = category;
        this.categoryItems = getItemsForCategory(category);
        this.gui = Bukkit.createInventory(null, 54, ChatColor.DARK_PURPLE + getCategoryName(category) + " Items");
        
        setupGUI();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    private List<Material> getItemsForCategory(CategorySelectorGUI.ItemCategory category) {
        List<Material> items = new ArrayList<>();
        
        switch (category) {
            case WEAPONS:
                items.addAll(Arrays.asList(
                    Material.WOODEN_SWORD, Material.STONE_SWORD, Material.IRON_SWORD, Material.GOLDEN_SWORD, Material.DIAMOND_SWORD, Material.NETHERITE_SWORD,
                    Material.WOODEN_AXE, Material.STONE_AXE, Material.IRON_AXE, Material.GOLDEN_AXE, Material.DIAMOND_AXE, Material.NETHERITE_AXE,
                    Material.BOW, Material.CROSSBOW, Material.TRIDENT, Material.MACE
                ));
                break;
            case ARMOR:
                items.addAll(Arrays.asList(
                    Material.LEATHER_HELMET, Material.LEATHER_CHESTPLATE, Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS,
                    Material.CHAINMAIL_HELMET, Material.CHAINMAIL_CHESTPLATE, Material.CHAINMAIL_LEGGINGS, Material.CHAINMAIL_BOOTS,
                    Material.IRON_HELMET, Material.IRON_CHESTPLATE, Material.IRON_LEGGINGS, Material.IRON_BOOTS,
                    Material.GOLDEN_HELMET, Material.GOLDEN_CHESTPLATE, Material.GOLDEN_LEGGINGS, Material.GOLDEN_BOOTS,
                    Material.DIAMOND_HELMET, Material.DIAMOND_CHESTPLATE, Material.DIAMOND_LEGGINGS, Material.DIAMOND_BOOTS,
                    Material.NETHERITE_HELMET, Material.NETHERITE_CHESTPLATE, Material.NETHERITE_LEGGINGS, Material.NETHERITE_BOOTS,
                    Material.TURTLE_HELMET, Material.SHIELD
                ));
                break;
            case BLOCKS:
                items.addAll(Arrays.asList(
                    Material.STONE, Material.COBBLESTONE, Material.DIRT, Material.GRASS_BLOCK, Material.SAND, Material.GRAVEL,
                    Material.OAK_LOG, Material.OAK_PLANKS, Material.GLASS, Material.OBSIDIAN, Material.BEDROCK,
                    Material.IRON_BLOCK, Material.GOLD_BLOCK, Material.DIAMOND_BLOCK, Material.EMERALD_BLOCK,
                    Material.TNT, Material.WOOL, Material.BRICKS, Material.STONE_BRICKS, Material.NETHERRACK, Material.END_STONE
                ));
                break;
            case FOOD:
                items.addAll(Arrays.asList(
                    Material.APPLE, Material.GOLDEN_APPLE, Material.ENCHANTED_GOLDEN_APPLE, Material.BREAD, Material.COOKED_BEEF,
                    Material.COOKED_PORKCHOP, Material.COOKED_CHICKEN, Material.COOKED_COD, Material.COOKED_SALMON,
                    Material.CAKE, Material.COOKIE, Material.MELON_SLICE, Material.SWEET_BERRIES, Material.GLOW_BERRIES,
                    Material.CARROT, Material.POTATO, Material.BAKED_POTATO, Material.BEETROOT, Material.MUSHROOM_STEW
                ));
                break;
            case POTIONS:
                items.addAll(Arrays.asList(
                    Material.POTION, Material.SPLASH_POTION, Material.LINGERING_POTION, Material.GLASS_BOTTLE,
                    Material.BREWING_STAND, Material.CAULDRON, Material.BLAZE_POWDER, Material.NETHER_WART,
                    Material.SPIDER_EYE, Material.FERMENTED_SPIDER_EYE, Material.MAGMA_CREAM, Material.SUGAR,
                    Material.GLISTERING_MELON_SLICE, Material.GOLDEN_CARROT, Material.RABBIT_FOOT, Material.DRAGON_BREATH
                ));
                break;
            case TOOLS:
                items.addAll(Arrays.asList(
                    Material.WOODEN_PICKAXE, Material.STONE_PICKAXE, Material.IRON_PICKAXE, Material.GOLDEN_PICKAXE, Material.DIAMOND_PICKAXE, Material.NETHERITE_PICKAXE,
                    Material.WOODEN_SHOVEL, Material.STONE_SHOVEL, Material.IRON_SHOVEL, Material.GOLDEN_SHOVEL, Material.DIAMOND_SHOVEL, Material.NETHERITE_SHOVEL,
                    Material.WOODEN_HOE, Material.STONE_HOE, Material.IRON_HOE, Material.GOLDEN_HOE, Material.DIAMOND_HOE, Material.NETHERITE_HOE,
                    Material.FISHING_ROD, Material.SHEARS, Material.FLINT_AND_STEEL, Material.BUCKET, Material.WATER_BUCKET, Material.LAVA_BUCKET
                ));
                break;
            case UTILITY:
                items.addAll(Arrays.asList(
                    Material.ENDER_PEARL, Material.ENDER_EYE, Material.FLINT_AND_STEEL, Material.FIRE_CHARGE,
                    Material.SNOWBALL, Material.EGG, Material.FISHING_ROD, Material.COMPASS, Material.CLOCK,
                    Material.MAP, Material.LEAD, Material.NAME_TAG, Material.SADDLE, Material.BOAT,
                    Material.MINECART, Material.CHEST_MINECART, Material.FURNACE_MINECART, Material.TNT_MINECART,
                    Material.TOTEM_OF_UNDYING, Material.ELYTRA, Material.FIREWORK_ROCKET
                ));
                break;
            case MISC:
                items.addAll(Arrays.asList(
                    Material.BOOK, Material.PAPER, Material.FEATHER, Material.INK_SAC, Material.BONE, Material.STRING,
                    Material.STICK, Material.COAL, Material.CHARCOAL, Material.DIAMOND, Material.EMERALD, Material.GOLD_INGOT,
                    Material.IRON_INGOT, Material.REDSTONE, Material.GUNPOWDER, Material.GLOWSTONE_DUST,
                    Material.EXPERIENCE_BOTTLE, Material.ENCHANTED_BOOK, Material.ANVIL, Material.ENCHANTING_TABLE
                ));
                break;
        }
        
        return items;
    }
    
    private String getCategoryName(CategorySelectorGUI.ItemCategory category) {
        switch (category) {
            case WEAPONS: return "Weapons";
            case ARMOR: return "Armor";
            case BLOCKS: return "Blocks";
            case FOOD: return "Food";
            case POTIONS: return "Potions";
            case TOOLS: return "Tools";
            case UTILITY: return "Utility";
            case MISC: return "Misc";
            default: return "Items";
        }
    }
    
    private void setupGUI() {
        gui.clear();
        
        int itemsPerPage = 45;
        int startIndex = currentPage * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, categoryItems.size());
        
        // Add items for current page
        for (int i = startIndex; i < endIndex; i++) {
            Material material = categoryItems.get(i);
            ItemStack item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.WHITE + formatMaterialName(material.name()));
            item.setItemMeta(meta);
            gui.setItem(i - startIndex, item);
        }
        
        // Navigation buttons
        if (currentPage > 0) {
            ItemStack prevPage = new ItemStack(Material.ARROW);
            ItemMeta prevMeta = prevPage.getItemMeta();
            prevMeta.setDisplayName(ChatColor.YELLOW + "Previous Page");
            prevPage.setItemMeta(prevMeta);
            gui.setItem(45, prevPage);
        }
        
        if (endIndex < categoryItems.size()) {
            ItemStack nextPage = new ItemStack(Material.ARROW);
            ItemMeta nextMeta = nextPage.getItemMeta();
            nextMeta.setDisplayName(ChatColor.YELLOW + "Next Page");
            nextPage.setItemMeta(nextMeta);
            gui.setItem(53, nextPage);
        }
        
        // Back button
        ItemStack back = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.setDisplayName(ChatColor.RED + "Back");
        back.setItemMeta(backMeta);
        gui.setItem(49, back);
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
        
        if (slot == 45 && currentPage > 0) { // Previous page
            currentPage--;
            setupGUI();
        } else if (slot == 53 && (currentPage + 1) * 45 < categoryItems.size()) { // Next page
            currentPage++;
            setupGUI();
        } else if (slot == 49) { // Back button
            new CategorySelectorGUI(plugin, player, parentGUI, targetSlot).open();
        } else if (slot < 45) { // Item selection
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem != null && clickedItem.getType() != Material.AIR) {
                parentGUI.setSlotItem(targetSlot, clickedItem.clone());
                player.closeInventory();
                player.openInventory(parentGUI.getGui());
                player.sendMessage(ChatColor.GREEN + "Item added to slot " + (targetSlot + 1) + "!");
            }
        }
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        Player closer = (Player) event.getPlayer();
        
        if (closer.equals(player) && event.getInventory().equals(gui)) {
            activeGuis.remove(player.getUniqueId());
            // Unregister this listener
            InventoryClickEvent.getHandlerList().unregister(this);
            InventoryCloseEvent.getHandlerList().unregister(this);
        }
    }
}