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
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class EnhancedItemModificationGUI implements Listener {
    
    private final CustomKitDuels plugin;
    private final Player player;
    private final KitEditorGUI parentGUI;
    private final int targetSlot;
    private ItemStack targetItem;
    private final Inventory gui;
    private static final Map<UUID, EnhancedItemModificationGUI> activeGuis = new HashMap<>();
    private static final Set<UUID> waitingForCustomAmount = new HashSet<>();
    private boolean isActive = true;
    private boolean isNavigating = false;
    
    public EnhancedItemModificationGUI(CustomKitDuels plugin, Player player, KitEditorGUI parentGUI, int targetSlot, ItemStack targetItem) {
        this.plugin = plugin;
        this.player = player;
        this.parentGUI = parentGUI;
        this.targetSlot = targetSlot;
        this.targetItem = targetItem.clone();
        this.gui = Bukkit.createInventory(null, 54, ChatColor.DARK_RED + "modify item");
        
        plugin.getLogger().info("[DEBUG] Creating EnhancedItemModificationGUI for player " + player.getName() + " slot " + targetSlot);
        
        setupGUI();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    private void setupGUI() {
        gui.clear();
        
        // Display the item being modified
        gui.setItem(4, targetItem.clone());
        
        if (isStackableItem(targetItem)) {
            setupStackableItemGUI();
        } else if (isEnchantableItem(targetItem)) {
            setupEnchantableItemGUI();
        } else {
            setupBasicItemGUI();
        }
        
        // Remove item option
        ItemStack removeItem = new ItemStack(Material.BARRIER);
        ItemMeta removeMeta = removeItem.getItemMeta();
        removeMeta.setDisplayName(ChatColor.RED + "remove item");
        removeMeta.setLore(Arrays.asList(ChatColor.GRAY + "remove this item from the slot"));
        removeItem.setItemMeta(removeMeta);
        gui.setItem(53, removeItem);
        
        // Back button
        ItemStack backItem = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta backMeta = backItem.getItemMeta();
        backMeta.setDisplayName(ChatColor.RED + "back");
        backMeta.setLore(Arrays.asList(ChatColor.GRAY + "return to kit editor"));
        backItem.setItemMeta(backMeta);
        gui.setItem(45, backItem);
    }
    
    private void setupStackableItemGUI() {
        // Title
        ItemStack titleItem = new ItemStack(Material.BOOK);
        ItemMeta titleMeta = titleItem.getItemMeta();
        titleMeta.setDisplayName(ChatColor.YELLOW + "change item count");
        titleMeta.setLore(Arrays.asList(
            ChatColor.GRAY + "select the amount you want",
            ChatColor.GRAY + "current: " + targetItem.getAmount()
        ));
        titleItem.setItemMeta(titleMeta);
        gui.setItem(0, titleItem);
        
        // Amount options: 1, 8, 16, 32, 64
        int[] amounts = {1, 8, 16, 32, 64};
        int[] slots = {19, 20, 21, 22, 23};
        
        for (int i = 0; i < amounts.length; i++) {
            ItemStack amountItem = targetItem.clone();
            amountItem.setAmount(Math.min(amounts[i], targetItem.getMaxStackSize()));
            
            ItemMeta meta = amountItem.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.GREEN + "amount: " + amounts[i]);
                meta.setLore(Arrays.asList(ChatColor.YELLOW + "click to set amount to " + amounts[i]));
                amountItem.setItemMeta(meta);
            }
            
            gui.setItem(slots[i], amountItem);
        }
        
        // Custom amount option
        ItemStack customItem = new ItemStack(Material.PAPER);
        ItemMeta customMeta = customItem.getItemMeta();
        customMeta.setDisplayName(ChatColor.AQUA + "custom amount");
        customMeta.setLore(Arrays.asList(
            ChatColor.GRAY + "enter a custom amount in chat",
            ChatColor.YELLOW + "max: " + targetItem.getMaxStackSize()
        ));
        customItem.setItemMeta(customMeta);
        gui.setItem(24, customItem);
    }
    
    private void setupEnchantableItemGUI() {
        List<Enchantment> relevantEnchants = getRelevantEnchantments(targetItem.getType());
        
        int slot = 9;
        for (Enchantment enchant : relevantEnchants) {
            if (slot >= 45) break; // Don't overflow into control area
            
            // Create a row for each enchantment type
            for (int level = 1; level <= enchant.getMaxLevel() && slot < 45; level++) {
                ItemStack enchantBook = new ItemStack(Material.ENCHANTED_BOOK);
                enchantBook.setAmount(level); // Stack size shows level
                
                ItemMeta meta = enchantBook.getItemMeta();
                String enchantName = formatEnchantmentName(enchant.getKey().getKey());
                meta.setDisplayName(ChatColor.LIGHT_PURPLE + enchantName + " " + level);
                
                int currentLevel = targetItem.getEnchantmentLevel(enchant);
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.GRAY + "current level: " + (currentLevel > 0 ? currentLevel : "none"));
                lore.add(ChatColor.YELLOW + "click to apply " + enchantName + " " + level);
                
                if (currentLevel == level) {
                    lore.add(ChatColor.GREEN + "✓ currently applied");
                }
                
                meta.setLore(lore);
                enchantBook.setItemMeta(meta);
                
                gui.setItem(slot, enchantBook);
                slot++;
            }
            
            // Move to next row after each enchantment type
            slot = ((slot / 9) + 1) * 9;
        }
        
        // Durability and rename buttons in last row
        ItemStack durabilityItem = new ItemStack(Material.ANVIL);
        ItemMeta durabilityMeta = durabilityItem.getItemMeta();
        durabilityMeta.setDisplayName(ChatColor.YELLOW + "change durability");
        durabilityMeta.setLore(Arrays.asList(
            ChatColor.GRAY + "modify item durability",
            ChatColor.YELLOW + "click to open durability editor"
        ));
        durabilityItem.setItemMeta(durabilityMeta);
        gui.setItem(46, durabilityItem);
        
        ItemStack renameItem = new ItemStack(Material.NAME_TAG);
        ItemMeta renameMeta = renameItem.getItemMeta();
        renameMeta.setDisplayName(ChatColor.AQUA + "rename item");
        renameMeta.setLore(Arrays.asList(
            ChatColor.GRAY + "give your item a custom name",
            ChatColor.YELLOW + "click to rename"
        ));
        renameItem.setItemMeta(renameMeta);
        gui.setItem(47, renameItem);
    }
    
    private void setupBasicItemGUI() {
        // For non-stackable, non-enchantable items, just show basic options
        ItemStack infoItem = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = infoItem.getItemMeta();
        infoMeta.setDisplayName(ChatColor.YELLOW + "item options");
        infoMeta.setLore(Arrays.asList(
            ChatColor.GRAY + "this item cannot be modified",
            ChatColor.GRAY + "you can only remove it"
        ));
        infoItem.setItemMeta(infoMeta);
        gui.setItem(22, infoItem);
    }
    
    private boolean isStackableItem(ItemStack item) {
        return item.getMaxStackSize() > 1;
    }
    
    private boolean isEnchantableItem(ItemStack item) {
        Material type = item.getType();
        return type.toString().contains("SWORD") || 
               type.toString().contains("AXE") ||
               type.toString().contains("PICKAXE") ||
               type.toString().contains("SHOVEL") ||
               type.toString().contains("HOE") ||
               type.toString().contains("BOW") ||
               type.toString().contains("CROSSBOW") ||
               type.toString().contains("TRIDENT") ||
               type.toString().contains("HELMET") ||
               type.toString().contains("CHESTPLATE") ||
               type.toString().contains("LEGGINGS") ||
               type.toString().contains("BOOTS") ||
               type == Material.SHIELD ||
               type == Material.FISHING_ROD ||
               type == Material.SHEARS ||
               type == Material.FLINT_AND_STEEL ||
               type == Material.ELYTRA ||
               type == Material.MACE;
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
        }
        
        // Tool enchantments
        if (materialName.contains("PICKAXE") || materialName.contains("SHOVEL") || materialName.contains("HOE")) {
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
            
            if (materialName.contains("BOOTS")) {
                enchantments.addAll(Arrays.asList(
                    Enchantment.FEATHER_FALLING, Enchantment.DEPTH_STRIDER, 
                    Enchantment.FROST_WALKER, Enchantment.SOUL_SPEED
                ));
            }
        }
        
        return enchantments;
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
        plugin.getLogger().info("[DEBUG] Opening EnhancedItemModificationGUI for " + player.getName());
        
        EnhancedItemModificationGUI existing = activeGuis.get(player.getUniqueId());
        if (existing != null && existing != this) {
            plugin.getLogger().info("[DEBUG] Cleaning up existing EnhancedItemModificationGUI for " + player.getName());
            existing.forceCleanup();
        }
        
        activeGuis.put(player.getUniqueId(), this);
        isActive = true;
        isNavigating = false;
        player.openInventory(gui);
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player clicker = (Player) event.getWhoClicked();
        
        if (!clicker.equals(player) || !event.getInventory().equals(gui) || !isActive || isNavigating) {
            return;
        }
        
        event.setCancelled(true);
        int slot = event.getSlot();
        
        plugin.getLogger().info("[DEBUG] EnhancedItemModificationGUI click event - Player: " + player.getName() + ", Slot: " + slot);
        
        if (slot == 45) { // Back
            returnToParent();
            return;
        }
        
        if (slot == 53) { // Remove item
            removeItem();
            return;
        }
        
        if (isStackableItem(targetItem)) {
            handleStackableItemClick(slot);
        } else if (isEnchantableItem(targetItem)) {
            handleEnchantableItemClick(slot);
        }
    }
    
    private void handleStackableItemClick(int slot) {
        if (slot >= 19 && slot <= 23) {
            // Amount buttons
            int[] amounts = {1, 8, 16, 32, 64};
            int index = slot - 19;
            if (index < amounts.length) {
                int newAmount = Math.min(amounts[index], targetItem.getMaxStackSize());
                targetItem.setAmount(newAmount);
                parentGUI.setSlotItem(targetSlot, targetItem);
                player.sendMessage(ChatColor.GREEN + "item amount set to " + newAmount + "!");
                returnToParent();
            }
        } else if (slot == 24) {
            // Custom amount
            requestCustomAmount();
        }
    }
    
    private void handleEnchantableItemClick(int slot) {
        if (slot >= 9 && slot < 45) {
            ItemStack clickedItem = gui.getItem(slot);
            if (clickedItem != null && clickedItem.getType() == Material.ENCHANTED_BOOK) {
                // Extract enchantment info from the item
                String displayName = clickedItem.getItemMeta().getDisplayName();
                int level = clickedItem.getAmount();
                
                // Find the enchantment based on display name
                for (Enchantment enchant : getRelevantEnchantments(targetItem.getType())) {
                    String enchantName = formatEnchantmentName(enchant.getKey().getKey());
                    if (displayName.contains(enchantName)) {
                        // Remove conflicting enchantments
                        removeConflictingEnchantments(enchant);
                        
                        // Apply the enchantment
                        targetItem.addUnsafeEnchantment(enchant, level);
                        parentGUI.setSlotItem(targetSlot, targetItem);
                        player.sendMessage(ChatColor.GREEN + "applied " + enchantName + " " + level + "!");
                        returnToParent();
                        return;
                    }
                }
            }
        } else if (slot == 46) {
            // Durability editor - for now just reset to full durability
            if (targetItem.getType().getMaxDurability() > 0) {
                targetItem.setDurability((short) 0); // Full durability
                parentGUI.setSlotItem(targetSlot, targetItem);
                player.sendMessage(ChatColor.GREEN + "item durability restored!");
                returnToParent();
            }
        } else if (slot == 47) {
            // Rename item
            requestItemName();
        }
    }
    
    private void removeConflictingEnchantments(Enchantment newEnchantment) {
        // Simple conflict removal - remove protection enchants if adding another protection
        if (newEnchantment == Enchantment.PROTECTION || newEnchantment == Enchantment.FIRE_PROTECTION ||
            newEnchantment == Enchantment.BLAST_PROTECTION || newEnchantment == Enchantment.PROJECTILE_PROTECTION) {
            targetItem.removeEnchantment(Enchantment.PROTECTION);
            targetItem.removeEnchantment(Enchantment.FIRE_PROTECTION);
            targetItem.removeEnchantment(Enchantment.BLAST_PROTECTION);
            targetItem.removeEnchantment(Enchantment.PROJECTILE_PROTECTION);
        }
        
        // Remove damage enchants if adding another damage enchant
        if (newEnchantment == Enchantment.SHARPNESS || newEnchantment == Enchantment.SMITE ||
            newEnchantment == Enchantment.BANE_OF_ARTHROPODS) {
            targetItem.removeEnchantment(Enchantment.SHARPNESS);
            targetItem.removeEnchantment(Enchantment.SMITE);
            targetItem.removeEnchantment(Enchantment.BANE_OF_ARTHROPODS);
        }
    }
    
    private void requestCustomAmount() {
        plugin.getLogger().info("[DEBUG] Requesting custom amount from " + player.getName());
        waitingForCustomAmount.add(player.getUniqueId());
        isNavigating = true;
        player.closeInventory();
        player.sendMessage(ChatColor.YELLOW + "enter the amount (1-" + targetItem.getMaxStackSize() + ") in chat:");
    }
    
    private void requestItemName() {
        plugin.getLogger().info("[DEBUG] Requesting item name from " + player.getName());
        waitingForCustomAmount.add(player.getUniqueId()); // Reuse the same set
        isNavigating = true;
        player.closeInventory();
        player.sendMessage(ChatColor.YELLOW + "enter the new item name in chat:");
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (!waitingForCustomAmount.contains(event.getPlayer().getUniqueId())) {
            return;
        }
        
        Player chatPlayer = event.getPlayer();
        if (!chatPlayer.equals(player)) {
            return;
        }
        
        event.setCancelled(true);
        waitingForCustomAmount.remove(player.getUniqueId());
        
        String message = event.getMessage().trim();
        
        // Check if it's a number (for amount) or text (for name)
        try {
            int amount = Integer.parseInt(message);
            if (amount < 1 || amount > targetItem.getMaxStackSize()) {
                player.sendMessage(ChatColor.RED + "invalid amount! must be between 1 and " + targetItem.getMaxStackSize());
            } else {
                targetItem.setAmount(amount);
                parentGUI.setSlotItem(targetSlot, targetItem);
                player.sendMessage(ChatColor.GREEN + "item amount set to " + amount + "!");
                
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    returnToParent();
                });
                return;
            }
        } catch (NumberFormatException e) {
            // It's a name, not a number
            ItemMeta meta = targetItem.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', message));
                targetItem.setItemMeta(meta);
                parentGUI.setSlotItem(targetSlot, targetItem);
                player.sendMessage(ChatColor.GREEN + "item renamed to: " + message);
                
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    returnToParent();
                });
                return;
            }
        }
        
        // Reopen the modification GUI if there was an error
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            isNavigating = false;
            player.openInventory(gui);
        });
    }
    
    private void removeItem() {
        plugin.getLogger().info("[DEBUG] Removing item from slot " + targetSlot);
        parentGUI.clearSlot(targetSlot);
        player.sendMessage(ChatColor.YELLOW + "item removed from slot!");
        returnToParent();
    }
    
    private void returnToParent() {
        plugin.getLogger().info("[DEBUG] Returning to parent GUI for player " + player.getName());
        isNavigating = true;
        forceCleanup();
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            parentGUI.refreshAndReopen();
        }, 1L);
    }
    
    public void updateItem(ItemStack newItem) {
        this.targetItem = newItem.clone();
        parentGUI.setSlotItem(targetSlot, targetItem);
        setupGUI(); // Refresh the modification GUI
    }
    
    public void refreshAndReopen() {
        plugin.getLogger().info("[DEBUG] Refreshing and reopening EnhancedItemModificationGUI for " + player.getName());
        isNavigating = false;
        setupGUI();
        
        if (!player.getOpenInventory().getTopInventory().equals(gui)) {
            player.openInventory(gui);
        }
    }
    
    private void forceCleanup() {
        plugin.getLogger().info("[DEBUG] Force cleanup EnhancedItemModificationGUI for " + player.getName());
        isActive = false;
        activeGuis.remove(player.getUniqueId());
        waitingForCustomAmount.remove(player.getUniqueId());
        InventoryClickEvent.getHandlerList().unregister(this);
        InventoryCloseEvent.getHandlerList().unregister(this);
        AsyncPlayerChatEvent.getHandlerList().unregister(this);
        player.closeInventory();
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        Player closer = (Player) event.getPlayer();
        
        if (closer.equals(player) && event.getInventory().equals(gui)) {
            plugin.getLogger().info("[DEBUG] EnhancedItemModificationGUI inventory closed by " + player.getName() + ", Active: " + isActive + ", Navigating: " + isNavigating);
            
            // Don't cleanup if waiting for chat input or navigating
            if (waitingForCustomAmount.contains(player.getUniqueId()) || isNavigating) {
                return;
            }
            
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (isActive && !isNavigating && activeGuis.containsKey(player.getUniqueId())) {
                    plugin.getLogger().info("[DEBUG] Final cleanup EnhancedItemModificationGUI for " + player.getName());
                    forceCleanup();
                    parentGUI.refreshAndReopen();
                }
            }, 3L);
        }
    }
}