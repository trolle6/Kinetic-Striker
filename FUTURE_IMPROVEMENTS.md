# 🔮 TNT Rocket League - Future Improvements & Known Issues

## 🎯 LATER Tasks (User Mentioned)

### 🦜 Animals & Celebrations
- [ ] **Make animals actually dance** (vanilla animations are HARD!)
  - Parrots: Using ParrotAccessor but might need different approach
  - Hoglins: No easy celebration trigger found
  - **Workaround considered:** Particle text instead (for non-particle players)
  - **Current status:** Animals spawn but don't animate properly

- [ ] **Fix parrot floating/hovering**
  - Currently float slightly above barrier blocks
  - Need to make them "settle" properly on ground
  - Tried: NoGravity false, setOnGround, barrier platforms
  - **Still not perfectly grounded**

- [ ] **Particle effect balance**
  - User mentioned "some might be too much"
  - Need to test and tune:
    - Musical notes frequency/count
    - Happy villager particles (parrots)
    - Flame particles (hoglins)
    - Lava drips (hoglins)
    - Enchant glitter (parrots)

### 🎨 Victory Celebration Ideas
- [ ] **Particle Text in Sky** (NEW IDEA!)
  - Spell out "RED WINS" or "BLUE WINS" with particles
  - Display below TNT dispenser
  - Alternative to dancing animals for players with particles disabled
  - **Question:** Do both or just particle text?

### 🤖 AI Opponent (1v1 vs Bot)
- [ ] **Player vs AI mode** (POSTPONED - Very Complex!)
  - Would need:
    - AI pathfinding
    - Ball chasing logic
    - Shooting mechanics
    - Defensive positioning
  - **Estimated work:** 500-1000 lines of code
  - **Complexity:** 5/5
  - **User decision:** Shelved for now

### 🎮 Gameplay Mechanics

- [ ] **Fishing Rod Mechanics** (ABANDONED)
  - Tried multiple approaches:
    - Pull toward player
    - Follow hook movement
    - Timer-based pulling
    - Hockey puck impulse
  - **Final decision:** Removed, using projectile-style hits only
  - **Could revisit:** If needed later

---

## 🐛 Known Issues

### Movement & Physics
- [x] ~~TNT not falling (was stuck)~~ ✅ FIXED - Added slow falling
- [x] ~~TNT too slow horizontally~~ ✅ FIXED - Set friction to 0.998
- [x] ~~Fishing hook dragging player around~~ ✅ FIXED - Removed pull mechanics
- [x] ~~Boost canceling horizontal movement~~ ✅ FIXED - Now directional boost

### Entities
- ⚠️ **Parrots hovering** - Still floating slightly above ground
- ⚠️ **Animals not dancing** - Animations not triggering properly
- ⚠️ **Hoglins showing as piglins** - Fixed to spawn hoglins but no dance animation

### Balance
- [x] ~~Wind charges too powerful~~ ✅ FIXED - Reduced from 2.5x to 1.5x

---

## 💡 Potential Future Features

### Quality of Life
- [ ] **Better particle visibility settings**
  - Option to disable certain particle effects?
  - Particle-free mode for low-end PCs?

- [ ] **Scoreboard display**
  - Persistent scoreboard on side of screen
  - Instead of just chat messages

- [ ] **Kill cam / replay system**
  - Show goal from different angle?
  - Slow-motion replay of epic shots?

### Gameplay
- [ ] **Power-ups / Items**
  - Speed boost pickups?
  - Temporary TNT size changes?
  - Shield bubbles?

- [ ] **Multiple arenas**
  - Different sizes/shapes
  - Special mechanics per arena?

- [ ] **Game modes**
  - Elimination mode?
  - Time-based scoring?
  - King of the Hill variant?

### Social
- [ ] **Player stats tracking**
  - Goals scored
  - Assists
  - Wins/losses
  - MVP system

- [ ] **Team chat**
  - Separate chat for team coordination
  - Quick chat commands

---

## 🔧 Technical Debt

### Code Quality
- [ ] **Clean up debug messages**
  - Remove excessive debug logging once stable
  - Keep only critical messages

- [ ] **Optimize particle spawning**
  - Currently spawning many particles every 5 ticks
  - Could cause lag with many players?

- [ ] **Entity cleanup verification**
  - Ensure all animals/barriers properly removed
  - No entity buildup over multiple games

### Documentation
- [x] ~~Game balance document~~ ✅ Created GAME_BALANCE.md
- [x] ~~Multiplayer guide~~ ✅ Created MULTIPLAYER_READY.md
- [x] ~~Fun features document~~ ✅ Created FUN_UPDATE.md
- [ ] **Video tutorial** (if sharing with others)

---

## 🎯 Priority Levels

### HIGH PRIORITY (Affects Gameplay)
1. ⚠️ Make animals properly grounded (floating issue)
2. ⚠️ Decide: Dancing animals OR particle text victory?

### MEDIUM PRIORITY (Polish)
3. Balance particle effects (too many?)
4. Clean up debug messages
5. Test with multiple players (8+ players)

### LOW PRIORITY (Nice to Have)
6. AI opponent system
7. Additional game modes
8. Stats tracking
9. Replay system

---

## 📝 Notes for Later

### Modding Pain Points Discovered:
- **Entity animations are HARD** in vanilla Minecraft
  - Dancing requires AI + specific memory states
  - Celebration animations inconsistent across mob types
  - No simple "play animation" command
  
- **Fishing hook physics are weird**
  - Hook moves through air differently than expected
  - Entity collision happens before hit events
  - Difficult to make intuitive pulling mechanics

- **Particle visibility**
  - Some players disable particles for performance
  - Need fallback for victory celebrations

### What Worked Well:
- ✅ Mixin system for custom physics
- ✅ Allay mount control (smooth and responsive)
- ✅ Directional boost system (feels great!)
- ✅ TNT disc physics (super satisfying)
- ✅ Team banner joining (intuitive!)
- ✅ Countdown system (good game feel)
- ✅ Barrier blocks for platforms (invisible and functional)

---

## 🤔 Design Decisions to Make

1. **Victory Celebration:**
   - Keep trying to make animals dance?
   - Switch to particle text display?
   - Do both?
   - **Needs user input!**

2. **Particles:**
   - Current setup might be "too much"
   - Which effects to keep/remove?
   - **Needs playtesting!**

3. **Testing Mode:**
   - Keep team goal spawning for testing?
   - Or revert to center spawning for real games?
   - **Toggle command?**

---

## 📊 Current Game State

**Status:** ✅ Multiplayer Ready (with minor issues)

**What Works:**
- Core gameplay loop
- Scoring system
- Team selection
- Victory conditions
- Most physics

**What's Wonky:**
- Victory animal animations
- Animal positioning (floating)

**What's Perfect:**
- TNT physics ⭐
- Boost system ⭐
- Slow falling mechanic ⭐
- Team names & branding ⭐
- Particle effects (mostly) ⭐

---

## 🚀 Next Steps

1. **Immediate:** Try particle text instead of animals?
2. **Short-term:** Balance particle effects
3. **Long-term:** Consider AI opponent if wanted

---

**Last Updated:** [Auto-generated during polishing session]

**Note:** This is a living document - add to it whenever we say "we'll do that later!"

