package com.yourname.customkitduels.commands;

import com.yourname.customkitduels.CustomKitDuels;
import com.yourname.customkitduels.data.Arena;
import com.yourname.customkitduels.data.Kit;
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
            case "setarena":
                return handleSetArena(sender, args);
            case "setpos1":
                return handleSetPos1(sender, args);
            case "setpos2":
                return handleSetPos2(sender, args);
            case "setspawn1":
                return handleSetSpawn1(sender, args);
            case "setspawn2":
                return handleSetSpawn2(sender, args);
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
        if (sender.hasPermission("customkitduels.admin")) {
            sender.sendMessage(ChatColor.AQUA + "Admin Commands:");
            sender.sendMessage(ChatColor.YELLOW + "/ckd setarena <name> - Create arena");
            sender.sendMessage(ChatColor.YELLOW + "/ckd setpos1/setpos2 <arena> - Set arena bounds");
            sender.sendMessage(ChatColor.YELLOW + "/ckd setspawn1/setspawn2 <arena> - Set spawn points");
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
    
    private boolean handleSetArena(CommandSender sender, String[] args) {
        if (!sender.hasPermission("customkitduels.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /ckd setarena <name>");
            return true;
        }
        
        String arenaName = args[1];
        plugin.getArenaManager().createArena(arenaName);
        sender.sendMessage(ChatColor.GREEN + "Arena '" + arenaName + "' created. Now set positions and spawn points.");
        return true;
    }
    
    private boolean handleSetPos1(CommandSender sender, String[] args) {
        if (!sender.hasPermission("customkitduels.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }
        
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can set positions.");
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /ckd setpos1 <arena>");
            return true;
        }
        
        Player player = (Player) sender;
        String arenaName = args[1];
        
        Arena arena = plugin.getArenaManager().getArena(arenaName);
        if (arena == null) {
            sender.sendMessage(ChatColor.RED + "Arena not found.");
            return true;
        }
        
        arena.setPos1(player.getLocation());
        plugin.getArenaManager().saveArena(arena);
        sender.sendMessage(ChatColor.GREEN + "Position 1 set for arena '" + arenaName + "'.");
        return true;
    }
    
    private boolean handleSetPos2(CommandSender sender, String[] args) {
        if (!sender.hasPermission("customkitduels.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }
        
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can set positions.");
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /ckd setpos2 <arena>");
            return true;
        }
        
        Player player = (Player) sender;
        String arenaName = args[1];
        
        Arena arena = plugin.getArenaManager().getArena(arenaName);
        if (arena == null) {
            sender.sendMessage(ChatColor.RED + "Arena not found.");
            return true;
        }
        
        arena.setPos2(player.getLocation());
        plugin.getArenaManager().saveArena(arena);
        sender.sendMessage(ChatColor.GREEN + "Position 2 set for arena '" + arenaName + "'.");
        return true;
    }
    
    private boolean handleSetSpawn1(CommandSender sender, String[] args) {
        if (!sender.hasPermission("customkitduels.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }
        
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can set spawn points.");
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /ckd setspawn1 <arena>");
            return true;
        }
        
        Player player = (Player) sender;
        String arenaName = args[1];
        
        Arena arena = plugin.getArenaManager().getArena(arenaName);
        if (arena == null) {
            sender.sendMessage(ChatColor.RED + "Arena not found.");
            return true;
        }
        
        arena.setSpawn1(player.getLocation());
        plugin.getArenaManager().saveArena(arena);
        sender.sendMessage(ChatColor.GREEN + "Spawn point 1 set for arena '" + arenaName + "'.");
        return true;
    }
    
    private boolean handleSetSpawn2(CommandSender sender, String[] args) {
        if (!sender.hasPermission("customkitduels.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }
        
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can set spawn points.");
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /ckd setspawn2 <arena>");
            return true;
        }
        
        Player player = (Player) sender;
        String arenaName = args[1];
        
        Arena arena = plugin.getArenaManager().getArena(arenaName);
        if (arena == null) {
            sender.sendMessage(ChatColor.RED + "Arena not found.");
            return true;
        }
        
        arena.setSpawn2(player.getLocation());
        plugin.getArenaManager().saveArena(arena);
        sender.sendMessage(ChatColor.GREEN + "Spawn point 2 set for arena '" + arenaName + "'.");
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
            List<String> commands = Arrays.asList("createkit", "editkit", "deletekit", "listkits", "duel", "accept");
            if (sender.hasPermission("customkitduels.admin")) {
                commands = new ArrayList<>(commands);
                commands.addAll(Arrays.asList("setarena", "setpos1", "setpos2", "setspawn1", "setspawn2", "reload"));
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
            } else if (args[0].equalsIgnoreCase("setpos1") || args[0].equalsIgnoreCase("setpos2") ||
                       args[0].equalsIgnoreCase("setspawn1") || args[0].equalsIgnoreCase("setspawn2")) {
                return plugin.getArenaManager().getAvailableArenas().stream()
                        .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("duel")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                List<Kit> kits = plugin.getKitManager().getPlayerKits(player.getUniqueId());
                return kits.stream()
                        .map(Kit::getName)
                        .filter(name -> name.toLowerCase().startsWith(args[2].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }
        
        return completions;
    }
}