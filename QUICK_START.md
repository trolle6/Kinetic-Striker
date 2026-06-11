# Quick Start Guide - TNT Rocket League

## 🚀 Get Playing in 5 Minutes!

### Step 1: Build the Mod
```bash
# Windows
.\gradlew.bat build

# Linux/Mac
./gradlew build
```

### Step 2: Install
1. Find the JAR in `build/libs/`
2. Copy to your Minecraft `mods` folder
3. Make sure Fabric Loader + Fabric API are installed

### Step 3: Launch Minecraft
Start Minecraft with Fabric profile

### Step 4: Create a Basic Arena
In-game, run these commands (you need OP/admin):

```minecraft
# Example arena setup (adjust coordinates for your location)
/tntrl setarena ~-25 ~ ~-15 ~25 ~15 ~15
/tntrl setgoal red ~-25 ~ ~-3 ~-23 ~5 ~3
/tntrl setgoal blue ~23 ~ ~-3 ~25 ~5 ~3
```

**Tip**: Stand in the middle of where you want your arena!

### Step 5: Start the Game
```minecraft
/tntrl start
```

### Step 6: Join a Team
```minecraft
/tntrl join red
# or
/tntrl join blue
```

### Step 7: Get Equipment
```minecraft
/give @s elytra
/give @s fishing_rod
/give @s tnt_rocket_leauge:boost
```

### Step 8: PLAY!
- **Fly**: Equip elytra, jump, and press jump mid-air
- **Hit Ball**: Punch the TNT
- **Hook Ball**: Right-click with fishing rod
- **Boost**: Right-click with boost item while flying (2s cooldown)
- **Score**: Get ball into opponent's goal!

---

## 🎮 Controls

| Action | Control |
|--------|---------|
| Fly | Elytra + Jump |
| Hit Ball | Left Click (Attack) |
| Hook Ball | Right Click with Fishing Rod |
| Boost | Right Click with Boost Item (while flying) |
| Score | Get TNT in opponent's goal |

---

## 📋 Useful Commands

| Command | Description |
|---------|-------------|
| `/tntrl start` | Start game (OP only) |
| `/tntrl stop` | End game (OP only) |
| `/tntrl join red` | Join red team |
| `/tntrl join blue` | Join blue team |
| `/tntrl leave` | Leave game |
| `/tntrl score` | Show current score |

---

## 🏗️ Arena Building Tips

### Minimum Arena Size
- Width: 50 blocks
- Height: 15 blocks
- Length: 30 blocks

### Recommended Arena Design
```
[RED GOAL] ←──── 40-60 blocks ────→ [BLUE GOAL]
     ↑                                    ↑
   3x6x5                                3x6x5
   blocks                               blocks

Ceiling height: 15+ blocks for flying
Floor: Any solid block
Walls: Recommended to keep ball in bounds
```

### Quick Arena Builder Tip
1. Find flat area or create one
2. Build two goals at opposite ends (like soccer goals)
3. Use `/tntrl setarena` for the entire field
4. Use `/tntrl setgoal` for each goal zone
5. Make sure goals are inside the arena bounds!

---

## ⚡ Pro Tips

1. **Ball Control**: Hook with fishing rod, then hit while hooked for powerful directed shots
2. **Aerial Plays**: Boost straight up, then dive down for overhead hits
3. **Defense**: Position yourself between ball and your goal
4. **Boost Management**: Don't waste boosts - they have cooldown!
5. **Team Play**: Call out positions and passes
6. **Wall Bounces**: Use arena walls to angle shots

---

## 🐛 Troubleshooting

**Ball isn't spawning?**
- Make sure arena bounds are set correctly
- Check that goals are defined
- Try `/tntrl stop` then `/tntrl start`

**Can't join team?**
- Game must be started first
- Use `/tntrl start` before joining

**Boost item not working?**
- Must be flying with elytra
- Check for 2-second cooldown
- Make sure you right-clicked the item

**Goals not scoring?**
- Verify goal zones are set correctly
- Goals should be inside arena bounds
- Ball must be fully inside goal zone

---

## 🎯 First Game Checklist

- [ ] Mod built and installed
- [ ] Fabric Loader + Fabric API installed
- [ ] Minecraft launched
- [ ] Arena boundaries set (`/tntrl setarena`)
- [ ] Red goal set (`/tntrl setgoal red`)
- [ ] Blue goal set (`/tntrl setgoal blue`)
- [ ] Game started (`/tntrl start`)
- [ ] Joined a team (`/tntrl join`)
- [ ] Got elytra
- [ ] Got fishing rod
- [ ] Got boost item
- [ ] Ready to score!

---

## 🎊 Have Fun!

You're all set! Grab some friends and start playing TNT Rocket League!

**Remember**: The ball never explodes, so don't worry about TNT damage. Just focus on scoring goals! 🚀⚽

For more details, see `README.md` and `IMPLEMENTATION_SUMMARY.md`.

