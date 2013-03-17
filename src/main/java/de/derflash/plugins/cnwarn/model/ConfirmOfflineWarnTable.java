package de.derflash.plugins.cnwarn.model;

public class ConfirmOfflineWarnTable {
    public String playerName;
    public String message;
    public int rating;

    public ConfirmOfflineWarnTable(String playerName, String message, Integer rating) {
        this.playerName = playerName;
        this.message = message;
        this.rating = rating;
    }
}
