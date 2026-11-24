# Controlify for Ornithe 1.8.9

This is an experimental legacy port kept separate from Controlify's current multi-version build. Ornithe provides the 1.8.9 modding toolchain; [Pylon](https://modrinth.com/mod/pylon/) supplies the LWJGL 3 and SDL 3 runtime.

## Build and launch

Download the mod JAR from [GitHub Releases](https://github.com/ooTruffle/Controlify-Ornithe/releases), or build it from source.

Use a JDK and run:

```powershell
.\gradlew.bat build
.\gradlew.bat runClient
```

The Windows test client used Minecraft 1.8.9 on Ornithe Gen 2 with Fabric Loader 0.19.5, Ornithe Standard Libraries 0.21.0, Pylon 0.2.9, OneConfig 1.9.3, Fabric Language Kotlin 1.14.1+kotlin.2.4.20, PolyCompose 3.0.5, and Java 25. The mod requires Java 21 or later. Use PolyCompose 3.0.5 for Ornithe 1.8.9. The 3.0.6 release is missing a class used to render OneConfig notifications.

## Port status

The current port initializes SDL 3 gamepad input, selects a connected SDL gamepad, reads standard stick and button state, applies analog movement and frame-smoothed camera input, exposes gyro and accelerometer samples, and provides rumble calls. OneConfig's Controlify settings include a switch to hide the menu guide and the movable in-game guide.

## Menu and inventory controls

| Xbox-style input | Action |
| --- | --- |
| Left stick / D-pad | Move the cursor. The D-pad snaps between slots in inventory screens |
| A | Left-click, or hold while moving to drag |
| X | Right-click |
| LB / RB | Scroll up / down |
| B | Go back |
| Y | Quick-move the stack under the cursor in an inventory |

## Gameplay controls

Open **OneConfig → Controlify → Controller → Controller Camera Speed (%)** to set right-stick turn speed from 25% to 300%. The default is 100%.

Adjust **Left Stick Deadzone (%)** and **Right Stick Deadzone (%)** independently. Both default to 18%. Use **Gameplay Bindings** and **Menu Bindings** to remap actions. The tables show the default Xbox-style layout.

Turn on **Gyro Aim** in the same section to aim by moving an SDL-compatible controller with a gyroscope. It works alongside the right stick during gameplay. **Gyro Sensitivity (%)** adjusts the motion separately and defaults to 100%.

**Vibration** plays a short pulse when your player takes damage. **Test Vibration** plays a preview from OneConfig.

| Xbox-style input | Action |
| --- | --- |
| Left stick / right stick | Move / look |
| Left trigger / right trigger | Use item / attack |
| A / B / left stick click | Jump / sneak / sprint |
| Y / X | Open inventory / drop one item |
| Left bumper / right bumper | Previous / next hotbar slot |
| D-pad up / down | Open chat / cycle camera perspective |
| D-pad right (hold) | Show the multiplayer player list |
| Right stick click | Pick block |
| Start | Pause / resume |

This is not yet the full Controlify feature set. Per-controller profiles, an on-screen keyboard, radial menus, richer haptics, resource-pack data, and mod compatibility integrations still need legacy implementations. The original code targets Minecraft 1.19.4 and newer; its modern game APIs do not exist in 1.8.9.

The development client launched with Ornithe, Pylon, and this mod. SDL initialized and detected an Xbox One controller during the latest run. Controller navigation and movement feel still need hands-on testing.
