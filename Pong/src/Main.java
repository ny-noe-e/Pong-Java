import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Java Pong");

            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(900, 700);
            frame.setLocationRelativeTo(null);

            GamePanel panel = new GamePanel();
            frame.setContentPane(panel);

            frame.setVisible(true);

            panel.requestFocusInWindow();
        });
    }
}
