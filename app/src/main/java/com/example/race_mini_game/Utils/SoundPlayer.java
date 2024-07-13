package com.example.race_mini_game.Utils;

import android.content.Context;
import android.media.MediaPlayer;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class SoundPlayer {      // SoundPlayer for sound effects

    private Context context;
    private Executor executor;
    private MediaPlayer mediaPlayer;

    public SoundPlayer(Context context) {
        this.context = context;
        this.executor = Executors.newSingleThreadExecutor();
    }

    public void playSound(int resID) {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        executor.execute(() -> {
            mediaPlayer = MediaPlayer.create(context, resID);
            mediaPlayer.setVolume(1.0f, 1.0f);
            mediaPlayer.start();
            });
    }

    public void stopSound() {
        if (mediaPlayer != null){
            executor.execute(() ->{
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
            });
        }
    }
}
