# TNT Rocket League - Game Balance & Polish

## ⚡ Current Balance Values (Multiplayer-Ready)

### Ball Physics
- **TNT Friction:** 0.998 (loses 0.2% speed per tick - super slippery!)
- **TNT Gravity:** -0.02 blocks/tick (slow falling, altitude challenge)
- **Ball Behavior:** Echo VR / Air Hockey disc physics

### Player Movement
- **Base Speed:** 0.3 blocks/tick
- **Boost Power:** 5.5x camel dash power (charged by holding space)
- **Boost Duration:** 20 ticks (1 second)
- **Boost Cooldown:** 40 ticks (2 seconds)
- **Boost Direction:** Where player is looking (3D directional)

### Combat & Scoring
- **Melee Hits:** 2.0 blocks/tick base + player momentum × 1.5
- **Wind Charges:** 1.5x projectile velocity (nerfed for skill-based gameplay)
- **Fishing Rod:** 2.0x hook velocity (basic projectile hit)
- **Score to Win:** 3 points

### Player Roles
**Attackers:**
- 3 stacks Wind Charges (192 total)
- Full Netherite armor (Protection 4, Unbreaking 3)
- Instruction: "Punch TNT or shoot Wind Charges to score!"

**Goalies:**
- 1 stack Wind Charges (64 total)
- Shield (Unbreaking 3)
- Full Netherite armor
- Instruction: "Use Shield to block TNT, Wind Charges to clear!"

---

## 🎮 Gameplay Features

### Game Flow
1. **Lobby Phase:** Walk through colored banners to join teams
2. **Countdown:** 3-second freeze before game starts
3. **Active Play:** Fast-paced TNT hockey action
4. **Goal Scoring:** Instant score + celebration + opposing team gets serve
5. **Victory:** First to 3 points wins with music celebration

### Visual/Audio Feedback
- ✅ Ball trail particles every 5 ticks
- ✅ Hit effect particles on impact
- ✅ Goal explosion effects (3x amplified)
- ✅ Level-up sound on goals
- ✅ Victory music (Creator for RED, Pigstep for BLUE)
- ✅ Camel dash sound on boost

### Player Controls
- **WASD:** Move Allay mount
- **Mouse:** Look direction (controls boost direction)
- **SPACE (Hold & Release):** Charge boost, release to fire
- **Left Click:** Punch TNT (melee hit)
- **Right Click:** Throw wind charge
- **XP Bar:** Shows boost charge level

### Arena Features
- Automatic boundary detection (out of bounds reset)
- Goal detection system (Red/Blue wool zones)
- Spawn system with team-based positioning
- Serve system (opposing team gets ball after goal)

---

## 🏆 Multiplayer Ready Checklist

### Core Mechanics ✅
- [x] Smooth 3D movement on Allay mounts
- [x] Directional boost system
- [x] Fast, skill-based TNT physics
- [x] Balanced hit strengths
- [x] Slow falling challenge (altitude management)

### Game Flow ✅
- [x] Team selection (Red/Blue banners)
- [x] Pre-game countdown
- [x] Goal detection & scoring
- [x] Victory conditions
- [x] Ball reset on out-of-bounds
- [x] Player respawn on goals

### Polish ✅
- [x] Clear instructions per role
- [x] Score announcements with formatting
- [x] Victory celebration with music
- [x] Particle effects for all actions
- [x] Sound feedback
- [x] XP bar for boost indication

### Balance ✅
- [x] Wind charges nerfed (2.5x → 1.5x) for skill
- [x] Melee hits powerful but require positioning
- [x] Boost strong but limited by cooldown
- [x] TNT maintains momentum (slippery physics)
- [x] Slow falling adds strategic depth

---

## 🎯 Quick Start for Players

**Join Game:**
1. Walk through RED or BLUE banner to join team
2. Wait for countdown (3 seconds)
3. Game begins!

**How to Play:**
- **Hit TNT into opponent's goal (wool zone)**
- **Punch it** or **shoot wind charges** to move it
- **Charge boost** (hold space) for big plays
- **Manage altitude** - you and TNT slowly fall!

**Tips:**
- Position yourself before hitting TNT
- Charge full boost for maximum power
- Use directional boost (look where you want to go)
- Teamwork! Pass the TNT to teammates
- Goalies: defend your wool zone!

---

## ⚙️ Technical Details

### Mixin System
- **AllayMixin:** Custom mount physics & boost system
- **TntEntityMixin:** Disc physics (friction + slow falling)
- **ProjectileMixin:** Wind charge hit detection & amplification
- **PlayerEntityMixin:** Melee hit detection & knockback
- **FishingHookMixin:** Projectile-style hits

### Game Architecture
- **GameManager:** Singleton managing active games
- **Game:** Individual match instance with scoring
- **Arena:** Boundary & goal zone definitions
- **PlayerLoadout:** Role-based equipment system
- **CountdownManager:** Pre-game countdown system
- **ServingSystem:** Post-goal ball serves

---

## 🚀 Performance Notes

- **Server-Side Logic:** All physics calculations on server
- **Particle Throttling:** Effects every 5 ticks (not every tick)
- **Efficient Tracking:** UUID-based ball/player management
- **No Client Prediction:** All movement server-authoritative

---

**Game Status:** ✅ MULTIPLAYER READY

Ready for 2v2, 3v3, or 4v4 competitive play!

