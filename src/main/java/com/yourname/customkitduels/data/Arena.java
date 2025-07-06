package com.yourname.customkitduels.data;

import org.bukkit.Location;

public class Arena {
    
    private final String name;
    private Location pos1;
    private Location pos2;
    private Location spawn1;
    private Location spawn2;
    private boolean regeneration;
    private String schematicName;
    
    public Arena(String name) {
        this.name = name;
        this.regeneration = false;
        this.schematicName = name.toLowerCase() + "_arena";
    }
    
    public String getName() {
        return name;
    }
    
    public Location getPos1() {
        return pos1;
    }
    
    public void setPos1(Location pos1) {
        this.pos1 = pos1;
    }
    
    public Location getPos2() {
        return pos2;
    }
    
    public void setPos2(Location pos2) {
        this.pos2 = pos2;
    }
    
    public Location getSpawn1() {
        return spawn1;
    }
    
    public void setSpawn1(Location spawn1) {
        this.spawn1 = spawn1;
    }
    
    public Location getSpawn2() {
        return spawn2;
    }
    
    public void setSpawn2(Location spawn2) {
        this.spawn2 = spawn2;
    }
    
    public boolean hasRegeneration() {
        return regeneration;
    }
    
    public void setRegeneration(boolean regeneration) {
        this.regeneration = regeneration;
    }
    
    public String getSchematicName() {
        return schematicName;
    }
    
    public void setSchematicName(String schematicName) {
        this.schematicName = schematicName;
    }
    
    public boolean isComplete() {
        return pos1 != null && pos2 != null && spawn1 != null && spawn2 != null;
    }
    
    public boolean isRegenerationReady() {
        return isComplete() && regeneration && schematicName != null && !schematicName.isEmpty();
    }
}