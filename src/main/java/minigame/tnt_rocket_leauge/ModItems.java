package minigame.tnt_rocket_leauge;

import net.minecraft.world.item.Items;

public class ModItems {
    // Use vanilla items instead of custom ones to avoid registry sync issues
    public static final net.minecraft.world.item.Item BOOST_ITEM = Items.FIREWORK_ROCKET;
    
    public static void initialize() {
        // No custom item registration needed - using vanilla items
    }
}

