# TR PORT Android Mobile Project

A native Android portrait-first UI prototype for a mobile game-compatibility project. It includes:
- Home, downloads, graphics, gamepad, settings sections
- Device information scan (real Android device properties, memory/storage/display and Vulkan/OpenGL feature detection)
- Graphics/FPS/resolution profile controls saved locally
- Low-device and Ultra-low profile toggles
- Five package-size choices (7/16/24/43/72 GB) with download-manager UI scaffolding
- Touch layout customization scaffold and gamepad key input display
- GitHub Actions build workflow to create a debug APK

## Honest scope
This is **not a GTA V Android port or game runtime**. It does not include Rockstar game files, GTA assets, or an actual game engine capable of launching GTA V. FPS/graphics/resolution values are saved as project preferences; they do not change a game that is not present. Download choices are not connected to packages until authorized URLs and SHA-256 checksums are supplied in code. No fake package data is included.

## Build APK
Upload the contents of this ZIP to the root of a GitHub repository. Open Actions and run `Android APK Build`. Download the `TR-PORT-APK` artifact if the build succeeds.
