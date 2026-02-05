import java.util.concurrent.ThreadLocalRandom;

public class AshParticle {
    double x, y;
    double vx, vy;

    double life;     // seconds left
    double maxLife;  // total lifetime

    int size;

    private AshParticle() {}

    public static AshParticle spawn(int w, int h, boolean anywhere) {
        AshParticle p = new AshParticle();

        p.size = ThreadLocalRandom.current().nextInt(1, 3); // 1..2 px

        p.x = ThreadLocalRandom.current().nextDouble(0, w);
        p.y = anywhere
                ? ThreadLocalRandom.current().nextDouble(0, h)
                : (h + ThreadLocalRandom.current().nextDouble(0, 40)); // respawn slightly below screen

        // slow drift + upward float
        p.vx = ThreadLocalRandom.current().nextDouble(-18, 18);
        p.vy = ThreadLocalRandom.current().nextDouble(-40, -12);

        // lifetime + fade (so they don't "disappear forever")
        p.maxLife = ThreadLocalRandom.current().nextDouble(2.5, 5.5);
        p.life = p.maxLife;

        return p;
    }

    public void respawn(int w, int h) {
        AshParticle n = spawn(w, h, false);
        this.x = n.x;
        this.y = n.y;
        this.vx = n.vx;
        this.vy = n.vy;
        this.life = n.life;
        this.maxLife = n.maxLife;
        this.size = n.size;
    }

    public void update(double dt, int w, int h) {
        // subtle wobble
        vx += ThreadLocalRandom.current().nextDouble(-10, 10) * dt;

        x += vx * dt;
        y += vy * dt;

        // wrap horizontally
        if (x < -2) x = w + 2;
        if (x > w + 2) x = -2;

        // lifetime
        life -= dt;

        // if it went off the top or its life ended, respawn at bottom
        if (y < -10 || life <= 0) {
            respawn(w, h);
        }
    }

    // fade in + fade out
    public double alpha() {
        if (maxLife <= 0) return 0;

        double t = 1.0 - (life / maxLife); // 0..1 (age)
        double fadeIn = clamp01(t / 0.18);                 // first ~18% fades in
        double fadeOut = clamp01((1.0 - t) / 0.22);        // last ~22% fades out
        double a = Math.min(fadeIn, fadeOut);

        // keep ash subtle
        return 0.35 * a;
    }

    private static double clamp01(double v) {
        if (v < 0) return 0;
        if (v > 1) return 1;
        return v;
    }
}
