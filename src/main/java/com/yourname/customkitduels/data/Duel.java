package com.yourname.customkitduels.data;

import org.bukkit.entity.Player;

public class Duel {
    
    private final Player player1;
    private final Player player2;
    private final Kit kit;
    private final Arena arena;
    private final long startTime;
    
    public Duel(Player player1, Player player2, Kit kit, Arena arena) {
        this.player1 = player1;
        this.player2 = player2;
        this.kit = kit;
        this.arena = arena;
        this.startTime = System.currentTimeMillis();
    }
    
    public Player getPlayer1() {
        return player1;
    }
    
    public Player getPlayer2() {
        return player2;
    }
    
    public Kit getKit() {
        return kit;
    }
    
    public Arena getArena() {
        return arena;
    }
    
    public long getStartTime() {
        return startTime;
    }
    
    public Player getOpponent(Player player) {
        if (player.equals(player1)) {
            return player2;
        } else if (player.equals(player2)) {
            return player1;
        }
        return null;
    }
    
    public boolean isParticipant(Player player) {
        return player.equals(player1) || player.equals(player2);
    }
}