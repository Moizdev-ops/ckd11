# CustomKitDuels

A comprehensive Minecraft Spigot plugin for creating custom kit duels with advanced features and beautiful GUIs.

## 🌟 Features

### 🎮 Player Features
- **Custom Kit Creation**: Create unlimited custom kits with intuitive GUI
- **Kit Settings**: Configure kit health (1-20 hearts) and natural regeneration
- **Bulk Mode**: Quickly fill multiple slots with the same item
- **Rounds Duels**: First-to-X rounds system with live scoreboard
- **Item Modification**: Enchant items, change stack sizes, modify potions
- **Arena System**: Multiple arenas with automatic regeneration support

### ⚔️ Duel System
- **Rounds-based Duels**: Choose from 1-10 rounds to win
- **Live Scoreboard**: Real-time duel statistics and progress
- **Countdown System**: 5-second countdown with note block sounds
- **Inventory Management**: 2-second preparation time between rounds
- **Arena Regeneration**: Automatic arena restoration using FAWE

### 🛠️ Admin Features
- **Arena Management**: Create and configure arenas with GUI editor
- **Spawn System**: Set global spawn point for post-duel teleportation
- **Category Editor**: Customize item categories for kit creation
- **Configuration**: Extensive customization options
- **Permissions**: Granular permission system

## 📋 Commands

### Player Commands
```
/ckd createkit <name>     - Create a new custom kit
/ckd editkit <name>       - Edit an existing kit
/ckd deletekit <name>     - Delete a kit
/ckd listkits             - List your kits
/ckd duel <player> <kit>  - Challenge a player to a duel
/ckd accept               - Accept a duel request
```

### Admin Commands
```
/ckd arena create <name>     - Create a new arena
/ckd arena editor            - Open arena selection GUI
/ckd arena list              - List all arenas
/ckd arena delete <name>     - Delete an arena
/ckd setspawn                - Set the global spawn point
/ckd editcategory <category> - Edit item categories
/ckd reload                  - Reload configuration
```

## 🔧 Installation

1. **Download** the plugin JAR file
2. **Place** it in your server's `plugins` folder
3. **Restart** your server
4. **Configure** arenas using admin commands
5. **Set spawn** with `/ckd setspawn`
6. Players can start creating kits and dueling!

## ⚙️ Configuration

### Main Config (`config.yml`)
```yaml
settings:
  max-kits-per-player: 10
  duel-request-timeout: 30
  blocked-commands:
    - "tp"
    - "teleport"
    - "home"
```

### Scoreboard (`scoreboard.yml`)
Customize the duel scoreboard with hex colors and placeholders:
```yaml
title: "&#00FF98&lPakMC"
lines:
  - " &#00FF98&lDUEL &7(&#C3F6E2FT<rounds>&7)"
  - " &#C3F6E2| Duration: &#00FF98<duration>"
  - " &#C3F6E2| Round: &#00FF98<current_round>"
```

**Available Placeholders:**
- `<rounds>` - Target rounds to win
- `<duration>` - Duel duration (MM:SS)
- `<current_round>` - Current round number
- `<player_score>` - Player's wins
- `<opponent_score>` - Opponent's wins
- `<player_name>` - Player's name
- `<opponent_name>` - Opponent's name

## 🏟️ Arena Setup

1. **Create Arena**: `/ckd arena create <name>`
2. **Open Editor**: `/ckd arena editor`
3. **Select Arena** from the GUI
4. **Set Positions**: Use Shift+Left-click in air to set corners and spawn points
5. **Enable Regeneration**: Toggle arena regeneration (requires FAWE)
6. **Save**: Click the save button

### Arena Requirements
- **Position 1 & 2**: Define arena boundaries
- **Spawn Point 1 & 2**: Player spawn locations
- **FAWE Plugin**: Required for arena regeneration

## 🎯 Kit Creation Guide

### Basic Kit Creation
1. Use `/ckd createkit <name>`
2. **Add Items**: Left-click empty slots to browse categories
3. **Modify Items**: Right-click items to enchant/modify
4. **Bulk Mode**: Shift-click for quick item placement
5. **Kit Settings**: Configure health and regeneration
6. **Save**: Click the emerald to save your kit

