# 🦜 Dancing Animals Problem - Technical Explanation for AI Assistance

## 🎯 WHAT WE'RE TRYING TO DO

We're making a Minecraft Fabric mod (Java, Minecraft 1.21.1) where victory celebrations spawn dancing animals:
- **Blue Team Wins:** 12 Blue Parrots should DANCE to Pigstep music disc
- **Red Team Wins:** 12 Hoglins should do CELEBRATION animation

---

## ❌ THE PROBLEM

**Parrots:**
- They spawn correctly ✅
- They're set to dancing state via accessor ✅
- Console shows "Parrot dance set! Dancing=true" ✅
- **BUT THEY DON'T ACTUALLY DANCE** ❌

**Hoglins:**
- They spawn correctly ✅
- Celebrate memory is set ✅
- Console shows "Hoglin celebrate memory set!" ✅
- **BUT THEY DON'T ANIMATE** ❌

---

## 🔧 WHAT WE'VE TRIED

### Attempt 1: NoAI = true (Freeze Them)
```java
parrot.setNoAi(true);
```
- **Result:** Can't move ✅ BUT can't animate either ❌
- **Why:** NoAI disables ALL AI including animations

### Attempt 2: NoAI = false + Teleport Every Tick
```java
parrot.setNoAi(false);
// Every tick:
parrot.teleportTo(spawnPos);
```
- **Result:** They wander away, teleporting resets animation state ❌

### Attempt 3: NoAI = false + Movement Speed = 0
```java
parrot.setNoAi(false);
parrot.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.0);
parrot.getAttribute(Attributes.FLYING_SPEED).setBaseValue(0.0);
```
- **Result:** They still fly/walk away somehow! ❌
- **Why:** Not sure - movement speed = 0 should prevent movement?

### Attempt 4: Current State (Still Doesn't Work)
```java
// Spawn setup:
parrot.setNoAi(false); // AI enabled
parrot.getAttribute(MOVEMENT_SPEED).setBaseValue(0.0);
parrot.getAttribute(FLYING_SPEED).setBaseValue(0.0);
parrot.setSilent(true);
parrot.setInvulnerable(true);
parrot.setGlowingTag(true);

// Dancing trigger:
ParrotAccessor accessor = (ParrotAccessor) parrot;
accessor.setJukebox(parrotPos); // Set jukebox position
accessor.setPartyParrot(true); // Set dancing = true
parrot.setRecordPlayingNearby(parrotPos, true);

// Also play music:
level.playSound(null, centerX, centerY, centerZ, MUSIC_DISC_PIGSTEP, RECORDS, 3.0f, 1.0f);
level.playSound(null, parrotX, parrotY, parrotZ, MUSIC_DISC_PIGSTEP, RECORDS, 0.0f, 1.0f); // Volume 0

// Every tick during celebration:
animal.setGlowingTag(true);
// Lock Y position only
if (Math.abs(currentY - spawnY) > 0.1) {
    animal.setPos(currentX, spawnY, currentZ);
}

// Every 20 ticks:
accessor.setPartyParrot(true); // Re-trigger
```

**Console Output:**
```
Parrot spawned with ZERO movement speed - should stay and dance!
Parrot dance set! Dancing=true
```

**What Happens:**
- Parrots spawn
- They're glowing with blue outline ✅
- Musical notes particles appear above them ✅
- **They don't dance/bob heads** ❌
- Some still float/hover slightly

---

## 📚 VANILLA BEHAVIOR (What We Know)

### Parrot Dancing (Vanilla):
1. Parrots have a private boolean field `partyParrot`
2. When `partyParrot = true`, they play dancing animation (head bobbing, wing flapping)
3. This is triggered when:
   - They're within 3 blocks of a jukebox
   - The jukebox is playing a music disc
   - Their `setRecordPlayingNearby(BlockPos, true)` is called

### Hoglin Celebration (Vanilla):
1. Hoglins have a celebration animation when they kill piglins
2. Triggered by brain memory `CELEBRATE_LOCATION`
3. They hop around and do a "victory dance"

---

## 🧩 OUR CODE STRUCTURE

**ParrotAccessor.java** (Mixin Accessor):
```java
@Mixin(Parrot.class)
public interface ParrotAccessor {
    @Accessor("partyParrot")
    void setPartyParrot(boolean dancing);
    
    @Accessor("partyParrot")
    boolean getPartyParrot();
    
    @Accessor("jukebox")
    void setJukebox(BlockPos pos);
}
```

**Game.java** (Victory Spawning):
- Located in `private void spawnVictoryDancers(Team winningTeam)`
- Spawns 12 animals in circle (radius 6.0, centered on integer coordinates)
- Places 3x3 barrier platform under each animal
- Sets up dancing/celebration state
- Plays victory music

**Game.java** (Tick Loop):
- Located in `public void tickBall(PrimedTnt tnt)`
- During victory (when `victoryCleanupTimer >= 0`):
  - Locks Y position to prevent floating
  - Re-applies glow every tick
  - Re-triggers dancing every 20 ticks
  - Spawns particles every 5 ticks

---

## ❓ QUESTIONS FOR AI ASSISTANCE

### Question 1: Movement Speed
**Why do parrots still move when movement speed = 0?**
```java
parrot.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.0);
parrot.getAttribute(Attributes.FLYING_SPEED).setBaseValue(0.0);
```
- Does this actually prevent movement in Minecraft?
- Is there a different attribute or method to freeze entity position?
- Do parrots have special flying movement that bypasses this?

