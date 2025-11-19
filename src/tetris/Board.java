package tetris;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JPanel;
import javax.swing.Timer;

import tetris.Tetromino.Shape;

public class Board extends JPanel implements ActionListener {

    private final int BOARD_WIDTH = 10;
    private final int BOARD_HEIGHT = 22;
    private final int INITIAL_DELAY = 400;

    private Timer timer;
    private boolean isFallingFinished = false;
    private boolean isStarted = false;
    private boolean isPaused = false;
    private int numLinesRemoved = 0;
    private int curX = 0;
    private int curY = 0;
    private Tetromino curPiece;
    private Tetromino nextPiece;
    private Tetromino holdPiece;
    private boolean canHold = true;
    private Shape[] board;
    private SidePanel sidePanel;
    private int score = 0;
    private int level = 1;
    private AudioPlayer audio;

    // Lock Delay
    private Timer lockTimer;
    private boolean isLocking = false;
    private final int LOCK_DELAY = 500;

    // Visual Juice
    private int shakeOffsetX = 0;
    private int shakeOffsetY = 0;
    private int shakeDuration = 0;

    public Board() {
        setFocusable(true);
        curPiece = new Tetromino();
        nextPiece = new Tetromino();
        nextPiece.setRandomShape();

        audio = new AudioPlayer();

        timer = new Timer(INITIAL_DELAY, this);
        timer.start();

        lockTimer = new Timer(LOCK_DELAY, e -> {
            if (isLocking) {
                pieceDropped();
                isLocking = false;
                lockTimer.stop();
            }
        });
        lockTimer.setRepeats(false);

        board = new Shape[BOARD_WIDTH * BOARD_HEIGHT];
        addKeyListener(new TAdapter());
        clearBoard();
        setBackground(new Color(30, 30, 30));
    }

    public void setSidePanel(SidePanel sp) {
        this.sidePanel = sp;
    }

    public void start() {
        isStarted = true;
        isPaused = false;
        isLocking = false;
        numLinesRemoved = 0;
        score = 0;
        level = 1;
        clearBoard();
        newPiece();
        timer.start();
        audio.startMusic();
    }

    private void pause() {
        if (!isStarted)
            return;

        isPaused = !isPaused;
        if (isPaused) {
            timer.stop();
            lockTimer.stop();
        } else {
            timer.start();
        }
        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        doDrawing((Graphics2D) g);
    }

    private void doDrawing(Graphics2D g2d) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Apply Screen Shake
        if (shakeDuration > 0) {
            shakeOffsetX = (int) (Math.random() * 10 - 5);
            shakeOffsetY = (int) (Math.random() * 10 - 5);
            shakeDuration--;
        } else {
            shakeOffsetX = 0;
            shakeOffsetY = 0;
        }
        g2d.translate(shakeOffsetX, shakeOffsetY);

        Dimension size = getSize();
        int boardTop = (int) size.getHeight() - BOARD_HEIGHT * squareHeight();

        // Draw Grid Background
        g2d.setColor(new Color(40, 40, 40));
        for (int i = 0; i < BOARD_HEIGHT; ++i) {
            for (int j = 0; j < BOARD_WIDTH; ++j) {
                g2d.drawRect(j * squareWidth(), boardTop + i * squareHeight(), squareWidth(), squareHeight());
            }
        }

        // Draw Board
        for (int i = 0; i < BOARD_HEIGHT; ++i) {
            for (int j = 0; j < BOARD_WIDTH; ++j) {
                Shape shape = shapeAt(j, BOARD_HEIGHT - i - 1);
                if (shape != Shape.NoShape)
                    drawSquare(g2d, 0 + j * squareWidth(),
                            boardTop + i * squareHeight(), shape, false);
            }
        }

        if (curPiece.getShape() != Shape.NoShape) {
            // Draw Ghost Piece
            int ghostY = curY;
            while (ghostY > 0) {
                if (!tryMove(curPiece, curX, ghostY - 1, true))
                    break;
                --ghostY;
            }

            for (int i = 0; i < 4; ++i) {
                int x = curX + curPiece.x(i);
                int y = ghostY - curPiece.y(i);
                drawSquare(g2d, 0 + x * squareWidth(),
                        boardTop + (BOARD_HEIGHT - y - 1) * squareHeight(),
                        curPiece.getShape(), true);
            }

            // Draw Current Piece
            for (int i = 0; i < 4; ++i) {
                int x = curX + curPiece.x(i);
                int y = curY - curPiece.y(i);
                drawSquare(g2d, 0 + x * squareWidth(),
                        boardTop + (BOARD_HEIGHT - y - 1) * squareHeight(),
                        curPiece.getShape(), false);
            }
        }

