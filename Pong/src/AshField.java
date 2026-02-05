import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class AshField {

    private final List<AshParticle> ash = new ArrayList<>();
    private final int targetCount;

    public AshField(int targetCount) {
        this.targetCount = Math.max(0, targetCount);
    }

    public void update(double dt, int w, int h) {
        if (w <= 0 || h <= 0) return;

        // ensure we always maintain the amount of ash
        while (ash.size() < targetCount) {
            ash.add(AshParticle.spawn(w, h, true)); // allow spawn anywhere initially
        }

        for (AshParticle p : ash) {
            p.update(dt, w, h);
        }
    }

    public void draw(Graphics2D g2) {
        // draw tiny specks; alpha is per particle
        for (AshParticle p : ash) {
            int a = (int) (p.alpha() * 255);
            if (a <= 0) continue;

            g2.setColor(new Color(230, 230, 230, a));
            g2.fillRect((int) p.x, (int) p.y, p.size, p.size);
        }
    }
}
