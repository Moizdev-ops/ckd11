package com.yourname.customkitduels.data;

import org.bukkit.entity.Player;

public class RoundsDuel {
    
    private final Player player1;
    private final Player player2;
    private final Kit kit;
    private final Arena arena;
    private final int targetRounds;
    private int player1Wins;
    private int player2Wins;
    private int currentRound;
    private final long startTime;
    private boolean isActive;
    
    public RoundsDuel(Player player1, Player player2, Kit kit, Arena arena, int targetRounds) {
        this.player1 = player1;
        this.player2 = player2;
        this.kit = kit;
        this.arena = arena;
        this.targetRounds = targetRounds;
        this.player1Wins = 0;
        this.player2Wins = 0;
        this.currentRound = 1;
        this.startTime = System.currentTimeMillis();
        this.isActive = true;
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
    
    public int getTargetRounds() {
        return targetRounds;
    }
    
    public int getPlayer1Wins() {
        return player1Wins;
    }
    
    public int getPlayer2Wins() {
        return player2Wins;
    }
    
    public int getCurrentRound() {
        return currentRound;
    }
    
    public long getStartTime() {
        return startTime;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void setActive(boolean active) {
        this.isActive = active;
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
    
    public void addWin(Player winner) {
        if (winner.equals(player1)) {
            player1Wins++;
        } else if (winner.equals(player2)) {
            player2Wins++;
        }
        currentRound++;
    }
    
    public boolean isComplete() {
        return player1Wins >= targetRounds || player2Wins >= targetRounds;
    }
    
    public Player getOverallWinner() {
        if (player1Wins >= targetRounds) {
            return player1;
        } else if (player2Wins >= targetRounds) {
            return player2;
        }
        return null;
    }
    
    public String getScoreString() {
        return player1.getName() + " " + player1Wins + " - " + player2Wins + " " + player2.getName();
    }
    
    public String getProgressString() {
        return "Round " + currentRound + " | " + getScoreString() + " | First to " + targetRounds;
    }
}