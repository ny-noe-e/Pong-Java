import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class AshField {

    private final List<AshParticle> ash = new ArrayList<>();
    private final int targetCount;

    public AshField(int targetCount) {
        this.targetCount = Math.max(0, targetCount);
    }

    public void update(double dt, int w, int h) {
        // lazy init / keep count stable
        while (ash.size() < targetCount) {
            ash.add(AshParticle.randomSpawn(w, h));
        }

        for (AshParticle p : ash) {
            p.update(dt, w, h);
        }
    }

    public void draw(Graphics2D g2) {
        Composite old = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.35f));

        // tiny light-gray specks
        g2.setColor(new Color(0.9f, 0.9f, 0.9f, 1.0f));
        for (AshParticle p : ash) {
            int s = p.size;
            g2.fillRect((int) p.x, (int) p.y, s, s);
        }

        g2.setComposite(old);
    }
}
