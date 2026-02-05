package com.ponggame;

import javafx.scene.canvas.GraphicsContext;

/**
 * Represents a paddle in the Pong game.
 * Handles paddle movement and rendering.
 */
public class Paddle {
    
    private static final double SPEED = 5.0;
    
    private double x;
    private double y;
    private final double width;
    private final double height;
    
    public Paddle(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
    
    /**
     * Moves the paddle up, ensuring it doesn't go past the top boundary.
     */
    public void moveUp(double canvasHeight) {
        y = Math.max(0, y - SPEED);
    }
    
    /**
     * Moves the paddle down, ensuring it doesn't go past the bottom boundary.
     */
    public void moveDown(double canvasHeight) {
        y = Math.min(canvasHeight - height, y + SPEED);
    }
    
    /**
     * Renders the paddle on the canvas.
     */
    public void render(GraphicsContext gc) {
        gc.fillRect(x, y, width, height);
    }
    
    public double getX() {
        return x;
    }
    
    public double getY() {
        return y;
    }
    
    public double getWidth() {
        return width;
    }
    
    public double getHeight() {
        return height;
    }
}
