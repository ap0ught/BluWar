BluWar (English)
================

A game similar to Worms on MIDP 2.x platform via Bluetooth

## Description

BluWar is a Java ME (J2ME) game designed for mobile devices that support the MIDP 2.x platform. The game offers gameplay similar to the popular Worms series and includes Bluetooth multiplayer functionality.

## Features

- Classic worms-style gameplay
- Bluetooth multiplayer support
- Custom map system with different block types:
  - VOID blocks (air/empty space)
  - FILL blocks (solid ground)
  - Detailed blocks (mixed ground and air with pixel-level detail)
- Dynamic map tile loading for smooth scrolling
- Real-time FPS display

## Technical Details

- **Platform**: Java ME (MIDP 2.x)
- **Graphics**: Custom tile-based rendering system
- **Map Format**: Custom BluWar format (.bwh header files, .bwm map data)
- **Build System**: Apache Ant with NetBeans project structure

## Map Format

Maps use a two-file system:
- `.bwh` files contain map metadata (name, dimensions, block details)
- `.bwm` files contain the actual map data

Map blocks are represented as:
- `a` - air (VOID blocks)
- `l` - land (FILL blocks) 
- `t`/`f` - detailed blocks with pixel-level true/false data

## Development

This project was developed using NetBeans IDE and requires J2ME development tools for building and deployment to mobile devices.