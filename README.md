# OxeVy

<div align="center">

**!!!!WARNING THIS PROJECT IS IN BETA!!!!**

**An AI based utility client on OyVey-Ported**

Built with: ChatGPT • DeepSeek • OpenCode • Gemini • Cursor

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Version](https://img.shields.io/badge/version-1.0.1-blue.svg)](https://github.com/daneq1/oxevy)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.11+-brightgreen.svg)](https://www.minecraft.net/)
[![Fabric](https://img.shields.io/badge/Fabric-1.21.11+-orange.svg)](https://fabricmc.net/)

</div>

## Sneak Peek
![Sneak Peek](image.png)

## Preview

### OyVey-Ported
![Original UI](images/ui.png)

### OxeVy UI
![OxeVy UI](images/ui1.png)
![OxeVy UI2](images/ui2.png)

## Features

### Combat
- **KillAura** - Automatic attack with customizable CPS, cooldown, rotation sync
- **AimBot** - Auto-aim at entities
- **Criticals** - Force critical hits
- **Strafe** - Combat strafe
- **KeyPearl** - Auto-throw ender pearls
- **AutoTrap** - Trap enemies with blocks (supports offhand)
- **AutoCobweb** - Trap enemies in cobwebs

### Movement
- **Flight** - Fly mode
- **Speed** - Speed hack
- **Timer** - Modify game tick speed
- **Step** - Step up blocks
- **ReverseStep** - Reverse step down blocks

### Player
- **NoFall** - Prevent fall damage
- **AntiCobweb** - Escape cobwebs faster (Jump + Speed options)
- **FastPlace** - Faster item placement
- **Velocity** - Modify knockback
- **AutoTotem** - Auto-switch to totem
- **AutoEat** - Auto-eat when hungry
- **AirPlace** - Place blocks in air
- **FastBreak** - Break blocks faster
- **Reach** - Increase attack reach

### Render
- **ESP** - Entity ESP highlighting
- **Tracers** - Lines to entities with customizable target/source positions
- **Nametags** - Custom nametags above players
- **Fullbright** - Permanent brightness
- **BlockHighlight** - Highlight targeted block
- **ChestESP** - Visualize containers through walls
- **HealthBar** - Display health bars above entities

### HUD
- **ArrayList** - Enabled modules list with sliding animations
- **Watermark** - Client watermark display
- **MenuWatermark** - Shows watermark on main menu
- **Coordinates** - Player position display
- **TargetHUD** - Target information with armor/ping/combat prediction
- **ServerInfo** - Server details display
- **FPS** - FPS counter

### Client
- **ClickGui** - Module configuration GUI with Ctrl+F search and color customization
- **HudEditor** - Drag-and-drop HUD position editor
- **Notifications** - Module toggle notifications

## Multi-Config Support

Save and load multiple configurations:
- `.config save <name>` - Save current config
- `.config load <name>` - Load existing config
- `.config list` - List all saved configs
- `.config delete <name>` - Delete a config

## Technologies Used

| AI Tool | Purpose |
|---------|---------|
| ChatGPT | Code optimization and feature implementation |
| DeepSeek | Prompt engineering |
| OpenCode | Open-source best practices |
| Gemini | UI/UX improvements |
| Cursor | Development assistance |

## Requirements

- Java 21
- Minecraft 1.21.11
- Fabric Loader 0.18.4+
- Fabric API

## Installation

1. Clone the repository
   ```bash
   git clone https://github.com/daneq1/oxevy.git
