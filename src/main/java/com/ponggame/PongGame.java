package com.ponggame;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main application class for the Pong game.
 * This class initializes and launches the JavaFX application.
 */
public class PongGame extends Application {
    
    private static final int WINDOW_WIDTH = 800;
    private static final int WINDOW_HEIGHT = 600;
    private static final String GAME_TITLE = "Pong Game";
    
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle(GAME_TITLE);
        
        // Create the game controller which manages the game logic and UI
        GameController gameController = new GameController(WINDOW_WIDTH, WINDOW_HEIGHT);
        
        // Create the scene with the game's root pane
        Scene scene = new Scene(gameController.getRoot(), WINDOW_WIDTH, WINDOW_HEIGHT);
        
        // Set up key event handlers for paddle control
        gameController.setupKeyHandlers(scene);
        
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
        
        // Start the game loop
        gameController.startGame();
    }
    
    @Override
    public void stop() {
        // Clean up resources when the application is closed
        System.out.println("Game closed");
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
