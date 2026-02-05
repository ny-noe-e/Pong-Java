import javax.swing.*;
import java.awt.*;

public class GamePanel extends JPanel {

    private final int BALL_SIZE = 20;


    @Override
    protected void paintComponent(Graphics g){
        super.paintComponent(g);

        int x = (getWidth() - BALL_SIZE) / 2;
        int y = (getHeight() - BALL_SIZE) / 2;

        g.setColor(Color.WHITE);
        g.fillOval(x, y, BALL_SIZE, BALL_SIZE);
    }
}
