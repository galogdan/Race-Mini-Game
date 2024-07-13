package com.example.race_mini_game;

import android.content.DialogInterface;
import android.location.Location;
import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.location.FusedLocationProviderClient;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.race_mini_game.Interfaces.MoveCallback;
import com.example.race_mini_game.Logic.GameManager;
import com.example.race_mini_game.Utils.MoveDetector;
import com.example.race_mini_game.Models.Leaderboards;
import com.google.android.gms.location.LocationServices;

public class MainActivity extends AppCompatActivity implements GameManager.GameCallback {

    private static final String TAG = "MainActivity";
    private static final int PERMISSION_REQUEST_VIBRATE = 1001;
    private static final int PERMISSION_REQUEST_LOCATION = 1002;
    private Location lastKnownLocation;
    private FusedLocationProviderClient fusedLocationClient;
    private RelativeLayout gameLayout;
    private ImageView playerView;
    private Button leftButton, rightButton;
    private ImageView[] heartViews;
    private TextView scoreTextView;
    private TextView distanceTextView;

    private int playerWidth;
    private int playerHeight;
    private int obstacleWidth;
    private int obstacleHeight;
    private int coinWidth;
    private int coinHeight;
    private GameManager gameManager;
    private MoveDetector moveDetector;
    private boolean sensorMode;
    private boolean fastMode;
    private Leaderboards leaderboards;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        float density = getResources().getDisplayMetrics().density;
        playerWidth = (int) (65f * density);
        playerHeight = (int) (65f * density);
        obstacleWidth = (int) (30f * density);
        obstacleHeight = (int) (100f * density);
        coinWidth = (int) (30f * density); // Define coin width
        coinHeight = (int) (30f * density); // Define coin height
        Intent intent = getIntent();
        sensorMode = intent.getBooleanExtra("sensorMode", false);
        fastMode = intent.getBooleanExtra("fastMode", false);

        if (sensorMode) {

            // Initialize MoveDetector
            moveDetector = new MoveDetector(this, new MoveCallback() {
                @Override
                public void onMoveLeft() {
                    movePlayerLeft();
                }

                @Override
                public void onMoveRight() {
                    movePlayerRight();
                }
            });
        }
        initializeViews();

        leaderboards = new Leaderboards(this);
        gameManager = new GameManager(this, gameLayout, playerView, heartViews,
                playerWidth, playerHeight, obstacleWidth, obstacleHeight, coinWidth, coinHeight, fastMode);
        gameManager.setGameCallback(this);
        gameManager.setLeaderboards(leaderboards);

        // Set the distance change listener
        gameManager.setOnDistanceChangeListener(distance -> runOnUiThread(() -> distanceTextView.setText(String.format("Distance: %.2f Km", distance))));

        requestVibratePermission();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        requestLocationPermission();

