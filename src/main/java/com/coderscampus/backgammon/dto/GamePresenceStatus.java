package com.coderscampus.backgammon.dto;

public class GamePresenceStatus {
    private Long gameId;
    private Long user1Id;
    private Long user2Id;
    private boolean allConnected;
    private boolean user1Connected;
    private boolean user2Connected;
    private String disconnectedUserName;
    private long reconnectGraceSecondsRemaining;

    public Long getGameId() {
        return gameId;
    }

    public void setGameId(Long gameId) {
        this.gameId = gameId;
    }

    public Long getUser1Id() {
        return user1Id;
    }

    public void setUser1Id(Long user1Id) {
        this.user1Id = user1Id;
    }

    public Long getUser2Id() {
        return user2Id;
    }

    public void setUser2Id(Long user2Id) {
        this.user2Id = user2Id;
    }

    public boolean isAllConnected() {
        return allConnected;
    }

    public void setAllConnected(boolean allConnected) {
        this.allConnected = allConnected;
    }

    public boolean isUser1Connected() {
        return user1Connected;
    }

    public void setUser1Connected(boolean user1Connected) {
        this.user1Connected = user1Connected;
    }

    public boolean isUser2Connected() {
        return user2Connected;
    }

    public void setUser2Connected(boolean user2Connected) {
        this.user2Connected = user2Connected;
    }

    public String getDisconnectedUserName() {
        return disconnectedUserName;
    }

    public void setDisconnectedUserName(String disconnectedUserName) {
        this.disconnectedUserName = disconnectedUserName;
    }

    public long getReconnectGraceSecondsRemaining() {
        return reconnectGraceSecondsRemaining;
    }

    public void setReconnectGraceSecondsRemaining(long reconnectGraceSecondsRemaining) {
        this.reconnectGraceSecondsRemaining = reconnectGraceSecondsRemaining;
    }
}