        // Game Over Overlay
        if (!isStarted) {
            g2d.setColor(new Color(0, 0, 0, 150));
            g2d.fillRect(0, 0, getWidth(), getHeight());

            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Segoe UI", Font.BOLD, 30));
            String msg = "GAME OVER";
            int msgWidth = g2d.getFontMetrics().stringWidth(msg);
            g2d.drawString(msg, (getWidth() - msgWidth) / 2, getHeight() / 2 - 20);

            g2d.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            String restartMsg = "Press 'R' to Restart";
            int restartWidth = g2d.getFontMetrics().stringWidth(restartMsg);
            g2d.drawString(restartMsg, (getWidth() - restartWidth) / 2, getHeight() / 2 + 20);
        }

        g2d.translate(-shakeOffsetX, -shakeOffsetY);
    }

    private void dropDown() {
        int newY = curY;
        while (newY > 0) {
            if (!tryMove(curPiece, curX, newY - 1, false))
                break;
            --newY;
        }
        pieceDropped();
        audio.playDrop();
        triggerShake(5); // Shake on hard drop
    }

    private void oneLineDown() {
        if (!tryMove(curPiece, curX, curY - 1, false)) {
            if (!isLocking) {
                isLocking = true;
                lockTimer.restart();
            }
        } else {
            // Successfully moved down, reset lock delay
            if (isLocking) {
                isLocking = false;
                lockTimer.stop();
            }
        }
    }

    private void clearBoard() {
        for (int i = 0; i < BOARD_HEIGHT * BOARD_WIDTH; ++i)
            board[i] = Shape.NoShape;
    }

    private void pieceDropped() {
        for (int i = 0; i < 4; ++i) {
            int x = curX + curPiece.x(i);
            int y = curY - curPiece.y(i);
            board[(y * BOARD_WIDTH) + x] = curPiece.getShape();
        }

        removeFullLines();

        if (!isFallingFinished)
            newPiece();
    }

    private void newPiece() {
        curPiece.setShape(nextPiece.getShape());
        nextPiece.setRandomShape();

        curX = BOARD_WIDTH / 2 + 1;
        curY = BOARD_HEIGHT - 1 + curPiece.minY();
        canHold = true;
        isLocking = false;

        if (!tryMove(curPiece, curX, curY, false)) {
            curPiece.setShape(Shape.NoShape);
            timer.stop();
            isStarted = false;
            audio.playGameOver();
        }
        if (sidePanel != null)
            sidePanel.repaint();
    }

    private void holdPiece() {
        if (!canHold)
            return;

        if (holdPiece == null) {
            holdPiece = new Tetromino();
            holdPiece.setShape(curPiece.getShape());
            newPiece();
        } else {
            Shape temp = curPiece.getShape();
            curPiece.setShape(holdPiece.getShape());
            holdPiece.setShape(temp);

            curX = BOARD_WIDTH / 2 + 1;
            curY = BOARD_HEIGHT - 1 + curPiece.minY();
        }

        canHold = false;
        isLocking = false;
        if (sidePanel != null)
            sidePanel.repaint();
        repaint();
    }

    private boolean tryMove(Tetromino newPiece, int newX, int newY, boolean testOnly) {
        for (int i = 0; i < 4; ++i) {
            int x = newX + newPiece.x(i);
            int y = newY - newPiece.y(i);
            if (x < 0 || x >= BOARD_WIDTH || y < 0 || y >= BOARD_HEIGHT)
                return false;
            if (shapeAt(x, y) != Shape.NoShape)
                return false;
        }

        if (!testOnly) {
            curPiece = newPiece;
            curX = newX;
            curY = newY;

            // Reset lock delay on successful move
            if (isLocking) {
                lockTimer.restart();
            }

            repaint();
        }
        return true;
    }

    // Wall Kick logic
    private void tryRotate(boolean left) {
        Tetromino rotated = left ? curPiece.rotateLeft() : curPiece.rotateRight();

        // 1. Try normal rotation
        if (tryMove(rotated, curX, curY, false)) {
            audio.playRotate();
            return;
        }

        // 2. Try Wall Kicks (Simple offsets)
        int[][] offsets = { { 1, 0 }, { -1, 0 }, { 0, 1 }, { 1, 1 }, { -1, 1 } };

        for (int[] offset : offsets) {
            if (tryMove(rotated, curX + offset[0], curY + offset[1], false)) {
                audio.playRotate();
                return;
            }
        }
    }

    private void removeFullLines() {
        int numFullLines = 0;

        for (int i = BOARD_HEIGHT - 1; i >= 0; --i) {
            boolean lineIsFull = true;

            for (int j = 0; j < BOARD_WIDTH; ++j) {
                if (shapeAt(j, i) == Shape.NoShape) {
                    lineIsFull = false;
                    break;
                }
            }

            if (lineIsFull) {
                ++numFullLines;
                for (int k = i; k < BOARD_HEIGHT - 1; ++k) {
                    for (int j = 0; j < BOARD_WIDTH; ++j)
                        board[(k * BOARD_WIDTH) + j] = shapeAt(j, k + 1);
                }
            }
        }

        if (numFullLines > 0) {
            numLinesRemoved += numFullLines;
            score += numFullLines * 100 * level;
            isFallingFinished = true;
            curPiece.setShape(Shape.NoShape);
            audio.playClear();
            triggerShake(3 * numFullLines); // Shake on clear

            if (numLinesRemoved / 10 > level - 1) {
                level++;
                int newDelay = Math.max(100, INITIAL_DELAY - (level * 30));
                timer.setDelay(newDelay);
            }

            if (sidePanel != null)
                sidePanel.repaint();
            repaint();
        }
    }

    private void triggerShake(int duration) {
        this.shakeDuration = duration;
    }

    private void drawSquare(Graphics2D g, int x, int y, Shape shape, boolean isGhost) {
        Color colors[] = { new Color(0, 0, 0), new Color(204, 102, 102),
                new Color(102, 204, 102), new Color(102, 102, 204),
                new Color(204, 204, 102), new Color(204, 102, 204),
                new Color(102, 204, 204), new Color(218, 170, 0)
        };

        Color color = colors[shape.ordinal()];

        if (isGhost) {
            g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 50));
            g.fillRect(x + 1, y + 1, squareWidth() - 2, squareHeight() - 2);
            g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 100));
            g.setStroke(new BasicStroke(1));
            g.drawRect(x + 1, y + 1, squareWidth() - 2, squareHeight() - 2);
        } else {
            g.setColor(color);
            g.fillRect(x + 1, y + 1, squareWidth() - 2, squareHeight() - 2);

            g.setColor(color.brighter());
            g.drawLine(x, y + squareHeight() - 1, x, y);
            g.drawLine(x, y, x + squareWidth() - 1, y);

            g.setColor(color.darker());
            g.drawLine(x + 1, y + squareHeight() - 1,
                    x + squareWidth() - 1, y + squareHeight() - 1);
            g.drawLine(x + squareWidth() - 1, y + squareHeight() - 1,
                    x + squareWidth() - 1, y + 1);
        }
    }

    private int squareWidth() {
        return (int) getSize().getWidth() / BOARD_WIDTH;
    }

    private int squareHeight() {
        return (int) getSize().getHeight() / BOARD_HEIGHT;
    }

    private Shape shapeAt(int x, int y) {
        return board[(y * BOARD_WIDTH) + x];
    }

    public int getScore() {
        return score;
    }

    public int getLevel() {
        return level;
    }

    public int getLines() {
        return numLinesRemoved;
    }

    public Tetromino getNextPiece() {
        return nextPiece;
    }

    public Tetromino getHoldPiece() {
        return holdPiece;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (isFallingFinished) {
            isFallingFinished = false;
            newPiece();
        } else {
            oneLineDown();
        }
    }

    class TAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            int keycode = e.getKeyCode();

            if (!isStarted) {
                if (keycode == 'r' || keycode == 'R') {
                    start();
                }
                return;
            }

            if (curPiece.getShape() == Shape.NoShape) {
                return;
            }

            if (keycode == 'p' || keycode == 'P') {
                pause();
                return;
            }

            if (isPaused)
                return;

            switch (keycode) {
                case KeyEvent.VK_LEFT:
                    if (tryMove(curPiece, curX - 1, curY, false))
                        audio.playMove();
                    break;
                case KeyEvent.VK_RIGHT:
                    if (tryMove(curPiece, curX + 1, curY, false))
                        audio.playMove();
                    break;
                case KeyEvent.VK_DOWN:
                    tryRotate(false);
                    break;
                case KeyEvent.VK_UP:
                    tryRotate(true);
                    break;
                case KeyEvent.VK_SPACE:
                    dropDown();
                    break;
                case KeyEvent.VK_D:
                    oneLineDown();
                    break;
                case KeyEvent.VK_C:
                case KeyEvent.VK_SHIFT:
                    holdPiece();
                    break;
            }
        }
    }
}
