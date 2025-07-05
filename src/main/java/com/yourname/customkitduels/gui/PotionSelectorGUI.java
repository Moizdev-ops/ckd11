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
    private int currentPage = 0;
    private static final Map<UUID, PotionSelectorGUI> activeGuis = new HashMap<>();
    private boolean isActive = true;
    
    public PotionSelectorGUI(CustomKitDuels plugin, Player player, ItemModificationGUI parentGUI, ItemStack targetItem) {
        this.plugin = plugin;
        this.player = player;
        this.parentGUI = parentGUI;
        this.targetItem = targetItem.clone();
        this.availablePotions = getAvailablePotions();
        this.gui = Bukkit.createInventory(null, 54, ChatColor.DARK_AQUA + "Select Potion Type");
        
        plugin.getLogger().info("[DEBUG] Creating PotionSelectorGUI for player " + player.getName());
        
        setupGUI();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    private List<PotionType> getAvailablePotions() {
        List<PotionType> potions = new ArrayList<>();
        
        // Add all useful potion types
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
        
        int itemsPerPage = 36;
        int startIndex = currentPage * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, availablePotions.size());
        
        // Add potion options for current page
        int slot = 9;
        for (int i = startIndex; i < endIndex; i++) {
            PotionType potionType = availablePotions.get(i);
            
            // Create potion item with the same type as target
            ItemStack potionItem = new ItemStack(targetItem.getType());
            PotionMeta potionMeta = (PotionMeta) potionItem.getItemMeta();
            
            // Set base potion data
            potionMeta.setBasePotionData(new PotionData(potionType, false, false));
            
            String potionName = formatPotionName(potionType.name());
            potionMeta.setDisplayName(ChatColor.AQUA + potionName);
            
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Click to select this potion type");
            lore.add(ChatColor.YELLOW + "Left-click: Normal");
            lore.add(ChatColor.YELLOW + "Right-click: Extended");
            lore.add(ChatColor.YELLOW + "Shift-click: Upgraded");
            
            potionMeta.setLore(lore);
            potionItem.setItemMeta(potionMeta);
            
            gui.setItem(slot, potionItem);
            slot++;
        }
        
        // Navigation buttons
        if (currentPage > 0) {
            ItemStack prevPage = new ItemStack(Material.ARROW);
            ItemMeta prevMeta = prevPage.getItemMeta();
            prevMeta.setDisplayName(ChatColor.YELLOW + "Previous Page");
            prevPage.setItemMeta(prevMeta);
            gui.setItem(45, prevPage);
        }
        
        if (endIndex < availablePotions.size()) {
            ItemStack nextPage = new ItemStack(Material.ARROW);
            ItemMeta nextMeta = nextPage.getItemMeta();
            nextMeta.setDisplayName(ChatColor.YELLOW + "Next Page");
            nextPage.setItemMeta(nextMeta);
            gui.setItem(53, nextPage);
        }
        
        // Back button
        ItemStack backButton = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta backMeta = backButton.getItemMeta();
        backMeta.setDisplayName(ChatColor.RED + "Back");
        backMeta.setLore(Arrays.asList(ChatColor.GRAY + "Return to item modification"));
        backButton.setItemMeta(backMeta);
        gui.setItem(49, backButton);
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
        
        if (!clicker.equals(player) || !event.getInventory().equals(gui) || !isActive) {
            return;
        }
        
        event.setCancelled(true);
        int slot = event.getSlot();
        
        if (slot == 45 && currentPage > 0) { // Previous page
            currentPage--;
            setupGUI();
            return;
        }
        
        if (slot == 53 && (currentPage + 1) * 36 < availablePotions.size()) { // Next page
            currentPage++;
            setupGUI();
            return;
        }
        
        if (slot == 49) { // Back button
            returnToParent();
            return;
        }
        
        // Handle potion selection
        if (slot >= 9 && slot < 45) {
            int potionIndex = (currentPage * 36) + (slot - 9);
            if (potionIndex < availablePotions.size()) {
                PotionType selectedType = availablePotions.get(potionIndex);
                applyPotionType(selectedType, event);
            }
        }
    }
    
    private void applyPotionType(PotionType potionType, InventoryClickEvent event) {
        boolean extended = event.isRightClick();
        boolean upgraded = event.isShiftClick();
        
        // Some potions can't be extended or upgraded
        if (extended && !canBeExtended(potionType)) {
            extended = false;
        }
        if (upgraded && !canBeUpgraded(potionType)) {
            upgraded = false;
        }
        
        // Extended and upgraded are mutually exclusive
        if (extended && upgraded) {
            upgraded = false;
        }
        
        PotionMeta potionMeta = (PotionMeta) targetItem.getItemMeta();
        potionMeta.setBasePotionData(new PotionData(potionType, extended, upgraded));
        targetItem.setItemMeta(potionMeta);
        
        String modifiers = "";
        if (extended) modifiers += " (Extended)";
        if (upgraded) modifiers += " (Upgraded)";
        
        player.sendMessage(ChatColor.GREEN + "Potion type set to " + formatPotionName(potionType.name()) + modifiers + "!");
        
        parentGUI.updateItem(targetItem);
        returnToParent();
    }
    
    private boolean canBeExtended(PotionType potionType) {
        switch (potionType) {
            case NIGHT_VISION:
            case INVISIBILITY:
            case JUMP:
            case FIRE_RESISTANCE:
            case SPEED:
            case SLOWNESS:
            case WATER_BREATHING:
            case POISON:
            case REGEN:
            case STRENGTH:
            case WEAKNESS:
            case SLOW_FALLING:
                return true;
            default:
                return false;
        }
    }
    
    private boolean canBeUpgraded(PotionType potionType) {
        switch (potionType) {
            case JUMP:
            case SPEED:
            case INSTANT_HEAL:
            case INSTANT_DAMAGE:
            case POISON:
            case REGEN:
            case STRENGTH:
            case TURTLE_MASTER:
                return true;
            default:
                return false;
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
        
        if (closer.equals(player) && event.getInventory().equals(gui)) {
            plugin.getLogger().info("[DEBUG] PotionSelectorGUI inventory closed by " + player.getName() + ", Active: " + isActive);
            
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (isActive && activeGuis.containsKey(player.getUniqueId())) {
                    plugin.getLogger().info("[DEBUG] Final cleanup PotionSelectorGUI for " + player.getName());
                    forceCleanup();
                    parentGUI.refreshAndReopen();
                }
            }, 3L);
        }
    }
}