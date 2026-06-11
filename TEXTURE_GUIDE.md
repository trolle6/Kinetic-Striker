# Texture Guide

## Creating the Boost Item Texture

You need to create a texture for the boost item. Place a 16x16 pixel PNG file at:
```
src/main/resources/assets/tnt_rocket_leauge/textures/item/boost.png
```

### Texture Suggestions:

1. **Rocket/Firework Icon**: A small rocket or firework sprite
2. **Wind/Speed Icon**: Swoosh lines indicating speed
3. **Fire Charge Style**: Similar to Minecraft's fire charge but with a different color
4. **Wing Icon**: Small wings or feathers

### Quick Option:
If you want to use an existing Minecraft texture temporarily, you can modify the model file:
```json
{
  "parent": "minecraft:item/generated",
  "textures": {
    "layer0": "minecraft:item/fire_charge"
  }
}
```

This will use the fire charge texture as a placeholder until you create your own.

### Creating Custom Texture:
1. Use any image editor (Paint.NET, GIMP, Photoshop, etc.)
2. Create a 16x16 pixel canvas
3. Draw your icon (keep it simple and pixelated for Minecraft style)
4. Save as PNG with transparency
5. Place in the textures/item folder

### Example Simple Design:
- Background: Transparent
- Main shape: Orange/red rocket or flame
- Accent: Yellow/white highlights for glow effect
- Optional: Add small particle effects around edges

