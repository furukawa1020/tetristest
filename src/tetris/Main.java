package tetris;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import javax.swing.JFrame;

public class Main extends JFrame {

    public Main() {
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        
        var board = new Board();
        var sidePanel = new SidePanel(board);
        board.setSidePanel(sidePanel);
        
        add(board, BorderLayout.CENTER);
        add(sidePanel, BorderLayout.EAST);
        
        board.start();

        setTitle("Tetris");
        setSize(400, 500); // Increased width for side panel
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            var game = new Main();
            game.setVisible(true);
        });
    }
}
