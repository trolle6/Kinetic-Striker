package minigame.tnt_rocket_leauge.structure;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.server.MinecraftServer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages loading and saving of game structures.
 * Structures are stored as NBT files in the world's data folder.
 */
public class StructureManager {
    private static StructureManager instance;
    private final Map<String, GameStructure> loadedStructures = new HashMap<>();
    private MinecraftServer server;
    
    private StructureManager() {}
    
    public static StructureManager getInstance() {
        if (instance == null) {
            instance = new StructureManager();
        }
        return instance;
    }
    
    public void setServer(MinecraftServer server) {
        this.server = server;
    }
    
    public void createStructure(String id, String name) {
        GameStructure structure = new GameStructure(id, name);
        loadedStructures.put(id, structure);
    }
    
    public GameStructure getStructure(String id) {
        return loadedStructures.get(id);
    }
    
    public void deleteStructure(String id) {
        loadedStructures.remove(id);
        
        // Also delete from disk if exists
        if (server != null) {
            File structureFile = getStructureFile(id);
            if (structureFile.exists()) {
                structureFile.delete();
            }
        }
    }
    
    public Collection<GameStructure> getAllStructures() {
        return loadedStructures.values();
    }
    
    // Save to NBT file
    public boolean saveStructure(String id) {
        GameStructure structure = loadedStructures.get(id);
        if (structure == null || server == null) {
            return false;
        }
        
        try {
            File structureFile = getStructureFile(id);
            structureFile.getParentFile().mkdirs();
            
            CompoundTag tag = structure.toNBT();
            NbtIo.writeCompressed(tag, structureFile.toPath());
            
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Load from NBT file
    public boolean loadStructure(String id) {
        if (server == null) {
            return false;
        }
        
        try {
            File structureFile = getStructureFile(id);
            if (!structureFile.exists()) {
                return false;
            }
            
            CompoundTag tag = NbtIo.readCompressed(structureFile.toPath(), NbtAccounter.unlimitedHeap());
            GameStructure structure = GameStructure.fromNBT(tag);
            loadedStructures.put(id, structure);
            
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Export to JSON
    public boolean exportToJSON(String id, String filePath) {
        GameStructure structure = loadedStructures.get(id);
        if (structure == null) {
            return false;
        }
        
        try {
            File jsonFile = new File(filePath);
            jsonFile.getParentFile().mkdirs();
            
            try (FileWriter writer = new FileWriter(jsonFile)) {
                writer.write(structure.toJSON());
            }
            
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Import from JSON
    public boolean importFromJSON(String filePath) {
        try {
            File jsonFile = new File(filePath);
            if (!jsonFile.exists()) {
                return false;
            }
            
            String json = Files.readString(jsonFile.toPath());
            GameStructure structure = GameStructure.fromJSON(json);
            loadedStructures.put(structure.getId(), structure);
            
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    private File getStructureFile(String id) {
        File worldDir = server.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT).toFile();
        File structuresDir = new File(worldDir, "tntrl_structures");
        return new File(structuresDir, id + ".dat");
    }
    
    public void clearAll() {
        loadedStructures.clear();
    }
}

