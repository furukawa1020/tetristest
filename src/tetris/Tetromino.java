package tetris;

import java.awt.Color;

public class Tetromino {
    public enum Shape {
        NoShape, ZShape, SShape, LineShape, TShape, SquareShape, LShape, MirroredLShape
    }

    private Shape pieceShape;
    private int[][] coords;
    private int[][][] coordsTable;

    public Tetromino() {
        coords = new int[4][2];
        setShape(Shape.NoShape);
    }

    public void setShape(Shape shape) {
        coordsTable = new int[][][] {
            { { 0, 0 },   { 0, 0 },   { 0, 0 },   { 0, 0 } },
            { { 0, -1 },  { 0, 0 },   { -1, 0 },  { -1, 1 } },
            { { 0, -1 },  { 0, 0 },   { 1, 0 },   { 1, 1 } },
            { { 0, -1 },  { 0, 0 },   { 0, 1 },   { 0, 2 } },
            { { -1, 0 },  { 0, 0 },   { 1, 0 },   { 0, 1 } },
            { { 0, 0 },   { 1, 0 },   { 0, 1 },   { 1, 1 } },
            { { -1, -1 }, { 0, -1 },  { 0, 0 },   { 0, 1 } },
            { { 1, -1 },  { 0, -1 },  { 0, 0 },   { 0, 1 } }
        };

        for (int i = 0; i < 4 ; i++) {
            for (int j = 0; j < 2; ++j) {
                coords[i][j] = coordsTable[shape.ordinal()][i][j];
            }
        }
        pieceShape = shape;
    }

    private void setX(int index, int x) { coords[index][0] = x; }
    private void setY(int index, int y) { coords[index][1] = y; }
    public int x(int index) { return coords[index][0]; }
    public int y(int index) { return coords[index][1]; }
    public Shape getShape()  { return pieceShape; }

    public void setRandomShape() {
        var r = new java.util.Random();
        int x = Math.abs(r.nextInt()) % 7 + 1;
        Shape[] values = Shape.values();
        setShape(values[x]);
    }

    public int minX() {
        int m = coords[0][0];
        for (int i=0; i < 4; i++) {
            m = Math.min(m, coords[i][0]);
        }
        return m;
    }


    public int minY() {
        int m = coords[0][1];
        for (int i=0; i < 4; i++) {
            m = Math.min(m, coords[i][1]);
        }
        return m;
    }

    public Tetromino rotateLeft() {
        if (pieceShape == Shape.SquareShape)
            return this;

        Tetromino result = new Tetromino();
        result.pieceShape = pieceShape;

        for (int i = 0; i < 4; ++i) {
            result.setX(i, result.y(i));
            result.setY(i, -result.x(i));
        }
        return result;
    }

    public Tetromino rotateRight() {
        if (pieceShape == Shape.SquareShape)
            return this;

        Tetromino result = new Tetromino();
        result.pieceShape = pieceShape;

        for (int i = 0; i < 4; ++i) {
            result.setX(i, -result.y(i));
            result.setY(i, result.x(i));
        }
        return result;
    }
    
    public Color getColor() {
        switch (pieceShape) {
            case ZShape: return new Color(204, 102, 102);
            case SShape: return new Color(102, 204, 102);
            case LineShape: return new Color(102, 102, 204);
            case TShape: return new Color(204, 204, 102);
            case SquareShape: return new Color(204, 102, 204);
            case LShape: return new Color(102, 204, 204);
            case MirroredLShape: return new Color(218, 170, 0);
            default: return new Color(0, 0, 0);
        }
    }
}
