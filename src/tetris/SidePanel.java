package tetris;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JPanel;

public class SidePanel extends JPanel {
    private static final int PANEL_WIDTH = 150;
    private static final int PANEL_HEIGHT = 440; // Match Board height roughly (22 * 20)
    private Board board;

    public SidePanel(Board board) {
        this.board = board;
        setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        setBackground(new Color(50, 50, 50)); // Dark background
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 16));

        // Score
        g2d.drawString("SCORE", 20, 30);
        g2d.drawString(String.valueOf(board.getScore()), 20, 50);

        // Level
        g2d.drawString("LEVEL", 20, 90);
        g2d.drawString(String.valueOf(board.getLevel()), 20, 110);

        // Lines
        g2d.drawString("LINES", 20, 150);
        g2d.drawString(String.valueOf(board.getLines()), 20, 170);

        // Next Piece
        g2d.drawString("NEXT", 20, 220);
        drawPiecePreview(g2d, board.getNextPiece(), 20, 240);

        // Hold Piece
        g2d.drawString("HOLD", 20, 320);
        drawPiecePreview(g2d, board.getHoldPiece(), 20, 340);
    }

    private void drawPiecePreview(Graphics2D g2d, Tetromino piece, int x, int y) {
        if (piece == null || piece.getShape() == Tetromino.Shape.NoShape) return;

        Color color = piece.getColor();
        int size = 20; // Smaller block size for preview

        for (int i = 0; i < 4; i++) {
            int drawX = x + (piece.x(i) + 1) * size; // Offset to center roughly
            int drawY = y - (piece.y(i) - 1) * size;
            
            g2d.setColor(color);
            g2d.fillRect(drawX, drawY, size - 1, size - 1);
            
            g2d.setColor(color.brighter());
            g2d.drawLine(drawX, drawY + size - 1, drawX, drawY);
            g2d.drawLine(drawX, drawY, drawX + size - 1, drawY);

            g2d.setColor(color.darker());
            g2d.drawLine(drawX + 1, drawY + size - 1, drawX + size - 1, drawY + size - 1);
            g2d.drawLine(drawX + size - 1, drawY + size - 1, drawX + size - 1, drawY + 1);
        }
    }
}
