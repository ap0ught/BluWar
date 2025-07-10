# BluWar: Technical Concepts & Glossary

This document explains the key technologies, APIs, and constraints involved in BluWar’s development and runtime. It complements the main **README.md** by diving deeper into the technical underpinnings.

---

## 1. Java ME & MIDP

**Java ME (Micro Edition)**  
- A lightweight Java runtime targeting embedded and mobile devices.  
- Built on a *configuration* (CLDC – Connected Limited Device Configuration) plus one or more *profiles*.
- BluWar is structured to minimize memory footprint and avoid advanced Java SE features (e.g., reflection, generics).
- Uses primitive types and arrays for data storage, and avoids unnecessary object creation.

**MIDP 2.x (Mobile Information Device Profile)**  
- A Java ME profile defining APIs for user interface (`javax.microedition.lcdui`), application lifecycle (`javax.microedition.midlet`), networking, and storage.  
- Introduces enhanced UI components, multimedia support, push registry, and improved security over MIDP 1.0.  
- Applications extend `MIDlet` and implement methods: `startApp()`, `pauseApp()`, `destroyApp()`.
- UI screens are implemented as subclasses of `Displayable` (e.g., `Form`, `Canvas`, `List`).
- Event handling is managed via command listeners and key events.

---

## 2. Bluetooth Multiplayer (JSR 82)

- **JSR 82** is the Java ME Bluetooth API.  
- Key classes:
  - `DiscoveryAgent` for finding devices and services.
  - `LocalDevice` to turn on/off the Bluetooth radio.
  - `StreamConnection` (RFCOMM) for byte‐stream communication.
  - `ServiceRecord` to register a Bluetooth server endpoint.
- BluWar uses RFCOMM sockets to exchange game state, player turns, and map updates over Bluetooth.
- Implements both server and client roles for peer-to-peer gameplay.
- Connection setup includes device discovery, service search, and stream negotiation.
- Game packets are serialized as byte arrays for efficient transmission.
- Handles connection loss and reconnection logic, with user feedback via UI.

---

## 3. Turn-Based Artillery Gameplay

- Inspired by the classic **Worms** series:
  - Each player takes a turn aiming and firing a projectile.
  - Gravity and wind affect projectile arcs.
  - Terrain is *destructible*: shots remove “blocks” from the map.
- Core components:
  - **Physics engine** for projectile motion and collision detection.
    - Calculates trajectory using basic kinematics, factoring in wind and gravity.
    - Detects collisions with terrain and players.
  - **Game loop** that sequences turns, applies damage, and checks victory conditions.
    - Alternates between local and remote player turns.
    - Synchronizes game state over Bluetooth in multiplayer mode.
    - Updates UI to reflect current turn, health, and terrain changes.
- Implements random wind generation and variable gravity per round.
- Supports multiple weapon types with different effects.

---

## 4. Map & Terrain Format (`.bwh`)

- Custom binary map files storing:
  - Grid dimensions (`wbl`, `hbl`): number of horizontal/vertical blocks.
  - `blockSize`: the pixel size of each block.
  - A 2D array of block‐presence flags or height‐values.
- Loaded at runtime by `Map.java`, rendered onto a Java ME `Canvas`, and updated when blocks are destroyed.
- Map loading parses the binary format into in-memory arrays for fast access.
- Terrain rendering uses double-buffering to minimize flicker.
- Terrain updates (block destruction) trigger partial redraws for efficiency.
- Map files are compressed to reduce JAR size and memory usage.

---

## 5. Packaging: JAR & JAD

- **.jar** (Java Archive):
  - Bundles compiled `.class` files, images, sounds, and `.bwh` maps.
  - Signed when required by device security policies.
  - Manifest includes MIDlet attributes and permissions.
- **.jad** (Java Application Descriptor):
  - Plain‐text metadata describing:
    - `MIDlet-Name`, `MIDlet-Vendor`, `MIDlet-Version`
    - `MicroEdition-Configuration`, `MicroEdition-Profile`
    - Required permissions (e.g., `javax.microedition.io.Connector.bluetooth`)
  - Used by device installers to verify compatibility and request permissions.
- Build scripts automate JAR/JAD generation and resource inclusion.

---

## 6. Emulators vs. Real Devices

- **Emulators** (Oracle WTK, MicroEmu):
  - Enable rapid testing of UI, logic, and map rendering.
  - **Limitation**: Most lack true Bluetooth radios; multiplayer tests must move to real devices.
  - Useful for debugging game logic, UI layout, and resource loading.
