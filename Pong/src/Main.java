import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {

        JFrame frame = new JFrame("Pong");



        frame.setSize(700, 600);

        GamePanel panel = new GamePanel();
        panel.setBackground(Color.BLACK);
        frame.setContentPane(panel);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null); // center window

        frame.setVisible(true);



    }
}
