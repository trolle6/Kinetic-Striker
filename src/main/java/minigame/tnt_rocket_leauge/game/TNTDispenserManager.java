package minigame.tnt_rocket_leauge.game;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages TNT dispensers for the game.
 * Features:
 * - Auto-activation on countdown "GO!"
 * - Auto-refill with TNT before triggering
 * - 5-second spam prevention cooldown
 */
public class TNTDispenserManager {
    private final Map<BlockPos, Long> dispenserCooldowns = new HashMap<>();
    private static final long COOLDOWN_TICKS = 100; // 5 seconds
    
    /**
     * Activates a dispenser at the given position.
     * Automatically refills with TNT if empty and enforces cooldown.
     */
    public boolean activateDispenser(ServerLevel level, BlockPos pos) {
        // Check cooldown
        long currentTime = level.getGameTime();
        Long lastActivation = dispenserCooldowns.get(pos);
        
        if (lastActivation != null && (currentTime - lastActivation) < COOLDOWN_TICKS) {
            return false; // Still on cooldown
        }
        
        // Verify it's a dispenser
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof DispenserBlock)) {
            return false;
        }
        
        // Get dispenser block entity
        if (!(level.getBlockEntity(pos) instanceof DispenserBlockEntity dispenser)) {
            return false;
        }
        
        // Auto-refill with TNT if empty or low
        int tntCount = 0;
        for (int i = 0; i < dispenser.getContainerSize(); i++) {
            ItemStack stack = dispenser.getItem(i);
            if (stack.is(Items.TNT)) {
                tntCount += stack.getCount();
            }
        }
        
        // If no TNT, add some
        if (tntCount == 0) {
            // Find empty slot and add TNT
            for (int i = 0; i < dispenser.getContainerSize(); i++) {
                if (dispenser.getItem(i).isEmpty()) {
                    dispenser.setItem(i, new ItemStack(Items.TNT, 64));
                    break;
                }
            }
        }
        
        // Trigger dispenser by powering it
        level.setBlock(pos, state.setValue(DispenserBlock.TRIGGERED, true), 3);
        
        // Schedule block update to dispense
        level.scheduleTick(pos, state.getBlock(), 0);
        
        // Play sound
        level.playSound(null, pos, SoundEvents.DISPENSER_DISPENSE, SoundSource.BLOCKS, 1.0f, 1.0f);
        
        // Set cooldown
        dispenserCooldowns.put(pos, currentTime);
        
        return true;
    }
    
    /**
     * Auto-activates all dispensers in the playing area on "GO!"
     */
    public void activateAllDispensers(ServerLevel level, BlockPos corner1, BlockPos corner2) {
        int minX = Math.min(corner1.getX(), corner2.getX());
        int maxX = Math.max(corner1.getX(), corner2.getX());
        int minY = Math.min(corner1.getY(), corner2.getY());
        int maxY = Math.max(corner1.getY(), corner2.getY());
        int minZ = Math.min(corner1.getZ(), corner2.getZ());
        int maxZ = Math.max(corner1.getZ(), corner2.getZ());
        
        // Search for dispensers in the arena
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockState state = level.getBlockState(pos);
                    
                    if (state.getBlock() instanceof DispenserBlock) {
                        activateDispenser(level, pos);
                    }
                }
            }
        }
    }
    
    public void clearCooldowns() {
        dispenserCooldowns.clear();
    }
}

