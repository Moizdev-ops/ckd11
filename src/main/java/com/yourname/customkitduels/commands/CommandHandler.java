package com.yourname.customkitduels.commands;

import com.yourname.customkitduels.CustomKitDuels;
import com.yourname.customkitduels.data.Arena;
import com.yourname.customkitduels.data.Kit;
import com.yourname.customkitduels.gui.ArenaEditorGUI;
import com.yourname.customkitduels.gui.ArenaListGUI;
import com.yourname.customkitduels.gui.CategoryEditorGUI;
import com.yourname.customkitduels.gui.KitEditorGUI;
import com.yourname.customkitduels.gui.KitListGUI;
import com.yourname.customkitduels.gui.KitSelectorGUI;
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
            case "create":
                return handleCreateKit(sender);
            case "edit":
                return handleEditKit(sender);
            case "delete":
                return handleDeleteKit(sender, args);
            case "list":
                return handleListKits(sender);
            case "duel":
                return handleDuel(sender, args);
            case "accept":
                return handleAccept(sender);
            case "editcategory":
                return handleEditCategory(sender, args);
            case "arena":
                return handleArenaCommand(sender, args);
            case "setspawn":
                return handleSetSpawn(sender);
            case "reload":
                return handleReload(sender);
            default:
                sender.sendMessage(ChatColor.RED + "unknown command. use /customkit for help.");
                return true;
        }
    }
    
    private void sendHelpMessage(CommandSender sender) {
        sender.sendMessage(ChatColor.RED + "=== customkit commands ===");
        sender.sendMessage(ChatColor.YELLOW + "/customkit create - create a new kit");
        sender.sendMessage(ChatColor.YELLOW + "/customkit edit - edit your kits");
        sender.sendMessage(ChatColor.YELLOW + "/customkit delete <name> - delete a kit");
        sender.sendMessage(ChatColor.YELLOW + "/customkit list - list your kits");
        sender.sendMessage(ChatColor.YELLOW + "/customkit duel <player> - challenge a player");
        sender.sendMessage(ChatColor.YELLOW + "/customkit accept - accept a duel request");
        if (sender.hasPermission("customkitduels.admin")) {
            sender.sendMessage(ChatColor.AQUA + "admin commands:");
            sender.sendMessage(ChatColor.YELLOW + "/customkit arena create <name> - create new arena");
            sender.sendMessage(ChatColor.YELLOW + "/customkit arena editor - open arena editor gui");
            sender.sendMessage(ChatColor.YELLOW + "/customkit arena list - list all arenas");
            sender.sendMessage(ChatColor.YELLOW + "/customkit arena delete <name> - delete an arena");
            sender.sendMessage(ChatColor.YELLOW + "/customkit setspawn - set global spawn point");
            sender.sendMessage(ChatColor.YELLOW + "/customkit editcategory <category> - edit item category");
            sender.sendMessage(ChatColor.YELLOW + "/customkit reload - reload config");
        }
    }
    
    private boolean handleCreateKit(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "only players can create kits.");
            return true;
        }
        
        if (!sender.hasPermission("customkitduels.use")) {
            sender.sendMessage(ChatColor.RED + "you don't have permission to use this command.");
            return true;
        }
        
        Player player = (Player) sender;
        
        // Generate next kit name
        List<Kit> playerKits = plugin.getKitManager().getPlayerKits(player.getUniqueId());
        String kitName = "kit " + (playerKits.size() + 1);
        
        // Check if kit already exists (shouldn't happen but just in case)
        int counter = playerKits.size() + 1;
        while (plugin.getKitManager().hasKit(player.getUniqueId(), kitName)) {
            counter++;
            kitName = "kit " + counter;
        }
        
        new KitEditorGUI(plugin, player, kitName, true).open();
        return true;
    }
    
    private boolean handleEditKit(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "only players can edit kits.");
            return true;
        }
        
        if (!sender.hasPermission("customkitduels.use")) {
            sender.sendMessage(ChatColor.RED + "you don't have permission to use this command.");
            return true;
        }
        
        Player player = (Player) sender;
        
        // Open kit list GUI for editing
        new KitListGUI(plugin, player).open();
        return true;
    }
    
    private boolean handleDeleteKit(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "only players can delete kits.");
            return true;
        }
        
        if (!sender.hasPermission("customkitduels.use")) {
            sender.sendMessage(ChatColor.RED + "you don't have permission to use this command.");
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "usage: /customkit delete <name>");
            return true;
        }
        
        Player player = (Player) sender;
        String kitName = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        
        if (plugin.getKitManager().deleteKit(player.getUniqueId(), kitName)) {
            sender.sendMessage(ChatColor.GREEN + "kit '" + kitName + "' deleted successfully.");
        } else {
            sender.sendMessage(ChatColor.RED + "you don't have a kit with that name.");
        }
        return true;
    }
    
    private boolean handleListKits(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "only players can list kits.");
            return true;
        }
        
        if (!sender.hasPermission("customkitduels.use")) {
            sender.sendMessage(ChatColor.RED + "you don't have permission to use this command.");
            return true;
        }
        
        Player player = (Player) sender;
        List<Kit> kits = plugin.getKitManager().getPlayerKits(player.getUniqueId());
        
        if (kits.isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "you don't have any kits.");
            return true;
        }
        
        sender.sendMessage(ChatColor.GOLD + "your kits:");
        for (Kit kit : kits) {
            sender.sendMessage(ChatColor.YELLOW + "- " + kit.getName());
        }
        return true;
    }
    
    private boolean handleDuel(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "only players can duel.");
            return true;
        }
        
        if (!sender.hasPermission("customkitduels.use")) {
            sender.sendMessage(ChatColor.RED + "you don't have permission to use this command.");
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "usage: /customkit duel <player>");
            return true;
        }
        
        Player player = (Player) sender;
        String targetName = args[1];
        
        Player target = plugin.getServer().getPlayer(targetName);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "player not found.");
            return true;
        }
        
        if (target.equals(player)) {
            sender.sendMessage(ChatColor.RED + "you can't duel yourself.");
            return true;
        }
        
        // Check if player has any kits
        List<Kit> kits = plugin.getKitManager().getPlayerKits(player.getUniqueId());
        if (kits.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "you don't have any kits! create one with /customkit create");
            return true;
        }
        
        // Open kit selector GUI
        new KitSelectorGUI(plugin, player, target).open();
        return true;
    }
    
    private boolean handleAccept(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "only players can accept duels.");
            return true;
        }
        
        if (!sender.hasPermission("customkitduels.use")) {
            sender.sendMessage(ChatColor.RED + "you don't have permission to use this command.");
            return true;
        }
        
        Player player = (Player) sender;
        plugin.getDuelManager().acceptRoundsDuel(player);
        return true;
    }
    
    private boolean handleEditCategory(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "only players can edit categories.");
            return true;
        }
        
        if (!sender.hasPermission("customkitduels.admin")) {
            sender.sendMessage(ChatColor.RED + "you don't have permission to use this command.");
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "usage: /customkit editcategory <category_name>");
            return true;
        }
        
        Player player = (Player) sender;
        String categoryName = args[1];
        
        new CategoryEditorGUI(plugin, player, categoryName).open();
        return true;
    }
    
    private boolean handleArenaCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("customkitduels.admin")) {
            sender.sendMessage(ChatColor.RED + "you don't have permission to use this command.");
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "usage: /customkit arena <create|editor|list|delete> [name]");
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
                sender.sendMessage(ChatColor.RED + "unknown arena command. use: create, editor, list, or delete");
                return true;
        }
    }
    
    private boolean handleArenaCreate(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "usage: /customkit arena create <name>");
            return true;
        }
        
        String arenaName = args[2];
        
        if (plugin.getArenaManager().hasArena(arenaName)) {
            sender.sendMessage(ChatColor.RED + "an arena with that name already exists!");
            return true;
        }
        
        plugin.getArenaManager().createArena(arenaName);
        sender.sendMessage(ChatColor.GREEN + "arena '" + arenaName + "' created successfully!");
        sender.sendMessage(ChatColor.YELLOW + "use /customkit arena editor to configure it.");
        return true;
    }
    
    private boolean handleArenaEditor(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "only players can use the arena editor.");
            return true;
        }
        
        Player player = (Player) sender;
        
        // If specific arena name provided, open that arena directly
        if (args.length >= 3) {
            String arenaName = args[2];
            Arena arena = plugin.getArenaManager().getArena(arenaName);
            if (arena == null) {
                sender.sendMessage(ChatColor.RED + "arena '" + arenaName + "' not found!");
                return true;
            }
            
            new ArenaEditorGUI(plugin, player, arena).open();
        } else {
            // Open arena list GUI
            new ArenaListGUI(plugin, player).open();
        }
        
        return true;
    }
    
    private boolean handleArenaList(CommandSender sender) {
        List<String> allArenas = plugin.getArenaManager().getAllArenas();
        List<String> availableArenas = plugin.getArenaManager().getAvailableArenas();
        
        if (allArenas.isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "no arenas exist. create one with /customkit arena create <name>");
            return true;
        }
        
        sender.sendMessage(ChatColor.GOLD + "=== arenas (" + allArenas.size() + " total, " + availableArenas.size() + " available) ===");
        
        for (String arenaName : allArenas) {
            Arena arena = plugin.getArenaManager().getArena(arenaName);
            String status = arena.isComplete() ? ChatColor.GREEN + "✓ ready" : ChatColor.RED + "✗ incomplete";
            String regen = arena.hasRegeneration() ? ChatColor.AQUA + " [regen]" : "";
            sender.sendMessage(ChatColor.YELLOW + "- " + arenaName + " " + status + regen);
        }
        
        return true;
    }
    
    private boolean handleArenaDelete(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "usage: /customkit arena delete <name>");
            return true;
        }
        
        String arenaName = args[2];
        
        if (!plugin.getArenaManager().hasArena(arenaName)) {
            sender.sendMessage(ChatColor.RED + "arena '" + arenaName + "' not found!");
            return true;
        }
        
        if (plugin.getArenaManager().deleteArena(arenaName)) {
            sender.sendMessage(ChatColor.GREEN + "arena '" + arenaName + "' deleted successfully!");
        } else {
            sender.sendMessage(ChatColor.RED + "failed to delete arena '" + arenaName + "'!");
        }
        
        return true;
    }
    
    private boolean handleSetSpawn(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "only players can set spawn.");
            return true;
        }
        
        if (!sender.hasPermission("customkitduels.admin")) {
            sender.sendMessage(ChatColor.RED + "you don't have permission to use this command.");
            return true;
        }
        
        Player player = (Player) sender;
        plugin.getSpawnManager().setSpawn(player.getLocation());
        sender.sendMessage(ChatColor.GREEN + "spawn location set to your current position!");
        return true;
    }
    
    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("customkitduels.admin")) {
            sender.sendMessage(ChatColor.RED + "you don't have permission to use this command.");
            return true;
        }
        
        plugin.reloadPluginConfig();
        sender.sendMessage(ChatColor.GREEN + "configuration reloaded.");
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            List<String> commands = Arrays.asList("create", "edit", "delete", "list", "duel", "accept", "editcategory");
            if (sender.hasPermission("customkitduels.admin")) {
                commands = new ArrayList<>(commands);
                commands.addAll(Arrays.asList("arena", "setspawn", "reload"));
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
            } else if (args[0].equalsIgnoreCase("delete")) {
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
            if (args[0].equalsIgnoreCase("arena") && 
                      (args[1].equalsIgnoreCase("editor") || args[1].equalsIgnoreCase("delete"))) {
                return plugin.getArenaManager().getAllArenas().stream()
                        .filter(name -> name.toLowerCase().startsWith(args[2].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }
        
        return completions;
    }
}