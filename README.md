# BluWar

BluWar is a 2D, turn-based artillery game for the MIDP 2.x platform (Java ME), inspired by the classic “Worms” series. It supports real-time multiplayer battles over Bluetooth between feature-phone devices.

---

## Table of Contents

1. [Features](#features)
2. [Getting Started](#getting-started)
    - [Requirements](#requirements)
    - [Installation & Build](#installation--build)
    - [Running the Game](#running-the-game)
3. [Technical Concepts](#technical-concepts)
4. [Project Structure](#project-structure)
5. [Known Issues & Roadmap](#known-issues--roadmap)
6. [Contributing](#contributing)
7. [License](#license)
8. [Credits](#credits)

---

## Features

- Turn-based, 2D artillery combat on destructible terrain
- Multiplayer via Bluetooth (JSR 82)
- Multiple maps loaded from custom binary `.bwh` files
- Simple UI optimized for low-memory, low-CPU devices
- GPL-v2 licensed open-source code

---

## Getting Started

### Requirements

- Java ME SDK (e.g., Oracle Wireless Toolkit) or any MIDP 2.x–compatible build tools
- Device or emulator with MIDP 2.0+ and JSR 82 (Bluetooth API) support
- (~512 KB–1 MB) of heap memory; limited CPU performance

### Installation & Build

1. Clone this repository:
   ```bash
   git clone https://github.com/ap0ught/BluWar.git
   cd BluWar
   ```
2. Compile the source under `src/ua/naiksoftware/bluwar/` using your Java ME toolchain.
3. Package the `.class` files and resources into `BluWar.jar` and produce a matching `BluWar.jad`.
4. Sign the JAR if required by your target device’s security policies.

### Running the Game

- **Emulator**  
  Launch via Java ME SDK emulator (note: many emulators lack real Bluetooth support).
- **On Device**
    1. Transfer `.jad` and `.jar` to your phone.
    2. Install via OTA or Bluetooth file transfer.
    3. Grant Bluetooth permissions when prompted.
    4. Launch BluWar, choose “Host” or “Join,” and pair with another device.

---

## Technical Concepts

1. **MIDP 2.x (Mobile Information Device Profile)**
    - Part of Java ME; defines APIs (`MIDlet`, `Display`, `Canvas`) for mobile apps.
    - Adds multimedia, push registry, and advanced UI over MIDP 1.0.

2. **Java ME (Micro Edition)**
    - A lightweight Java runtime for embedded/mobile devices.
    - CLDC (Connected Limited Device Configuration) plus profiles (e.g., MIDP).

3. **Bluetooth Multiplayer (JSR 82)**
    - Uses `DiscoveryAgent`, `StreamConnection`, `ServiceRecord`.
    - RFCOMM sockets for bidirectional streams.

4. **Worms-style Gameplay**
    - Turn-based artillery with gravity, projectile arcs, and collision.
    - Terrain destruction: map described as a 2D grid of “blocks” in `.bwh` files.

5. **JAR & JAD Packaging**
    - `.jar`: bytecode + assets (maps, images, sounds).
    - `.jad`: metadata (MIDlet-Name, MIDlet-Vendor, permissions, JAR URL).

6. **Emulator vs. Real Device**
    - Emulators: easy for UI/logic tests, but often no true Bluetooth radio.
    - On-device testing is required to validate pairing and data exchange.

7. **Performance Constraints**
    - Low heap (~512 KB), limited GC—favor primitive arrays, avoid large object graphs.
    - Minimize garbage creation, reuse buffers.

8. **Custom Map Format (`.bwh`)**
    - Binary header + block grid, with dimensions (`wbl`, `hbl`, `blockSize`).
    - Loaded at runtime via `Map` and rendered on a `Canvas`.

9. **Lifecycle & UI Flow**
    - `Main` extends `MIDlet` → shows `WaitScreen` → spawns loader thread → initializes `Game`.
    - Implements `Initializer` to notify when loading completes.

---

## Project Structure

```
BluWar/
├── src/
│   └── ua/naiksoftware/bluwar/
│       ├── Main.java
│       ├── Game.java
│       ├── WaitScreen.java
│       ├── Initializer.java
│       └── maps/
│           └── Map.java
├── build/
│   └── preprocessed/…     # SDK-generated stubs
├── resources/
│   ├── map1.bwh
│   └── …                  # additional assets
├── README.md
└── LICENSE
```

---

## Known Issues & Roadmap

- Bluetooth pairing can fail on certain modern devices
- No automated test suite—manual testing only
- UI scaling issues on high-resolution screens/emulators
- Missing advanced weapons, animations, and sound effects
- 🛠️ **Roadmap:**
    - Add robust retry logic for Bluetooth connections
    - Introduce unit tests with MicroEmu headless mode
    - Expand map editor tool and include sample maps
    - Refactor rendering loop for smoother animations

---

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/YourFeature`)
3. Commit your changes with clear messages
4. Ensure code follows existing style and runs on MIDP 2.x
5. Submit a Pull Request describing your changes

Please respect the GPL v2 license when contributing.

---

## License

This project is licensed under the **GNU General Public License v2.0**.  
See [LICENSE](LICENSE) for full text.

---

## Credits

- Original game by **NaikSoftware** (2013)
- Fork & maintenance by **ap0ught** (2025)
- Inspired by **Worms** (Team17)  