package minigame.tnt_rocket_leauge.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import minigame.tnt_rocket_leauge.game.Team;
import minigame.tnt_rocket_leauge.structure.AreaType;
import minigame.tnt_rocket_leauge.structure.GameStructure;
import minigame.tnt_rocket_leauge.structure.StructureArea;
import minigame.tnt_rocket_leauge.structure.StructureManager;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class StructureCommand {
    
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("structure")
            .requires(source -> Commands.LEVEL_GAMEMASTERS.check(source.permissions()))
            
            // /structure create <id> [name]
            .then(Commands.literal("create")
                .then(Commands.argument("id", StringArgumentType.word())
                    .executes(ctx -> createStructure(ctx, StringArgumentType.getString(ctx, "id"), null))
                    .then(Commands.argument("name", StringArgumentType.greedyString())
                        .executes(ctx -> createStructure(ctx, 
                            StringArgumentType.getString(ctx, "id"),
                            StringArgumentType.getString(ctx, "name"))))))
            
            // /structure area add <structure_id> <area_name> <type> [team]
            .then(Commands.literal("area")
                .then(Commands.literal("add")
                    .then(Commands.argument("structure_id", StringArgumentType.word())
                        .then(Commands.argument("area_name", StringArgumentType.word())
                            .then(Commands.argument("type", StringArgumentType.word())
                                .suggests((ctx, builder) -> {
                                    for (AreaType type : AreaType.values()) {
                                        builder.suggest(type.name().toLowerCase());
                                    }
                                    return builder.buildFuture();
                                })
                                .executes(ctx -> addArea(ctx,
                                    StringArgumentType.getString(ctx, "structure_id"),
                                    StringArgumentType.getString(ctx, "area_name"),
                                    StringArgumentType.getString(ctx, "type"),
                                    null))
                                .then(Commands.argument("team", StringArgumentType.word())
                                    .suggests((ctx, builder) -> {
                                        builder.suggest("red");
                                        builder.suggest("blue");
                                        return builder.buildFuture();
                                    })
                                    .executes(ctx -> addArea(ctx,
                                        StringArgumentType.getString(ctx, "structure_id"),
                                        StringArgumentType.getString(ctx, "area_name"),
                                        StringArgumentType.getString(ctx, "type"),
                                        StringArgumentType.getString(ctx, "team"))))))))
                
                // /structure area setpos <structure_id> <area_name> <1|2>
                .then(Commands.literal("setpos")
                    .then(Commands.argument("structure_id", StringArgumentType.word())
                        .then(Commands.argument("area_name", StringArgumentType.word())
                            .then(Commands.argument("corner", StringArgumentType.word())
                                .suggests((ctx, builder) -> {
                                    builder.suggest("1");
                                    builder.suggest("2");
                                    return builder.buildFuture();
                                })
                                .executes(ctx -> setAreaCorner(ctx,
                                    StringArgumentType.getString(ctx, "structure_id"),
                                    StringArgumentType.getString(ctx, "area_name"),
                                    StringArgumentType.getString(ctx, "corner")))))))
                
                // /structure area list <structure_id>
                .then(Commands.literal("list")
                    .then(Commands.argument("structure_id", StringArgumentType.word())
                        .executes(ctx -> listAreas(ctx, StringArgumentType.getString(ctx, "structure_id"))))))
            
            // /structure save <id>
            .then(Commands.literal("save")
                .then(Commands.argument("id", StringArgumentType.word())
                    .executes(ctx -> saveStructure(ctx, StringArgumentType.getString(ctx, "id")))))
            
            // /structure load <id>
            .then(Commands.literal("load")
                .then(Commands.argument("id", StringArgumentType.word())
                    .executes(ctx -> loadStructure(ctx, StringArgumentType.getString(ctx, "id")))))
            
            // /structure delete <id>
            .then(Commands.literal("delete")
                .then(Commands.argument("id", StringArgumentType.word())
                    .executes(ctx -> deleteStructure(ctx, StringArgumentType.getString(ctx, "id")))))
            
            // /structure list
            .then(Commands.literal("list")
                .executes(StructureCommand::listStructures))
        );
    }
    
    private static int createStructure(CommandContext<CommandSourceStack> ctx, String id, String name) {
        StructureManager manager = StructureManager.getInstance();
        
        if (manager.getStructure(id) != null) {
            ctx.getSource().sendFailure(Component.literal("Structure '" + id + "' already exists!"));
            return 0;
        }
        
        String structureName = name != null ? name : id;
        manager.createStructure(id, structureName);
        
        ctx.getSource().sendSuccess(() ->
            Component.literal("✓ Created structure: ").withStyle(ChatFormatting.GREEN)
                .append(Component.literal(structureName).withStyle(ChatFormatting.GOLD))
                .append(Component.literal(" (ID: " + id + ")").withStyle(ChatFormatting.GRAY)),
            true);
        
        return 1;
    }
    
    private static int addArea(CommandContext<CommandSourceStack> ctx, String structureId, 
                               String areaName, String typeStr, String teamStr) {
        StructureManager manager = StructureManager.getInstance();
        GameStructure structure = manager.getStructure(structureId);
        
        if (structure == null) {
            ctx.getSource().sendFailure(Component.literal("Structure '" + structureId + "' not found!"));
            return 0;
        }
        
        // Parse area type
        AreaType type;
        try {
            type = AreaType.valueOf(typeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            ctx.getSource().sendFailure(Component.literal("Invalid area type! Valid types: " + 
                String.join(", ", java.util.Arrays.stream(AreaType.values())
                    .map(t -> t.name().toLowerCase()).toArray(String[]::new))));
            return 0;
        }
        
        // Parse team (optional)
        final Team finalTeam; // Make it final for lambda
        if (teamStr != null) {
            try {
                finalTeam = Team.valueOf(teamStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                ctx.getSource().sendFailure(Component.literal("Invalid team! Use 'red' or 'blue'."));
                return 0;
            }
        } else {
            finalTeam = null;
        }
        
        // Create and add area
        StructureArea area = new StructureArea(areaName, type, finalTeam);
        structure.addArea(area);
        
        ctx.getSource().sendSuccess(() ->
            Component.literal("✓ Added area: ").withStyle(ChatFormatting.GREEN)
                .append(Component.literal(areaName).withStyle(type.getChatColor()))
                .append(Component.literal(" (" + type.getDisplayName() + ")").withStyle(ChatFormatting.GRAY))
                .append(finalTeam != null ? 
                    Component.literal(" - Team: " + finalTeam.name()).withStyle(finalTeam.getColor()) : 
                    Component.empty())
                .append(Component.literal("\nNow set corners with: /structure area setpos " + structureId + " " + areaName + " 1").withStyle(ChatFormatting.YELLOW)),
            true);
        
        return 1;
    }
    
    private static int setAreaCorner(CommandContext<CommandSourceStack> ctx, String structureId,
                                     String areaName, String cornerStr) {
        StructureManager manager = StructureManager.getInstance();
        GameStructure structure = manager.getStructure(structureId);
        
        if (structure == null) {
            ctx.getSource().sendFailure(Component.literal("Structure '" + structureId + "' not found!"));
            return 0;
        }
        
        StructureArea area = structure.getArea(areaName);
        if (area == null) {
            ctx.getSource().sendFailure(Component.literal("Area '" + areaName + "' not found in structure!"));
            return 0;
        }
        
        // Get player position
        if (!(ctx.getSource().getEntity() instanceof ServerPlayer player)) {
            ctx.getSource().sendFailure(Component.literal("This command must be run by a player!"));
            return 0;
        }
        
        BlockPos pos = player.blockPosition();
        
        if (cornerStr.equals("1")) {
            area.setCorner1(pos);
            ctx.getSource().sendSuccess(() ->
                Component.literal("✓ Set corner 1 of ").withStyle(ChatFormatting.GREEN)
                    .append(Component.literal(areaName).withStyle(area.getType().getChatColor()))
                    .append(Component.literal(" to " + pos.toShortString()).withStyle(ChatFormatting.GRAY))
                    .append(area.isComplete() ? 
                        Component.literal("\n✓ Area is now complete!").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD) :
                        Component.literal("\nNow set corner 2!").withStyle(ChatFormatting.YELLOW)),
                true);
        } else if (cornerStr.equals("2")) {
            area.setCorner2(pos);
            ctx.getSource().sendSuccess(() ->
                Component.literal("✓ Set corner 2 of ").withStyle(ChatFormatting.GREEN)
                    .append(Component.literal(areaName).withStyle(area.getType().getChatColor()))
                    .append(Component.literal(" to " + pos.toShortString()).withStyle(ChatFormatting.GRAY))
                    .append(area.isComplete() ? 
                        Component.literal("\n✓ Area is now complete!").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD) :
                        Component.literal("\nNow set corner 1!").withStyle(ChatFormatting.YELLOW)),
                true);
        } else {
            ctx.getSource().sendFailure(Component.literal("Corner must be '1' or '2'!"));
            return 0;
        }
        
        return 1;
    }
    
    private static int listAreas(CommandContext<CommandSourceStack> ctx, String structureId) {
        StructureManager manager = StructureManager.getInstance();
        GameStructure structure = manager.getStructure(structureId);
        
        if (structure == null) {
            ctx.getSource().sendFailure(Component.literal("Structure '" + structureId + "' not found!"));
            return 0;
        }
        
        ctx.getSource().sendSuccess(() -> {
            Component msg = Component.literal("═══ Structure: " + structure.getName() + " ═══\n").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD);
            
            if (structure.getAreaCount() == 0) {
                msg = msg.copy().append(Component.literal("No areas defined yet.").withStyle(ChatFormatting.GRAY));
            } else {
                for (StructureArea area : structure.getAllAreas()) {
                    msg = msg.copy()
                        .append(Component.literal("• ").withStyle(ChatFormatting.GRAY))
                        .append(Component.literal(area.getName()).withStyle(area.getType().getChatColor(), ChatFormatting.BOLD))
                        .append(Component.literal(" - " + area.getType().getDisplayName()).withStyle(ChatFormatting.GRAY));
                    
                    if (area.getTeam() != null) {
                        msg = msg.copy().append(Component.literal(" [" + area.getTeam().name() + "]").withStyle(area.getTeam().getColor()));
                    }
                    
                    if (area.isComplete()) {
                        msg = msg.copy().append(Component.literal(" ✓").withStyle(ChatFormatting.GREEN));
                    } else {
                        msg = msg.copy().append(Component.literal(" ⚠ Incomplete").withStyle(ChatFormatting.YELLOW));
                    }
                    
                    msg = msg.copy().append(Component.literal("\n"));
                }
            }
            
            return msg;
        }, false);
        
        return 1;
    }
    
    private static int saveStructure(CommandContext<CommandSourceStack> ctx, String id) {
        StructureManager manager = StructureManager.getInstance();
        
        if (manager.getStructure(id) == null) {
            ctx.getSource().sendFailure(Component.literal("Structure '" + id + "' not found!"));
            return 0;
        }
        
        boolean success = manager.saveStructure(id);
        
        if (success) {
            ctx.getSource().sendSuccess(() ->
                Component.literal("✓ Saved structure: ").withStyle(ChatFormatting.GREEN)
                    .append(Component.literal(id).withStyle(ChatFormatting.GOLD)),
                true);
        } else {
            ctx.getSource().sendFailure(Component.literal("Failed to save structure!"));
        }
        
        return success ? 1 : 0;
    }
    
    private static int loadStructure(CommandContext<CommandSourceStack> ctx, String id) {
        StructureManager manager = StructureManager.getInstance();
        
        boolean success = manager.loadStructure(id);
        
        if (success) {
            GameStructure structure = manager.getStructure(id);
            ctx.getSource().sendSuccess(() ->
                Component.literal("✓ Loaded structure: ").withStyle(ChatFormatting.GREEN)
                    .append(Component.literal(structure.getName()).withStyle(ChatFormatting.GOLD))
                    .append(Component.literal(" (" + structure.getAreaCount() + " areas)").withStyle(ChatFormatting.GRAY)),
                true);
        } else {
            ctx.getSource().sendFailure(Component.literal("Failed to load structure '" + id + "'! File not found."));
        }
        
        return success ? 1 : 0;
    }
    
    private static int deleteStructure(CommandContext<CommandSourceStack> ctx, String id) {
        StructureManager manager = StructureManager.getInstance();
        
        if (manager.getStructure(id) == null) {
            ctx.getSource().sendFailure(Component.literal("Structure '" + id + "' not found!"));
            return 0;
        }
        
        manager.deleteStructure(id);
        
        ctx.getSource().sendSuccess(() ->
            Component.literal("✓ Deleted structure: ").withStyle(ChatFormatting.GREEN)
                .append(Component.literal(id).withStyle(ChatFormatting.RED)),
            true);
        
        return 1;
    }
    
    private static int listStructures(CommandContext<CommandSourceStack> ctx) {
        StructureManager manager = StructureManager.getInstance();
        var structures = manager.getAllStructures();
        
        if (structures.isEmpty()) {
            ctx.getSource().sendSuccess(() ->
                Component.literal("No structures loaded.").withStyle(ChatFormatting.GRAY),
                false);
            return 1;
        }
        
        ctx.getSource().sendSuccess(() -> {
            Component msg = Component.literal("═══ Loaded Structures ═══\n").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD);
            
            for (GameStructure structure : structures) {
                msg = msg.copy()
                    .append(Component.literal("• ").withStyle(ChatFormatting.GRAY))
                    .append(Component.literal(structure.getName()).withStyle(ChatFormatting.AQUA))
                    .append(Component.literal(" (ID: " + structure.getId() + ")").withStyle(ChatFormatting.DARK_GRAY))
                    .append(Component.literal(" - " + structure.getAreaCount() + " areas").withStyle(ChatFormatting.GRAY));
                
                if (structure.isComplete()) {
                    msg = msg.copy().append(Component.literal(" ✓").withStyle(ChatFormatting.GREEN));
                } else {
                    msg = msg.copy().append(Component.literal(" ⚠").withStyle(ChatFormatting.YELLOW));
                }
                
                msg = msg.copy().append(Component.literal("\n"));
            }
            
            return msg;
        }, false);
        
        return 1;
    }
}

