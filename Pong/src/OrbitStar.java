import java.awt.*;
import java.util.concurrent.ThreadLocalRandom;

public class OrbitStar {

    // orbit center
    double cx, cy;

    // polar position
    double r;           // radius
    double angle;       // current angle

    // ALL SAME DIRECTION: positive = clockwise/counterclockwise depending on screen coords
    double angSpeed;    // radians/sec (always same sign)

    // ellipse factor (natural look)
    double ellipse;

    // star look
    int size;           // 1..2 px
    double baseAlpha;   // 0..1 baseline brightness

    // twinkle
    double twPhase;     // 0..2π
    double twSpeed;     // radians/sec
    double twAmount;    // 0..1 (subtle)

    // color tint (some slightly warm)
    int rCol, gCol, bCol;

    // streak length factor (depends on radius + brightness)
    double streakLen;

    private OrbitStar() {}

    public static OrbitStar random(int w, int h) {
        OrbitStar s = new OrbitStar();

        // center (slight offset feels more like real photos sometimes)
        s.cx = w / 2.0;
        s.cy = h / 2.0;

        // radius distribution: more stars further out
        double maxR = Math.hypot(w, h) * 0.62;
        double t = ThreadLocalRandom.current().nextDouble(); // 0..1
        s.r = 30 + (maxR - 30) * Math.sqrt(t);

        s.angle = ThreadLocalRandom.current().nextDouble(0, Math.PI * 2);

        // speed: still subtle, but visible
        // ALL SAME DIRECTION -> always positive (you can flip by making it negative)
        s.angSpeed = ThreadLocalRandom.current().nextDouble(0.06, 0.16);

        s.ellipse = ThreadLocalRandom.current().nextDouble(0.86, 1.14);

        s.size = ThreadLocalRandom.current().nextInt(1, 3); // 1..2 px

        // brightness distribution: mostly dim, a few brighter
        double a = ThreadLocalRandom.current().nextDouble(0.06, 0.28);
        if (ThreadLocalRandom.current().nextDouble() < 0.10) {
            a = ThreadLocalRandom.current().nextDouble(0.28, 0.60);
        }
        s.baseAlpha = a;

        // twinkle: very subtle
        s.twPhase = ThreadLocalRandom.current().nextDouble(0, Math.PI * 2);
        s.twSpeed = ThreadLocalRandom.current().nextDouble(1.5, 3.8);
        s.twAmount = ThreadLocalRandom.current().nextDouble(0.06, 0.18);

        // some stars slightly warm/yellowish
        // 0.0 = pure white, 1.0 = warmer
        double warm = 0.0;
        double p = ThreadLocalRandom.current().nextDouble();
        if (p < 0.18) warm = ThreadLocalRandom.current().nextDouble(0.25, 0.70); // warm stars
        else if (p < 0.28) warm = ThreadLocalRandom.current().nextDouble(0.10, 0.25); // slight warm

        // base white + warm mix
        // warm target ~ (255, 235, 190)
        s.rCol = 255;
        s.gCol = (int) Math.round(255 * (1.0 - warm) + 235 * warm);
        s.bCol = (int) Math.round(255 * (1.0 - warm) + 190 * warm);

        // streak length: bigger at outer radius + brighter stars get longer streaks
        s.streakLen = 3.0 + 14.0 * (s.r / maxR) + 30.0 * s.baseAlpha;

        return s;
    }

    public void update(double dt, int w, int h) {
        // keep center synced to window size (in case you resize)
        cx = w / 2.0;
        cy = h / 2.0;

        angle += angSpeed * dt;
        twPhase += twSpeed * dt;
    }

    private double twinkledAlpha() {
        // sin in [ -1..1 ] -> map to [1 - twAmount .. 1]
        double tw = (Math.sin(twPhase) + 1.0) * 0.5; // 0..1
        double mul = (1.0 - twAmount) + twAmount * tw;
        double a = baseAlpha * mul;

        // keep subtle, clamp
        if (a < 0) a = 0;
        if (a > 1) a = 1;
        return a;
    }

    public void draw(Graphics2D g2) {
        double a = twinkledAlpha();
        int alpha = (int) Math.round(a * 255);
        if (alpha <= 0) return;

        // position
        double px = cx + Math.cos(angle) * r;
        double py = cy + Math.sin(angle) * r * ellipse;

        // tangent direction (for streak)
        // derivative wrt angle: (-sin, cos)
        double tx = -Math.sin(angle);
        double ty =  Math.cos(angle) * ellipse;

        // normalize tangent
        double tlen = Math.hypot(tx, ty);
        if (tlen > 0.0001) {
            tx /= tlen;
            ty /= tlen;
        }

        // streak size: subtle & depends on radius/brightness
        double len = streakLen;
        double x2 = px - tx * len;
        double y2 = py - ty * len;

        Paint oldP = g2.getPaint();
        Stroke oldS = g2.getStroke();

        // gradient streak: bright head -> transparent tail
        g2.setStroke(new BasicStroke(Math.max(1.0f, (float) (size * 0.9)),
                BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        g2.setPaint(new GradientPaint(
                (float) px, (float) py, new Color(rCol, gCol, bCol, alpha),
                (float) x2, (float) y2, new Color(rCol, gCol, bCol, 0)
        ));
        g2.drawLine((int) px, (int) py, (int) x2, (int) y2);

        // tiny head sparkle
        int headAlpha = Math.min(255, alpha + 40);
        g2.setPaint(oldP);
        g2.setColor(new Color(rCol, gCol, bCol, headAlpha));
        g2.fillRect((int) px, (int) py, size, size);

        g2.setStroke(oldS);
        g2.setPaint(oldP);
    }
}