### Question 2: ParrotAccessor Field Name
**Is `"partyParrot"` the correct field name in Minecraft 1.21.1?**
- Our accessor seems to work (no exceptions)
- Console shows "Dancing=true"
- But parrots don't animate
- Should we use a different field name?
- Possible alternatives: `dancing`, `partyPos`, `isPartyingNearJukebox`?

### Question 3: Animation Requirements
**What are the EXACT requirements for parrot dancing animation to play?**
- We have:
  - `partyParrot = true` ✅
  - `setRecordPlayingNearby()` ✅
  - Music playing nearby ✅
  - Parrot within 3 blocks of "jukebox" position ✅
  - AI enabled (NoAI = false) ✅
- What are we missing?

### Question 4: Hoglin Celebration
**How to properly trigger hoglin celebration animation?**
- We're setting:
  ```java
  hoglin.getBrain().setMemory(
      MemoryModuleType.CELEBRATE_LOCATION,
      hoglin.blockPosition()
  );
  ```
- Is this correct?
- Are there other required memories?
- Is there a different method to trigger celebration?

### Question 5: NoAI vs Animations
**How to freeze entity position while keeping animations working?**
- NoAI = true → No movement ✅ BUT no animations ❌
- NoAI = false + Movement Speed = 0 → Still moves somehow ❌
- NoAI = false + Teleport every tick → Resets animations ❌
- **What's the correct approach?**

---

## 🎯 DESIRED OUTCOME

**We want:**
1. Animals spawn in circle around center
2. Animals are **completely frozen** in place (no walking, no flying, no floating)
3. Animals play their **victory animations:**
   - Parrots: Head bobbing, wing flapping (dancing to music)
   - Hoglins: Hopping, celebrating (victory dance)
4. Animals are glowing with team colors
5. Epic particles around them

**Currently have:**
- ✅ Spawning correctly
- ✅ Glowing with team colors
- ✅ Epic particles
- ✅ Dancing state is being set (confirmed by console)
- ❌ Animations not playing
- ❌ Movement not fully frozen (they wander/float)

---

## 🔍 TECHNICAL DETAILS

**Minecraft Version:** 1.21.1  
**Mod Loader:** Fabric 0.16.0  
**Mixin System:** SpongePowered Mixin 0.8.7  
**Language:** Java 21  

**Relevant Files:**
- `src/main/java/minigame/tnt_rocket_leauge/mixin/accessor/ParrotAccessor.java`
- `src/main/java/minigame/tnt_rocket_leauge/game/Game.java` (method: `spawnVictoryDancers()` and `tickBall()`)

**Dependencies:**
- Fabric API 0.116.7+1.21.1
- MixinExtras 0.4.0

---

## 💡 WHAT WE NEED

**Please help us figure out:**

1. **How to properly freeze entity movement** while keeping AI/animations active
2. **Why parrots don't dance** despite `partyParrot = true` being set
3. **How to trigger hoglin celebration** animation properly
4. **Alternative approaches** if current method is fundamentally flawed

---

## 📝 ADDITIONAL CONTEXT

- We've spent hours trying different approaches
- The accessor mixin DOES work (no errors, getter returns true)
- Animals spawn correctly and are visible
- Particle effects work perfectly
- Glowing effect works
- **Just the animations won't trigger!**

---

## 🚨 SPECIFIC HELP NEEDED

**Primary Question:**
> In Minecraft 1.21.1 Fabric modding, how do you make a Parrot entity dance (play its head-bobbing animation) while keeping it completely frozen in place?

**Secondary Question:**
> How do you trigger a Hoglin's celebration/victory dance animation using brain memories or other methods?

**Technical Constraints:**
- Must use Fabric (not Forge)
- Must work with vanilla animations (no custom animation mods)
- Entity must be frozen in exact position
- Must work reliably (not probabilistic)

---

## 🎯 CODE TO SHARE

**Current Parrot Spawning Code:**
```java
Parrot parrot = new Parrot(EntityType.PARROT, level);
parrot.setPos(x + 0.5, y, z + 0.5);
parrot.setSilent(true);
parrot.setInvulnerable(true);

// Movement freeze
parrot.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.0);
parrot.getAttribute(Attributes.FLYING_SPEED).setBaseValue(0.0);

// Visual
parrot.setVariant(Parrot.Variant.BLUE);
parrot.setGlowingTag(true);

// Dancing trigger
ParrotAccessor accessor = (ParrotAccessor) parrot;
BlockPos parrotPos = new BlockPos(x, y, z);
accessor.setJukebox(parrotPos);
accessor.setPartyParrot(true); // Console confirms this is set!
parrot.setRecordPlayingNearby(parrotPos, true);

level.addFreshEntity(parrot);

// Music playing
level.playSound(null, centerX, centerY, centerZ, MUSIC_DISC_PIGSTEP, RECORDS, 3.0f, 1.0f);
level.playSound(null, x, y, z, MUSIC_DISC_PIGSTEP, RECORDS, 0.0f, 1.0f);
```

**Current Hoglin Code:**
```java
Hoglin hoglin = new Hoglin(EntityType.HOGLIN, level);
hoglin.setPos(x + 0.5, y, z + 0.5);
hoglin.setSilent(true);
hoglin.setInvulnerable(true);

// Movement freeze
hoglin.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.0);

// Visual
hoglin.setGlowingTag(true);

// Celebration trigger
hoglin.getBrain().setMemory(
    MemoryModuleType.CELEBRATE_LOCATION,
    hoglin.blockPosition()
);

level.addFreshEntity(hoglin);
```

---

**Thank you for any help!**

We've exhausted our ideas and need expert advice on Minecraft entity animations! 🙏

