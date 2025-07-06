# CustomKitDuels

A comprehensive Minecraft Spigot plugin for creating custom kit duels with advanced features, beautiful GUIs, and modern text formatting.

## 🌟 Enhanced Features

### 🎮 Player Features
- **Custom Kit Creation**: Create unlimited custom kits with intuitive GUI
- **Kit Settings**: Configure kit health (1-30 hearts) and natural regeneration
- **Bulk Mode**: Quickly fill multiple slots with the same item
- **Rounds Duels**: First-to-X rounds system with live scoreboard
- **Item Modification**: Enchant items, change stack sizes, modify potions
- **Arena System**: Multiple arenas with automatic regeneration support
- **Health Indicators**: Real-time health display with multiple methods

### ⚔️ Enhanced Duel System
- **Rounds-based Duels**: Choose from 1-10 rounds to win
- **Live Scoreboard**: Real-time duel statistics with hex colors and gradients
- **Countdown System**: 5-second countdown with note block sounds
- **Inventory Management**: 2-second preparation time between rounds
- **Arena Regeneration**: Automatic arena restoration using FAWE
- **Health Display**: Multiple methods for showing player health

### 🛠️ Admin Features
- **Arena Management**: Create and configure arenas with GUI editor
- **Spawn System**: Set global spawn point for post-duel teleportation
- **Category Editor**: Customize item categories for kit creation
- **Configuration**: Extensive customization options with modern formatting
- **Permissions**: Granular permission system

## 📚 Libraries Used

### Core Libraries
- **Adventure API (4.14.0)**: Modern text components and hex color support
- **Adventure Platform Bukkit (4.3.2)**: Bukkit integration for Adventure
- **Adventure Text MiniMessage (4.14.0)**: Advanced text parsing and formatting
- **FastBoard (2.0.3)**: High-performance scoreboards with better update handling

### Optional Dependencies
- **HolographicDisplays (3.0.0)**: Enhanced floating health displays (soft dependency)
- **ProtocolLib (5.1.0)**: Packet-based health displays (soft dependency)

### Purpose of Each Library

