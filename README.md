# Echo Clone Mod

**Made by mr APPLE**  
**Minecraft 1.20.1 | Forge 47.2+**

---

Record your actions and summon looping clones of yourself. Have up to 10 clones running
at the same time, each repeating exactly what you did — movement, sprinting, sneaking,
placing blocks, breaking blocks, attacking, and more.

---

## Features

- **Record & Clone** — Press `R` to start recording. Do anything. Press `R` again
  to stop and instantly spawn a clone that loops your actions forever.

- **Multiple Clones** — Up to 10 clones per player at the same time.

- **8 Built-In Skins** — Dream, Technoblade, Notch, jeb_, MrBeast, Skeppy,
  BadBoyHalo, Steve. Switch between them in the Clone Manager.

- **Custom UI** — Press `G` to open the Clone Manager:
  - Browse all 8 skins with face preview
  - See live recording status
  - Remove nearest clone or all clones at once

- **Persistent** — Clones survive world restarts (saved to NBT).

- **Server-side** — Works in both singleplayer and multiplayer.

---

## Controls

| Key | Action |
|-----|--------|
| `R` | Start / Stop recording & spawn clone |
| `G` | Open Clone Manager |

Both rebindable in **Options → Controls → Echo Clone**.

---

## Building from Source

### What you need
- Java 17 JDK — https://adoptium.net
- Git

### Steps
```bash
# 1. Unzip/extract the project
cd echo-clone-mod

# 2. Set up Forge MDK (downloads Minecraft — takes a few minutes first time)
gradlew genEclipseRuns    # Eclipse / VS Code
# or:
gradlew genIntellijRuns   # IntelliJ IDEA

# 3. Build
gradlew build

# Output: build/libs/echoclone-1.0.0.jar
```

### Install
1. Install Forge 47.2+ for Minecraft 1.20.1 from https://files.minecraftforge.net
2. Put `echoclone-1.0.0.jar` in `.minecraft/mods/`
3. Launch Minecraft with the Forge profile

---

## CurseForge

Uploaded by **mr APPLE**.  
https://www.curseforge.com/minecraft/mc-mods/echo-clone

---

## License

MIT — free to use, modify, and share. Credit **mr APPLE** if you redistribute.
