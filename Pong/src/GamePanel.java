import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class GamePanel extends JPanel {

    private boolean scoredThisPass = false;

    int score1 = 0;
    int score2 = 0;

    // Ball state
    private final int BALL_SIZE = 20;
    private double x, y;
    private double vx, vy;
    private long lastNanos;

    // Paddle configuration
    private final int PADDLE_WIDTH = 14;
    private final int PADDLE_HEIGHT = 110;
    private final int PADDLE_MARGIN = 30;
    private double leftPaddleY, rightPaddleY;
    private final double PADDLE_SPEED = 720.0;

    // Paddle velocity (for spin)
    private double leftPaddleVel = 0.0;   // px/s
    private double rightPaddleVel = 0.0;  // px/s

    // Input flags
    private boolean leftUp, leftDown, rightUp, rightDown;

    // Particles
    private final FireTrail fireTrail = new FireTrail();

    // Background ash
    private final AshField ashField = new AshField(180); // amount of ash particles

    // Physics
    private final double BALL_RESTITUTION = 1.0;

    // Ball spin tuning
    private final double SPIN_FACTOR = 0.35;
    private final double MAX_BALL_SPEED = 1500;
    private final double MIN_BALL_SPEED = 800;

    // Screen shake
    private double shakeTimeLeft = 0.0;     // seconds remaining
    private double shakeDuration = 0.10;    // seconds
    private int shakeStrength = 12;         // pixels
    private int shakeOffsetX = 0;
    private int shakeOffsetY = 0;

    // Paddle glow
    private double leftGlowTime = 0.0;
    private double rightGlowTime = 0.0;
    private final double GLOW_DURATION = 0.18; // seconds

    public GamePanel() {
        setBackground(Color.BLACK);
        setFocusable(true);

        x = 0;
        y = 0;

        double speed = 1020.0;
        double angle = ThreadLocalRandom.current().nextDouble(0, Math.PI * 2);
        vx = Math.cos(angle) * speed;
        vy = Math.sin(angle) * speed;

        lastNanos = System.nanoTime();

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_W -> leftUp = true;
                    case KeyEvent.VK_S -> leftDown = true;
                    case KeyEvent.VK_UP -> rightUp = true;
                    case KeyEvent.VK_DOWN -> rightDown = true;
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_W -> leftUp = false;
                    case KeyEvent.VK_S -> leftDown = false;
                    case KeyEvent.VK_UP -> rightUp = false;
                    case KeyEvent.VK_DOWN -> rightDown = false;
                }
            }
        });

        new Timer(16, e -> tick()).start();
    }

    private void startShake(double durationSeconds, int strengthPixels) {
        shakeTimeLeft = Math.max(shakeTimeLeft, durationSeconds);
        shakeDuration = durationSeconds;
        shakeStrength = strengthPixels;
    }

    private void triggerLeftGlow() {
        leftGlowTime = GLOW_DURATION;
    }

    private void triggerRightGlow() {
        rightGlowTime = GLOW_DURATION;
    }

    private void tick() {
        long now = System.nanoTime();
        double dt = (now - lastNanos) / 1_000_000_000.0;
        lastNanos = now;

        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) return;
        if (dt <= 0) dt = 1.0 / 60.0;

        // init positions once we have a size
        if (x == 0 && y == 0) {
            x = (w - BALL_SIZE) / 2.0;
            y = (h - BALL_SIZE) / 2.0;
            leftPaddleY = (h - PADDLE_HEIGHT) / 2.0;
            rightPaddleY = (h - PADDLE_HEIGHT) / 2.0;
            fireTrail.clear();
        }

        // background ash always updates
        ashField.update(dt, w, h);

        if (leftUp) leftPaddleY -= PADDLE_SPEED * dt;
        if (leftDown) leftPaddleY += PADDLE_SPEED * dt;
        if (rightUp) rightPaddleY -= PADDLE_SPEED * dt;
        if (rightDown) rightPaddleY += PADDLE_SPEED * dt;

        leftPaddleY = Math.max(0, Math.min(h - PADDLE_HEIGHT, leftPaddleY));
        rightPaddleY = Math.max(0, Math.min(h - PADDLE_HEIGHT, rightPaddleY));

        leftPaddleVel = (leftPaddleY - prevLeft) / dt;
        rightPaddleVel = (rightPaddleY - prevRight) / dt;

        // Ball movement
        x += vx * dt;
        y += vy * dt;

        // Fire trail
        fireTrail.emitFire(
                x + BALL_SIZE / 2.0,
                y + BALL_SIZE / 2.0,
                vx, vy,
                4
        );
        fireTrail.update(dt);

        // Wall collisions
        if (x < 0) { x = 0; vx = Math.abs(vx); }
        else if (x > w - BALL_SIZE) { x = w - BALL_SIZE; vx = -Math.abs(vx); }

        if (y < 0) { y = 0; vy = Math.abs(vy); }
        else if (y > h - BALL_SIZE) { y = h - BALL_SIZE; vy = -Math.abs(vy); }

        double leftX = PADDLE_MARGIN;
        double rightX = w - PADDLE_MARGIN - PADDLE_WIDTH;

        // LEFT paddle hit (spin)
        if (vx < 0 &&
                x <= leftX + PADDLE_WIDTH &&
                x + BALL_SIZE >= leftX &&
                y + BALL_SIZE >= leftPaddleY &&
                y <= leftPaddleY + PADDLE_HEIGHT) {

            x = leftX + PADDLE_WIDTH;
            vx = -vx * BALL_RESTITUTION;

            vy += leftPaddleVel * SPIN_FACTOR;

            fireTrail.emitExplosion(x, y + BALL_SIZE / 2.0, 70);
            startShake(0.10, 12);
            triggerLeftGlow();

            clampBallSpeed();
        }

        // RIGHT paddle hit (spin)
        if (vx > 0 &&
                x + BALL_SIZE >= rightX &&
                x <= rightX + PADDLE_WIDTH &&
                y + BALL_SIZE >= rightPaddleY &&
                y <= rightPaddleY + PADDLE_HEIGHT) {

            x = rightX - BALL_SIZE;
            vx = -vx * BALL_RESTITUTION;

            vy += rightPaddleVel * SPIN_FACTOR;

            fireTrail.emitExplosion(x + BALL_SIZE, y + BALL_SIZE / 2.0, 70);
            startShake(0.10, 12);
            triggerRightGlow();

            clampBallSpeed();
        }

        // Scoring + subtle edge flash
        if (!scoredThisPass) {
            if (x < 10) { score2++; scoredThisPass = true; }
            else if (x + BALL_SIZE > w - 10) { score1++; scoredThisPass = true; }
        }

        // Timers
        leftGlowTime = Math.max(0.0, leftGlowTime - dt);
        rightGlowTime = Math.max(0.0, rightGlowTime - dt);

        // Screen shake offsets
        if (shakeTimeLeft > 0) {
            shakeTimeLeft -= dt;

            double t = Math.max(0.0, shakeTimeLeft) / Math.max(0.0001, shakeDuration); // 1..0
            int strengthNow = (int) Math.round(shakeStrength * t);
            shakeOffsetX = ThreadLocalRandom.current().nextInt(-strengthNow, strengthNow + 1);
            shakeOffsetY = ThreadLocalRandom.current().nextInt(-strengthNow, strengthNow + 1);
        } else {
            shakeOffsetX = 0;
            shakeOffsetY = 0;
        }

        repaint();
    }

    private void drawPaddleGlow(Graphics2D g2, int x, int y, int w, int h, double glowTimeLeft) {
        if (glowTimeLeft <= 0) return;

        float a = (float) (glowTimeLeft / GLOW_DURATION); // 1..0
        int pad = 10;          // glow thickness
        int extra = 10;        // glow expansion

        Composite old = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.55f * a));
        g2.setColor(new Color(1.0f, 0.8f, 0.2f, 1.0f));

        // draw a few layered rectangles for a soft-ish glow
        g2.fillRoundRect(x - extra, y - extra, w + extra * 2, h + extra * 2, 18, 18);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.35f * a));
        g2.fillRoundRect(x - (extra + pad), y - (extra + pad), w + (extra + pad) * 2, h + (extra + pad) * 2, 22, 22);

        g2.setComposite(old);
    }

    // Rendering
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Gameplay layer (shaken)
        g2.translate(shakeOffsetX, shakeOffsetY);

        // Background ash (behind everything)
        ashField.draw(g2);

        // particles behind ball
        fireTrail.draw(g2);

        // ball
        g2.setColor(Color.RED);
        g2.fillOval((int) x, (int) y, BALL_SIZE, BALL_SIZE);

        // paddles + glow + nicer shape
        int leftPx = PADDLE_MARGIN;
        int leftPy = (int) leftPaddleY;

        int rightPx = getWidth() - PADDLE_MARGIN - PADDLE_WIDTH;
        int rightPy = (int) rightPaddleY;

        drawPaddleGlow(g2, leftPx, leftPy, PADDLE_WIDTH, PADDLE_HEIGHT, leftGlowTime);
        drawPaddleGlow(g2, rightPx, rightPy, PADDLE_WIDTH, PADDLE_HEIGHT, rightGlowTime);

        g2.setColor(Color.WHITE);
        g2.fillRect(leftPx, leftPy, PADDLE_WIDTH, PADDLE_HEIGHT);
        g2.fillRect(rightPx, rightPy, PADDLE_WIDTH, PADDLE_HEIGHT);

        // UI
        g2.drawString(String.valueOf(score1), (getWidth() / 2) - 30, 100);
        g2.drawString(String.valueOf(score2), (getWidth() / 2) + 30, 100);

        // HUD layer (stable)
        g2.translate(-shakeOffsetX, -shakeOffsetY);
    }
}
