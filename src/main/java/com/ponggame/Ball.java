package com.ponggame;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.Circle;

/**
 * Represents the ball in the Pong game.
 * Handles ball movement, collision detection, and rendering.
 */
public class Ball {
    
    private static final double INITIAL_SPEED = 3.0;
    
    private double x;
    private double y;
    private final double radius;
    private double velocityX;
    private double velocityY;
    
    public Ball(double x, double y, double radius) {
        this.x = x;
        this.y = y;
        this.radius = radius;
        resetVelocity();
    }
    
    /**
     * Updates the ball's position based on its velocity.
     */
    public void update() {
        x += velocityX;
        y += velocityY;
    }
    
    /**
     * Reverses the ball's horizontal direction.
     */
    public void reverseHorizontalDirection() {
        velocityX = -velocityX;
    }
    
    /**
     * Reverses the ball's vertical direction.
     */
    public void reverseVerticalDirection() {
        velocityY = -velocityY;
    }
    
    /**
     * Resets the ball to a specific position and randomizes its velocity.
     */
    public void reset(double x, double y) {
        this.x = x;
        this.y = y;
        resetVelocity();
    }
    
    /**
     * Initializes the ball's velocity with a random direction.
     */
    private void resetVelocity() {
        // Random direction: either left or right
        velocityX = Math.random() > 0.5 ? INITIAL_SPEED : -INITIAL_SPEED;
        // Random vertical component
        velocityY = (Math.random() - 0.5) * INITIAL_SPEED;
    }
    
    /**
     * Renders the ball on the canvas.
     */
    public void render(GraphicsContext gc) {
        gc.fillOval(x - radius, y - radius, radius * 2, radius * 2);
    }
    
    /**
     * Checks if the ball intersects with a paddle.
     */
    public boolean intersects(Paddle paddle) {
        // Get paddle bounds
        double paddleX = paddle.getX();
        double paddleY = paddle.getY();
        double paddleWidth = paddle.getWidth();
        double paddleHeight = paddle.getHeight();
        
        // Find the closest point on the paddle to the ball's center
        double closestX = Math.max(paddleX, Math.min(x, paddleX + paddleWidth));
        double closestY = Math.max(paddleY, Math.min(y, paddleY + paddleHeight));
        
        // Calculate the distance between the ball's center and the closest point
        double distanceX = x - closestX;
        double distanceY = y - closestY;
        double distanceSquared = (distanceX * distanceX) + (distanceY * distanceY);
        
        // Check if the distance is less than the ball's radius
        return distanceSquared < (radius * radius);
    }
    
    public double getX() {
        return x;
    }
    
    public double getY() {
        return y;
    }
    
    public double getRadius() {
        return radius;
    }
}