1. **Adventure API**: 
   - Provides modern text components
   - Full hex color support (#RRGGBB)
   - Gradient and rainbow text effects
   - Better Unicode support

2. **FastBoard**:
   - High-performance scoreboard implementation
   - Automatic line length handling
   - Better update performance than vanilla Bukkit scoreboards
   - Flicker-free updates

3. **MiniMessage**:
   - Modern text formatting syntax
   - Supports gradients: `<gradient:#start:#end>text</gradient>`
   - Supports hex colors: `<#RRGGBB>` or `&#RRGGBB`
   - Named colors: `<red>`, `<green>`, etc.

4. **HolographicDisplays** (Optional):
   - Creates floating text above players
   - Better visual health indicators
   - More immersive experience

5. **ProtocolLib** (Optional):
   - Packet manipulation for advanced features
   - Custom health display methods
   - Enhanced compatibility

## 📋 Commands

### Player Commands
```
/ckd createkit <name>     - Create a new custom kit
/ckd editkit <name>       - Edit an existing kit
/ckd deletekit <name>     - Delete a kit
/ckd listkits             - List your kits
/ckd duel <player>        - Challenge a player to a duel (opens kit selector)
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
4. **Optional**: Install HolographicDisplays and/or ProtocolLib for enhanced features
5. **Configure** arenas using admin commands
6. **Set spawn** with `/ckd setspawn`
7. Players can start creating kits and dueling!

## ⚙️ Enhanced Configuration

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

### Enhanced Scoreboard (`scoreboard.yml`)
Now supports MiniMessage format with hex colors and gradients:
```yaml
title: "<gradient:#00FF98:#C3F6E2><bold>PakMC</bold></gradient>"
lines:
  - " "
  - " <#00FF98><bold>DUEL</bold> <gray>(FT<#C3F6E2><rounds></gray>)"
  - " <#C3F6E2>│ Duration: <#00FF98><duration>"
  - " <#C3F6E2>│ Round: <#00FF98><current_round>"
  - " "
  - " <#00FF98><bold>SCORE</bold>"
  - " <#C3F6E2>│ <green><player_score></green> - <red><opponent_score></red>"
  - " <#C3F6E2>│ <gray><player_name> vs <opponent_name></gray>"
  - " "
  - "    <#C3F6E2>pakmc.xyz"
```

**Enhanced Formatting Options:**
- **Hex Colors**: `<#RRGGBB>` or `&#RRGGBB`
- **Named Colors**: `<red>`, `<green>`, `<blue>`, etc.
- **Gradients**: `<gradient:#start:#end>text</gradient>`
- **Rainbow**: `<rainbow>text</rainbow>`
- **Bold/Italic**: `<bold>`, `<italic>`, `<underlined>`

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

### Enhanced Kit Settings
- **Hearts**: Set player health (1-30 hearts) - now supports more than 20!
- **Natural Regen**: Enable/disable health regeneration from saturation
- **Health Indicators**: Enable/disable visual health display during duels

### Bulk Mode
- **Activate**: Shift-click any slot or use the bulk button
- **Quick Fill**: Click multiple slots to place the same item
- **Exit**: Right-click the bulk mode button

## 🎮 How Duels Work

### Starting a Duel
1. **Challenge**: `/ckd duel <player>`
2. **Select Kit**: Choose from your available kits
3. **Select Rounds**: Choose 1-10 rounds to win
4. **Accept**: Target player uses `/ckd accept`
5. **Countdown**: 4-second preparation countdown
6. **Fight**: Duel begins!

### Enhanced Rounds System
- **Win Condition**: First to reach target rounds wins
- **Round End**: Player death ends the round
- **Preparation**: 2-second break + 5-second countdown
- **Live Scoreboard**: Real-time updates with beautiful formatting
- **Health Display**: Multiple methods for showing player health
- **Arena Regen**: Automatic restoration between rounds

### Post-Duel
- **Restoration**: Players are restored to original state
- **Teleport**: 2-second delay, then teleport to spawn
- **Cleanup**: All displays and effects are properly cleaned up

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

### Optional (Soft Dependencies)
- **FastAsyncWorldEdit (FAWE)**: For arena regeneration
- **WorldEdit**: Alternative to FAWE (limited features)
- **HolographicDisplays**: For enhanced floating health displays
- **ProtocolLib**: For packet-based health displays

## 📁 File Structure

```
plugins/CustomKitDuels/
├── config.yml              # Main configuration
├── scoreboard.yml           # Enhanced scoreboard with MiniMessage
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

## 🎨 Enhanced Customization

### Scoreboard Formatting
The new scoreboard system supports advanced formatting:

```yaml
# Gradient title
title: "<gradient:#00FF98:#C3F6E2><bold>PakMC</bold></gradient>"

# Hex colors and formatting
lines:
  - "<#FF0000>Red text"
  - "<gradient:#FF0000:#00FF00>Rainbow gradient</gradient>"
  - "<rainbow>Rainbow text</rainbow>"
  - "<bold><italic>Bold and italic</italic></bold>"
```

### Health Display Options
The plugin now supports multiple health display methods:

1. **Custom Name**: Shows health below player name (most compatible)
2. **HolographicDisplays**: Floating text above players (requires plugin)
3. **ProtocolLib**: Packet-based displays (requires plugin)

## 🐛 Troubleshooting

### Common Issues

**Scoreboard not showing properly**
- Ensure you're using valid MiniMessage syntax
- Check for syntax errors in `scoreboard.yml`
- Restart server after configuration changes

**Health indicators not working**
- Check if health indicators are enabled in kit settings
- Install HolographicDisplays or ProtocolLib for enhanced features
- Verify console for any error messages

**Colors not displaying**
- Ensure your client supports the color format
- Use legacy color codes (&a, &b) for older clients
- Check that Adventure API is properly loaded

**Arena regeneration not working**
- Install FastAsyncWorldEdit (FAWE)
- Ensure arena positions are set
- Check console for errors

### Performance Tips
- FastBoard provides better performance than vanilla scoreboards
- Health display updates every 0.5 seconds for optimal performance
- Use gradients sparingly for better performance
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
**Enhanced with**: Adventure API, FastBoard, MiniMessage