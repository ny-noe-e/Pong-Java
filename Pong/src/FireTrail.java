import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FireTrail {

    private final List<Particle> particles = new ArrayList<>();

    public void emitFire(double x, double y, double ballVx, double ballVy, int count) {
        for (int i = 0; i < count; i++)
            particles.add(new Particle(x, y, ballVx, ballVy, Particle.Type.FIRE));
    }

    public void emitExplosion(double x, double y, int count) {
        for (int i = 0; i < count; i++)
            particles.add(new Particle(x, y, 0, 0, Particle.Type.EXPLOSION));
    }

    public void update(double dt) {
        Iterator<Particle> it = particles.iterator();
        while (it.hasNext()) {
            if (!it.next().update(dt)) it.remove();
        }
    }

    public void draw(Graphics2D g2) {
        Composite old = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.9f));

        for (Particle p : particles) p.draw(g2);

        g2.setComposite(old);
    }

    public void clear() {
        particles.clear();
    }
}
