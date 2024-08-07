package com.example.race_mini_game.Models;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Leaderboards {     // Leaderboards class

    private static final String PREF_NAME = "Leaderboards";
    private static final String KEY_RECORDS = "Records";
    private static final int MAX_RECORDS = 10;

    private SharedPreferences sharedPreferences;
    private Gson gson;

    public Leaderboards(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public List<Record> getTopScores() {
        String json = sharedPreferences.getString(KEY_RECORDS, null);
        Type type = new TypeToken<ArrayList<Record>>() {}.getType();
        List<Record> records = gson.fromJson(json, type);

        if (records == null) {
            records = new ArrayList<>();
        }

        Collections.sort(records);
        return records.subList(0, Math.min(records.size(), MAX_RECORDS));
    }

    public boolean isHighScore(int score) {
        List<Record> topScores = getTopScores();
        return topScores.isEmpty() || topScores.size() < MAX_RECORDS || score > topScores.get(topScores.size() - 1).getScore();
    }

    public void addScore(String playerName, int score, double latitude, double longitude) {
        List<Record> scores = getTopScores();
        scores.add(new Record(playerName, score, latitude, longitude));
        Collections.sort(scores);

        if (scores.size() > MAX_RECORDS) {
            scores = scores.subList(0, MAX_RECORDS);
        }

        String json = gson.toJson(scores);
        sharedPreferences.edit().putString(KEY_RECORDS, json).apply();
    }
}