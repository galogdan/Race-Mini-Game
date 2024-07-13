package com.example.race_mini_game.Logic;

import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.SystemClock;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.example.race_mini_game.Models.Leaderboards;
import com.example.race_mini_game.R;
import com.example.race_mini_game.Utils.SoundPlayer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class GameManager {      // Handles the game logic

    private static final String TAG = "GameManager";
    private static final int LANES = 5;
    private static final int INITIAL_LIVES = 3;
    private static final float OBSTACLE_SPAWN_CHANCE = 0.3f;
    private static final float OBSTACLE_SPEED = 600f;
    private static final long VIBRATION_DURATION = 500;
    private static final int MAX_OBSTACLES = 3;
    private static final long OBSTACLE_SPAWN_DELAY = 500; // 0.5 seconds
    private static final long GAME_LOOP_DELAY = 8;

    private Context context;
    private RelativeLayout gameLayout;
    private ImageView playerView;
    private ArrayList<ImageView> obstacles;
    private ArrayList<ImageView> coins;
    private ImageView[] heartViews;
    private float[] lanePositions;
    private int playerLane;
    private int lives;
    private int score;
    private long lastObstacleSpawnTime;
    private long lastCoinSpawnTime;
    private boolean isGameRunning;
    private Random random;
    private SoundPlayer soundPlayer;
    private int playerWidth;
    private int playerHeight;
    private int obstacleWidth;
    private int obstacleHeight;
    private int coinWidth;
    private int coinHeight;
    private GameCallback gameCallback;
    private long lastUpdateTime;
    private float distance;
    private OnDistanceChangeListener distanceChangeListener;
    private Leaderboards leaderboards;
    private boolean fastMode;

    private Handler gameLoopHandler;
    private Runnable gameLoopRunnable;


    public interface GameCallback {
        void onGameOver();
        void onLivesUpdated(int lives);
        void onScoreUpdated(int score);
        void onGameOver(int finalScore);
        void onNewHighScore(int finalScore);
    }

    public interface OnDistanceChangeListener {
        void onDistanceChange(float distance);
    }

    public void setOnDistanceChangeListener(OnDistanceChangeListener listener) {
        this.distanceChangeListener = listener;
    }

    public GameManager(Context context, RelativeLayout gameLayout, ImageView playerView,
                       ImageView[] heartViews, int playerWidth, int playerHeight,
                       int obstacleWidth, int obstacleHeight, int coinWidth, int coinHeight, boolean fastMode) {
        this.context = context;
        this.gameLayout = gameLayout;
        this.playerView = playerView;
        this.heartViews = heartViews;
        this.playerWidth = playerWidth;
        this.playerHeight = playerHeight;
        this.obstacleWidth = obstacleWidth;
        this.obstacleHeight = obstacleHeight;
        this.coinWidth = coinWidth;
        this.coinHeight = coinHeight;
        this.obstacles = new ArrayList<>();
        this.coins = new ArrayList<>();
        this.random = new Random();
        this.fastMode = fastMode;
        initializeSounds();
        initializeGameLoop();
    }



    public void setGameCallback(GameCallback callback) {
        this.gameCallback = callback;
    }

    private void initializeGame() {
        playerLane = LANES / 2;
        lives = INITIAL_LIVES;
        lastObstacleSpawnTime = 0;
        lastCoinSpawnTime = 0;
        score = 0;
        distance = 0;
        updateLives();
        updatePlayerPosition();
        clearObstacles();
        clearCoins();
        if (gameCallback != null) {
            gameCallback.onScoreUpdated(score);
        }
        if (distanceChangeListener != null) {
            distanceChangeListener.onDistanceChange(distance);
        }
    }

    private void initializeSounds() {
        soundPlayer = new SoundPlayer(this.context) ;
    }

    private void initializeGameLoop() {
        gameLoopHandler = new Handler();
        gameLoopRunnable = new Runnable() {
            @Override
            public void run() {
                if (isGameRunning) {
                    long currentTime = SystemClock.elapsedRealtime();
                    long deltaTime = currentTime - lastUpdateTime;
                    lastUpdateTime = currentTime;
                    update(deltaTime);
                    gameLoopHandler.postDelayed(this, GAME_LOOP_DELAY);
                }
            }
        };
    }

    public void calculateLanePositions() {
        int gameWidth = gameLayout.getWidth();
        float laneWidth = gameWidth / (float) LANES;

        lanePositions = new float[LANES];
        for (int i = 0; i < LANES; i++) {
            lanePositions[i] = (i + 0.5f) * laneWidth;
        }

        updatePlayerPosition();
    }

    public void updatePlayerPosition() {
        if (lanePositions != null && playerLane >= 0 && playerLane < LANES) {
            float targetX = lanePositions[playerLane];
            playerView.setX(targetX - (playerWidth / 2f));
        }
    }

    public void movePlayer(int direction) {
        int newLane = playerLane + direction;
        if (newLane >= 0 && newLane < LANES) {
            playerLane = newLane;
            updatePlayerPosition();
        }
    }

    private void updateLives() {
        for (int i = 0; i < heartViews.length; i++) {
            heartViews[i].setVisibility(i < lives ? View.VISIBLE : View.INVISIBLE);
        }
        if (gameCallback != null) {
            gameCallback.onLivesUpdated(lives);
        }
    }

    public void spawnObstacle() {
        if (lanePositions == null || lanePositions.length == 0) {
            Log.e(TAG, "Lane positions not initialized");
            return;
        }

        ImageView obstacle = new ImageView(context);
        obstacle.setImageResource(R.drawable.stop2);
        obstacle.setTag("uncollided");

        int lane = random.nextInt(LANES);
        float targetX = lanePositions[lane];
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(obstacleWidth, obstacleHeight);
        params.leftMargin = (int) (targetX - (obstacleWidth / 2f));
        params.topMargin = -obstacleHeight;
        gameLayout.addView(obstacle, params);
        obstacles.add(obstacle);

    }

    public void spawnCoin() {
        if (lanePositions == null || lanePositions.length == 0) {
            return;
        }

        ImageView coin = new ImageView(context);
        coin.setImageResource(R.drawable.coin);  // Ensure you have a coin drawable resource
        coin.setTag("uncollected");

        int lane = random.nextInt(LANES);
        float targetX = lanePositions[lane];
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(coinWidth, coinHeight);
        params.leftMargin = (int) (targetX - (coinWidth / 2f));
        params.topMargin = -coinHeight;
        gameLayout.addView(coin, params);
        coins.add(coin);
    }

    public boolean checkCollision() {
        for (ImageView obstacle : obstacles) {
            if ("collided".equals(obstacle.getTag())) {
                continue;
            }

            float obstacleLeft = obstacle.getX();
            float obstacleRight = obstacleLeft + obstacleWidth;
            float obstacleTop = obstacle.getY();
            float obstacleBottom = obstacleTop + obstacleHeight;

            float playerLeft = playerView.getX();
            float playerRight = playerLeft + playerWidth;
            float playerTop = playerView.getY();
            float playerBottom = playerTop + playerHeight;

            if (playerLeft < obstacleRight && playerRight > obstacleLeft &&
                    playerTop < obstacleBottom && playerBottom > obstacleTop) {
                obstacle.setTag("collided");
                return true;
            }
        }
        return false;
    }

    public boolean checkCoinCollection() {
        for (int i = coins.size() - 1; i >= 0; i--) {
            ImageView coin = coins.get(i);
            if ("collected".equals(coin.getTag())) {
                continue;
            }

            float coinLeft = coin.getX();
            float coinRight = coinLeft + coinWidth;
            float coinTop = coin.getY();
            float coinBottom = coinTop + coinHeight;

            float playerLeft = playerView.getX();
            float playerRight = playerLeft + playerWidth;
            float playerTop = playerView.getY();
            float playerBottom = playerTop + playerHeight;

            if (playerLeft < coinRight && playerRight > coinLeft &&
                    playerTop < coinBottom && playerBottom > coinTop) {
                coin.setTag("collected");
                gameLayout.removeView(coin);
                coins.remove(i);
                score += 100; // Increment the score by 100 for each coin collected
                if (gameCallback != null) {
                    gameCallback.onScoreUpdated(score);
                }
                return true;
            }
        }
        return false;
    }

    public void handleCollision() {
        lives--;
        updateLives();

        soundPlayer.playSound(R.raw.crash_sound);
        vibrate();
        soundPlayer.stopSound();

        // show toast message on collision
        Toast.makeText(context, "Collision detected!", Toast.LENGTH_SHORT).show();

        if (lives <= 0) {
            gameOver();
        }
    }

    private void vibrate() {
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.VIBRATE)
                == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null && vibrator.hasVibrator()) {
                vibrator.vibrate(VibrationEffect.createOneShot(VIBRATION_DURATION, VibrationEffect.DEFAULT_AMPLITUDE));
            }
        }
    }

    public void gameOver() {
        isGameRunning = false;
        boolean isHighScore = leaderboards.isHighScore(score);
        if (isHighScore) {
            if (gameCallback != null) {
                gameCallback.onNewHighScore(score);
            }
        } else {
            if (gameCallback != null) {
                gameCallback.onGameOver(score);
            }
        }
    }

    public void startGame() {
        isGameRunning = true;
        lastUpdateTime = SystemClock.elapsedRealtime();
        initializeGame();
        gameLoopHandler.post(gameLoopRunnable);
    }

    public void stopGame() {
        isGameRunning = false;
        gameLoopHandler.removeCallbacks(gameLoopRunnable);
    }

    public boolean isGameRunning() {
        return isGameRunning;
    }

    public void clearObstacles() {
        for (ImageView obstacle : obstacles) {
            gameLayout.removeView(obstacle);
        }
        obstacles.clear();
    }

    public void clearCoins() {
        for (ImageView coin : coins) {
            gameLayout.removeView(coin);
        }
        coins.clear();
    }

    public void moveObstacles(float deltaSeconds) {
        float speed = OBSTACLE_SPEED;
        if (fastMode)
            speed += speed;
        for (int i = obstacles.size() - 1; i >= 0; i--) {
            ImageView obstacle = obstacles.get(i);
            float newY = obstacle.getY() + (speed * deltaSeconds);
            obstacle.setY(newY);

            if (newY > gameLayout.getHeight()) {
                gameLayout.removeView(obstacle);
                obstacles.remove(i);
            }
        }
    }

    public void moveCoins(float deltaSeconds) {
        float speed = OBSTACLE_SPEED;
        if(fastMode)
            speed += speed;
        for (int i = coins.size() - 1; i >= 0; i--) {
            ImageView coin = coins.get(i);
            float newY = coin.getY() + (speed * deltaSeconds); // Coins move at the same speed as obstacles
            coin.setY(newY);

            if (newY > gameLayout.getHeight()) {
                gameLayout.removeView(coin);
                coins.remove(i);
            }
        }
    }


    // inside the update method, update the distance and notify the listener
    public void update(long deltaTime) {
        float speed = OBSTACLE_SPEED;
        if (fastMode)
            speed += speed;
        float deltaSeconds = deltaTime / 1000f;
        moveObstacles(deltaSeconds);
        moveCoins(deltaSeconds);

        long currentTime = SystemClock.elapsedRealtime();

        if (currentTime - lastObstacleSpawnTime > OBSTACLE_SPAWN_DELAY && obstacles.size() < MAX_OBSTACLES) {
            float randomValue = random.nextFloat();
            if (randomValue < OBSTACLE_SPAWN_CHANCE) {
                spawnObstacle();
                lastObstacleSpawnTime = currentTime;  // Update the lastObstacleSpawnTime here
            }
        }

        // Spawn coins at random intervals
        if (currentTime - lastCoinSpawnTime > OBSTACLE_SPAWN_DELAY && random.nextFloat() < 0.1) {
            spawnCoin();
            lastCoinSpawnTime = currentTime;
        }

        if (checkCollision()) {
            handleCollision();
        }
        checkCoinCollection();

        // Update distance
        distance += speed * deltaSeconds;
        if (distanceChangeListener != null) {
            if (fastMode)
                score += 2;
            score += 1;
            if (gameCallback != null) {
                gameCallback.onScoreUpdated(score);
            }
            distanceChangeListener.onDistanceChange(distance/10000);

        }
    }


    public void release() {
        if (soundPlayer != null) {

            soundPlayer = null;
        }
    }

    public void setLeaderboards(Leaderboards leaderboards) {
        this.leaderboards = leaderboards;
    }


}
