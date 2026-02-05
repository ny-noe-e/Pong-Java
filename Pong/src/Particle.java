import java.awt.*;
import java.util.concurrent.ThreadLocalRandom;

public class Particle {

    enum Type { FIRE, EXPLOSION }

    double x, y;
    double vx, vy;
    double life, maxLife;
    float size;
    Type type;

    public Particle(double x, double y, double ballVx, double ballVy, Type type) {
        this.x = x;
        this.y = y;
        this.type = type;

        if (type == Type.FIRE) {
            double spread = 1.4;
            this.vx = (-ballVx * 0.03) + ThreadLocalRandom.current().nextDouble(-spread, spread);
            this.vy = (-ballVy * 0.03) + ThreadLocalRandom.current().nextDouble(-spread, spread);
            this.maxLife = ThreadLocalRandom.current().nextDouble(0.18, 0.35);
            this.size = (float) ThreadLocalRandom.current().nextDouble(3.0, 7.0);
        } else {
            // BIG explosion
            double angle = ThreadLocalRandom.current().nextDouble(0, Math.PI * 2);
            double speed = ThreadLocalRandom.current().nextDouble(600, 1100);
            this.vx = Math.cos(angle) * speed * 0.016;
            this.vy = Math.sin(angle) * speed * 0.016;

            this.maxLife = ThreadLocalRandom.current().nextDouble(0.20, 0.35);
            this.size = (float) ThreadLocalRandom.current().nextDouble(8.0, 16.0);
        }

        this.life = maxLife;
    }

    public boolean update(double dt) {
        x += vx;
        y += vy;

        if (type == Type.FIRE) {
            vx *= 0.92;
            vy = vy * 0.92 - 0.08;
        } else {
            vx *= 0.86;
            vy *= 0.86;
        }

        life -= dt;
        return life > 0;
    }

    public void draw(Graphics2D g2) {
        float a = (float) (life / maxLife);

        if (type == Type.FIRE) {
            g2.setColor(new Color(1.0f, 0.5f, 0.0f, Math.min(1f, a)));
        } else {
            // hot explosion color
            g2.setColor(new Color(
                    1.0f,
                    ThreadLocalRandom.current().nextFloat(0.6f, 0.9f),
                    0.2f,
                    Math.min(1f, a)
            ));
        }

        float s = size * (0.8f + 1.2f * a);
        g2.fillOval((int) (x - s / 2f), (int) (y - s / 2f), (int) s, (int) s);
    }
}
