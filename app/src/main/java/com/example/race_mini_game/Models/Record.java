package com.example.race_mini_game.Models;

public class Record implements Comparable<Record> {

    private String playerName;
    private int score;
    private double latitude;
    private double longitude;

    public Record(String playerName, int score, double latitude, double longitude) {
        this.playerName = playerName;
        this.score = score;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getName() {
        return playerName;
    }

    public int getScore() {
        return score;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    @Override
    public int compareTo(Record other) {
        return Integer.compare(other.score, this.score); // For descending order
    }
}