import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {

            JFrame frame = new JFrame("Pong");

            frame.setSize(700, 600);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLocationRelativeTo(null);

            frame.setContentPane(new GamePanel());
            frame.setVisible(true);
        });
    }
}
