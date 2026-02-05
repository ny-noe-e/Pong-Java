import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.concurrent.ThreadLocalRandom;

public class GamePanel extends JPanel {

    private final int BALL_SIZE = 20;
    private double x, y;
    private double vx, vy;
    private long lastNanos;

    private final int PADDLE_WIDTH = 14;
    private final int PADDLE_HEIGHT = 110;
    private final int PADDLE_MARGIN = 30;
    private double leftPaddleY, rightPaddleY;
    private final double PADDLE_SPEED = 720.0;

    private boolean leftUp, leftDown, rightUp, rightDown;

    private final double BALL_RESTITUTION = 1.0;

    public GamePanel() {
        setBackground(Color.BLACK);
        setFocusable(true);
        requestFocusInWindow();

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

    private void tick() {
        long now = System.nanoTime();
        double dt = (now - lastNanos) / 1_000_000_000.0;
        lastNanos = now;

        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) return;

        if (x == 0 && y == 0) {
            x = (w - BALL_SIZE) / 2.0;
            y = (h - BALL_SIZE) / 2.0;
            leftPaddleY = (h - PADDLE_HEIGHT) / 2.0;
            rightPaddleY = (h - PADDLE_HEIGHT) / 2.0;
        }

        if (leftUp) leftPaddleY -= PADDLE_SPEED * dt;
        if (leftDown) leftPaddleY += PADDLE_SPEED * dt;
        if (rightUp) rightPaddleY -= PADDLE_SPEED * dt;
        if (rightDown) rightPaddleY += PADDLE_SPEED * dt;

        leftPaddleY = Math.max(0, Math.min(h - PADDLE_HEIGHT, leftPaddleY));
        rightPaddleY = Math.max(0, Math.min(h - PADDLE_HEIGHT, rightPaddleY));

        x += vx * dt;
        y += vy * dt;

        if (x < 0 || x > w - BALL_SIZE) vx = -vx;
        if (y < 0 || y > h - BALL_SIZE) vy = -vy;

        double leftX = PADDLE_MARGIN;
        double rightX = w - PADDLE_MARGIN - PADDLE_WIDTH;

        if (vx < 0 &&
                x <= leftX + PADDLE_WIDTH &&
                x + BALL_SIZE >= leftX &&
                y + BALL_SIZE >= leftPaddleY &&
                y <= leftPaddleY + PADDLE_HEIGHT) {
            x = leftX + PADDLE_WIDTH;
            vx = -vx * BALL_RESTITUTION;
        }

        if (vx > 0 &&
                x + BALL_SIZE >= rightX &&
                x <= rightX + PADDLE_WIDTH &&
                y + BALL_SIZE >= rightPaddleY &&
                y <= rightPaddleY + PADDLE_HEIGHT) {
            x = rightX - BALL_SIZE;
            vx = -vx * BALL_RESTITUTION;
        }


        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.setColor(Color.YELLOW);
        g.fillOval((int) x, (int) y, BALL_SIZE, BALL_SIZE);

        g.setColor(Color.RED);
        g.fillRect(PADDLE_MARGIN, (int) leftPaddleY, PADDLE_WIDTH, PADDLE_HEIGHT);

        g.setColor(Color.CYAN);
        g.fillRect(getWidth() - PADDLE_MARGIN - PADDLE_WIDTH,
                (int) rightPaddleY, PADDLE_WIDTH, PADDLE_HEIGHT);
    }
}
