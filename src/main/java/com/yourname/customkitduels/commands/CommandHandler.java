package com.yourname.customkitduels.commands;

import com.yourname.customkitduels.CustomKitDuels;
import com.yourname.customkitduels.data.Arena;
import com.yourname.customkitduels.data.Kit;
import com.yourname.customkitduels.gui.ArenaEditorGUI;
import com.yourname.customkitduels.gui.CategoryEditorGUI;
import com.yourname.customkitduels.gui.KitEditorGUI;
import com.yourname.customkitduels.gui.RoundsSelectorGUI;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CommandHandler implements CommandExecutor, TabCompleter {
    
    private final CustomKitDuels plugin;
    
    public CommandHandler(CustomKitDuels plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelpMessage(sender);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "createkit":
                return handleCreateKit(sender, args);
            case "editkit":
                return handleEditKit(sender, args);
            case "deletekit":
                return handleDeleteKit(sender, args);
            case "listkits":
                return handleListKits(sender);
            case "duel":
                return handleDuel(sender, args);
            case "accept":
                return handleAccept(sender);
            case "editcategory":
                return handleEditCategory(sender, args);
            case "arena":
                return handleArenaCommand(sender, args);
            case "reload":
                return handleReload(sender);
            default:
                sender.sendMessage(ChatColor.RED + "Unknown command. Use /ckd for help.");
                return true;
        }
    }
    
    private void sendHelpMessage(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== CustomKitDuels Commands ===");
        sender.sendMessage(ChatColor.YELLOW + "/ckd createkit <name> - Create a new kit");
        sender.sendMessage(ChatColor.YELLOW + "/ckd editkit <name> - Edit an existing kit");
        sender.sendMessage(ChatColor.YELLOW + "/ckd deletekit <name> - Delete a kit");
        sender.sendMessage(ChatColor.YELLOW + "/ckd listkits - List your kits");
        sender.sendMessage(ChatColor.YELLOW + "/ckd duel <player> <kit> - Challenge a player (opens rounds selector)");
        sender.sendMessage(ChatColor.YELLOW + "/ckd accept - Accept a duel request");
        sender.sendMessage(ChatColor.YELLOW + "/ckd editcategory <category> - Edit item category");
        if (sender.hasPermission("customkitduels.admin")) {
            sender.sendMessage(ChatColor.AQUA + "Admin Commands:");
            sender.sendMessage(ChatColor.YELLOW + "/ckd arena create <name> - Create new arena");
            sender.sendMessage(ChatColor.YELLOW + "/ckd arena editor <name> - Open arena editor GUI");
            sender.sendMessage(ChatColor.YELLOW + "/ckd arena list - List all arenas");
            sender.sendMessage(ChatColor.YELLOW + "/ckd arena delete <name> - Delete an arena");
            sender.sendMessage(ChatColor.YELLOW + "/ckd reload - Reload config");
        }
    }
    
    private boolean handleCreateKit(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can create kits.");
            return true;
        }
        
        if (!sender.hasPermission("customkitduels.use")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /ckd createkit <name>");
            return true;
        }
        
        Player player = (Player) sender;
        String kitName = args[1];
        
        if (plugin.getKitManager().hasKit(player.getUniqueId(), kitName)) {
            sender.sendMessage(ChatColor.RED + "You already have a kit with that name.");
            return true;
        }
        
        new KitEditorGUI(plugin, player, kitName).open();
        return true;
    }
    
    private boolean handleEditKit(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can edit kits.");
            return true;
        }
        
        if (!sender.hasPermission("customkitduels.use")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /ckd editkit <name>");
            return true;
        }
        
        Player player = (Player) sender;
        String kitName = args[1];
        
        Kit kit = plugin.getKitManager().getKit(player.getUniqueId(), kitName);
        if (kit == null) {
            sender.sendMessage(ChatColor.RED + "You don't have a kit with that name.");
            return true;
        }
        
        new KitEditorGUI(plugin, player, kitName).open();
        return true;
    }
    
    private boolean handleDeleteKit(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can delete kits.");
            return true;
        }
        
        if (!sender.hasPermission("customkitduels.use")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /ckd deletekit <name>");
            return true;
        }
        
        Player player = (Player) sender;
        String kitName = args[1];
        
        if (plugin.getKitManager().deleteKit(player.getUniqueId(), kitName)) {
            sender.sendMessage(ChatColor.GREEN + "Kit '" + kitName + "' deleted successfully.");
        } else {
            sender.sendMessage(ChatColor.RED + "You don't have a kit with that name.");
        }
        return true;
    }
    
    private boolean handleListKits(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can list kits.");
            return true;
        }
        
        if (!sender.hasPermission("customkitduels.use")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }
        
        Player player = (Player) sender;
        List<Kit> kits = plugin.getKitManager().getPlayerKits(player.getUniqueId());
        
        if (kits.isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "You don't have any kits.");
            return true;
        }
        
        sender.sendMessage(ChatColor.GOLD + "Your kits:");
        for (Kit kit : kits) {
            sender.sendMessage(ChatColor.YELLOW + "- " + kit.getName());
        }
        return true;
    }
    
    private boolean handleDuel(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can duel.");
            return true;
        }
        
        if (!sender.hasPermission("customkitduels.use")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }
        
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /ckd duel <player> <kit>");
            return true;
        }
        
        Player player = (Player) sender;
        String targetName = args[1];
        String kitName = args[2];
        
        Player target = plugin.getServer().getPlayer(targetName);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found.");
            return true;
        }
        
        if (target.equals(player)) {
            sender.sendMessage(ChatColor.RED + "You can't duel yourself.");
            return true;
        }
        
        Kit kit = plugin.getKitManager().getKit(player.getUniqueId(), kitName);
        if (kit == null) {
            sender.sendMessage(ChatColor.RED + "You don't have a kit with that name.");
            return true;
        }
        
        // Open rounds selector GUI instead of sending duel request directly
        new RoundsSelectorGUI(plugin, player, target, kit).open();
        return true;
    }
    
    private boolean handleAccept(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can accept duels.");
            return true;
        }
        
        if (!sender.hasPermission("customkitduels.use")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }
        
        Player player = (Player) sender;
        plugin.getDuelManager().acceptRoundsDuel(player);
        return true;
    }
    
    private boolean handleEditCategory(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can edit categories.");
            return true;
        }
        
        if (!sender.hasPermission("customkitduels.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /ckd editcategory <category_name>");
            return true;
        }
        
        Player player = (Player) sender;
        String categoryName = args[1];
        
        new CategoryEditorGUI(plugin, player, categoryName).open();
        return true;
    }
    
    private boolean handleArenaCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("customkitduels.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /ckd arena <create|editor|list|delete> [name]");
            return true;
        }
        
        String arenaSubCommand = args[1].toLowerCase();
        
        switch (arenaSubCommand) {
            case "create":
                return handleArenaCreate(sender, args);
            case "editor":
                return handleArenaEditor(sender, args);
            case "list":
                return handleArenaList(sender);
            case "delete":
                return handleArenaDelete(sender, args);
            default:
                sender.sendMessage(ChatColor.RED + "Unknown arena command. Use: create, editor, list, or delete");
                return true;
        }
    }
    
    private boolean handleArenaCreate(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /ckd arena create <name>");
            return true;
        }
        
        String arenaName = args[2];
        
        if (plugin.getArenaManager().hasArena(arenaName)) {
            sender.sendMessage(ChatColor.RED + "An arena with that name already exists!");
            return true;
        }
        
        plugin.getArenaManager().createArena(arenaName);
        sender.sendMessage(ChatColor.GREEN + "Arena '" + arenaName + "' created successfully!");
        sender.sendMessage(ChatColor.YELLOW + "Use /ckd arena editor " + arenaName + " to configure it.");
        return true;
    }
    
    private boolean handleArenaEditor(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use the arena editor.");
            return true;
        }
        
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /ckd arena editor <name>");
            return true;
        }
        
        Player player = (Player) sender;
        String arenaName = args[2];
        
        Arena arena = plugin.getArenaManager().getArena(arenaName);
        if (arena == null) {
            sender.sendMessage(ChatColor.RED + "Arena '" + arenaName + "' not found!");
            return true;
        }
        
        new ArenaEditorGUI(plugin, player, arena).open();
        return true;
    }
    
    private boolean handleArenaList(CommandSender sender) {
        List<String> allArenas = plugin.getArenaManager().getAllArenas();
        List<String> availableArenas = plugin.getArenaManager().getAvailableArenas();
        
        if (allArenas.isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "No arenas exist. Create one with /ckd arena create <name>");
            return true;
        }
        
        sender.sendMessage(ChatColor.GOLD + "=== Arenas (" + allArenas.size() + " total, " + availableArenas.size() + " available) ===");
        
        for (String arenaName : allArenas) {
            Arena arena = plugin.getArenaManager().getArena(arenaName);
            String status = arena.isComplete() ? ChatColor.GREEN + "✓ Ready" : ChatColor.RED + "✗ Incomplete";
            String regen = arena.hasRegeneration() ? ChatColor.AQUA + " [Regen]" : "";
            sender.sendMessage(ChatColor.YELLOW + "- " + arenaName + " " + status + regen);
        }
        
        return true;
    }
    
    private boolean handleArenaDelete(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /ckd arena delete <name>");
            return true;
        }
        
        String arenaName = args[2];
        
        if (!plugin.getArenaManager().hasArena(arenaName)) {
            sender.sendMessage(ChatColor.RED + "Arena '" + arenaName + "' not found!");
            return true;
        }
        
        if (plugin.getArenaManager().deleteArena(arenaName)) {
            sender.sendMessage(ChatColor.GREEN + "Arena '" + arenaName + "' deleted successfully!");
        } else {
            sender.sendMessage(ChatColor.RED + "Failed to delete arena '" + arenaName + "'!");
        }
        
        return true;
    }
    
    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("customkitduels.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }
        
        plugin.reloadPluginConfig();
        sender.sendMessage(ChatColor.GREEN + "Configuration reloaded.");
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            List<String> commands = Arrays.asList("createkit", "editkit", "deletekit", "listkits", "duel", "accept", "editcategory");
            if (sender.hasPermission("customkitduels.admin")) {
                commands = new ArrayList<>(commands);
                commands.addAll(Arrays.asList("arena", "reload"));
            }
            
            return commands.stream()
                    .filter(cmd -> cmd.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("duel")) {
                return plugin.getServer().getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            } else if (args[0].equalsIgnoreCase("editkit") || args[0].equalsIgnoreCase("deletekit")) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    List<Kit> kits = plugin.getKitManager().getPlayerKits(player.getUniqueId());
                    return kits.stream()
                            .map(Kit::getName)
                            .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                            .collect(Collectors.toList());
                }
            } else if (args[0].equalsIgnoreCase("editcategory")) {
                return Arrays.asList("WEAPONS", "ARMOR", "BLOCKS", "FOOD", "POTIONS", "TOOLS", "UTILITY", "MISC").stream()
                        .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            } else if (args[0].equalsIgnoreCase("arena")) {
                return Arrays.asList("create", "editor", "list", "delete").stream()
                        .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("duel")) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    List<Kit> kits = plugin.getKitManager().getPlayerKits(player.getUniqueId());
                    return kits.stream()
                            .map(Kit::getName)
                            .filter(name -> name.toLowerCase().startsWith(args[2].toLowerCase()))
                            .collect(Collectors.toList());
                }
            } else if (args[0].equalsIgnoreCase("arena") && 
                      (args[1].equalsIgnoreCase("editor") || args[1].equalsIgnoreCase("delete"))) {
                return plugin.getArenaManager().getAllArenas().stream()
                        .filter(name -> name.toLowerCase().startsWith(args[2].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }
        
        return completions;
    }
}