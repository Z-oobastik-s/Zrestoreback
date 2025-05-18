# ZRestoreBack

<div align="center">

![ZRestoreBack Logo](https://i.ibb.co/00HXTXg/zrestoreback.jpg)

**A comprehensive death management plugin for Minecraft servers**

*Tested on Minecraft 1.21.4 | Author: Zoobastiks*

</div>

## 📋 Table of Contents
- [Overview](#-overview)
- [Features](#-features)
- [Installation](#-installation)
- [Commands](#-commands)
- [Permissions](#-permissions)
- [Configuration](#-configuration)
  - [General Settings](#general-settings)
  - [World Settings](#world-settings)
  - [Economy Settings](#economy-settings)
  - [Message Settings](#message-settings)
  - [Sound Settings](#sound-settings)
  - [Commands Execution Settings](#commands-execution-settings)
  - [Data Saving Settings](#data-saving-settings)
- [Message Formatting](#-message-formatting)
- [Example Use Cases](#-example-use-cases)
- [Support](#-support)
- [Compatibility](#-compatibility)

## 🌟 Overview

ZRestoreBack is a powerful and customizable plugin that allows players to return to their death locations and recover lost items. It integrates with Vault for economy support, provides an intuitive command interface, and offers extensive configuration options to fit any server's needs.

## ✨ Features

- **Death Point Management**: Store multiple death locations for each player
- **Item Recovery**: Allow players to recover their lost items for a configurable fee
- **Economy Integration**: Uses Vault for all economic transactions
- **Customizable Messages**: Fully customizable messages with support for color codes, gradients, and MiniMessage format
- **Multi-World Support**: Configure which worlds the plugin should operate in
- **Sound Effects**: Configurable sounds for death, teleportation, and item recovery
- **Admin Commands**: Admin tools to view and manage other players' death points
- **Automatic Data Saving**: Configurable autosave intervals to prevent data loss
- **Command Execution**: Execute custom commands on player death

## 📥 Installation

1. Download the latest version of ZRestoreBack.jar
2. Place the JAR file in your server's `plugins/` directory
3. Install [Vault](https://www.spigotmc.org/resources/vault.34315/) and a compatible economy plugin
4. Restart your server
5. Configure the plugin in `plugins/ZRestoreBack/config.yml`

## 🔧 Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/dp` or `/deathpoints` | Show the help menu | zrestoreback.use |
| `/dp list` | Show a list of your death points | zrestoreback.use |
| `/dp tp <number>` | Teleport to a specific death point | zrestoreback.use |
| `/dp <number>` | Shorthand for teleporting to a death point | zrestoreback.use |
| `/dp items <number>` | Recover items from a specific death point | zrestoreback.use |
| `/dp help` | Display help information | zrestoreback.use |
| `/dp reload` | Reload the plugin configuration | zrestoreback.reload |
| `/dp save` | Manually save death point data | zrestoreback.save |
| `/dp check <player>` | View another player's death points | zrestoreback.check |

## 🔒 Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| zrestoreback.use | Allows using basic plugin commands | true |
| zrestoreback.admin | Grants access to all plugin features and others' death points | op |
| zrestoreback.reload | Allows reloading the plugin configuration | op |
| zrestoreback.save | Allows manually saving death data | op |
| zrestoreback.check | Allows checking other players' death points | op |

## ⚙️ Configuration

The plugin's configuration file (`config.yml`) offers extensive customization options.

### General Settings

```yaml
settings:
  # Maximum number of death points stored per player
  max_deaths_stored: 5
  
  # Maximum number of death points shown in the list
  max_deaths_shown: 5
  
  # Auto-save interval in minutes
  auto_save_interval: 10
  
  # Show save messages in console
  show_save_messages: true
```

### World Settings

```yaml
settings:
  # Worlds where the plugin is active
  enabled_worlds:
    - "world"
    - "world_nether"
    - "world_the_end"
  
  # Worlds where the plugin is disabled
  disabled_worlds:
    - "lobby"
    - "oneblock_world"
```

### Economy Settings

```yaml
economy:
  # Cost for teleporting to death location
  teleport_cost: 100.0
  
  # Cost for recovering lost items
  items_return_cost: 250.0
```

### Message Settings

```yaml
messages:
  # Header for the death points list
  header: "<gradient:#FF0000:#FFFF00>===== Death Points =====</gradient>"
  
  # Format for each death point
  # Available placeholders: %world%, %x%, %y%, %z%, %index%
  death_point: "<#00AAFF>Death #%index%: <white>%world% <gray>(%x%, %y%, %z%)</gray></white>"
  
  # Footer for the death points list
  footer: "<gradient:#FFFF00:#FF0000>=====================</gradient>"
  
  # Teleport button text
  teleport_button: "[Teleport]"
  
  # Item recovery button text
  items_button: "[Items]"
  
  # Successful teleport message
  teleport_success: "<gradient:#00FF00:#00AAFF>You have been teleported to your death point for %cost% coins.</gradient>"
  
  # Insufficient funds for teleport
  teleport_fail: "&cYou don't have enough funds to teleport.\n&cRequired: %cost% coins."
  
  # Successful item recovery message
  items_return_success: "&aYour items have been recovered for %cost% coins."
  
  # Insufficient funds for item recovery
  items_return_fail: "<#FF0000>You don't have enough funds to recover items.\n<#FF5555>Required: %cost% coins."
  
  # No death points message
  no_death_points: "&cYou have no saved death points."
  
  # Invalid death point message
  invalid_death_point: "<red>The specified death point does not exist.</red>"
```

### Sound Settings

```yaml
sounds:
  # Death sound
  death:
    enabled: true
    sound: "entity.wither.death"
  
  # Teleport sound
  teleport:
    enabled: true
    sound: "entity.enderman.teleport"
  
  # Item recovery sound
  items_return:
    enabled: true
    sound: "entity.player.levelup"
```

### Commands Execution Settings

```yaml
commands:
  # Enable/disable command execution on death
  execute_on_death: false
  
  # Commands to execute on player death
  # Available placeholders: %player%, %world%, %x%, %y%, %z%
  on_death:
    - "say %player% died in %world% at coordinates %x% %y% %z%"
```

### Data Saving Settings

The plugin automatically saves all death point data to `plugins/ZRestoreBack/death_data.yml`. You can configure the autosave interval:

```yaml
settings:
  # Auto-save interval in minutes (0 to disable)
  auto_save_interval: 10
  
  # Show save messages in console
  show_save_messages: true
```

## 🎨 Message Formatting

ZRestoreBack supports multiple message formatting options:

### Color Codes
Use the standard Minecraft color codes with `&` or `§`:
- `&a` - Light green text
- `&b` - Aqua text
- `&c` - Red text
- `&e` - Yellow text
- `&f` - White text

### MiniMessage Format
For advanced formatting, use MiniMessage syntax:
- `<red>Red Text</red>`
- `<gradient:#FF0000:#00FF00>Gradient Text</gradient>`
- `<#FF5555>Custom HEX Color</#FF5555>`
- `<bold>Bold Text</bold>`
- `<italic>Italic Text</italic>`
- `<underlined>Underlined Text</underlined>`

### Interactive Components
Create interactive buttons with:
- `<click:run_command:/dp tp 1>Click to teleport</click>`
- `<hover:show_text:Tooltip text>Hover over me</hover>`

## 🧩 Example Use Cases

### Basic Player Usage
1. Player dies in the game
2. They type `/dp` to see their death points
3. They click the [Teleport] button or use `/dp tp 1` to return to their latest death point
4. They pay the configured amount to teleport
5. They can also use `/dp items 1` to recover their lost items for an additional fee

### Admin Usage
1. Admin wants to check a player's death points
2. They use `/dp check PlayerName` to view the player's death points
3. They can teleport to the player's death point or recover their items without any fee
4. They can use `/dp reload` to reload the configuration or `/dp save` to manually save data

## 🆘 Support

For support, bug reports, or feature requests, please contact:

- **Author**: Zoobastiks
- **Telegram**: [https://t.me/Zoobastiks](https://t.me/Zoobastiks)

Support is available **ONLY** through Telegram. Please include detailed information about your issue when requesting support.

## 🔌 Compatibility

- **Tested Minecraft Version**: 1.21.4
- **Required Dependencies**: 
  - Vault
  - Any economy plugin compatible with Vault (e.g., EssentialsX, CMI, etc.)
- **Optional Dependencies**: None

While the plugin is tested on Minecraft 1.21.4, it may work on other versions as well. However, support is only guaranteed for the tested version.

## License

ZRestoreBack © 2025 by Zoobastiks. All rights reserved. 