# Pong-Java

A classic Pong game implementation built with JavaFX. This project demonstrates a clean Java coding structure for game development using JavaFX for graphics and animation.

## Features

- Two-player local gameplay
- Smooth paddle and ball physics
- Score tracking
- Win condition (first to 5 points)
- Responsive keyboard controls

## Project Structure

```
Pong-Java/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/
│       │       └── ponggame/
│       │           ├── PongGame.java        # Main application entry point
│       │           ├── GameController.java  # Game logic and rendering
│       │           ├── Ball.java            # Ball entity
│       │           └── Paddle.java          # Paddle entity
│       └── resources/                       # Resources (empty for now)
├── pom.xml                                  # Maven configuration
└── README.md                                # This file
```

## Requirements

- Java 11 or higher
- Maven 3.6 or higher
- JavaFX 17 (automatically downloaded by Maven)

## Building the Project

To build the project, run:

```bash
mvn clean compile
```

## Running the Game

To run the game using Maven:

```bash
mvn javafx:run
```

## Controls

### Left Player (Left Paddle)
- **W** - Move paddle up
- **S** - Move paddle down

### Right Player (Right Paddle)
- **UP Arrow** - Move paddle up
- **DOWN Arrow** - Move paddle down

### Game Controls
- **SPACE** - Restart game (after game over)

## Game Rules

1. The ball starts in the center and moves in a random direction
2. Players control paddles to bounce the ball back
3. If the ball passes a paddle, the opponent scores a point
4. First player to reach 5 points wins
5. Press SPACE to restart after a game ends

## Architecture

The game follows object-oriented design principles:

- **PongGame**: Main JavaFX Application class that sets up the window and scene
- **GameController**: Manages game state, update loop, rendering, and input handling
- **Ball**: Represents the game ball with physics and collision detection
- **Paddle**: Represents player paddles with movement and boundary checking

The game uses JavaFX's `Timeline` for a fixed framerate game loop (60 FPS) and `Canvas` for rendering.

## License

This project is open source and available under the MIT License.
