import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class StarField {

    private final List<OrbitStar> stars = new ArrayList<>();
    private final int targetCount;

    private int lastW = -1;
    private int lastH = -1;

    public StarField(int targetCount) {
        this.targetCount = Math.max(0, targetCount);
    }

    public void update(double dt, int w, int h) {
        if (w <= 0 || h <= 0) return;

        // rebuild on resize (keeps distribution correct)
        if (w != lastW || h != lastH) {
            stars.clear();
            lastW = w;
            lastH = h;
        }

        while (stars.size() < targetCount) {
            stars.add(OrbitStar.random(w, h));
        }

        for (OrbitStar s : stars) {
            s.update(dt, w, h);
        }
    }

    public void draw(Graphics2D g2) {
        // stars as short streaks (like long exposure)
        for (OrbitStar s : stars) {
            s.draw(g2);
        }
    }
}
