# TNT Rocket League

A Minecraft Fabric mod that creates a Rocket League / Harry Potter Quidditch-style minigame using primed TNT, elytras, and fishing rods!

## Features

- **Persistent Primed TNT Ball**: TNT that never explodes and acts as the game ball
- **Fishing Rod Mechanics**: Hook and pull the TNT ball with fishing rods
- **Elytra Boost System**: Custom boost item for enhanced aerial gameplay
- **Team-based Gameplay**: Red vs Blue teams with goal scoring
- **Particle Effects**: Visual effects for goals, hits, and boosts
- **Arena System**: Customizable arenas with goal zones

## Installation

1. Install [Fabric Loader](https://fabricmc.net/use/)
2. Install [Fabric API](https://www.curseforge.com/minecraft/mc-mods/fabric-api)
3. Place this mod's JAR file in your `mods` folder
4. Launch Minecraft with the Fabric profile

## How to Play

### Setting Up an Arena

1. **Define the arena boundaries** (requires OP/admin):
   ```
   /tntrl setarena <corner1> <corner2>
   ```
   Example: `/tntrl setarena ~-50 ~ ~-30 ~50 ~20 ~30`

2. **Set the RED team goal**:
   ```
   /tntrl setgoal red <corner1> <corner2>
   ```
   Example: `/tntrl setgoal red ~-50 ~ ~-5 ~-48 ~10 ~5`

3. **Set the BLUE team goal**:
   ```
   /tntrl setgoal blue <corner1> <corner2>
   ```
   Example: `/tntrl setgoal blue ~48 ~ ~-5 ~50 ~10 ~5`

### Starting a Game

1. **Start the game** (requires OP/admin):
   ```
   /tntrl start
   ```

2. **Players join teams**:
   ```
   /tntrl join red
   /tntrl join blue
   ```

3. **Play!**
   - Hit the TNT ball to move it
   - Use fishing rods to hook and pull the ball
   - Fly with elytras for aerial gameplay
   - Use the boost item while flying with elytra for speed bursts
   - Score goals by getting the ball into the opposing team's goal!

### Other Commands

- `/tntrl score` - Show current score
- `/tntrl leave` - Leave the current game
- `/tntrl stop` - End the game (requires OP/admin)

## Gameplay Mechanics

### TNT Ball Physics
- The TNT ball has custom physics with air resistance and gravity
- It bounces off arena boundaries
- When hit by a player, it receives strong knockback based on the player's momentum
- Leaves a particle trail for better visibility

### Fishing Rod Interaction
- Right-click to cast your fishing rod at the TNT ball
- The ball will be pulled towards you while hooked
- Great for controlling the ball or setting up shots

### Elytra Boost
- Obtain the boost item: `/give @s tnt_rocket_leauge:boost`
- Equip an elytra and start flying
- Use (right-click) the boost item while flying to get a speed burst in the direction you're looking
- Has a 2-second cooldown

### Scoring
- Get the TNT ball into the opposing team's goal to score
- Red team scores in the BLUE goal
- Blue team scores in the RED goal
- Each goal triggers a celebration with particles and sounds
- The ball respawns at the center after each goal

## Tips for Arena Design

1. **Size**: A field roughly 100x40x60 blocks works well
2. **Goals**: 10x10x3 blocks on opposite ends
3. **Height**: Allow plenty of vertical space for elytra flight (at least 20 blocks)
4. **Walls**: Build walls around the arena to contain the ball
5. **Spawn Platform**: Create a safe platform at the center spawn point

## Example Arena Setup

```
# Step 1: Build a rectangular field with walls
# Step 2: Set arena bounds
/tntrl setarena -50 60 -30 50 100 30

# Step 3: Set goals at each end
/tntrl setgoal red -50 60 -5 -48 70 5
/tntrl setgoal blue 48 60 -5 50 70 5

# Step 4: Start the game
/tntrl start

# Step 5: Players join teams and get equipment
/tntrl join red
/give @a elytra
/give @a fishing_rod
/give @a tnt_rocket_leauge:boost
```

## Credits

Created for fun multiplayer minigame action!

## License

See LICENSE.txt

