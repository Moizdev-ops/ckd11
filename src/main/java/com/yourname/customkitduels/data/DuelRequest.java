package com.yourname.customkitduels.data;

import org.bukkit.entity.Player;

public class DuelRequest {
    
    private final Player challenger;
    private final Player target;
    private final Kit kit;
    private final Arena arena;
    private final long timestamp;
    
    public DuelRequest(Player challenger, Player target, Kit kit, Arena arena) {
        this.challenger = challenger;
        this.target = target;
        this.kit = kit;
        this.arena = arena;
        this.timestamp = System.currentTimeMillis();
    }
    
    public Player getChallenger() {
        return challenger;
    }
    
    public Player getTarget() {
        return target;
    }
    
    public Kit getKit() {
        return kit;
    }
    
    public Arena getArena() {
        return arena;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
}