# CustomKitDuels

A comprehensive Minecraft Spigot plugin for creating custom kit duels on your server.

## Features

### Player Features
- **Custom Kit Creation**: Players can create and edit their own custom kits via intuitive GUI interfaces
- **Kit Management**: Players can delete, list, and manage their personal kits
- **Duel System**: Send duel requests to other players using your custom kits
- **Duel Acceptance**: Accept incoming duel requests with a simple command

### Admin Features
- **Arena Management**: Set up duel arenas with defined boundaries and spawn points
- **Configuration**: Reload plugin configuration without server restart
- **Permission System**: Control who can use which features

### Technical Features
- **Data Persistence**: Kits are stored per player in YAML format
- **Graceful Handling**: Proper cleanup when players disconnect or die during duels
- **Modern API**: Built for Minecraft 1.21.4 using modern Spigot practices
- **Safety Features**: Prevents teleportation and certain commands during duels

## Commands

### Player Commands
- `/ckd createkit <name>` - Opens GUI to save current inventory as a kit
- `/ckd editkit <name>` - Opens GUI to modify a saved kit
- `/ckd deletekit <name>` - Deletes a player's kit
- `/ckd listkits` - Lists your available kits
- `/ckd duel <player> <kit>` - Sends a duel request to another player
- `/ckd accept` - Accepts the latest duel request

### Admin Commands
- `/ckd setarena <name>` - Creates a new arena
- `/ckd setpos1 <arena>` - Sets the first corner of an arena
- `/ckd setpos2 <arena>` - Sets the second corner of an arena
- `/ckd setspawn1 <arena>` - Sets the first spawn point in an arena
- `/ckd setspawn2 <arena>` - Sets the second spawn point in an arena
- `/ckd reload` - Reloads plugin configuration

## Permissions

- `customkitduels.use` - Allows basic use of the plugin (default: true)
- `customkitduels.admin` - Allows admin commands (default: op)

## Installation

1. Download the plugin JAR file
2. Place it in your server's `plugins` folder
3. Restart your server
4. Configure arenas using the admin commands
5. Players can start creating kits and dueling!

## Configuration

The plugin generates a `config.yml` file with customizable settings:

- Maximum kits per player
- Duel request timeout
- Blocked commands during duels
- Custom messages
- Arena configurations

## Arena Setup

1. Use `/ckd setarena <name>` to create a new arena
2. Set the arena boundaries with `/ckd setpos1 <arena>` and `/ckd setpos2 <arena>`
3. Set spawn points with `/ckd setspawn1 <arena>` and `/ckd setspawn2 <arena>`
4. The arena is now ready for duels!

## How Duels Work

1. Players create custom kits using `/ckd createkit <name>`
2. Challenge another player with `/ckd duel <player> <kit>`
3. The challenged player accepts with `/ckd accept`
4. Both players are teleported to a random available arena
5. Players receive their selected kit and the duel begins
6. When one player dies or quits, the duel ends
7. Both players are restored and teleported back to their original locations

## Support

For issues, suggestions, or contributions, please visit the plugin's repository or contact the developer.

## Version

Current version: 1.0.0
Target Minecraft version: 1.21.4