import javax.swing.*;
import java.awt.*;
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

    // Rotating star background
    private final StarField starField = new StarField(260);

    // Shooting stars (Sternschnuppen)
    private final ArrayList<ShootingStar> stars = new ArrayList<>();
    private double nextStarIn = ThreadLocalRandom.current().nextDouble(2.5, 6.0);

    // Physics
    private final double BALL_RESTITUTION = 1.0;

    // Ball spin tuning
    private final double SPIN_FACTOR = 0.35;
    private final double MAX_BALL_SPEED = 1500;
    private final double MIN_BALL_SPEED = 800;

    // Screen shake
    private double shakeTimeLeft = 0.0;
    private double shakeDuration = 0.10;
    private int shakeStrength = 12;
    private int shakeOffsetX = 0;
    private int shakeOffsetY = 0;

    // Paddle glow
    private double leftGlowTime = 0.0;
    private double rightGlowTime = 0.0;
    private final double GLOW_DURATION = 0.18;

    // HUD fonts
    private final Font scoreFont = new Font("SansSerif", Font.BOLD, 56);

    // Subtle edge damage gradient
    private double leftEdgeFlash = 0.0;
    private double rightEdgeFlash = 0.0;
    private final double EDGE_FLASH_DURATION = 0.18;
    private final int EDGE_GRADIENT_WIDTH = 90;
    private final int EDGE_MAX_ALPHA = 45;

    // Fullscreen toggle state (F11)
    private boolean fullscreen = false;

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

        // Use key bindings (more reliable than KeyListener)
        setupKeyBindings();

        new Timer(16, e -> tick()).start();
    }

    private void setupKeyBindings() {
        InputMap im = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = getActionMap();

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_W, 0, false), "L_UP_P");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_W, 0, true), "L_UP_R");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, 0, false), "L_DN_P");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, 0, true), "L_DN_R");

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0, false), "R_UP_P");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0, true), "R_UP_R");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0, false), "R_DN_P");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0, true), "R_DN_R");

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0, false), "TOGGLE_FS");

        am.put("L_UP_P", new AbstractAction() { public void actionPerformed(java.awt.event.ActionEvent e) { leftUp = true; }});
        am.put("L_UP_R", new AbstractAction() { public void actionPerformed(java.awt.event.ActionEvent e) { leftUp = false; }});
        am.put("L_DN_P", new AbstractAction() { public void actionPerformed(java.awt.event.ActionEvent e) { leftDown = true; }});
        am.put("L_DN_R", new AbstractAction() { public void actionPerformed(java.awt.event.ActionEvent e) { leftDown = false; }});

        am.put("R_UP_P", new AbstractAction() { public void actionPerformed(java.awt.event.ActionEvent e) { rightUp = true; }});
        am.put("R_UP_R", new AbstractAction() { public void actionPerformed(java.awt.event.ActionEvent e) { rightUp = false; }});
        am.put("R_DN_P", new AbstractAction() { public void actionPerformed(java.awt.event.ActionEvent e) { rightDown = true; }});
        am.put("R_DN_R", new AbstractAction() { public void actionPerformed(java.awt.event.ActionEvent e) { rightDown = false; }});

        am.put("TOGGLE_FS", new AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                toggleFullscreen();
            }
        });
    }

    private void toggleFullscreen() {
        Window w = SwingUtilities.getWindowAncestor(this);
        if (!(w instanceof JFrame frame)) return;

        GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();

        // Dispose is required when changing undecorated
        frame.dispose();

        fullscreen = !fullscreen;

        if (fullscreen) {
            frame.setUndecorated(true);                 // removes top border/title bar
            frame.setResizable(false);
            frame.setVisible(true);
            device.setFullScreenWindow(frame);          // true fullscreen
        } else {
            device.setFullScreenWindow(null);           // exit fullscreen
            frame.setUndecorated(false);
            frame.setResizable(true);
            frame.setVisible(true);

            // optional: restore a nice window size
            frame.setSize(900, 700);
            frame.setLocationRelativeTo(null);
        }

        // keep input working
        requestFocusInWindow();
    }

    private void startShake(double durationSeconds, int strengthPixels) {
        shakeTimeLeft = Math.max(shakeTimeLeft, durationSeconds);
        shakeDuration = durationSeconds;
        shakeStrength = strengthPixels;
    }

    private void triggerLeftGlow() { leftGlowTime = GLOW_DURATION; }
    private void triggerRightGlow() { rightGlowTime = GLOW_DURATION; }

    private void flashLeftEdge() { leftEdgeFlash = EDGE_FLASH_DURATION; }
    private void flashRightEdge() { rightEdgeFlash = EDGE_FLASH_DURATION; }

    private static double clamp01(double v) {
        if (v < 0) return 0;
        if (v > 1) return 1;
        return v;
    }

    private double pulseAlpha(double timeLeft, double duration) {
        if (timeLeft <= 0 || duration <= 0) return 0;
        double age = 1.0 - (timeLeft / duration); // 0..1
        double fadeIn = clamp01(age / 0.20);
        double fadeOut = clamp01((1.0 - age) / 0.35);
        return Math.min(fadeIn, fadeOut);
    }

    private void clampBallSpeed() {
        double speed = Math.hypot(vx, vy);
        if (speed <= 0.0001) return;

        if (speed > MAX_BALL_SPEED) {
            double s = MAX_BALL_SPEED / speed;
            vx *= s;
            vy *= s;
        } else if (speed < MIN_BALL_SPEED) {
            double s = MIN_BALL_SPEED / speed;
            vx *= s;
            vy *= s;
        }
    }

    private void spawnShootingStar(int w, int h) {
        boolean fromLeft = ThreadLocalRandom.current().nextBoolean();

        double startX = fromLeft ? -120 : w + 120;
        double startY = ThreadLocalRandom.current().nextDouble(h * 0.10, h * 0.60);

        double dir = fromLeft ? 1.0 : -1.0;
        double speed = ThreadLocalRandom.current().nextDouble(900, 1400);

        double vx = dir * speed;
        double vy = ThreadLocalRandom.current().nextDouble(180, 420);

        double life = ThreadLocalRandom.current().nextDouble(0.60, 1.10);
        stars.add(new ShootingStar(startX, startY, vx, vy, life));
    }

    private void updateShootingStars(double dt, int w, int h) {
        nextStarIn -= dt;
        if (nextStarIn <= 0) {
            spawnShootingStar(w, h);
            nextStarIn = ThreadLocalRandom.current().nextDouble(2.5, 6.0);
        }

        for (int i = stars.size() - 1; i >= 0; i--) {
            ShootingStar s = stars.get(i);
            s.update(dt);
            if (s.isDead() || s.x < -250 || s.x > w + 250 || s.y < -250 || s.y > h + 250) {
                stars.remove(i);
            }
        }
    }

    private void tick() {
        long now = System.nanoTime();
        double dt = (now - lastNanos) / 1_000_000_000.0;
        lastNanos = now;

        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) return;
        if (dt <= 0) dt = 1.0 / 60.0;

        if (x == 0 && y == 0) {
            x = (w - BALL_SIZE) / 2.0;
            y = (h - BALL_SIZE) / 2.0;
            leftPaddleY = (h - PADDLE_HEIGHT) / 2.0;
            rightPaddleY = (h - PADDLE_HEIGHT) / 2.0;
            fireTrail.clear();
        }

        starField.update(dt, w, h);
        updateShootingStars(dt, w, h);

        double prevLeft = leftPaddleY;
        double prevRight = rightPaddleY;

        if (leftUp) leftPaddleY -= PADDLE_SPEED * dt;
        if (leftDown) leftPaddleY += PADDLE_SPEED * dt;
        if (rightUp) rightPaddleY -= PADDLE_SPEED * dt;
        if (rightDown) rightPaddleY += PADDLE_SPEED * dt;

        leftPaddleY = Math.max(0, Math.min(h - PADDLE_HEIGHT, leftPaddleY));
        rightPaddleY = Math.max(0, Math.min(h - PADDLE_HEIGHT, rightPaddleY));

        leftPaddleVel = (leftPaddleY - prevLeft) / dt;
        rightPaddleVel = (rightPaddleY - prevRight) / dt;

        x += vx * dt;
        y += vy * dt;

        fireTrail.emitFire(x + BALL_SIZE / 2.0, y + BALL_SIZE / 2.0, vx, vy, 4);
        fireTrail.update(dt);

        if (x < 0) { x = 0; vx = Math.abs(vx); }
        else if (x > w - BALL_SIZE) { x = w - BALL_SIZE; vx = -Math.abs(vx); }

        if (y < 0) { y = 0; vy = Math.abs(vy); }
        else if (y > h - BALL_SIZE) { y = h - BALL_SIZE; vy = -Math.abs(vy); }

        double leftX = PADDLE_MARGIN;
        double rightX = w - PADDLE_MARGIN - PADDLE_WIDTH;

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

        if (!scoredThisPass) {
            if (x < 10) { score2++; scoredThisPass = true; flashLeftEdge(); }
            else if (x + BALL_SIZE > w - 10) { score1++; scoredThisPass = true; flashRightEdge(); }
        }
        if (x > 10 && x + BALL_SIZE < w - 10) scoredThisPass = false;

        leftGlowTime = Math.max(0.0, leftGlowTime - dt);
        rightGlowTime = Math.max(0.0, rightGlowTime - dt);
        leftEdgeFlash = Math.max(0.0, leftEdgeFlash - dt);
        rightEdgeFlash = Math.max(0.0, rightEdgeFlash - dt);

        if (shakeTimeLeft > 0) {
            shakeTimeLeft -= dt;
            double t = Math.max(0.0, shakeTimeLeft) / Math.max(0.0001, shakeDuration);
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

        float a = (float) (glowTimeLeft / GLOW_DURATION);

        Composite old = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.55f * a));
        g2.setColor(new Color(1.0f, 0.8f, 0.2f, 1.0f));

        int extra = 10;
        int pad = 10;

        g2.fillRoundRect(x - extra, y - extra, w + extra * 2, h + extra * 2, 22, 22);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.30f * a));
        g2.fillRoundRect(x - (extra + pad), y - (extra + pad), w + (extra + pad) * 2, h + (extra + pad) * 2, 26, 26);

        g2.setComposite(old);
    }

    private void drawPaddle(Graphics2D g2, int x, int y, int w, int h, boolean isLeft) {
        int arc = Math.min(24, h / 4);

        Paint oldPaint = g2.getPaint();
        GradientPaint gp = new GradientPaint(
                x, y, new Color(245, 245, 245, 235),
                x, y + h, new Color(180, 180, 180, 235)
        );
        g2.setPaint(gp);
        g2.fillRoundRect(x, y, w, h, arc, arc);

        g2.setPaint(oldPaint);
        g2.setColor(new Color(255, 255, 255, 70));
        g2.drawRoundRect(x, y, w, h, arc, arc);

        g2.setColor(new Color(255, 255, 255, 90));
        if (isLeft) g2.drawLine(x + 2, y + 8, x + 2, y + h - 8);
        else g2.drawLine(x + w - 3, y + 8, x + w - 3, y + h - 8);

        g2.setColor(new Color(0, 0, 0, 45));
        if (isLeft) g2.drawLine(x + w - 3, y + 8, x + w - 3, y + h - 8);
        else g2.drawLine(x + 2, y + 8, x + 2, y + h - 8);
    }

    private void drawHud(Graphics2D g2) {
        int w = getWidth();

        String s1 = String.valueOf(score1);
        String s2 = String.valueOf(score2);

        g2.setFont(scoreFont);
        FontMetrics fm = g2.getFontMetrics();

        int centerX = w / 2;
        int panelY = 26;

        int gap = 28;
        int textY = panelY + 54;

        int w1 = fm.stringWidth(s1);
        int w2 = fm.stringWidth(s2);

        int totalTextWidth = w1 + gap + fm.stringWidth(":") + gap + w2;
        int panelW = totalTextWidth + 44;
        int panelH = 68;
        int panelX = centerX - panelW / 2;

        Composite old = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.55f));
        g2.setColor(Color.BLACK);
        g2.fillRoundRect(panelX, panelY, panelW, panelH, 22, 22);

        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.22f));
        g2.setColor(Color.WHITE);
        g2.drawRoundRect(panelX, panelY, panelW, panelH, 22, 22);
        g2.setComposite(old);

        int startX = centerX - totalTextWidth / 2;

        g2.setColor(new Color(0, 0, 0, 170));
        g2.drawString(s1, startX + 2, textY + 2);
        g2.drawString(":", startX + w1 + gap + 2, textY + 2);
        g2.drawString(s2, startX + w1 + gap + fm.stringWidth(":") + gap + 2, textY + 2);

        g2.setColor(Color.WHITE);
        g2.drawString(s1, startX, textY);
        g2.drawString(":", startX + w1 + gap, textY);
        g2.drawString(s2, startX + w1 + gap + fm.stringWidth(":") + gap, textY);
    }

    private void drawEdgeDamage(Graphics2D g2) {
        int w = getWidth();
        int h = getHeight();

        double aL = pulseAlpha(leftEdgeFlash, EDGE_FLASH_DURATION);
        if (aL > 0) {
            int alpha = (int) Math.round(EDGE_MAX_ALPHA * aL);
            Paint old = g2.getPaint();
            g2.setPaint(new GradientPaint(
                    0, 0, new Color(255, 60, 60, alpha),
                    EDGE_GRADIENT_WIDTH, 0, new Color(255, 60, 60, 0)
            ));
            g2.fillRect(0, 0, EDGE_GRADIENT_WIDTH, h);
            g2.setPaint(old);
        }

        double aR = pulseAlpha(rightEdgeFlash, EDGE_FLASH_DURATION);
        if (aR > 0) {
            int alpha = (int) Math.round(EDGE_MAX_ALPHA * aR);
            Paint old = g2.getPaint();
            g2.setPaint(new GradientPaint(
                    w, 0, new Color(255, 60, 60, alpha),
                    w - EDGE_GRADIENT_WIDTH, 0, new Color(255, 60, 60, 0)
            ));
            g2.fillRect(w - EDGE_GRADIENT_WIDTH, 0, EDGE_GRADIENT_WIDTH, h);
            g2.setPaint(old);
        }
    }

    private void drawShootingStars(Graphics2D g2) {
        for (ShootingStar s : stars) s.draw(g2);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Gameplay layer (shaken)
        g2.translate(shakeOffsetX, shakeOffsetY);

        // background
        starField.draw(g2);
        drawShootingStars(g2);

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

        drawPaddle(g2, leftPx, leftPy, PADDLE_WIDTH, PADDLE_HEIGHT, true);
        drawPaddle(g2, rightPx, rightPy, PADDLE_WIDTH, PADDLE_HEIGHT, false);

        // HUD layer (stable)
        g2.translate(-shakeOffsetX, -shakeOffsetY);

        drawEdgeDamage(g2);
        drawHud(g2);
    }

    // --- Sternschnuppe (pure VFX) ---
    private static class ShootingStar {
        double x, y;
        double vx, vy;
        double life;
        final double maxLife;
        final double length;
        final float thickness;

        ShootingStar(double x, double y, double vx, double vy, double life) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.life = life;
            this.maxLife = life;

            this.length = ThreadLocalRandom.current().nextDouble(120, 220);
            this.thickness = (float) ThreadLocalRandom.current().nextDouble(1.5, 2.6);
        }

        void update(double dt) {
            x += vx * dt;
            y += vy * dt;
            life -= dt;
        }

        boolean isDead() { return life <= 0; }

        void draw(Graphics2D g2) {
            double t = 1.0 - (life / maxLife);
            double fadeIn = clamp01(t / 0.15);
            double fadeOut = clamp01((1.0 - t) / 0.35);
            double a = Math.min(fadeIn, fadeOut);

            int alphaHead = (int) Math.round(120 * a);
            int alphaTail = 0;

            double sp = Math.hypot(vx, vy);
            double dx = (sp > 0.0001) ? (vx / sp) : 1.0;
            double dy = (sp > 0.0001) ? (vy / sp) : 0.0;

            double x2 = x - dx * length;
            double y2 = y - dy * length;

            Stroke oldS = g2.getStroke();
            Paint oldP = g2.getPaint();

            g2.setStroke(new BasicStroke(thickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setPaint(new GradientPaint(
                    (float) x, (float) y, new Color(255, 255, 255, alphaHead),
                    (float) x2, (float) y2, new Color(255, 255, 255, alphaTail)
            ));
            g2.drawLine((int) x, (int) y, (int) x2, (int) y2);

            g2.setPaint(oldP);
            g2.setColor(new Color(255, 255, 255, (int) Math.round(140 * a)));
            g2.fillOval((int) (x - 2), (int) (y - 2), 4, 4);

            g2.setStroke(oldS);
            g2.setPaint(oldP);
        }

        private static double clamp01(double v) {
            if (v < 0) return 0;
            if (v > 1) return 1;
            return v;
        }
    }
}
