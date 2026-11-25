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

This backport uses Controlify's default binding data, SDL gamepad input and controller database, input curves, hold-to-repeat timing, gyro calibration, rumble effects, radial-menu texture, and English keyboard layout. Pylon supplies the SDL bindings, Feather mixins adapt the Minecraft calls, and OneConfig provides settings and controller guides. [Upstream Controlify at `ec4f067d`](https://github.com/isXander/Controlify/tree/ec4f067db8a037fcc17f0d36259f2086affc4f7c)

## Menu and inventory controls

| Xbox-style input | Action |
| --- | --- |
| Left stick / D-pad | Move the cursor / snap between buttons or inventory slots |
| A | Left-click, or hold while moving to drag |
| X | Right-click |
| Right stick up / down | Scroll up / down |
| B | Go back |
| Y | Quick-move the stack under the cursor in an inventory |

The menu cursor uses Controlify's cubic response for precise movement near the center of the stick. Releasing the stick over an inventory slot centers the cursor on that slot. D-pad navigation repeats when held.

## Gameplay controls

Open **OneConfig → Controlify → Controller** to adjust horizontal and vertical turn speed, look inversion, input curve, and sensitivity while drawing a bow. The upstream defaults are 100% horizontal speed, 90% vertical speed, and the Standard quadratic curve. Full horizontal deflection at 100% turns the camera at 200° per second.

The stick deadzones default to 12%, and triggers activate at 50%. Existing settings and remaps carry over. **Restore Upstream Defaults** applies the bindings and stick settings shown here to an existing installation.

Use **OneConfig → Controlify → Controller → Active Controller** to choose a connected gamepad. Automatic mode uses the first available gamepad. A manual selection waits for that controller to return after a disconnect. If SDL gives two controllers the same identity, the selection applies to the current session and must be selected again after restarting.

Bindings, stick and gyro options, vibration, and guide preferences are saved per controller. The first controller inherits your previous settings. New controllers start with upstream defaults. Profile matching uses the device serial number when available and SDL's path or name otherwise.

Buttons held when closing a menu or regaining window focus only trigger gameplay actions after you release and press them again.

Turn on **Gyro Aim** to use an SDL-compatible gyroscope. **Gyro Activation Mode** controls the activation binding. With the default **ON** mode and an unbound activation button, gyro aiming activates while drawing a bow. Set the mode to **OFF** to leave gyro aiming active during gameplay. The yaw axis, inversion, and gyro sensitivity are adjustable.

**Vibration** plays upstream effects for damage, nearby explosions, drawing a bow, blocking, and consuming items. **Test Vibration** plays a preview from OneConfig.

| Xbox-style input | Action |
| --- | --- |
| Left stick / right stick | Move / look |
| Left trigger / right trigger | Use item / attack |
| A / right stick click / left stick click | Jump / toggle sneak / toggle sprint |
| Y / D-pad down | Open inventory / drop item |
| Left bumper / right bumper | Previous / next hotbar slot |
| D-pad up / View | Open chat / cycle camera perspective |
| D-pad right (hold) | Open the radial menu. Select with the right stick and release to activate |
| D-pad left | Pick block |
| Start | Pause / resume |

The radial menu has gameplay and debug pages. Use LB and RB to change pages and B to cancel. Available actions include toggling the HUD or player list, dropping a stack, screenshots, and opening chat. Controller-opened chat uses upstream's English QWERTY layout with X for Backspace, Y for Space, L3 for Shift, and Start to send.

Radial customization, keyboards for text fields outside chat, flick stick, additional haptics, resource-pack overrides, and mod compatibility integrations are still pending. Minecraft 1.8.9 also lacks newer gameplay actions such as swapping items to the offhand.
