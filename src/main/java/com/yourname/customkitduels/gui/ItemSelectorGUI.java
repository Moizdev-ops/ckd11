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

public class ItemSelectorGUI implements Listener {
    
    private final CustomKitDuels plugin;
    private final Player player;
    private final KitEditorGUI parentGUI;
    private final int targetSlot;
    private final CategorySelectorGUI.ItemCategory category;
    private final Inventory gui;
    private final List<ItemStack> categoryItems;
    private int currentPage = 0;
    private static final Map<UUID, ItemSelectorGUI> activeGuis = new HashMap<>();
    private boolean isActive = true;
    
    public ItemSelectorGUI(CustomKitDuels plugin, Player player, KitEditorGUI parentGUI, int targetSlot, CategorySelectorGUI.ItemCategory category) {
        this.plugin = plugin;
        this.player = player;
        this.parentGUI = parentGUI;
        this.targetSlot = targetSlot;
        this.category = category;
        this.categoryItems = getItemsForCategory(category);
        this.gui = Bukkit.createInventory(null, 54, ChatColor.DARK_PURPLE + getCategoryName(category) + " Items");
        
        plugin.getLogger().info("[DEBUG] Creating ItemSelectorGUI for player " + player.getName() + " category " + category + " slot " + targetSlot);
        
        setupGUI();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    private List<ItemStack> getItemsForCategory(CategorySelectorGUI.ItemCategory category) {
        List<ItemStack> items = new ArrayList<>();
        
        switch (category) {
            case WEAPONS:
                items.addAll(createBasicItems(Arrays.asList(
                    Material.WOODEN_SWORD, Material.STONE_SWORD, Material.IRON_SWORD, Material.GOLDEN_SWORD, Material.DIAMOND_SWORD, Material.NETHERITE_SWORD,
                    Material.WOODEN_AXE, Material.STONE_AXE, Material.IRON_AXE, Material.GOLDEN_AXE, Material.DIAMOND_AXE, Material.NETHERITE_AXE,
                    Material.BOW, Material.CROSSBOW, Material.TRIDENT, Material.MACE
                )));
                break;
            case ARMOR:
                items.addAll(createBasicItems(Arrays.asList(
                    Material.LEATHER_HELMET, Material.LEATHER_CHESTPLATE, Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS,
                    Material.CHAINMAIL_HELMET, Material.CHAINMAIL_CHESTPLATE, Material.CHAINMAIL_LEGGINGS, Material.CHAINMAIL_BOOTS,
                    Material.IRON_HELMET, Material.IRON_CHESTPLATE, Material.IRON_LEGGINGS, Material.IRON_BOOTS,
                    Material.GOLDEN_HELMET, Material.GOLDEN_CHESTPLATE, Material.GOLDEN_LEGGINGS, Material.GOLDEN_BOOTS,
                    Material.DIAMOND_HELMET, Material.DIAMOND_CHESTPLATE, Material.DIAMOND_LEGGINGS, Material.DIAMOND_BOOTS,
                    Material.NETHERITE_HELMET, Material.NETHERITE_CHESTPLATE, Material.NETHERITE_LEGGINGS, Material.NETHERITE_BOOTS,
                    Material.TURTLE_HELMET, Material.SHIELD
                )));
                break;
            case BLOCKS:
                items.addAll(createBasicItems(Arrays.asList(
                    Material.STONE, Material.COBBLESTONE, Material.DIRT, Material.GRASS_BLOCK, Material.SAND, Material.GRAVEL,
                    Material.OAK_LOG, Material.OAK_PLANKS, Material.GLASS, Material.OBSIDIAN, Material.BEDROCK,
                    Material.IRON_BLOCK, Material.GOLD_BLOCK, Material.DIAMOND_BLOCK, Material.EMERALD_BLOCK,
                    Material.TNT, Material.WHITE_WOOL, Material.BRICKS, Material.STONE_BRICKS, Material.NETHERRACK, Material.END_STONE,
                    Material.RED_WOOL, Material.BLUE_WOOL, Material.GREEN_WOOL, Material.YELLOW_WOOL, Material.BLACK_WOOL,
                    Material.DEEPSLATE, Material.COPPER_BLOCK, Material.AMETHYST_BLOCK, Material.CALCITE, Material.TUFF
                )));
                break;
            case FOOD:
                items.addAll(createBasicItems(Arrays.asList(
                    Material.APPLE, Material.GOLDEN_APPLE, Material.ENCHANTED_GOLDEN_APPLE, Material.BREAD, Material.COOKED_BEEF,
                    Material.COOKED_PORKCHOP, Material.COOKED_CHICKEN, Material.COOKED_COD, Material.COOKED_SALMON,
                    Material.CAKE, Material.COOKIE, Material.MELON_SLICE, Material.SWEET_BERRIES, Material.GLOW_BERRIES,
                    Material.CARROT, Material.POTATO, Material.BAKED_POTATO, Material.BEETROOT, Material.MUSHROOM_STEW,
                    Material.SUSPICIOUS_STEW, Material.RABBIT_STEW, Material.PUMPKIN_PIE, Material.DRIED_KELP
                )));
                break;
            case POTIONS:
                // Add all potion types for normal, splash, and lingering potions
                items.addAll(createPotionItems());
                items.addAll(createBasicItems(Arrays.asList(
                    Material.GLASS_BOTTLE, Material.BREWING_STAND, Material.CAULDRON, Material.BLAZE_POWDER, Material.NETHER_WART,
                    Material.SPIDER_EYE, Material.FERMENTED_SPIDER_EYE, Material.MAGMA_CREAM, Material.SUGAR,
                    Material.GLISTERING_MELON_SLICE, Material.GOLDEN_CARROT, Material.RABBIT_FOOT, Material.DRAGON_BREATH,
                    Material.GHAST_TEAR, Material.PHANTOM_MEMBRANE, Material.HONEY_BOTTLE, Material.MILK_BUCKET
                )));
                break;
            case TOOLS:
                items.addAll(createBasicItems(Arrays.asList(
                    Material.WOODEN_PICKAXE, Material.STONE_PICKAXE, Material.IRON_PICKAXE, Material.GOLDEN_PICKAXE, Material.DIAMOND_PICKAXE, Material.NETHERITE_PICKAXE,
                    Material.WOODEN_SHOVEL, Material.STONE_SHOVEL, Material.IRON_SHOVEL, Material.GOLDEN_SHOVEL, Material.DIAMOND_SHOVEL, Material.NETHERITE_SHOVEL,
                    Material.WOODEN_HOE, Material.STONE_HOE, Material.IRON_HOE, Material.GOLDEN_HOE, Material.DIAMOND_HOE, Material.NETHERITE_HOE,
                    Material.FISHING_ROD, Material.SHEARS, Material.FLINT_AND_STEEL, Material.BUCKET, Material.WATER_BUCKET, Material.LAVA_BUCKET,
                    Material.COMPASS, Material.CLOCK, Material.SPYGLASS, Material.BRUSH
                )));
                break;
            case UTILITY:
                items.addAll(createBasicItems(Arrays.asList(
                    Material.ENDER_PEARL, Material.ENDER_EYE, Material.FLINT_AND_STEEL, Material.FIRE_CHARGE,
                    Material.SNOWBALL, Material.EGG, Material.FISHING_ROD, Material.COMPASS, Material.CLOCK,
                    Material.FILLED_MAP, Material.LEAD, Material.NAME_TAG, Material.SADDLE, Material.OAK_BOAT,
                    Material.MINECART, Material.CHEST_MINECART, Material.FURNACE_MINECART, Material.TNT_MINECART,
                    Material.TOTEM_OF_UNDYING, Material.ELYTRA, Material.FIREWORK_ROCKET, Material.RECOVERY_COMPASS,
                    Material.ECHO_SHARD, Material.GOAT_HORN, Material.WIND_CHARGE
                )));
                break;
            case MISC:
                items.addAll(createBasicItems(Arrays.asList(
                    Material.BOOK, Material.PAPER, Material.FEATHER, Material.INK_SAC, Material.BONE, Material.STRING,
                    Material.STICK, Material.COAL, Material.CHARCOAL, Material.DIAMOND, Material.EMERALD, Material.GOLD_INGOT,
                    Material.IRON_INGOT, Material.REDSTONE, Material.GUNPOWDER, Material.GLOWSTONE_DUST,
                    Material.EXPERIENCE_BOTTLE, Material.ENCHANTED_BOOK, Material.ANVIL, Material.ENCHANTING_TABLE,
                    Material.NETHERITE_INGOT, Material.COPPER_INGOT, Material.AMETHYST_SHARD, Material.PRISMARINE_SHARD,
                    Material.HEART_OF_THE_SEA, Material.NAUTILUS_SHELL, Material.DISC_FRAGMENT_5
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
    
    private List<ItemStack> createPotionItems() {
        List<ItemStack> potions = new ArrayList<>();
        
        // All available potion types
        PotionType[] potionTypes = {
            PotionType.WATER, PotionType.MUNDANE, PotionType.THICK, PotionType.AWKWARD,
            PotionType.NIGHT_VISION, PotionType.INVISIBILITY, PotionType.JUMP, PotionType.FIRE_RESISTANCE,
            PotionType.SPEED, PotionType.SLOWNESS, PotionType.WATER_BREATHING, PotionType.INSTANT_HEAL,
            PotionType.INSTANT_DAMAGE, PotionType.POISON, PotionType.REGEN, PotionType.STRENGTH,
            PotionType.WEAKNESS, PotionType.LUCK, PotionType.TURTLE_MASTER, PotionType.SLOW_FALLING
        };
        
        // Create normal, splash, and lingering versions
        Material[] potionMaterials = {Material.POTION, Material.SPLASH_POTION, Material.LINGERING_POTION};
        
        for (Material potionMaterial : potionMaterials) {
            for (PotionType potionType : potionTypes) {
                ItemStack potion = new ItemStack(potionMaterial);
                PotionMeta meta = (PotionMeta) potion.getItemMeta();
                meta.setBasePotionData(new PotionData(potionType, false, false));
                
                // Set display name
                String typeName = formatPotionName(potionType.name());
                String materialName = formatMaterialName(potionMaterial.name());
                meta.setDisplayName(ChatColor.WHITE + materialName + " of " + typeName);
                
                potion.setItemMeta(meta);
                potions.add(potion);
            }
        }
        
        return potions;
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
            ItemStack item = categoryItems.get(i).clone();
            ItemMeta meta = item.getItemMeta();
            if (meta.getDisplayName() == null || meta.getDisplayName().isEmpty()) {
                meta.setDisplayName(ChatColor.WHITE + formatMaterialName(item.getType().name()));
            }
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
        plugin.getLogger().info("[DEBUG] Opening ItemSelectorGUI for " + player.getName());
        
        // Clean up any existing item selector GUI for this player
        ItemSelectorGUI existing = activeGuis.get(player.getUniqueId());
        if (existing != null && existing != this) {
            plugin.getLogger().info("[DEBUG] Cleaning up existing ItemSelectorGUI for " + player.getName());
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
        
        plugin.getLogger().info("[DEBUG] ItemSelectorGUI click event - Player: " + player.getName() + ", Slot: " + slot + ", Active: " + isActive);
        
        if (slot == 45 && currentPage > 0) { // Previous page
            plugin.getLogger().info("[DEBUG] Previous page clicked");
            currentPage--;
            setupGUI();
        } else if (slot == 53 && (currentPage + 1) * 45 < categoryItems.size()) { // Next page
            plugin.getLogger().info("[DEBUG] Next page clicked");
            currentPage++;
            setupGUI();
        } else if (slot == 49) { // Back button
            plugin.getLogger().info("[DEBUG] Back to category clicked");
            isActive = false;
            returnToCategory();
        } else if (slot < 45) { // Item selection
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem != null && clickedItem.getType() != Material.AIR) {
                plugin.getLogger().info("[DEBUG] Item selected: " + clickedItem.getType() + " for slot " + targetSlot);
                isActive = false;
                parentGUI.setSlotItem(targetSlot, clickedItem.clone());
                player.sendMessage(ChatColor.GREEN + "Item added to slot " + getSlotDisplayName(targetSlot) + "!");
                returnToParent();
            }
        }
    }
    
    private String getSlotDisplayName(int slot) {
        if (slot < 36) {
            return "#" + (slot + 1);
        } else if (slot < 40) {
            String[] armorSlots = {"Boots", "Leggings", "Chestplate", "Helmet"};
            return armorSlots[slot - 36];
        } else if (slot == 40) {
            return "Offhand";
        }
        return "Unknown";
    }
    
    private void returnToCategory() {
        plugin.getLogger().info("[DEBUG] Returning to category selector for player " + player.getName());
        forceCleanup();
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            new CategorySelectorGUI(plugin, player, parentGUI, targetSlot).open();
        }, 1L);
    }
    
    private void returnToParent() {
        plugin.getLogger().info("[DEBUG] Returning to parent GUI for player " + player.getName());
        forceCleanup();
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            parentGUI.refreshAndReopen();
        }, 1L);
    }
    
    private void forceCleanup() {
        plugin.getLogger().info("[DEBUG] Force cleanup ItemSelectorGUI for " + player.getName());
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
            plugin.getLogger().info("[DEBUG] ItemSelectorGUI inventory closed by " + player.getName() + ", Active: " + isActive);
            
            // Delay cleanup to allow for navigation
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (isActive && activeGuis.containsKey(player.getUniqueId())) {
                    plugin.getLogger().info("[DEBUG] Final cleanup ItemSelectorGUI for " + player.getName());
                    forceCleanup();
                    parentGUI.refreshAndReopen();
                }
            }, 3L);
        }
    }
}