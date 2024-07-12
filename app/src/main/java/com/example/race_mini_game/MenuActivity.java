// MenuActivity.java
package com.example.race_mini_game;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MenuActivity extends AppCompatActivity {

    private static final String TAG = "MenuActivity";
    private Button startGameButton;
    private Button sensorModeButton;
    private Button fastSlowModeButton;
    private Button leaderboardsButton;
    private boolean fastMode;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        startGameButton = findViewById(R.id.startGameButton);
        sensorModeButton = findViewById(R.id.sensorModeButton);
        fastSlowModeButton = findViewById(R.id.fastSlowModeButton);
        leaderboardsButton = findViewById(R.id.leaderboardsButton);

        startGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MenuActivity.this, MainActivity.class);
                intent.putExtra("sensorMode", false);
                startActivity(intent);
            }
        });

        sensorModeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Sensor Mode button clicked.");
                Intent intent = new Intent(MenuActivity.this, MainActivity.class);
                intent.putExtra("sensorMode", true);
                startActivity(intent);
            }
        });


        fastSlowModeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!fastMode) {
                    fastMode = true;
                    Intent intent = new Intent(MenuActivity.this, MainActivity.class);
                    intent.putExtra("fastMode", true);
                    startActivity(intent);
                }
                else {
                    fastMode = false;
                }
                Intent intent = new Intent(MenuActivity.this, MainActivity.class);
                intent.putExtra("fastMode", false);

            }
        });

        leaderboardsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MenuActivity.this, LeaderboardActivity.class);
                startActivity(intent);
            }
        });
    }
}