- **Real Devices**:
  - Essential for verifying Bluetooth pairing, connection stability, and performance.
  - Typical feature-phones offer mid‐range CPU and 512 KB–1 MB heap.
  - Device-specific quirks (e.g., key mapping, screen resolution) are handled via runtime checks.
  - Performance profiling is done on-device to optimize memory and CPU usage.

---

## 7. Performance & Memory Constraints

- Java ME GC is simple and can stall the game—minimize allocations.  
- Favor:
  - Primitive arrays (`short[][]`, `byte[]`) over `Vector`, `String` concatenation.  
  - Object pooling (reuse `Sprite` or `Packet` instances).  
- Avoid:
  - Large resource files; compress maps and images.
- Game loop and rendering are optimized to avoid per-frame allocations.
- Sprite and projectile objects are recycled to reduce GC pressure.
- Audio and image assets are loaded once and reused.
- Map data is loaded in chunks to avoid out-of-memory errors.

---

## 8. Application Lifecycle & UI Flow

- **MIDlet Lifecycle**:
  - `Main.java` extends `MIDlet` and implements `startApp()`, `pauseApp()`, `destroyApp()`.
  - On start, shows the `WaitScreen` while loading resources.
  - Handles transitions between active, paused, and destroyed states as triggered by the device or user actions.
  - Ensures resources (threads, Bluetooth connections) are released in `pauseApp()` and `destroyApp()`.
  - Saves game state on pause, restores on resume if possible.
  - Handles device events (e.g., incoming calls) gracefully.

- **UI Flow & Navigation**:
  - **Startup Sequence**:
    1. MIDlet launches, enters `startApp()`.
    2. Displays `WaitScreen` to indicate loading progress.
    3. Once assets and maps are loaded, transitions to the main menu.
  - **Main Menu**:
    - Options: Start Game, Multiplayer, Settings, Exit.
    - User selects mode; triggers navigation to the appropriate screen.
    - Menu implemented as a `List` or custom `Canvas`.
  - **Multiplayer Setup**:
    - Initiates Bluetooth device discovery and connection.
    - Shows status screens for pairing, connection progress, and error messages.
    - On successful connection, transitions to the game canvas.
    - Handles both host and join scenarios.
  - **Game Canvas**:
    - Core gameplay UI: renders terrain, players, projectiles.
    - Handles user input for aiming, firing, and menu access.
    - Displays turn indicators and status messages.
    - Manages transitions between player turns (local and remote).
    - Updates health, wind, and other game stats in real time.
  - **Pause & Resume**:
    - User can pause the game; shows a pause menu.
    - Resumes gameplay or returns to main menu.
    - Handles device-level pause (incoming call, app switch) by saving state.
    - Ensures Bluetooth connections are suspended or closed as needed.
  - **Game End & Cleanup**:
    - Shows victory/defeat screen.
    - Offers options to restart, return to menu, or exit.
    - Calls `notifyDestroyed()` to terminate the MIDlet and release resources.
    - Cleans up threads, closes streams, and resets UI.

- **Error Handling & Recovery**:
  - Displays error dialogs for Bluetooth failures, resource loading issues, or unexpected exceptions.
  - Allows user to retry, return to menu, or exit gracefully.
  - Logs errors for debugging (where supported).
  - Recovers from lost connections by prompting user for action.

- **Bluetooth Session Management**:
  - Manages connection lifecycle: discovery, pairing, session establishment, and teardown.
  - Handles lost connections by notifying users and offering reconnection or exit options.
  - Ensures streams and sockets are closed on pause, destroy, or disconnect.
  - Synchronizes game state between devices after reconnect.

- **Threading & Responsiveness**:
  - Uses background threads for resource loading, Bluetooth operations, and network I/O.
  - UI updates are posted to the main thread to avoid blocking input or rendering.
  - Game loop runs in a dedicated thread for smooth animation.
  - Synchronization primitives (e.g., `wait/notify`) are used to coordinate threads.

---

## 9. Licensing

- **GPL v2.0**:  
  - Strong copyleft license; any distributed modifications or derivatives must also be licensed under GPL v2.  
  - Requires source‐code availability when distributing binary `.jar` builds.
  - All source files include GPL headers.
  - Third-party libraries must be compatible with GPL v2.0.

*End of Technical Glossary*
