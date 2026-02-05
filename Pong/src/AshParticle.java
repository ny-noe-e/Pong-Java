import java.util.concurrent.ThreadLocalRandom;

public class AshParticle {
    double x, y;
    double vx, vy;
    int size;

    private AshParticle(double x, double y, double vx, double vy, int size) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.size = size;
    }

    public static AshParticle randomSpawn(int w, int h) {
        double x = ThreadLocalRandom.current().nextDouble(0, Math.max(1, w));
        double y = ThreadLocalRandom.current().nextDouble(0, Math.max(1, h));

        // slow drift + slight upward float
        double vx = ThreadLocalRandom.current().nextDouble(-10, 10);
        double vy = ThreadLocalRandom.current().nextDouble(-28, -8);

        // really small ash
        int size = ThreadLocalRandom.current().nextInt(1, 3); // 1..2 px
        return new AshParticle(x, y, vx, vy, size);
    }

    public void update(double dt, int w, int h) {
        // subtle wobble
        vx += ThreadLocalRandom.current().nextDouble(-6, 6) * dt;

        x += vx * dt;
        y += vy * dt;

        // wrap around edges
    }
}