### Kit Settings
- **Hearts**: Set player health (1-20 hearts)
- **Natural Regen**: Enable/disable health regeneration from saturation

### Bulk Mode
- **Activate**: Shift-click any slot or use the bulk button
- **Quick Fill**: Click multiple slots to place the same item
- **Exit**: Right-click the bulk mode button

## 🎮 How Duels Work

### Starting a Duel
1. **Challenge**: `/ckd duel <player> <kit>`
2. **Select Rounds**: Choose 1-10 rounds to win
3. **Accept**: Target player uses `/ckd accept`
4. **Countdown**: 4-second preparation countdown
5. **Fight**: Duel begins!

### Rounds System
- **Win Condition**: First to reach target rounds wins
- **Round End**: Player death ends the round
- **Preparation**: 2-second break + 5-second countdown
- **Scoreboard**: Live updates during the duel
- **Arena Regen**: Automatic restoration between rounds

### Post-Duel
- **Restoration**: Players are restored to original state
- **Teleport**: 2-second delay, then teleport to spawn
- **Scoreboard**: Removed after duel completion

## 🔐 Permissions

```yaml
customkitduels.use:
  description: Basic plugin usage
  default: true

customkitduels.admin:
  description: Admin commands and features
  default: op
```

## 🔌 Dependencies

### Required
- **Spigot/Paper**: 1.21.4+
- **Java**: 21+

### Optional
- **FastAsyncWorldEdit (FAWE)**: For arena regeneration
- **WorldEdit**: Alternative to FAWE (limited features)

## 📁 File Structure

```
plugins/CustomKitDuels/
├── config.yml              # Main configuration
├── scoreboard.yml           # Scoreboard customization
├── spawn.yml               # Spawn location
├── arenas/                 # Arena configurations
│   ├── arena1.yml
│   └── arena2.yml
├── categories/             # Item categories
│   ├── WEAPONS.yml
│   ├── ARMOR.yml
│   └── ...
├── kits/                   # Player kits
│   └── <uuid>.yml
├── kit-settings/           # Kit configurations
│   └── <uuid>.yml
└── schematics/             # Arena schematics (FAWE)
    ├── arena1_arena.schem
    └── arena2_arena.schem
```

## 🎨 Customization

### Item Categories
Edit categories with `/ckd editcategory <category>`:
- **WEAPONS** - Swords, axes, bows, etc.
- **ARMOR** - All armor pieces and shields
- **BLOCKS** - Building and utility blocks
- **FOOD** - Food items and consumables
- **POTIONS** - Potions and brewing items
- **TOOLS** - Pickaxes, shovels, etc.
- **UTILITY** - Ender pearls, fire charges, etc.
- **MISC** - Books, music discs, etc.

### Scoreboard Colors
Use hex colors in `scoreboard.yml`:
```yaml
title: "&#FF0000&lRed Title"
lines:
  - "&#00FF00Green text"
  - "&bAqua text with &#FFD700gold"
```

## 🐛 Troubleshooting

### Common Issues

**Arena regeneration not working**
- Install FastAsyncWorldEdit (FAWE)
- Ensure arena positions are set
- Check console for errors

**Scoreboard not showing**
- Verify `scoreboard.yml` syntax
- Check for plugin conflicts
- Restart server after changes

**Kits not saving**
- Check file permissions
- Verify disk space
- Check console for errors

**Players stuck in duel**
- Use `/ckd reload` to reset
- Check arena boundaries
- Verify spawn point is set

### Performance Tips
- Limit arena regeneration frequency
- Use smaller arena sizes
- Regular server restarts for optimal performance

## 📞 Support

For issues, suggestions, or contributions:
- Check the console for error messages
- Verify all dependencies are installed
- Ensure proper permissions are set
- Test with minimal plugins to isolate conflicts

## 📄 License

This plugin is provided as-is for educational and server use. Please respect the terms of use for any dependencies.

---

**Version**: 1.0.0  
**Minecraft**: 1.21.4  
**Java**: 21+