package minigame.tnt_rocket_leauge.game;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;

public class PlayerLoadout {
    
    public static void giveEquipment(ServerPlayer player, PlayerRole role) {
        // Clear inventory first
        player.getInventory().clearContent();
        
        // Get enchantment registry
        var enchantmentRegistry = player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        
        // ARMOR - Full Netherite with Protection 4, Unbreaking 3
        giveArmorPiece(player, Items.NETHERITE_HELMET, EquipmentSlot.HEAD, enchantmentRegistry);
        giveArmorPiece(player, Items.NETHERITE_CHESTPLATE, EquipmentSlot.CHEST, enchantmentRegistry);
        giveArmorPiece(player, Items.NETHERITE_LEGGINGS, EquipmentSlot.LEGS, enchantmentRegistry);
        giveArmorPiece(player, Items.NETHERITE_BOOTS, EquipmentSlot.FEET, enchantmentRegistry);
        
        // Role-specific equipment
        if (role == PlayerRole.ATTACKER) {
            // ATTACKERS: Sword + Wind Charges for offense
            
            // PVP: Give attackers a sword for combat
            ItemStack sword = new ItemStack(Items.NETHERITE_SWORD);
            addEnchantment(sword, enchantmentRegistry.getOrThrow(Enchantments.SHARPNESS), 5);
            addEnchantment(sword, enchantmentRegistry.getOrThrow(Enchantments.UNBREAKING), 3);
            player.getInventory().add(sword);
            
            // Wind charges for shooting the TNT (reduced power)
            player.getInventory().add(new ItemStack(Items.WIND_CHARGE, 32));
            player.getInventory().add(new ItemStack(Items.WIND_CHARGE, 32));
            
        } else if (role == PlayerRole.GOALIE) {
            // GOALIES: Shield only + INVULNERABLE (they defend the goal!)
            
            // Make goalie invulnerable to damage
            player.getAbilities().invulnerable = true;
            player.onUpdateAbilities();
            
            // SHIELD - Unbreaking 3
            ItemStack shield = new ItemStack(Items.SHIELD);
            addEnchantment(shield, enchantmentRegistry.getOrThrow(Enchantments.UNBREAKING), 3);
            player.getInventory().add(shield);
            
            // No wind charges for goalies - they focus on defense
        }
        
        // Food
        player.getInventory().add(new ItemStack(Items.GOLDEN_APPLE, 16));
        
        // NO flight needed - riding an Allay!
        player.getAbilities().mayfly = false;
        player.getAbilities().flying = false;
        player.getAbilities().invulnerable = false;
        player.onUpdateAbilities();
    }
    
    private static void giveArmorPiece(ServerPlayer player, net.minecraft.world.item.Item armorItem,
                                       EquipmentSlot slot, net.minecraft.core.Registry<Enchantment> registry) {
        ItemStack armor = new ItemStack(armorItem);
        addEnchantment(armor, registry.getOrThrow(Enchantments.PROTECTION), 4);
        addEnchantment(armor, registry.getOrThrow(Enchantments.UNBREAKING), 3);
        player.setItemSlot(slot, armor);
    }
    
    private static void addEnchantment(ItemStack stack, Holder<Enchantment> enchantment, int level) {
        stack.enchant(enchantment, level);
    }
}

