package com.example.race_mini_game;

import android.os.VibrationEffect;
import android.view.Gravity;
import android.view.View;
import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private RelativeLayout gameLayout;
    private ImageView car;
    private LinearLayout livesLayout;
    private Button leftButton, rightButton;
    private int lives = 3;
    private final Handler handler = new Handler();
    private Vibrator vibrator;

    private int currentLane = 1; // 0 - left, 1 - center, 2 - right

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "onCreate: Initializing views");
        findViews();
        initViews();
        Log.d(TAG, "onCreate: Starting game");
        startGame();
    }

    private void findViews() {
        gameLayout = findViewById(R.id.gameLayout);
        car = findViewById(R.id.car1);
        livesLayout = findViewById(R.id.livesLayout);
        leftButton = findViewById(R.id.left_button);
        rightButton = findViewById(R.id.right_button);
    }

    private void initViews() {
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        leftButton.setOnClickListener(v -> {
            Log.d(TAG, "onClick: Left button clicked");
            moveCar(-1);
        });
        rightButton.setOnClickListener(v -> {
            Log.d(TAG, "onClick: Right button clicked");
            moveCar(1);
        });
    }

    private void moveCar(int direction) {
        if ((currentLane == 0 && direction == -1) || (currentLane == 2 && direction == 1)) {
            return; // Cannot move out of bounds
        }
        currentLane += direction;
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) car.getLayoutParams();
        params.removeRule(RelativeLayout.ALIGN_PARENT_START);
        params.removeRule(RelativeLayout.CENTER_HORIZONTAL);
        params.removeRule(RelativeLayout.ALIGN_PARENT_END);
        if (currentLane == 0) {
            params.addRule(RelativeLayout.ALIGN_PARENT_START);
        } else if (currentLane == 1) {
            params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        } else {
            params.addRule(RelativeLayout.ALIGN_PARENT_END);
        }
        car.setLayoutParams(params);
    }

    private void startGame() {
        Log.d(TAG, "startGame: Starting obstacle generator");
        handler.post(obstacleGenerator);
    }

    private final Runnable obstacleGenerator = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "run: Generating obstacle");
            generateObstacle();
            handler.postDelayed(this, 2000);
        }
    };

    private void generateObstacle() {
        Log.d(TAG, "generateObstacle: Creating new obstacle");
        ImageView obstacle = new ImageView(MainActivity.this);
        obstacle.setImageResource(R.drawable.stop2);
        int lane = new Random().nextInt(3);
        int obstacleMargin = 75; // Align obstacle in lane
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                250, 250);
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        if (lane == 0) {
            params.addRule(RelativeLayout.ALIGN_PARENT_START);
            params.setMarginStart(obstacleMargin);
        } else if (lane == 1) {
            params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        } else {
            params.addRule(RelativeLayout.ALIGN_PARENT_END);
            params.setMarginEnd(obstacleMargin);
        }
        obstacle.setLayoutParams(params);
        gameLayout.addView(obstacle);

        checkCollision(obstacle);

        obstacle.animate().translationY(gameLayout.getHeight())
                .setDuration(3000)
                .withEndAction(() -> gameLayout.removeView(obstacle)).start();
    }

    private void checkCollision(ImageView obstacle) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Rect obstacleRect = new Rect();
                obstacle.getHitRect(obstacleRect);
                Rect carRect = new Rect();
                car.getHitRect(carRect);

                if (Rect.intersects(obstacleRect, carRect)) {
                    Log.d(TAG, "checkCollision: Collision detected");
                    Toast toast = Toast.makeText(MainActivity.this, "Crash!", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    if (vibrator.hasVibrator()) {
                        vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                    }

                    lives--;
                    updateLivesUI();

                    if (lives == 0) {
                        Log.d(TAG, "checkCollision: Game over, resetting game");
                        handler.removeCallbacks(obstacleGenerator);
                        handler.postDelayed(() -> resetGame(), 2000);
                    }
                } else {
                    handler.postDelayed(this, 50); // Check collision every 50ms
                }
            }
        });
    }

    private void updateLivesUI() {
        Log.d(TAG, "updateLivesUI: Updating lives display");
        for (int i = 0; i < livesLayout.getChildCount(); i++) {
            ImageView life = (ImageView) livesLayout.getChildAt(i);
            if (i < lives) {
                life.setVisibility(View.VISIBLE);
            } else {
                life.setVisibility(View.INVISIBLE);
            }
        }
    }

    private void resetGame() {
        Log.d(TAG, "resetGame: Resetting game");
        lives = 3;
        updateLivesUI();
        startGame();
    }
}
