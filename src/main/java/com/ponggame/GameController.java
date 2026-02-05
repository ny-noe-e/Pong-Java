package com.ponggame;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import javafx.scene.Scene;

/**
 * GameController manages the game logic, rendering, and user input.
 * This class coordinates between the Ball and Paddle objects and handles the game loop.
 */
public class GameController {
    
    private static final int FRAMES_PER_SECOND = 60;
    private static final double WINNING_SCORE = 5;
    
    private final Canvas canvas;
    private final GraphicsContext gc;
    private final StackPane root;
    private final Timeline gameLoop;
    
    private Ball ball;
    private Paddle leftPaddle;
    private Paddle rightPaddle;
    
    private int leftScore;
    private int rightScore;
    private boolean gameOver;
    
    private boolean wPressed;
    private boolean sPressed;
    private boolean upPressed;
    private boolean downPressed;
    
    public GameController(int width, int height) {
        // Initialize canvas
        canvas = new Canvas(width, height);
        gc = canvas.getGraphicsContext2D();
        
        root = new StackPane(canvas);
        root.setStyle("-fx-background-color: black;");
        
        // Initialize game objects
        ball = new Ball(width / 2.0, height / 2.0, 10);
        leftPaddle = new Paddle(30, height / 2.0 - 50, 10, 100);
        rightPaddle = new Paddle(width - 40, height / 2.0 - 50, 10, 100);
        
        leftScore = 0;
        rightScore = 0;
        gameOver = false;
        
        // Initialize game loop
        gameLoop = new Timeline(new KeyFrame(Duration.millis(1000.0 / FRAMES_PER_SECOND), e -> update()));
        gameLoop.setCycleCount(Timeline.INDEFINITE);
    }
    
    /**
     * Sets up keyboard event handlers for paddle control.
     */
    public void setupKeyHandlers(Scene scene) {
        scene.setOnKeyPressed(e -> {
            KeyCode code = e.getCode();
            if (code == KeyCode.W) wPressed = true;
            if (code == KeyCode.S) sPressed = true;
            if (code == KeyCode.UP) upPressed = true;
            if (code == KeyCode.DOWN) downPressed = true;
            if (code == KeyCode.SPACE && gameOver) resetGame();
        });
        
        scene.setOnKeyReleased(e -> {
            KeyCode code = e.getCode();
            if (code == KeyCode.W) wPressed = false;
            if (code == KeyCode.S) sPressed = false;
            if (code == KeyCode.UP) upPressed = false;
            if (code == KeyCode.DOWN) downPressed = false;
        });
    }
    
    /**
     * Starts the game loop.
     */
    public void startGame() {
        gameLoop.play();
    }
    
    /**
     * Main game update loop - called every frame.
     */
    private void update() {
        if (!gameOver) {
            updatePaddles();
            ball.update();
            checkCollisions();
            checkScoring();
        }
        render();
    }
    
    /**
     * Updates paddle positions based on user input.
     */
    private void updatePaddles() {
        double canvasHeight = canvas.getHeight();
        
        // Left paddle (W/S keys)
        if (wPressed) leftPaddle.moveUp(canvasHeight);
        if (sPressed) leftPaddle.moveDown(canvasHeight);
        
        // Right paddle (UP/DOWN keys)
        if (upPressed) rightPaddle.moveUp(canvasHeight);
        if (downPressed) rightPaddle.moveDown(canvasHeight);
    }
    
    /**
     * Checks for collisions between ball and paddles/walls.
     */
    private void checkCollisions() {
        double canvasHeight = canvas.getHeight();
        
        // Ball collision with top and bottom walls
        if (ball.getY() <= 0 || ball.getY() >= canvasHeight) {
            ball.reverseVerticalDirection();
        }
        
        // Ball collision with paddles
        if (ball.intersects(leftPaddle) || ball.intersects(rightPaddle)) {
            ball.reverseHorizontalDirection();
        }
    }
    
    /**
     * Checks if a player has scored and updates the score.
     */
    private void checkScoring() {
        double canvasWidth = canvas.getWidth();
        
        // Ball went past left paddle - right player scores
        if (ball.getX() < 0) {
            rightScore++;
            checkWinCondition();
            resetBall();
        }
        
        // Ball went past right paddle - left player scores
        if (ball.getX() > canvasWidth) {
            leftScore++;
            checkWinCondition();
            resetBall();
        }
    }
    
    /**
     * Checks if a player has won the game.
     */
    private void checkWinCondition() {
        if (leftScore >= WINNING_SCORE || rightScore >= WINNING_SCORE) {
            gameOver = true;
        }
    }
    
    /**
     * Resets the ball to the center of the screen.
     */
    private void resetBall() {
        ball.reset(canvas.getWidth() / 2, canvas.getHeight() / 2);
    }
    
    /**
     * Resets the game to initial state.
     */
    private void resetGame() {
        leftScore = 0;
        rightScore = 0;
        gameOver = false;
        resetBall();
    }
    
    /**
     * Renders the game state to the canvas.
     */
    private void render() {
        // Clear canvas
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        
        // Draw center line
        gc.setStroke(Color.WHITE);
        gc.setLineDashes(10, 10);
        gc.strokeLine(canvas.getWidth() / 2, 0, canvas.getWidth() / 2, canvas.getHeight());
        gc.setLineDashes(null);
        
        // Draw paddles
        gc.setFill(Color.WHITE);
        leftPaddle.render(gc);
        rightPaddle.render(gc);
        
        // Draw ball
        ball.render(gc);
        
        // Draw scores
        gc.setFont(new Font("Arial", 40));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(String.valueOf(leftScore), canvas.getWidth() / 4, 50);
        gc.fillText(String.valueOf(rightScore), 3 * canvas.getWidth() / 4, 50);
        
        // Draw game over message
        if (gameOver) {
            gc.setFont(new Font("Arial", 50));
            String winner = leftScore > rightScore ? "Left Player Wins!" : "Right Player Wins!";
            gc.fillText(winner, canvas.getWidth() / 2, canvas.getHeight() / 2);
            gc.setFont(new Font("Arial", 20));
            gc.fillText("Press SPACE to restart", canvas.getWidth() / 2, canvas.getHeight() / 2 + 50);
        }
    }
    
    public StackPane getRoot() {
        return root;
    }
}
