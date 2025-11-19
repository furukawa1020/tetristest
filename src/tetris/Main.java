package tetris;

import java.awt.EventQueue;
import javax.swing.JFrame;

public class Main extends JFrame {

    public Main() {
        initUI();
    }

    private void initUI() {
        var board = new Board();
        add(board);
        board.start();

        setTitle("Tetris");
        setSize(200, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            var game = new Main();
            game.setVisible(true);
        });
    }
}