        gameLayout.post(this::calculateLanePositions);
    }

    private void movePlayerRight() {
        movePlayer(1);
    }

    private void movePlayerLeft() {
        movePlayer(-1);
    }

    private void initializeViews() {

        gameLayout = findViewById(R.id.gameLayout);
        playerView = findViewById(R.id.car1);
        leftButton = findViewById(R.id.left_button);
        rightButton = findViewById(R.id.right_button);
        heartViews = new ImageView[]{
                findViewById(R.id.life1),
                findViewById(R.id.life2),
                findViewById(R.id.life3)
        };
        scoreTextView = findViewById(R.id.scoreTextView);
        distanceTextView = findViewById(R.id.distanceTextView);

        leftButton.setOnClickListener(v -> movePlayer(-1));
        rightButton.setOnClickListener(v -> movePlayer(1));
        if (sensorMode) {
            findViewById(R.id.left_button).setVisibility(View.GONE);
            findViewById(R.id.right_button).setVisibility(View.GONE);
        }
    }

    private void calculateLanePositions() {
        gameManager.calculateLanePositions();
    }

    private void requestVibratePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.VIBRATE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.VIBRATE},
                    PERMISSION_REQUEST_VIBRATE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_VIBRATE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Vibration permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Vibration permission denied. Some features may be limited.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void movePlayer(int direction) {
        gameManager.movePlayer(direction);
    }

    private void startGame() {
        gameManager.startGame();
    }

    private void stopGame() {
        gameManager.stopGame();
    }

    @Override
    public void onGameOver() {
        runOnUiThread(() -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Game Over")
                    .setMessage("You've lost all your lives!")
                    .setCancelable(false)
                    .setPositiveButton("Restart", (dialog, id) -> {
                        dialog.dismiss();
                        restartGame();
                    });

            AlertDialog gameOverDialog = builder.create();
            gameOverDialog.show();
        });
    }

    @Override
    public void onLivesUpdated(int lives) {
        runOnUiThread(() -> {
            for (int i = 0; i < heartViews.length; i++) {
                heartViews[i].setVisibility(i < lives ? View.VISIBLE : View.INVISIBLE);
            }
        });
    }

    @Override
    public void onScoreUpdated(int score) {
        runOnUiThread(() -> scoreTextView.setText("Score: " + score));
    }

    public void onNewHighScore(int finalScore) {
        Log.d("MainActivity", "onNewHighScore called with score: " + finalScore);
        runOnUiThread(() -> showHighScoreDialog(finalScore));
    }

    private void showHighScoreDialog(int finalScore) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("New High Score!");
        builder.setMessage("Congratulations! You've achieved a high score of " + finalScore);

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_activity, null);
        final EditText input = dialogView.findViewById(R.id.edit_text_player_name);
        final TextView errorText = dialogView.findViewById(R.id.text_error_message);
        builder.setView(dialogView);

        builder.setCancelable(false); // Prevent dialog from closing on outside touch

        final AlertDialog dialog = builder.create();

        dialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", (dialogInterface, i) -> {
            // This will be overridden
        });

        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", (dialogInterface, i) -> {
            dialog.dismiss();
            returnToMenu();
        });

        dialog.show();

        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v -> {
            String playerName = input.getText().toString().trim();
            if (playerName.isEmpty()) {
                errorText.setText("Please enter a name");
                errorText.setVisibility(View.VISIBLE);
            } else {
                if (lastKnownLocation != null) {
                    leaderboards.addScore(playerName, finalScore, lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                } else {
                    leaderboards.addScore(playerName, finalScore, 0, 0); // Use default coordinates if location is unavailable
                }
                dialog.dismiss();
                openLeaderboard();
            }
        });
    }


    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (fusedLocationClient != null) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            lastKnownLocation = location;
                        }
                    });
        }
    }

    private void openLeaderboard() {
        Intent intent = new Intent(MainActivity.this, LeaderboardActivity.class);
        startActivity(intent);
        finish();
    }

    private void returnToMenu() {
        Intent intent = new Intent(MainActivity.this, MenuActivity.class);
        startActivity(intent);
        finish();
    }

    public void onGameOver(int finalScore) {
        runOnUiThread(() -> showGameOverDialog(finalScore));
    }

    private void showGameOverDialog(int finalScore) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Game Over")
                .setMessage("Your final score: " + finalScore)
                .setPositiveButton("Restart", (dialog, id) -> restartGame())
                .setNegativeButton("Main Menu", (dialog, id) -> returnToMenu());

        builder.setCancelable(false);
        AlertDialog dialog = builder.create();

        dialog.show();
    }

    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_REQUEST_LOCATION);
        } else {
            startLocationUpdates();
        }
    }



    private void restartGame() {
        stopGame();
        startGame();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!gameManager.isGameRunning()) {
            startGame();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopGame();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!gameManager.isGameRunning()) {
            startGame();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopGame();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        gameManager.release();
        stopGame();
    }
}
